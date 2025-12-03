package com.gwtt.dagachi.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import com.gwtt.dagachi.constants.ParticipationStatus;
import com.gwtt.dagachi.constants.PostingStatus;
import com.gwtt.dagachi.constants.PostingType;
import com.gwtt.dagachi.constants.Role;
import com.gwtt.dagachi.entity.Participation;
import com.gwtt.dagachi.entity.Posting;
import com.gwtt.dagachi.entity.User;
import com.gwtt.dagachi.repository.ParticipationRepository;
import com.gwtt.dagachi.repository.PostingRepository;
import com.gwtt.dagachi.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParticipationService 단위 테스트")
class ParticipationServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PostingRepository postingRepository;
  @Mock private ParticipationRepository participationRepository;

  @InjectMocks private ParticipationService participationService;

  private User author;
  private User participant;
  private Posting posting;

  @BeforeEach
  void setUp() {
    author =
        User.builder()
            .username("author")
            .password("password")
            .role(Role.USER)
            .nickname("작성자")
            .build();
    // Reflection으로 ID 설정
    setId(author, 1L);

    participant =
        User.builder()
            .username("participant")
            .password("password")
            .role(Role.USER)
            .nickname("참가자")
            .build();
    setId(participant, 2L);

    posting =
        Posting.builder()
            .title("테스트 포스팅")
            .description("설명")
            .type(PostingType.PROJECT)
            .maxCapacity(5)
            .author(author)
            .build();
    setId(posting, 1L);
  }

  @Nested
  @DisplayName("joinPosting 메서드")
  class JoinPostingTest {

    @Test
    @DisplayName("정상적으로 참가 신청을 처리한다")
    void success() {
      // given
      given(userRepository.findById(2L)).willReturn(Optional.of(participant));
      given(postingRepository.findByIdForUpdate(1L)).willReturn(Optional.of(posting));
      given(participationRepository.existsByParticipantAndPosting(participant, posting))
          .willReturn(false);

      // when
      participationService.joinPosting(2L, 1L);

      // then
      then(participationRepository).should().save(any(Participation.class));
    }

    @Test
    @DisplayName("본인 게시글에는 참가할 수 없다")
    void cannotJoinOwnPosting() {
      // given
      given(userRepository.findById(1L)).willReturn(Optional.of(author));
      given(postingRepository.findByIdForUpdate(1L)).willReturn(Optional.of(posting));

      // when & then
      assertThatThrownBy(() -> participationService.joinPosting(1L, 1L))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("본인이 참여할 수 없습니다.");
    }

    @Test
    @DisplayName("이미 참가한 게시글에는 중복 참가할 수 없다")
    void cannotJoinTwice() {
      // given
      given(userRepository.findById(2L)).willReturn(Optional.of(participant));
      given(postingRepository.findByIdForUpdate(1L)).willReturn(Optional.of(posting));
      given(participationRepository.existsByParticipantAndPosting(participant, posting))
          .willReturn(true);

      // when & then
      assertThatThrownBy(() -> participationService.joinPosting(2L, 1L))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("이미 해당 게시글에 참여했습니다.");
    }

    @Test
    @DisplayName("완료된 게시글에는 참가할 수 없다")
    void cannotJoinCompletedPosting() {
      // given
      posting.setStatus(PostingStatus.COMPLETED);
      given(userRepository.findById(2L)).willReturn(Optional.of(participant));
      given(postingRepository.findByIdForUpdate(1L)).willReturn(Optional.of(posting));
      given(participationRepository.existsByParticipantAndPosting(participant, posting))
          .willReturn(false);

      // when & then
      assertThatThrownBy(() -> participationService.joinPosting(2L, 1L))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("해당 게시글의 참여가 종료되었습니다.");
    }
  }

  @Nested
  @DisplayName("leavePosting 메서드")
  class LeavePostingTest {

    private Participation participation;

    @BeforeEach
    void setUp() {
      participation = Participation.builder().posting(posting).participant(participant).build();
      setId(participation, 1L);
    }

    @Test
    @DisplayName("정상적으로 참가를 취소한다")
    void success() {
      // given
      given(userRepository.findById(2L)).willReturn(Optional.of(participant));
      given(postingRepository.findById(1L)).willReturn(Optional.of(posting));
      given(participationRepository.findByParticipantAndPostingForUpdate(participant, posting))
          .willReturn(Optional.of(participation));

      // when
      participationService.leavePosting(2L, 1L);

      // then
      then(participationRepository).should().delete(participation);
    }

    @Test
    @DisplayName("승인된 참가는 취소할 수 없다")
    void cannotLeaveApprovedParticipation() {
      // given
      participation.setStatus(ParticipationStatus.APPROVED);
      given(userRepository.findById(2L)).willReturn(Optional.of(participant));
      given(postingRepository.findById(1L)).willReturn(Optional.of(posting));
      given(participationRepository.findByParticipantAndPostingForUpdate(participant, posting))
          .willReturn(Optional.of(participation));

      // when & then
      assertThatThrownBy(() -> participationService.leavePosting(2L, 1L))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("해당 참여 정보는 이미 승인되었습니다.");
    }

    @Test
    @DisplayName("거절된 참가는 취소할 수 없다")
    void cannotLeaveRejectedParticipation() {
      // given
      participation.setStatus(ParticipationStatus.REJECTED);
      given(userRepository.findById(2L)).willReturn(Optional.of(participant));
      given(postingRepository.findById(1L)).willReturn(Optional.of(posting));
      given(participationRepository.findByParticipantAndPostingForUpdate(participant, posting))
          .willReturn(Optional.of(participation));

      // when & then
      assertThatThrownBy(() -> participationService.leavePosting(2L, 1L))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("해당 참여 정보는 이미 거절되었습니다.");
    }
  }

  @Nested
  @DisplayName("approveUser 메서드")
  class ApproveUserTest {

    private Participation participation;

    @BeforeEach
    void setUp() {
      participation = Participation.builder().posting(posting).participant(participant).build();
      setId(participation, 1L);
    }

    @Test
    @DisplayName("정상적으로 참가를 승인한다")
    void success() {
      // given
      given(userRepository.findById(1L)).willReturn(Optional.of(author));
      given(participationRepository.findByIdWithPostingForUpdate(1L))
          .willReturn(Optional.of(participation));
      given(participationRepository.countByPostingAndStatus(posting, ParticipationStatus.APPROVED))
          .willReturn(0);

      // when
      participationService.approveUser(1L, 1L);

      // then
      assertThat(participation.getStatus()).isEqualTo(ParticipationStatus.APPROVED);
      then(participationRepository).should().save(participation);
    }

    @Test
    @DisplayName("작성자가 아니면 승인할 수 없다")
    void cannotApproveIfNotAuthor() {
      // given
      given(userRepository.findById(2L)).willReturn(Optional.of(participant));
      given(participationRepository.findByIdWithPostingForUpdate(1L))
          .willReturn(Optional.of(participation));

      // when & then
      assertThatThrownBy(() -> participationService.approveUser(2L, 1L))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("해당 게시글의 작성자가 아닙니다.");
    }

    @Test
    @DisplayName("최대 인원 초과 시 승인할 수 없다")
    void cannotApproveExceedMaxCapacity() {
      // given
      given(userRepository.findById(1L)).willReturn(Optional.of(author));
      given(participationRepository.findByIdWithPostingForUpdate(1L))
          .willReturn(Optional.of(participation));
      given(participationRepository.countByPostingAndStatus(posting, ParticipationStatus.APPROVED))
          .willReturn(5);

      // when & then
      assertThatThrownBy(() -> participationService.approveUser(1L, 1L))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("해당 게시글의 최대 참여 인원을 초과했습니다.");
    }

    @Test
    @DisplayName("이미 승인된 참가는 다시 승인할 수 없다")
    void cannotApproveAlreadyApproved() {
      // given
      participation.setStatus(ParticipationStatus.APPROVED);
      given(userRepository.findById(1L)).willReturn(Optional.of(author));
      given(participationRepository.findByIdWithPostingForUpdate(1L))
          .willReturn(Optional.of(participation));

      // when & then
      assertThatThrownBy(() -> participationService.approveUser(1L, 1L))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("해당 참여 정보는 이미 승인되었습니다.");
    }
  }

  // Reflection으로 ID 설정하는 헬퍼 메서드
  private void setId(Object entity, Long id) {
    try {
      var idField = entity.getClass().getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(entity, id);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
