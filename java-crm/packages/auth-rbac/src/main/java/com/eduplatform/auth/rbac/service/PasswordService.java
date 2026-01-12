package com.eduplatform.auth.rbac.service;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * PasswordService - Service mã hóa và verify password với BCrypt
 */
@Service
public class PasswordService {

    @Value("${security.bcrypt.rounds:12}")
    private int bcryptRounds;

    /**
     * Hash password với BCrypt
     */
    public Single<String> hash(String plainPassword) {
        return Single.fromCallable(() -> 
                BCrypt.hashpw(plainPassword, BCrypt.gensalt(bcryptRounds))
        ).subscribeOn(Schedulers.computation());
    }

    /**
     * Hash password synchronously (cho seed data, migrations)
     */
    public String hashSync(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(bcryptRounds));
    }

    /**
     * Verify password với hash
     */
    public Single<Boolean> verify(String plainPassword, String hashedPassword) {
        return Single.fromCallable(() -> 
                BCrypt.checkpw(plainPassword, hashedPassword)
        ).subscribeOn(Schedulers.computation());
    }

    /**
     * Verify password synchronously
     */
    public boolean verifySync(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    /**
     * Generate hash cho seed data (static method)
     * Usage: PasswordService.generateHash("123456")
     */
    public static String generateHash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    public static void main(String[] args) {
        // Generate hashes for seed data
        String password = "123456";
        String hash = generateHash(password);
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        System.out.println("Verify: " + BCrypt.checkpw(password, hash));
    }
}
