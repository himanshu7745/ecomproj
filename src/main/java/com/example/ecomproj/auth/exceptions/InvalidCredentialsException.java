package com.example.ecomproj.auth.exceptions;

import javax.naming.AuthenticationException;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
