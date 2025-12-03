package com.gwtt.dagachi.repository;

import static org.assertj.core.api.Assertions.*;

import com.gwtt.dagachi.config.JpaAuditingConfig;
import com.gwtt.dagachi.config.TestQueryDSLConfig;
import com.gwtt.dagachi.constants.ParticipationStatus;
import com.gwtt.dagachi.constants.PostingType;
import com.gwtt.dagachi.constants.Role;
import com.gwtt.dagachi.entity.Participation;
import com.gwtt.dagachi.entity.Posting;
import com.gwtt.dagachi.entity.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import({TestQueryDSLConfig.class, JpaAuditingConfig.class})
@ActiveProfiles("test")
@DisplayName("ParticipationRepository 테스트")
class ParticipationRepositoryTest {

  @Autowired private ParticipationRepository participationRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private PostingRepository postingRepository;

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
    author = userRepository.save(author);

    participant =
        User.builder()
            .username("participant")
            .password("password")
            .role(Role.USER)
            .nickname("참가자")
            .build();
    participant = userRepository.save(participant);

    posting =
        Posting.builder()
            .title("테스트 포스팅")
            .description("설명")
            .type(PostingType.PROJECT)
            .maxCapacity(5)
            .author(author)
            .build();
    posting = postingRepository.save(posting);
  }

  @Test
  @DisplayName("게시글 ID로 참가자 목록을 조회한다")
  void findByPostingId() {
    // given
    Participation p1 = Participation.builder().posting(posting).participant(participant).build();
    participationRepository.save(p1);

    User participant2 =
        User.builder()
            .username("participant2")
            .password("password")
            .role(Role.USER)
            .nickname("참가자2")
            .build();
    participant2 = userRepository.save(participant2);
    Participation p2 = Participation.builder().posting(posting).participant(participant2).build();
    participationRepository.save(p2);

    // when
    List<Participation> result = participationRepository.findByPostingId(posting.getId());

    // then
    assertThat(result).hasSize(2);
    assertThat(result)
        .extracting(p -> p.getParticipant().getNickname())
        .containsExactlyInAnyOrder("참가자", "참가자2");
  }

  @Test
  @DisplayName("참가자와 게시글로 참가 정보가 존재하는지 확인한다")
  void existsByParticipantAndPosting() {
    // given
    Participation participation =
        Participation.builder().posting(posting).participant(participant).build();
    participationRepository.save(participation);

    // when
    boolean exists = participationRepository.existsByParticipantAndPosting(participant, posting);
    boolean notExists = participationRepository.existsByParticipantAndPosting(author, posting);

    // then
    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }

  @Test
  @DisplayName("참가자와 게시글로 참가 정보를 조회한다")
  void findByParticipantAndPosting() {
    // given
    Participation participation =
        Participation.builder().posting(posting).participant(participant).build();
    participation = participationRepository.save(participation);

    // when
    Optional<Participation> result =
        participationRepository.findByParticipantAndPosting(participant, posting);

    // then
    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo(participation.getId());
  }

  @Test
  @DisplayName("게시글과 상태로 참가자 수를 카운트한다")
  void countByPostingAndStatus() {
    // given
    Participation p1 = Participation.builder().posting(posting).participant(participant).build();
    p1.setStatus(ParticipationStatus.APPROVED);
    participationRepository.save(p1);

    User participant2 =
        User.builder()
            .username("participant2")
            .password("password")
            .role(Role.USER)
            .nickname("참가자2")
            .build();
    participant2 = userRepository.save(participant2);
    Participation p2 = Participation.builder().posting(posting).participant(participant2).build();
    p2.setStatus(ParticipationStatus.APPROVED);
    participationRepository.save(p2);

    User participant3 =
        User.builder()
            .username("participant3")
            .password("password")
            .role(Role.USER)
            .nickname("참가자3")
            .build();
    participant3 = userRepository.save(participant3);
    Participation p3 = Participation.builder().posting(posting).participant(participant3).build();
    // PENDING 상태
    participationRepository.save(p3);

    // when
    int approvedCount =
        participationRepository.countByPostingAndStatus(posting, ParticipationStatus.APPROVED);
    int pendingCount =
        participationRepository.countByPostingAndStatus(posting, ParticipationStatus.PENDING);

    // then
    assertThat(approvedCount).isEqualTo(2);
    assertThat(pendingCount).isEqualTo(1);
  }

  @Test
  @DisplayName("Soft Delete된 참가는 조회되지 않는다")
  void softDeleteExclusion() {
    // given
    Participation participation =
        Participation.builder().posting(posting).participant(participant).build();
    participation = participationRepository.save(participation);

    // when - soft delete
    participationRepository.delete(participation);
    participationRepository.flush();

    // then
    Optional<Participation> result = participationRepository.findById(participation.getId());
    assertThat(result).isEmpty();

    List<Participation> allParticipations =
        participationRepository.findByPostingId(posting.getId());
    assertThat(allParticipations).isEmpty();
  }
}
