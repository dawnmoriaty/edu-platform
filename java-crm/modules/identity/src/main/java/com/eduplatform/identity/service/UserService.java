package com.eduplatform.identity.service;

import com.eduplatform.common.paging.Page;
import com.eduplatform.common.paging.Pageable;
import com.eduplatform.identity.dto.response.UserResponse;
import com.eduplatform.identity.entity.User;

import java.util.Optional;

public interface UserService {
    Optional<User> findById(Integer id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String identity);
    Page<UserResponse> findAll(Pageable pageable);
    User create(User user);
    User update(Integer id, User user);
    void delete(Integer id);
}
