package com.liz.library.domain.valueobject;

import com.liz.library.domain.exception.BusinessException;
import com.liz.library.domain.message.MessageCodes;
import lombok.Getter;

import java.util.Objects;
import java.util.regex.Pattern;


@Getter
public final class Email {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private final String value;

    private Email(String value) {
        this.value = value;
    }

    public static Email of(String value) {

        if (Objects.isNull(value) || value.isBlank()) {
            throw new BusinessException(MessageCodes.EMAIL_REQUIRED);
        }

        String normalizedEmail = value.trim().toLowerCase();

        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new BusinessException(MessageCodes.INVALID_EMAIL);
        }

        return new Email(normalizedEmail);
    }

    @Override
    public String toString() {
        return value;
    }
}