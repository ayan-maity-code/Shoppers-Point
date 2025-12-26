package com.shopperspoint.exceptionhandler;

public class UserNotFoundException extends  RuntimeException{

    public UserNotFoundException(String message){
        super(message);
    }
}
