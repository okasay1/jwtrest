package be.technocite.jwtrest.service;

import be.technocite.jwtrest.api.dto.RegisterUserCommand;
import be.technocite.jwtrest.model.Role;
import be.technocite.jwtrest.model.User;
import be.technocite.jwtrest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Custom pcq il en existe déjà un dans springboot, va gérer l'authentification par l'intermédiaire du DAO
// Traduit notre user en un user Springboot
@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder bCryptPasswordEncoder;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User registerUser(RegisterUserCommand command) {
        User user = new User();
        user.setFullname(command.getFullname());
        user.setPassword(bCryptPasswordEncoder.encode(command.getPassword()));
        user.setEnabled(true);
        user.setEmail(command.getEmail());
        user.setRoles(new HashSet<>(command.getRoles()));
        return userRepository.save(user);
    }

    @Override // info concernant le user, sert à le charger
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user != null) {// Role et authority c'est le même
            List<GrantedAuthority> authorities = getUserAuthorithies(user.getRoles());
            return buildSpringUser(user, authorities);
        } else {
            throw new UsernameNotFoundException(("Username not found"));
        }
    }

    private List<GrantedAuthority> getUserAuthorithies(Set<Role> roles) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role.toString())));
        return authorities;

    }

    private UserDetails buildSpringUser(User user, List<GrantedAuthority> authorities) {
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }
}
