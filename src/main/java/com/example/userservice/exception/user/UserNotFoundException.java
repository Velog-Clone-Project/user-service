package com.example.userservice.exception.user;

import com.example.common.exception.BaseCustomException;

public class UserNotFoundException extends BaseCustomException {
    public UserNotFoundException() {
        super("User not found");
    }

    @Override
    public int getStatusCode() {
        return 404;
    }
}