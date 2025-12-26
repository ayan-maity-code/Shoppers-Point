package com.shopperspoint.controller;


import com.shopperspoint.dto.GenericResponse;
import com.shopperspoint.dto.LoginRequestDTO;
import com.shopperspoint.service.LoginService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/api/user")
public class UserLoginController {

    private final LoginService loginService;

    @Autowired
    public UserLoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<GenericResponse> userLogin(@RequestBody LoginRequestDTO loginRequestDTO, HttpServletResponse response,
                                                     @RequestHeader(name = "Accept-Language", required = false) Locale locale) {
        return loginService.userLogin(loginRequestDTO.getEmail(), loginRequestDTO.getPassword(), response, locale);

    }
}
