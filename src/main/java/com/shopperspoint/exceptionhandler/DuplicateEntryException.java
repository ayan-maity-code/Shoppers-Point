package com.shopperspoint.exceptionhandler;

public class DuplicateEntryException extends RuntimeException{
    public DuplicateEntryException(String message){

        super(message);
    }
}
