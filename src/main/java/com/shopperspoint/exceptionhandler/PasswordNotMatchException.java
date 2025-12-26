package com.shopperspoint.exceptionhandler;

public class PasswordNotMatchException extends RuntimeException{
    public  PasswordNotMatchException(String message){
        super(message);
    }
}
