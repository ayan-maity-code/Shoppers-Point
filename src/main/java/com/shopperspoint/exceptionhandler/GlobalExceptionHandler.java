package com.shopperspoint.exceptionhandler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Arrays;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Value("${error.count}")
    private String error;

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        List<String> errorMessages = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + " : " + error.getDefaultMessage()).toList();

        ErrorDetails details = new ErrorDetails("Total Errors: " + errorMessages.size(),
                errorMessages, request.getDescription(false));

        return new ResponseEntity<>(details, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest request) {
        String userFriendlyMessage;

        if (ex.getMessage().contains("Could not resolve attribute")) {
            userFriendlyMessage = "Invalid sort field or attribute used in query";
        } else {
            userFriendlyMessage = "Something went wrong. Please try again later.";
        }
        ErrorDetails response = new ErrorDetails(error, Arrays.asList(userFriendlyMessage), request.getDescription(false));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorDetails> handleInvalidTokenException(Exception ex, WebRequest request) {
        ErrorDetails response = new ErrorDetails(error, Arrays.asList(ex.getMessage()), request.getDescription(false));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(TokenAlreadyUsedException.class)
    public ResponseEntity<ErrorDetails> handleTokenAlreadyUsedException(Exception ex, WebRequest request) {
        ErrorDetails response = new ErrorDetails(error, Arrays.asList(ex.getMessage()), request.getDescription(false));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateEntryException.class)
    public ResponseEntity<ErrorDetails> handleDuplicateEntryException(DuplicateEntryException ex, WebRequest request) {
        ErrorDetails response = new ErrorDetails(error, List.of(ex.getMessage()), request.getDescription(false));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PasswordNotMatchException.class)
    public ResponseEntity<ErrorDetails> handlePasswordNotMatchException(Exception ex, WebRequest request) {
        ErrorDetails response = new ErrorDetails(error, Arrays.asList(ex.getMessage()), request.getDescription(false));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleUserNotFoundException(
            Exception ex, WebRequest request) throws Exception {
        ErrorDetails response = new ErrorDetails(error, Arrays.asList(ex.getMessage()), request.getDescription(false));
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorDetails> handleTokeExpiredException(
            Exception ex, WebRequest request) throws Exception {
        ErrorDetails response = new ErrorDetails(error, Arrays.asList(ex.getMessage()), request.getDescription(false));
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AddressLimitExceededException.class)
    public ResponseEntity<ErrorDetails> handleAddressLimitExceededException(
            Exception ex, WebRequest request) throws Exception {
        ErrorDetails response = new ErrorDetails(error, Arrays.asList(ex.getMessage()), request.getDescription(false));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDetails> handleAccessDeniedException(
            Exception ex, WebRequest request) throws Exception {
        ErrorDetails response = new ErrorDetails(error, Arrays.asList(ex.getMessage()), request.getDescription(false));
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AccountAlreadyActivatedException.class)
    public ResponseEntity<ErrorDetails> handleAccountAlreadyActivatedException(
            Exception ex, WebRequest request) throws Exception {
        ErrorDetails response = new ErrorDetails(error, Arrays.asList(ex.getMessage()), request.getDescription(false));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ExceptionHandler(ResouceNotFound.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(
            Exception ex, WebRequest request) throws Exception {
        ErrorDetails response = new ErrorDetails(error, Arrays.asList(ex.getMessage()), request.getDescription(false));
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorDetails> handleBadRequestException(
            Exception ex, WebRequest request) throws Exception {
        ErrorDetails response = new ErrorDetails(error, Arrays.asList(ex.getMessage()), request.getDescription(false));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

}
