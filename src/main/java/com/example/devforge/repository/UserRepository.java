package com.example.devforge.repository;


import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.devforge.entity.User;





public interface UserRepository extends JpaRepository<User , Long >  {
    
Optional<User> findByEmail(String email);

Optional<User> findByUserName(String userName);

@Query("""
    SELECT u FROM User u
    WHERE u.isPrivate = false
       OR :isAdmin = true
       OR u.id = :viewerId
""")
Page<User> findVisibleUsers(Long viewerId, boolean isAdmin, Pageable pageable);


}
