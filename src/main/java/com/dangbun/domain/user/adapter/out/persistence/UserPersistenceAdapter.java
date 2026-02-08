package com.dangbun.domain.user.adapter.out.persistence;

import com.dangbun.common.hexagonal.PersistenceAdapter;
import com.dangbun.domain.user.application.port.out.UserCommandPort;
import com.dangbun.domain.user.application.port.out.UserQueryPort;
import com.dangbun.domain.user.domain.User;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserQueryPort, UserCommandPort {

    private final SpringDataUserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId)
                .map(userMapper::mapToDomainEntity);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::mapToDomainEntity);
    }

    @Override
    public Optional<User> findBySocialId(String socialId) {
        return userRepository.findBySocialId(socialId)
                .map(userMapper::mapToDomainEntity);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        UserJpaEntity jpaEntity;

        if (user.getUserId() != null) {
            // Update existing entity
            jpaEntity = userRepository.findById(user.getUserId().value())
                    .orElseGet(() -> userMapper.mapToJpaEntity(user));

            jpaEntity.updatePassword(user.getPassword());
            if (!user.getEnabled()) {
                jpaEntity.deactivate();
            } else {
                jpaEntity.activate();
            }
        } else {
            // Create new entity
            jpaEntity = userMapper.mapToJpaEntity(user);
        }

        UserJpaEntity savedEntity = userRepository.save(jpaEntity);
        return userMapper.mapToDomainEntity(savedEntity);
    }

    @Override
    public void delete(User user) {
        if (user.getUserId() != null) {
            userRepository.deleteById(user.getUserId().value());
        }
    }

    @Override
    public void deleteById(Long userId) {
        userRepository.deleteById(userId);
    }
}
