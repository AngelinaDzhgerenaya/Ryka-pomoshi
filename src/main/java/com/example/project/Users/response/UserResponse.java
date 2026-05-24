package com.example.project.users.response;

import com.example.project.users.entity.UserEntity;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class UserResponse {
    protected Long id;

    protected String firstName;
    protected String lastName;
    protected String phoneNumber;
    protected String email;
    protected String password;

    public static UserResponse of(UserEntity item) {
        return UserResponse.builder()
                .id(item.getId())
                .lastName(item.getLastName())
                .firstName(item.getFirstName())
                .phoneNumber(item.getPhoneNumber())
                .email(item.getEmail())
                .build();
    }
}
