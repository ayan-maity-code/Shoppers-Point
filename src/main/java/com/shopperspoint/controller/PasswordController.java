package com.shopperspoint.controller;


import com.shopperspoint.dto.GenericResponse;
import com.shopperspoint.dto.PasswordDTO;
import com.shopperspoint.service.PasswordService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class PasswordController {

    private PasswordService passwordService;

    @Autowired
    public PasswordController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }


    @PostMapping("/forgotPassword")
    public ResponseEntity<GenericResponse> forgotPassword(@RequestBody Map<String, String> requester) {
        String email = requester.get("email");
        return passwordService.forgotPassword(email);
    }

    @PutMapping("/resetPassword")
    public ResponseEntity<GenericResponse> resetPassword(@RequestHeader String token, @Valid @RequestBody PasswordDTO passwordDTO) {
        return passwordService.resetPassword(token, passwordDTO);
    }
}
