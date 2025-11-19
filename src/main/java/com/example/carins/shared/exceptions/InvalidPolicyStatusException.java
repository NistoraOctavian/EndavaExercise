package com.example.carins.shared.exceptions;

public class InvalidPolicyStatusException extends RuntimeException {
    public InvalidPolicyStatusException(String message) {
        super(message);
    }
}
