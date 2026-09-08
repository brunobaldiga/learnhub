package org.example.learnhub.user;

import lombok.RequiredArgsConstructor;
import org.example.learnhub.gateway.UserGateway;
import org.example.learnhub.user.service.UserService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserGatewayImpl implements UserGateway {
    private final UserService service;

    @Override
    public Map<Integer, String> findUsernamesByIds(Set<Integer> userIds) {
        return service.findUsernamesByIds(userIds);
    }

    @Override
    public String findUsernameById(Integer userId) {
        return service.findUsernamesById(userId);
    }

    @Override
    public List<Integer> findIdsByUsernameContaining(String username) {
        return service.findIdsByUsernameContaining(username);
    }
}
