package com.gallery.service;

import com.gallery.entity.User;
import com.gallery.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class UserService {
    private final UserRepository userRepository = new UserRepository();

    // авторизация
    public User authenticate(String username, String rawPassword) {

        User user = userRepository.findByUsername(username);
        if (user == null) return null;

        // проверка
        System.out.println("Username: " + username);
        System.out.println("rawPassword: " + rawPassword);
        System.out.println("hashFromDB: " + user.getPassword());
        System.out.println("BCrypt.check-pw: " + BCrypt.checkpw(rawPassword, user.getPassword()));


        // сравнение пароля с хешем в бд
        String hashFromDB = user.getPassword();
        if (BCrypt.checkpw(rawPassword, hashFromDB )) {
            return user;
        }

        return null;
    }

    // сохранить нового пользователя
    public void save(User user) {
        String password = user.getPassword();
        if (!password.startsWith("$2a$")) { // хэшированный пароль в формате BCrypt
            String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
            user.setPassword(hashed);
        }
        userRepository.save(user);
    }

    // поиск по логину
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void updateRole(User targetUser, String newRole, User currentUser) {

        if (currentUser == null) {
            throw new SecurityException("Неизвестный текущий пользователь");
        }

        // нельзя менять себе роль
        if (targetUser.getUsername().equalsIgnoreCase(currentUser.getUsername())) {
            throw new SecurityException("Нельзя менять роль самому себе");
        }

        // нельзя менять роль главного admin
        if ("admin".equalsIgnoreCase(targetUser.getUsername())) {
            throw new SecurityException("Нельзя менять роль главного администратора");
        }

        // нельзя менять роль, если текущий пользователь не admin
        if (!"admin".equalsIgnoreCase(currentUser.getRole())) {
            throw new SecurityException("Недостаточно прав для изменения роли");
        }

        targetUser.setRole(newRole);
        userRepository.update(targetUser);
    }

    public void deleteUser(User targetUser, User currentUser) {
        if (currentUser == null) {
            throw new SecurityException("Неизвестный текущий пользователь");
        }

        // нельзя удалить себя
        if (targetUser.getUsername().equalsIgnoreCase(currentUser.getUsername())) {
            throw new SecurityException("Нельзя удалить самого себя");
        }

        // нельзя удалить главного admin
        if ("admin".equalsIgnoreCase(targetUser.getUsername())) {
            throw new SecurityException("Нельзя удалить главного администратора");
        }

        // только админ или главный admin могут удалять
        boolean isCurrentUserSuperAdmin = "admin".equalsIgnoreCase(currentUser.getUsername());
        boolean isCurrentUserAdmin = "admin".equalsIgnoreCase(currentUser.getRole());

        if (!isCurrentUserSuperAdmin && !isCurrentUserAdmin) {
            throw new SecurityException("Недостаточно прав для удаления пользователя");
        }

        userRepository.delete(targetUser);
    }

}
