package com.dangbun.domain.user.entity;


import com.dangbun.global.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@Builder
@Table(name = "user")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private long id;

    @NotNull
    private String name;

    @NotNull
    @Column(name = "user_email")
    private String email;

    @NotNull
    private String password;

    private boolean enabled = true;

    public void updatePassword( String password) {
        this.password = password;
    }

    public void deactivate(){
        this.enabled = false;
    }

    public void activate(){
        this.enabled = true;
    }



}
