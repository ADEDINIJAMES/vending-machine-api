package com.tumpet.vending_machine_api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
@ResponseStatus(code = HttpStatus.CONFLICT)
public class ActiveSessionException extends RuntimeException {
        public ActiveSessionException(String message) {
            super(message);
        }
    }
