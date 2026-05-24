package com.example.project.service;

import com.example.project.users.entity.UserEntity;
import com.example.project.users.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserEntity> userEntityOptional ;

        // Ищем пользователя по email, если email не найден — ищем по телефону, если не найден — ищем по паспорту
        userEntityOptional = userRepository.findByEmail(username);
        if (userEntityOptional.isEmpty()) {
            userEntityOptional = userRepository.findByPhoneNumber(username);
        }

        if (userEntityOptional.isEmpty()) {
            throw new UsernameNotFoundException("Пользователь не найден");
        }
        UserEntity user = userEntityOptional.get();
        // Здесь добавляем роль пользователя
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("user"));
        // Возвращаем объект User с email, паролем и правами доступа
        return new User(user.getEmail(), user.getPassword(), authorities);


    }
}
