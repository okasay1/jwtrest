package be.technocite.jwtrest.config;

import be.technocite.jwtrest.model.Role;
import be.technocite.jwtrest.service.UserService;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;
import java.util.Set;


@Component
public class JwTokenProvider {

    private Logger logger = LoggerFactory.getLogger(JwTokenProvider.class);
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

    /*extraire le token du header de la requête*/
    String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // jusqu'ici le token ressemble à 'Bearer sekukfqjfjùrjfù457'
        if (bearerToken != null && bearerToken.startsWith("Bearer")) {
            return bearerToken.substring(7, bearerToken.length());

        }
        return null;
    }

    /*Vérifier si le token n'est pas périmé*/
    boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date()); // Si le token est avant maintenant, il est périmé
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtException("Invalid JWT token");
        }
    }

    /*Créer un objet Authentification qui sera plus tard cérifié comme valide par Spring*/
    Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(getEmail(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    /*Extraire la propriété email de la partie payload (claims) du token*/
    private String getEmail(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject();
    }

}
