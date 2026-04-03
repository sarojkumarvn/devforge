package com.example.devforge.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.devforge.entity.User;





public interface UserRepository extends JpaRepository<User , Long >  {
    
Optional<User> findByEmail(String email);

Optional<User> findByUserName(String userName);


}
