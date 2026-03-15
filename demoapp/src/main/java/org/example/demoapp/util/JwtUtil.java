package org.example.demoapp.util;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.example.demoapp.config.JwtConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {


    @Autowired
    private JwtConfig jwtConfig;

    public String generateToken(Long userId, String username){
        return JWT.create()
                .withClaim("userId",userId)
                .withClaim("username",username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtConfig.getExpire()))
                .sign(Algorithm.HMAC256(jwtConfig.getSecret()));
    }

    public boolean validateToken(String token){
        try{
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtConfig.getSecret()))
                    .build();

            verifier.verify(token);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public Long getUserIdFromToken(String token){
        try{
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtConfig.getSecret()))
                    .build();

            DecodedJWT decodedJWT = verifier.verify(token);

            return decodedJWT.getClaim("userId").asLong();
        }catch (Exception e){
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtConfig.getSecret()))
                    .build();
            DecodedJWT decodedJWT = verifier.verify(token);

            return decodedJWT.getExpiresAt().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
