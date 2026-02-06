package com.dangbun.domain.place.adapter.in.web;

import com.dangbun.domain.member.original.entity.MemberJpaEntity;
import com.dangbun.domain.member.original.entity.MemberRole;
import com.dangbun.domain.member.original.repository.MemberRepository;
/*
 * TODO: Place 도메인 헥사고날 아키텍처 전환 완료 후 수정 필요
 * - PlaceRepository 의존성 제거
 * - Member 도메인 헥사고날 아키텍처 전환 후:
 *   - MemberRepository 대신 MemberCommandPort 사용
 *   - UserRepository 대신 UserQueryPort 사용
 * - 현재 MemberJpaEntity를 직접 생성하는 로직을
 *   CreatePlaceUseCase 또는 별도 MemberCommandUseCase로 이동
 * - placeRepository.findById(placeId).get() 호출 제거 후
 *   PlaceCommandPort에서 반환된 Place 도메인 객체 활용
 */
import com.dangbun.common.hexagonal.WebAdapter;
import com.dangbun.domain.place.adapter.out.persistence.SpringDataPlaceRepository;
import com.dangbun.domain.place.application.port.in.command.CreatePlaceCommand;
import com.dangbun.domain.place.application.port.in.command.CreatePlaceUseCase;
import com.dangbun.domain.user.entity.CustomUserDetails;
import com.dangbun.domain.user.exception.custom.NoSuchUserException;
import com.dangbun.domain.user.repository.UserRepository;
import com.dangbun.domain.user.response.status.UserExceptionResponse;
import com.dangbun.global.docs.DocumentedApiErrors;
import com.dangbun.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.dangbun.domain.user.response.status.UserExceptionResponse.NO_SUCH_USER;

@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "Place", description = "PlaceController - 플레이스 관련 API")
@WebAdapter("/places")
public class CreatePlaceController {

    private final CreatePlaceUseCase createPlaceUsecase;

    /*
      Todo Member 컨텍스트 아키텍처 전환 시 수정
     */
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final SpringDataPlaceRepository placeRepository;

    @Operation(summary = "플레이스 생성", description = "플레이스를 생성합니다. 플레이스를 생성한 user는 매니저가 됩니다.")
    @DocumentedApiErrors(
            value = {UserExceptionResponse.class},
            includes = {"NO_SUCH_USER"}
    )
    @PostMapping
    public ResponseEntity<BaseResponse<PostCreatePlaceResponse>> createPlace(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                             @RequestBody PostCreatePlaceRequest request) {
        CreatePlaceCommand command = new CreatePlaceCommand(
                userDetails.getUser().getUserId(),
                request.placeName(),
                request.category(),
                request.categoryName(),
                request.managerName(),
                request.information()
        );


        Long placeId = createPlaceUsecase.createPlaceWithManager(command);

        /*
          Todo Member 컨텍스트 아키텍처 전환 시 수정
          전용 인커밍 포트 생성
         */

        MemberJpaEntity member = MemberJpaEntity.builder()
                .name(request.managerName())
                .place(placeRepository.findById(placeId).get())
                .information(request.information())
                .role(MemberRole.MANAGER)
                .status(true)
                .user(userRepository.findById(userDetails.getUser().getUserId()).orElseThrow(() -> new NoSuchUserException(NO_SUCH_USER)))
                .build();

        memberRepository.save(member);
        PostCreatePlaceResponse response = PostCreatePlaceResponse.of(placeId);

        return ResponseEntity.ok(BaseResponse.ok(response));
    }
}
