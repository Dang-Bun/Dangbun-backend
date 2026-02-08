package com.dangbun.domain.user.adapter.out.persistence;

import com.dangbun.domain.user.domain.LoginType;
import com.dangbun.domain.user.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class FakeUserRepositoryTest {

    private FakeUserRepository fakeUserRepository;

    @BeforeEach
    void setUp() {
        fakeUserRepository = new FakeUserRepository();
    }

    @Test
    void User_withId_저장_후_조회_userId_확인() {
        // given
        Long userId = 1L;
        String email = "test@test.com";

        User user = User.withId(
                new User.UserId(userId),
                "테스트유저",
                email,
                "encoded_Password1",
                LoginType.EMAIL,
                null,
                true,
                LocalDateTime.now()
        );

        // when
        User saved = fakeUserRepository.save(user);
        Optional<User> found = fakeUserRepository.findById(userId);

        // then
        assertThat(user.getUserId()).isNotNull();
        assertThat(user.getUserId().value()).isEqualTo(userId);
        assertThat(saved.getUserId()).isNotNull();
        assertThat(saved.getUserId().value()).isEqualTo(userId);
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isNotNull();
        assertThat(found.get().getUserId().value()).isEqualTo(userId);
    }

    @Test
    void User_withoutId_저장시_자동_ID_생성() {
        // given
        String email = "test@test.com";
        User user = User.withoutId("테스트유저", email, "password", LoginType.EMAIL, null, true);

        // when
        User saved = fakeUserRepository.save(user);
        Optional<User> found = fakeUserRepository.findById(1L);

        // then
        assertThat(user.getUserId()).isNull();
        assertThat(saved.getUserId()).isNotNull();
        assertThat(saved.getUserId().value()).isEqualTo(1L);
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(email);
    }

    @Test
    void 이메일로_조회() {
        // given
        String email = "test@test.com";
        User user = User.withoutId("테스트유저", email, "password", LoginType.EMAIL, null, true);
        fakeUserRepository.save(user);

        // when
        Optional<User> found = fakeUserRepository.findByEmail(email);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(email);
    }

    @Test
    void socialId로_조회() {
        // given
        String socialId = "kakao123";
        User user = User.withId(
                new User.UserId(1L),
                "카카오유저",
                "kakao@test.com",
                null,
                LoginType.KAKAO,
                socialId,
                true,
                LocalDateTime.now()
        );
        fakeUserRepository.save(user);

        // when
        Optional<User> found = fakeUserRepository.findBySocialId(socialId);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getSocialId()).isEqualTo(socialId);
    }

    @Test
    void 이메일_존재_여부_확인() {
        // given
        String email = "test@test.com";
        User user = User.withoutId("테스트유저", email, "password", LoginType.EMAIL, null, true);
        fakeUserRepository.save(user);

        // when, then
        assertThat(fakeUserRepository.existsByEmail(email)).isTrue();
        assertThat(fakeUserRepository.existsByEmail("nonexistent@test.com")).isFalse();
    }

    @Test
    void delete_userId가_있는_경우() {
        // given
        Long userId = 1L;
        User user = User.withId(
                new User.UserId(userId),
                "테스트유저",
                "test@test.com",
                "password",
                LoginType.EMAIL,
                null,
                true,
                LocalDateTime.now()
        );
        fakeUserRepository.save(user);

        // when
        fakeUserRepository.delete(user);

        // then
        assertThat(fakeUserRepository.findById(userId)).isEmpty();
    }

    @Test
    void delete_userId가_없는_경우_이메일로_삭제() {
        // given
        String email = "test@test.com";
        User user = User.withoutId("테스트유저", email, "password", LoginType.EMAIL, null, true);
        User saved = fakeUserRepository.save(user);

        // userId가 없는 원본 user 객체로 삭제 시도
        User userWithoutId = User.withoutId("테스트유저", email, "password", LoginType.EMAIL, null, true);

        // when
        fakeUserRepository.delete(userWithoutId);

        // then
        assertThat(fakeUserRepository.findByEmail(email)).isEmpty();
    }
}
