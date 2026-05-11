package com.openclassrooms.datashare.dto;

public record AuthMeDTO(
        boolean authenticated,
        String email) {
}