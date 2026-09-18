package com.jordi.kakebot.expense.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LenientBigDecimalDeserializerTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(BigDecimal.class, new LenientBigDecimalDeserializer());
        objectMapper = new ObjectMapper().registerModule(module);
    }

    @ParameterizedTest
    @ValueSource(strings = {"12.50", "12,50", "  12,50  "})
    void deserializeAcceptsDotCommaAndSurroundingSpaces(String input) throws Exception {
        BigDecimal result = objectMapper.readValue('"' + input + '"', BigDecimal.class);

        assertThat(result).isEqualByComparingTo("12.50");
    }

    @Test
    void deserializeAcceptsJsonNumber() throws Exception {
        assertThat(objectMapper.readValue("12.75", BigDecimal.class)).isEqualByComparingTo("12.75");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void deserializeReturnsNullForBlankValue(String input) throws Exception {
        assertThat(objectMapper.readValue('"' + input + '"', BigDecimal.class)).isNull();
    }

    @Test
    void deserializeReturnsNullForJsonNull() throws Exception {
        assertThat(objectMapper.readValue("null", BigDecimal.class)).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "12,3,4", "--5"})
    void deserializeRejectsInvalidNumber(String input) {
        assertThatThrownBy(() -> objectMapper.readValue('"' + input + '"', BigDecimal.class))
                .isInstanceOf(JsonMappingException.class)
                .hasMessageContaining("El importe debe ser un numero valido");
    }
}
