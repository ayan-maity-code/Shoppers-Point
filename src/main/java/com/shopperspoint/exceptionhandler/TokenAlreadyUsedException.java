package com.shopperspoint.exceptionhandler;

public class TokenAlreadyUsedException extends RuntimeException{
    public  TokenAlreadyUsedException(String message){
        super(message);
    }
}
