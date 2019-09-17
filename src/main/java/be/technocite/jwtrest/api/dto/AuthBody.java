package be.technocite.jwtrest.api.dto;

// Pour avoir un token pour le user au cas où il n'en a pas ou il a expiré
public class AuthBody {
    private String email;
    private String password;
}
