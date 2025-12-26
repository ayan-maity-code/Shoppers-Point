package com.shopperspoint.exceptionhandler;

public class AccountAlreadyActivatedException extends RuntimeException{
    public AccountAlreadyActivatedException(String message){
        super(message);
    }
}
