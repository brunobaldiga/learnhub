package org.example.learnhub.gateway;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserGateway {
    Map<Integer, String> findUsernamesByIds(Set<Integer> userIds);

    String findUsernameById(Integer userId);

    List<Integer> findIdsByUsernameContaining(String username);
}
