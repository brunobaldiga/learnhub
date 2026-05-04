package org.example.learnhub.config;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizationService implements UserDetailsService {
    private final UserRepository repository;


    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        return repository.findByUsername(identifier)
                .or(() -> repository.findByEmail(identifier))
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
