package com.dangbun.domain.user.adapter.out.persistence;

import com.dangbun.domain.user.domain.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User mapToDomainEntity(UserJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            return null;
        }
        System.out.println(">>> jpaEntity.getUserId() = " + jpaEntity.getUserId());

        return User.withId(
                new User.UserId(jpaEntity.getUserId()),
                jpaEntity.getName(),
                jpaEntity.getEmail(),
                jpaEntity.getPassword(),
                jpaEntity.getLoginType(),
                jpaEntity.getSocialId(),
                jpaEntity.getEnabled(),
                jpaEntity.getCreatedAt()
        );

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
