package com.dangbun.domain.user.adapter.out.persistence;

import com.dangbun.domain.user.application.port.out.UserCommandPort;
import com.dangbun.domain.user.application.port.out.UserQueryPort;
import com.dangbun.domain.user.domain.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 테스트용 인메모리 User 저장소
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeUserRepository implements UserCommandPort, UserQueryPort {

    private final Map<Long, User> storage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public User save(User user) {
        if (user.getUserId() == null) {
            Long newId = idGenerator.getAndIncrement();
            User saved = User.withId(
                    new User.UserId(newId),
                    user.getName(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getLoginType(),
                    user.getSocialId(),
                    user.getEnabled(),
                    LocalDateTime.now()
            );
            storage.put(newId, saved);
            return saved;
        } else {
            storage.put(user.getUserId().value(), user);
            return user;
        }
    }

    @Override
    public void delete(User user) {
        if (user.getUserId() != null) {
            storage.remove(user.getUserId().value());
        } else {
            // userId가 없는 경우 email로 찾아서 삭제
            storage.entrySet().removeIf(entry ->
                    user.getEmail() != null && user.getEmail().equals(entry.getValue().getEmail())
            );
        }
    }

    @Override
    public void deleteById(Long userId) {
        storage.remove(userId);
    }

    @Override
    public Optional<User> findById(Long userId) {
        return Optional.ofNullable(storage.get(userId));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return storage.values().stream()
                .filter(u -> email.equals(u.getEmail()))
                .findFirst();
    }

    @Override
    public Optional<User> findBySocialId(String socialId) {
        return storage.values().stream()
                .filter(u -> socialId != null && socialId.equals(u.getSocialId()))
                .findFirst();
    }

    @Override
    public boolean existsByEmail(String email) {
        return storage.values().stream()
                .anyMatch(u -> email.equals(u.getEmail()));
    }

    // 테스트 헬퍼 메서드
    public void clear() {
        storage.clear();
        idGenerator.set(1);
    }

    public int count() {
        return storage.size();
    }

    public List<User> findAll() {
        return new ArrayList<>(storage.values());
    }
}
