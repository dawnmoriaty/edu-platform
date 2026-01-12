package com.eduplatform.identity.repository.impl;

import com.eduplatform.entity.enums.UserStatus;
import com.eduplatform.identity.entity.User;
import com.eduplatform.identity.repository.UserRepository;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * UserRepositoryImpl - Mock implementation cho development
 * TODO: Thay bằng jOOQ implementation khi có database schema
 */
@Slf4j
@Repository
public class UserRepositoryImpl implements UserRepository {

    // Mock data store (production: jOOQ/PostgreSQL)
    private final Map<Integer, User> userStore = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(100);

    public UserRepositoryImpl() {
        initMockData();
    }

    private void initMockData() {
        // Super Admin
        createUser(1, "admin", "admin@eduplatform.com", "Administrator", 
                "$2a$10$N9qo8uLOickgx2ZMRZoMye", List.of(1)); // SUPER_ADMIN role
        
        // Admin
        createUser(2, "manager", "manager@eduplatform.com", "Manager User",
                "$2a$10$N9qo8uLOickgx2ZMRZoMye", List.of(2)); // ADMIN role
        
        // Teacher
        createUser(3, "teacher", "teacher@eduplatform.com", "Teacher User",
                "$2a$10$N9qo8uLOickgx2ZMRZoMye", List.of(3)); // TEACHER role
        
        // Student
        createUser(4, "student", "student@eduplatform.com", "Student User",
                "$2a$10$N9qo8uLOickgx2ZMRZoMye", List.of(4)); // STUDENT role

        log.info("Mock user data initialized with {} entries", userStore.size());
    }

    private void createUser(int id, String username, String email, String name, String password, List<Integer> roleIds) {
        User user = User.builder()
                .username(username)
                .email(email)
                .name(name)
                .password(password)
                .roleIds(roleIds)
                .status(UserStatus.ACTIVE)
                .build();
        user.setId(id);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userStore.put(id, user);
    }

    @Override
    public Single<User> findById(Integer id) {
        return Single.fromCallable(() -> {
            User user = userStore.get(id);
            if (user == null) {
                throw new RuntimeException("User not found: " + id);
            }
            return user;
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Maybe<User> findByUsername(String username) {
        return Maybe.fromCallable(() -> 
            userStore.values().stream()
                    .filter(u -> u.getUsername().equals(username))
                    .findFirst()
                    .orElse(null)
        ).subscribeOn(Schedulers.io());
    }

    @Override
    public Maybe<User> findByEmail(String email) {
        return Maybe.fromCallable(() ->
            userStore.values().stream()
                    .filter(u -> u.getEmail().equals(email))
                    .findFirst()
                    .orElse(null)
        ).subscribeOn(Schedulers.io());
    }

    @Override
    public Maybe<User> findByUsernameOrEmail(String identity) {
        return Maybe.fromCallable(() ->
            userStore.values().stream()
                    .filter(u -> u.getUsername().equals(identity) || u.getEmail().equals(identity))
                    .findFirst()
                    .orElse(null)
        ).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<List<Integer>> getRoleIds(Integer userId) {
        return Single.fromCallable(() -> {
            User user = userStore.get(userId);
            if (user == null || user.getRoleIds() == null) {
                return List.<Integer>of();
            }
            return user.getRoleIds();
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<User> create(User user) {
        return Single.fromCallable(() -> {
            int id = idGenerator.incrementAndGet();
            user.setId(id);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userStore.put(id, user);
            log.info("Created user: id={}, username={}", id, user.getUsername());
            return user;
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<User> update(User user) {
        return Single.fromCallable(() -> {
            if (!userStore.containsKey(user.getId())) {
                throw new RuntimeException("User not found: " + user.getId());
            }
            user.setUpdatedAt(LocalDateTime.now());
            userStore.put(user.getId(), user);
            return user;
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Boolean> delete(Integer id) {
        return Single.fromCallable(() -> userStore.remove(id) != null)
                .subscribeOn(Schedulers.io());
    }
}
