package com.shopperspoint.exceptionhandler;

public class AddressLimitExceededException extends RuntimeException{
    public AddressLimitExceededException(String message){
        super(message);
    }
}
