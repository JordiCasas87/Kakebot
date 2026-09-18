package com.jordi.kakebot.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.jordi.kakebot.TestcontainersConfiguration;
import com.jordi.kakebot.expense.dto.ExpenseRequestDto;
import com.jordi.kakebot.expense.dto.ExpenseResponseDto;
import com.jordi.kakebot.expense.enums.ExpenseCategory;
import com.jordi.kakebot.expense.repository.ExpenseRepository;
import com.jordi.kakebot.user.dto.UserCategoryLimitRequestDto;
import com.jordi.kakebot.user.dto.UserCategoryLimitResponseDto;
import com.jordi.kakebot.user.dto.UserLoginRequestDto;
import com.jordi.kakebot.user.dto.UserRegisterRequestDto;
import com.jordi.kakebot.user.dto.UserResponseDto;
import com.jordi.kakebot.user.enums.UserProvider;
import com.jordi.kakebot.user.repository.UserCategoryLimitRepository;
import com.jordi.kakebot.user.repository.UserRepository;
import com.jordi.kakebot.user.repository.UserTelegramLinkCodeRepository;
import java.math.BigDecimal;
import java.time.YearMonth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KakebotApiEndToEndTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserCategoryLimitRepository userCategoryLimitRepository;

    @Autowired
    private UserTelegramLinkCodeRepository userTelegramLinkCodeRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        expenseRepository.deleteAll();
        userCategoryLimitRepository.deleteAll();
        userTelegramLinkCodeRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registersUserThroughTheCompleteApplication() {
        ResponseEntity<UserResponseDto> response = restTemplate.postForEntity(
                "/api/users/register",
                new UserRegisterRequestDto("jordi", "secret1"),
                UserResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().username()).isEqualTo("jordi");
        assertThat(response.getBody().provider()).isEqualTo(UserProvider.LOCAL);
        assertThat(userRepository.findByUsername("jordi")).isPresent();
    }

    @Test
    void logsInAPreviouslyRegisteredUser() {
        UserResponseDto registeredUser = registerUser("login-user");

        ResponseEntity<UserResponseDto> response = restTemplate.postForEntity(
                "/api/users/login",
                new UserLoginRequestDto("login-user", "secret1"),
                UserResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(registeredUser.id());
        assertThat(response.getBody().username()).isEqualTo("login-user");
    }

    @Test
    void createsExpenseAndReadsItFromAnotherEndpoint() {
        UserResponseDto user = registerUser("expense-user");

        ExpenseResponseDto createdExpense = createExpense(
                user.id(),
                ExpenseCategory.FOOD,
                "Compra semanal",
                new BigDecimal("42.50")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", user.id().toString());
        ResponseEntity<ExpenseResponseDto[]> response = restTemplate.exchange(
                "/api/expenses/recent?limit=5",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ExpenseResponseDto[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody()[0].id()).isEqualTo(createdExpense.id());
        assertThat(response.getBody()[0].description()).isEqualTo("Compra semanal");
        assertThat(expenseRepository.findById(createdExpense.id())).isPresent();
    }

    @Test
    void configuresAndReadsMonthlyCategoryLimit() {
        UserResponseDto user = registerUser("limit-user");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Id", user.id().toString());

        ResponseEntity<UserCategoryLimitResponseDto> updateResponse = restTemplate.exchange(
                "/api/users/me/category-limits/comida",
                HttpMethod.PUT,
                new HttpEntity<>(new UserCategoryLimitRequestDto(new BigDecimal("300.00")), headers),
                UserCategoryLimitResponseDto.class
        );

        ResponseEntity<java.util.List<UserCategoryLimitResponseDto>> readResponse = restTemplate.exchange(
                "/api/users/me/category-limits",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {
                }
        );

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().category()).isEqualTo(ExpenseCategory.FOOD);
        assertThat(readResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(readResponse.getBody())
                .containsExactly(new UserCategoryLimitResponseDto(ExpenseCategory.FOOD, new BigDecimal("300.00")));
    }

    @Test
    void generatesMonthlyPdfUsingPersistedExpenses() {
        UserResponseDto user = registerUser("report-user");
        ExpenseResponseDto expense = createExpense(
                user.id(),
                ExpenseCategory.TRANSPORT,
                "Abono transporte",
                new BigDecimal("25.00")
        );
        YearMonth expenseMonth = YearMonth.from(expense.registeredAt());
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", user.id().toString());

        ResponseEntity<byte[]> response = restTemplate.exchange(
                "/api/reports/expenses/monthly/pdf?year={year}&month={month}",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                byte[].class,
                expenseMonth.getYear(),
                expenseMonth.getMonthValue()
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(response.getHeaders().getContentDisposition().getFilename())
                .isEqualTo("kakebot-gastos-%d-%02d.pdf".formatted(
                        expenseMonth.getYear(),
                        expenseMonth.getMonthValue()
                ));
        assertThat(response.getBody()).isNotNull().startsWith("%PDF".getBytes());
    }

    private UserResponseDto registerUser(String username) {
        ResponseEntity<UserResponseDto> response = restTemplate.postForEntity(
                "/api/users/register",
                new UserRegisterRequestDto(username, "secret1"),
                UserResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    private ExpenseResponseDto createExpense(
            Long userId,
            ExpenseCategory category,
            String description,
            BigDecimal amount
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Id", userId.toString());
        ResponseEntity<ExpenseResponseDto> response = restTemplate.exchange(
                "/api/expenses",
                HttpMethod.POST,
                new HttpEntity<>(new ExpenseRequestDto(category, description, amount), headers),
                ExpenseResponseDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }
}
