package com.jordi.kakebot.common.exception;

import com.jordi.kakebot.common.dto.ApiErrorResponseDto;
import com.jordi.kakebot.expense.exception.ExpenseNotFoundException;
import com.jordi.kakebot.expense.exception.ExpenseUserNotFoundException;
import com.jordi.kakebot.expense.exception.InvalidExpenseRequestException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponseDto> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<String> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .toList();

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Datos de entrada invalidos",
                request.getRequestURI(),
                details
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponseDto> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        List<String> details = exception.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Datos de entrada invalidos",
                request.getRequestURI(),
                details
        );
    }

    @ExceptionHandler(InvalidExpenseRequestException.class)
    public ResponseEntity<ApiErrorResponseDto> handleInvalidExpenseRequest(
            InvalidExpenseRequestException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request.getRequestURI(),
                List.of()
        );
    }

    @ExceptionHandler({ExpenseNotFoundException.class, ExpenseUserNotFoundException.class})
    public ResponseEntity<ApiErrorResponseDto> handleNotFound(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                List.of()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponseDto> handleUnexpected(
            Exception exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ha ocurrido un error interno inesperado",
                request.getRequestURI(),
                List.of()
        );
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }

    private ResponseEntity<ApiErrorResponseDto> buildErrorResponse(
            HttpStatus status,
            String message,
            String path,
            List<String> details
    ) {
        ApiErrorResponseDto response = new ApiErrorResponseDto(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                details
        );
        return ResponseEntity.status(status).body(response);
    }
}
