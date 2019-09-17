package be.technocite.jwtrest.config;

import be.technocite.jwtrest.model.Role;
import be.technocite.jwtrest.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Base64;
import java.util.Date;
import java.util.Set;


@Component
public class JwTokenProvider {

    private String secret = "secret"; // Comment le générer random et pas le hardcoder ?
    private long validityMS = 3600000; //1h

    @Autowired
    private UserService userDetailsService;

    @PostConstruct
    private void encodeSecret() {
        secret = Base64.getEncoder().encodeToString(secret.getBytes());
    }

    public String createToken(String email, Set<Role> roles) {
        Claims claims = Jwts.claims().setSubject(email); // on a rajouté la dépendence dans Maven <groupId>io.jsonwebtoken</groupId>
        claims.put("roles", roles);
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + validityMS);
        return Jwts.builder() // design pattern builder : on construit l'objet sans passe par les variables
                .setClaims(claims) // claims : payload
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }
}
