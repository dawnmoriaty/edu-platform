package com.eduplatform.identity.service;

import com.eduplatform.auth.rbac.model.SecurityUser;
import com.eduplatform.auth.rbac.service.PermissionService;
import com.eduplatform.auth.rbac.service.TokenService;
import com.eduplatform.common.constant.ErrorCode;
import com.eduplatform.common.exception.AppException;
import com.eduplatform.entity.enums.UserStatus;
import com.eduplatform.identity.entity.User;
import com.eduplatform.identity.repository.UserRepository;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

/**
 * AuthServiceImpl - Implementation với RxJava3
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PermissionService permissionService;

    @Override
    public Single<String> login(String identity, String password) {
        return userRepository.findByUsernameOrEmail(identity)
                .switchIfEmpty(Single.error(new AppException(ErrorCode.USER_NOT_FOUND)))
                .flatMap(user -> {
                    if (!BCrypt.checkpw(password, user.getPassword())) {
                        return Single.error(new AppException(ErrorCode.UNAUTHORIZED, "Mật khẩu không đúng"));
                    }
                    return buildSecurityUser(user)
                            .flatMap(tokenService::generate);
                });
    }

    @Override
    public Single<SecurityUser> register(String username, String email, String password, String name) {
        return userRepository.findByUsernameOrEmail(username)
                .flatMapSingle(existing -> Single.<User>error(
                        new AppException(ErrorCode.DUPLICATE_ENTRY, "Username đã tồn tại")))
                .switchIfEmpty(Maybe.defer(() -> {
                    User newUser = User.builder()
                            .username(username)
                            .email(email)
                            .password(BCrypt.hashpw(password, BCrypt.gensalt()))
                            .name(name)
                            .status(UserStatus.ACTIVE)
                            .build();
                    return userRepository.create(newUser).toMaybe();
                }))
                .toSingle()
                .flatMap(this::buildSecurityUser);
    }

    @Override
    public Single<SecurityUser> getCurrentUser(String token) {
        return tokenService.getSecurityUser(token)
                .flatMap(securityUser -> 
                        permissionService.getUserPermissions(securityUser.getId())
                                .map(permissions -> {
                                    securityUser.setPermissions(permissions);
                                    return securityUser;
                                })
                );
    }

    @Override
    public Single<String> refreshToken(String refreshToken) {
        return tokenService.isExpired(refreshToken)
                .flatMap(isExpired -> {
                    if (isExpired) {
                        return Single.error(new AppException(ErrorCode.TOKEN_EXPIRED));
                    }
                    return tokenService.getSecurityUser(refreshToken)
                            .flatMap(tokenService::generate);
                });
    }

    @Override
    public Single<Boolean> logout(String token) {
        return tokenService.invalidate(token);
    }

    @Override
    public Single<Boolean> changePassword(Integer userId, String oldPassword, String newPassword) {
        return userRepository.findById(userId)
                .flatMap(user -> {
                    if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
                        return Single.error(new AppException(ErrorCode.UNAUTHORIZED, "Mật khẩu cũ không đúng"));
                    }
                    user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
                    return userRepository.update(user);
                })
                .map(user -> true);
    }

    private Single<SecurityUser> buildSecurityUser(User user) {
        return permissionService.getUserPermissions(user.getId())
                .map(permissions -> SecurityUser.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .name(user.getName())
                        .avatar(user.getAvatar())
                        .roleIds(user.getRoleIds())
                        .roleCodes(user.getRoleCodes())
                        .permissions(permissions)
                        .build()
                );
    }
}
