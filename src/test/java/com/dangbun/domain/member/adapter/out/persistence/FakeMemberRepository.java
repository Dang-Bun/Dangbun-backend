package com.dangbun.domain.member.adapter.out.persistence;

import com.dangbun.domain.member.application.port.out.MemberCommandPort;
import com.dangbun.domain.member.application.port.out.MemberQueryPort;
import com.dangbun.domain.member.domain.Member;
import com.dangbun.domain.member.domain.MemberRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 테스트용 인메모리 Member 저장소
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeMemberRepository implements MemberCommandPort, MemberQueryPort {

    private final Map<Long, Member> storage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Member save(Member member) {
        if (member.getMemberId() == null) {
            Long newId = idGenerator.getAndIncrement();
            Member saved = Member.withId(
                    newId,
                    member.getRole(),
                    member.getName(),
                    member.getStatus(),
                    member.getInformation(),
                    member.getPlaceId(),
                    member.getUserId(),
                    LocalDateTime.now()
            );
            storage.put(newId, saved);
            return saved;
        } else {
            storage.put(member.getMemberId(), member);
            return member;
        }
    }

    @Override
    public void delete(Member member) {
        storage.remove(member.getMemberId());
    }

    @Override
    public List<Member> findByPlaceId(Long placeId) {
        return storage.values().stream()
                .filter(m -> m.getPlaceId().equals(placeId))
                .toList();
    }

    @Override
    public List<Member> findWaitingMembersByPlaceId(Long placeId) {
        return storage.values().stream()
                .filter(m -> m.getPlaceId().equals(placeId) && m.getRole() == MemberRole.WAITING)
                .toList();
    }

    @Override
    public Optional<Member> findByMemberIdAndPlaceId(Long memberId, Long placeId) {
        return storage.values().stream()
                .filter(m -> m.getMemberId().equals(memberId) && m.getPlaceId().equals(placeId))
                .findFirst();
    }

    @Override
    public Optional<Member> findByPlaceIdAndName(Long placeId, String name) {
        return storage.values().stream()
                .filter(m -> m.getPlaceId().equals(placeId) && m.getName().equals(name))
                .findFirst();
    }

    @Override
    public List<Member> findAllByNameIn(List<String> names) {
        return storage.values().stream()
                .filter(m -> names.contains(m.getName()))
                .toList();
    }

    @Override
    public List<Member> findAllByIds(List<Long> memberIds) {
        return storage.values().stream()
                .filter(m -> memberIds.contains(m.getMemberId()))
                .toList();
    }

    @Override
    public Page<Member> findByPlaceIdWithPageable(Long placeId, Pageable pageable) {
        List<Member> members = storage.values().stream()
                .filter(m -> m.getPlaceId().equals(placeId))
                .toList();
        return new PageImpl<>(members, pageable, members.size());
    }

    @Override
    public Page<Member> findByPlaceIdAndNameContaining(Long placeId, String searchName, Pageable pageable) {
        List<Member> members = storage.values().stream()
                .filter(m -> m.getPlaceId().equals(placeId) && m.getName().contains(searchName))
                .toList();
        return new PageImpl<>(members, pageable, members.size());
    }

    @Override
    public Member findById(Long memberId) {
        return storage.get(memberId);
    }

    @Override
    public List<Member> findByUserId(Long userId) {
        return storage.values().stream()
                .filter(m -> m.getUserId().equals(userId))
                .toList();
    }

    @Override
    public Optional<Member> findByUserIdAndPlaceId(Long userId, Long placeId) {
        return storage.values().stream()
                .filter(m -> m.getUserId().equals(userId) && m.getPlaceId().equals(placeId))
                .findFirst();
    }

    @Override
    public Optional<Member> findFirstByPlaceId(Long placeId) {
        return storage.values().stream()
                .filter(m -> m.getPlaceId().equals(placeId))
                .findFirst();
    }

    // 테스트 헬퍼 메서드
    public void clear() {
        storage.clear();
        idGenerator.set(1);
    }

    public int count() {
        return storage.size();
    }

    public Optional<Member> findById(Long memberId, boolean dummy) {
        return Optional.ofNullable(storage.get(memberId));
    }
}
