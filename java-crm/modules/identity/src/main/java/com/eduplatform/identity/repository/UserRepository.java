package com.eduplatform.identity.repository;

import com.eduplatform.identity.entity.User;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

import java.util.List;

/**
 * UserRepository - Repository cho User với RxJava3
 */
public interface UserRepository {

    Single<User> findById(Integer id);

    Maybe<User> findByUsername(String username);

    Maybe<User> findByEmail(String email);

    Maybe<User> findByUsernameOrEmail(String identity);

    Single<List<Integer>> getRoleIds(Integer userId);

    Single<User> create(User user);

    Single<User> update(User user);

    Single<Boolean> delete(Integer id);
}
