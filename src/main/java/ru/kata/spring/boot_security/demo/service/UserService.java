package ru.kata.spring.boot_security.demo.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import ru.kata.spring.boot_security.demo.entitys.User;

import java.util.List;

public interface UserService extends UserDetailsService {

    List<User> findAll(); // Получить всех пользователей

    User findById(Long id); // Найти пользователя по ID (с учётом отсутствия)

    void save(User user); // Добавить нового пользователя

    void update(Long id, User user); // Обновить данные пользователя по ID

    void delete(Long id); // Удалить пользователя по ID

    User findByUsername(String username);
}