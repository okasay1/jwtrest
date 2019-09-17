package be.technocite.jwtrest.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class JwtTokenFilter extends GenericFilterBean {

    private JwTokenProvider jwTokenProvider;

    public JwtTokenFilter(JwTokenProvider jwTokenProvider) {
        this.jwTokenProvider = jwTokenProvider;
    }

    /*on ajoute un filtre à la chaine de filtres qui précède l'accès au Handler (méthode REST)
     * Si le token n'est pas valide alors la requête est abandonnée
     * Si le token est bon alors la requete est passée aux filtres suivants
     * Une fois le dernier filtre pasé la requête est envoyée au ndler (méthode REST du Controller)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String token = jwTokenProvider.resolveToken((HttpServletRequest) request);
        if (token != null && jwTokenProvider.validateToken(token)) {
            Authentication authentication = jwTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication((authentication));
            // dans cette manière de faire on passe dans tous les cas la requête au filtre suivant
            chain.doFilter(request, response);
        } else {
            return;
        }


    }
}
