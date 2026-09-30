package com.example.petshotel.service;

import com.example.petshotel.domain.entity.User;

public interface CurrentUserService {
    User getByEmail(String email);
}