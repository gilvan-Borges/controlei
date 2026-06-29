package br.com.controlei.infrastructure.security;

import br.com.controlei.application.contracts.TokenProvider;
import br.com.controlei.domain.models.dtos.auth.AuthenticatedUser;
import br.com.controlei.domain.models.enums.Role;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtUtil implements TokenProvider {

    private final Algorithm algorithm;
    private final long expirationHours;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-hours}") long expirationHours) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.expirationHours = expirationHours;
    }

    public String generateToken(AuthenticatedUser user) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(expirationHours * 3600);

        return JWT.create()
                .withSubject(user.email())
                .withClaim("userId", user.userId().toString())
                .withClaim("familyId", user.familyId().toString())
                .withClaim("role", user.role().name())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(expiration))
                .sign(algorithm);
    }

    public Optional<AuthenticatedUser> validateToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decoded = verifier.verify(token);

            UUID userId = UUID.fromString(decoded.getClaim("userId").asString());
            UUID familyId = UUID.fromString(decoded.getClaim("familyId").asString());
            Role role = Role.valueOf(decoded.getClaim("role").asString());
            String email = decoded.getSubject();

            return Optional.of(new AuthenticatedUser(userId, familyId, email, role));
        } catch (JWTVerificationException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
