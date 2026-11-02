
package com.example.secureauth.repository;

import com.example.secureauth.entity.RevokedToken;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedTokenRepository
        extends JpaRepository<RevokedToken, String> {

    boolean existsByJti(String jti);
}
