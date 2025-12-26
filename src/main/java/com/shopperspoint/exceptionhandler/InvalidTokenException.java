package com.shopperspoint.exceptionhandler;

public class InvalidTokenException extends RuntimeException{

    public InvalidTokenException(String message){
        super(message);
    }
}
