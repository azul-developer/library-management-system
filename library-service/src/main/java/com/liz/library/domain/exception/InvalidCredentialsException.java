package com.liz.library.domain.exception;

import com.liz.library.domain.message.MessageCodes;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super(MessageCodes.INVALID_CREDENTIALS);
    }
}