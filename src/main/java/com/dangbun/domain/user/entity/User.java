package com.dangbun.domain.user.entity;


import com.dangbun.domain.user.LoginType;
import com.dangbun.global.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @NotNull
    private String name;

    @NotNull
    @Column(name = "user_email")
    private String email;

    private String password;

    @NotNull
    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    private String socialId;

    private Boolean enabled = true;

    public void updatePassword( String password) {
        this.password = password;
    }

    public void deactivate(){
        this.enabled = false;
    }

    public void activate(){
        this.enabled = true;
    }

    @Builder
    public User(String name, String email, String password, LoginType loginType, String socialId, Boolean enabled){
        this.name = name;
        this.email = email;
        this.password = loginType == LoginType.EMAIL ? password : null;
        this.loginType = loginType == null ? LoginType.EMAIL : loginType;
        this.socialId = socialId;
        this.enabled = enabled;
    }



}
