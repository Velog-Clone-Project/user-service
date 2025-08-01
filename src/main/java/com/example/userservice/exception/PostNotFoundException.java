package com.example.userservice.exception;

import com.example.common.exception.BaseCustomException;

public class PostNotFoundException extends BaseCustomException {
    public PostNotFoundException() {
        super("Posts not found");
    }

    @Override
    public int getStatusCode() {
        return 404;
    }
}
