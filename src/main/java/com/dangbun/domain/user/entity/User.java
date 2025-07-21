package com.dangbun.domain.user.entity;


import com.dangbun.global.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "user")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private long id;

    private String name;

    @Column(name = "user_email")
    private String email;

    private String password;

}
