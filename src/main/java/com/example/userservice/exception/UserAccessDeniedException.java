package com.example.userservice.exception;

import com.example.common.exception.BaseCustomException;

public class UserAccessDeniedException extends BaseCustomException {

    public UserAccessDeniedException() {
        super("Access denied ");
    }

    @Override
    public int getStatusCode() {
        return 404;
    }
}
