package com.example.project.users.request;

import com.example.project.users.exception.BadRequestException;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditUserRequest {
    private String lastName;
    private String firstName;
    private String phoneNumber;


    public String validate()  {
        // Проверка на обязательные поля
        if (lastName == null || lastName.isBlank()) return "Фамилия обязательна для заполнения";
        if (firstName == null || firstName.isBlank()) return "Имя обязательно для заполнения";
        if (phoneNumber == null || phoneNumber.isBlank())
            return "Номер телефона обязателен для заполнения";


        // Проверка фамилии
        if (!firstName.matches("^[A-Za-zА-Яа-яЁё]+$")) {
            return "Имя может содержать только буквы русского и латинского алфавита";
        }

        // Проверка имени
        if (!lastName.matches("^[A-Za-zА-Яа-яЁё]+$")) {
            return"Фамилия может содержать только буквы русского и латинского алфавита";
        }

        // Проверка номера телефона
        if (!phoneNumber.matches("^\\+7\\d{10}$")) {
            return"Такого номера не существует";
        }


        return null;
    }
}
