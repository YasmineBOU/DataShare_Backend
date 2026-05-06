package com.openclassrooms.datashare.controller;

import com.openclassrooms.datashare.dto.AuthDTO;
import com.openclassrooms.datashare.service.UserService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthDTO authDTO) {
        userService.register(authDTO);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDTO authDTO) {
        String jwtToken = userService.login(authDTO.getEmail(), authDTO.getPassword());
        return ResponseEntity.ok(Map.of("token", jwtToken, "message", "Logged successfully !"));
    }
}
