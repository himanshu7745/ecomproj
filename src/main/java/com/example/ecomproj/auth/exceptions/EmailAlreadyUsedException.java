package com.example.ecomproj.auth.exceptions;

import java.io.IOException;

public class EmailAlreadyUsedException extends RuntimeException {
    public EmailAlreadyUsedException(String message) {
        super(message);
    }
}
