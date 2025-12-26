package com.shopperspoint.controller;


import com.shopperspoint.dto.GenericResponse;
import com.shopperspoint.service.LogoutService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/api")
public class UserLogoutController {

    private final LogoutService logoutService;

    @Autowired
    public UserLogoutController(LogoutService logoutService) {
        this.logoutService = logoutService;
    }

    @PostMapping("/logout")
    public ResponseEntity<GenericResponse> userLogout(HttpServletRequest request, HttpServletResponse response,
                                                      @RequestHeader(name = "Accept-Language", required = false) Locale locale) {
        return logoutService.logout(request, response, locale);
    }
}
