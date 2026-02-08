package com.dangbun.domain.user.adapter.out.persistence;

import com.dangbun.domain.user.domain.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User mapToDomainEntity(UserJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }

        return User.withIdBuilder()
                .userId(new User.UserId(jpaEntity.getUserId()))
                .name(jpaEntity.getName())
                .email(jpaEntity.getEmail())
                .password(jpaEntity.getPassword())
                .loginType(jpaEntity.getLoginType())
                .socialId(jpaEntity.getSocialId())
                .enabled(jpaEntity.getEnabled())
                .createdAt(jpaEntity.getCreatedAt())
                .build();
    }

    public UserJpaEntity mapToJpaEntity(User domainEntity) {
        if (domainEntity == null) {
            return null;
        }

        UserJpaEntity jpaEntity = UserJpaEntity.builder()
                .name(domainEntity.getName())
                .email(domainEntity.getEmail())
                .password(domainEntity.getPassword())
                .loginType(domainEntity.getLoginType())
                .socialId(domainEntity.getSocialId())
                .enabled(domainEntity.getEnabled())
                .build();

        return jpaEntity;
    }
}
