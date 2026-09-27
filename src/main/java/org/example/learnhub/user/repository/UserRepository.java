package org.example.learnhub.user.repository;

import org.example.learnhub.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByUsernameIgnoreCase(String identifier);

    Optional<User> findByEmailIgnoreCase(String identifier);

    @Query("select user.id from User user where lower(user.username) like lower(concat('%', :username, '%'))")
    List<Integer> findIdsByUsernameContaining(String username);
}
