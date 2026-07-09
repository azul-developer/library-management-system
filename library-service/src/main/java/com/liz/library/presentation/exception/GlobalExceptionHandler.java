package com.liz.library.presentation.exception;


import com.liz.library.domain.exception.BusinessException;
import com.liz.library.domain.message.MessageCodes;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ApiError> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {

        HttpStatus status = resolveStatus(ex.getCode());

        ApiError error = buildError(
            status,
            resolveMessage(ex.getCode()),
            request.getRequestURI());

        return new ResponseEntity<>(error, status);
        }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        Locale locale = request.getLocale();

        List<ValidationError> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(fe -> new ValidationError(fe.getField(), messageSource.getMessage(fe, locale)))
            .toList();

        String message = "Validation failed";

        return buildError(
            HttpStatus.BAD_REQUEST,
            message,
            request.getRequestURI(),
            errors);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleOptimisticLock(
            OptimisticLockingFailureException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.CONFLICT,
                "Conflict while updating resource",
                request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        String messageCode = MessageCodes.DUPLICATED; // default

        Throwable cause = ex.getCause();
        String constraintName = null;

        try {
            if (cause instanceof ConstraintViolationException hibernateEx) {
                constraintName = hibernateEx.getConstraintName();
            }
        } catch (Throwable t) {
            log.debug("Error while inspecting cause for constraint name", t);
        }

        String lowerConstraint = constraintName != null ? constraintName.toLowerCase() : null;

        if (lowerConstraint != null) {
            if (lowerConstraint.contains("email")) {
                messageCode = MessageCodes.EMAIL_ALREADY_EXISTS;
            } else if (lowerConstraint.contains("isbn")) {
                messageCode = MessageCodes.BOOK_ALREADY_EXISTS;
            }
        } else {
            String rootMsg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
            if (rootMsg != null) {
                String lower = rootMsg.toLowerCase();
                if (lower.contains("email") || lower.contains("users_email")) {
                    messageCode = MessageCodes.EMAIL_ALREADY_EXISTS;
                } else if (lower.contains("isbn") || lower.contains("books_isbn")) {
                    messageCode = MessageCodes.BOOK_ALREADY_EXISTS;
                }
            }
        }

        log.debug("DataIntegrityViolation inferred messageCode={}", messageCode);

        return buildError(
                HttpStatus.CONFLICT,
                resolveMessage(messageCode),
                request.getRequestURI());
    }

    private HttpStatus resolveStatus(String code) {

        return switch (code) {
            case MessageCodes.BOOK_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case MessageCodes.EMAIL_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case MessageCodes.BOOK_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case MessageCodes.USER_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case MessageCodes.ROLE_NOT_FOUND -> HttpStatus.BAD_REQUEST;
            case MessageCodes.INVALID_TOTAL_COPIES -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.BAD_REQUEST;
        };
    }

    private ApiError buildError(
            HttpStatus status,
            String message,
            String path) {

        return ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .build();
    }

    private ApiError buildError(
            HttpStatus status,
            String message,
            String path,
            List<ValidationError> errors) {

        return ApiError.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .errors(errors)
                .path(path)
                .build();
    }

    private String resolveMessage(String code) {
        return messageSource.getMessage(code, null, code, Locale.getDefault());
    }
}