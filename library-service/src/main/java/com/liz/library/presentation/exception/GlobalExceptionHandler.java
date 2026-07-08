package com.liz.library.presentation.exception;


import com.liz.library.domain.exception.BusinessException;
import com.liz.library.domain.message.MessageCodes;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Locale;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {

        return buildError(
                resolveStatus(ex.getCode()),
                resolveMessage(ex.getCode()),
                request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String message = ex.getBindingResult()
                .getFieldError()
                .getDefaultMessage();

        return buildError(
                HttpStatus.BAD_REQUEST,
                message,
                request.getRequestURI());
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

        return buildError(
                HttpStatus.CONFLICT,
                resolveMessage(MessageCodes.BOOK_ALREADY_EXISTS),
                request.getRequestURI());
    }

    private HttpStatus resolveStatus(String code) {

        return switch (code) {
            case MessageCodes.BOOK_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case MessageCodes.BOOK_NOT_FOUND -> HttpStatus.NOT_FOUND;
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

    private String resolveMessage(String code) {
        return messageSource.getMessage(code, null, code, Locale.getDefault());
    }
}