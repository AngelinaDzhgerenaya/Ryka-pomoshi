package com.example.project.users.request;

import com.example.project.users.exception.BadRequestException;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequest {
    private String lastName;
    private String firstName;
    private String phoneNumber;

    private String email;
    private String password;

    public String validate()  {
        // Проверка на обязательные поля
        if (lastName == null || lastName.isBlank()) return "Фамилия обязательна для заполнения";
        if (firstName == null || firstName.isBlank()) return "Имя обязательно для заполнения";
        if (phoneNumber == null || phoneNumber.isBlank()) return "Номер телефона обязателен для заполнения";
        if (email == null || email.isBlank()) return "Email обязателен для заполнения";
        if (password == null || password.isBlank()) return "Пароль обязателен для заполнения";

        // Проверка фамилии
        if (!firstName.matches("^[A-Za-zА-Яа-яЁё]+$")) {
            return "Имя может содержать только буквы русского и латинского алфавита";
        }

        // Проверка имени
        if (!lastName.matches("^[A-Za-zА-Яа-яЁё]+$")) {
            return "Фамилия может содержать только буквы русского и латинского алфавита";
        }

        // Проверка номера телефона
        if (!phoneNumber.matches("^\\+7\\d{10}$")) {
            return "Такого номера не существует";
        }


        // Проверка email
        if (!email.matches("^[A-Za-z0-9._%+-]+@(mail\\.ru|gmail\\.com)$")) {
            return "Неверный формат email";
        }

        // Проверка длины пароля
        if (password.length() < 5 || password.length() > 12) {
            return "Неверная длина пароля";
        }

        // Проверка запрещённых символов в пароле
        if (!password.matches("^[^<>\\[\\]{}\\\\|;:А-Яа-яЁё]+$")) {
            return "Пароль содержит запрещённые символы";
        }
        return null;
    }
}
