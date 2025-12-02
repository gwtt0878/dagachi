package com.gwtt.dagachi.service;

import static org.assertj.core.api.Assertions.*;

import com.gwtt.dagachi.constants.ParticipationStatus;
import com.gwtt.dagachi.constants.PostingType;
import com.gwtt.dagachi.constants.Role;
import com.gwtt.dagachi.entity.Participation;
import com.gwtt.dagachi.entity.Posting;
import com.gwtt.dagachi.entity.User;
import com.gwtt.dagachi.repository.ParticipationRepository;
import com.gwtt.dagachi.repository.PostingRepository;
import com.gwtt.dagachi.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.gwtt.dagachi.config.TestQueryDSLConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;
import com.gwtt.dagachi.config.SecurityConfig;

@SpringBootTest
@Import({TestQueryDSLConfig.class, SecurityConfig.class})
@ActiveProfiles("test")
@DisplayName("ParticipationService 동시성 테스트")
class ParticipationConcurrencyTest {

  @Autowired private ParticipationService participationService;
  @Autowired private UserRepository userRepository;
  @Autowired private PostingRepository postingRepository;
  @Autowired private ParticipationRepository participationRepository;

  private User author;
  private Posting posting;
  private List<User> participants;

  @BeforeEach
  @Transactional
  void setUp() {
    // 기존 데이터 정리
    participationRepository.deleteAll();
    postingRepository.deleteAll();
    userRepository.deleteAll();

    // 작성자 생성
    author =
        User.builder()
            .username("author")
            .password("password")
            .role(Role.USER)
            .nickname("작성자")
            .build();
    author = userRepository.save(author);

    // 게시글 생성 (최대 5명)
    posting =
        Posting.builder()
            .title("동시성 테스트 포스팅")
            .description("설명")
            .type(PostingType.PROJECT)
            .maxCapacity(5)
            .author(author)
            .build();
    posting = postingRepository.save(posting);

    // 참가자 1000명 생성
    participants = new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
      User user =
          User.builder()
              .username("user" + i)
              .password("password")
              .role(Role.USER)
              .nickname("참가자" + i)
              .build();
      participants.add(userRepository.save(user));
    }
  }

  @Test
  @DisplayName("동시에 승인과 취소가 일어나도 데이터 정합성이 유지된다")
  void concurrentApproveAndLeave() throws InterruptedException {
    // given - 미리 3명 참가 신청
    List<Participation> participations = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      participationService.joinPosting(participants.get(i).getId(), posting.getId());
    }
    participations = participationRepository.findByPostingId(posting.getId());

    // when - 동시에 승인(작성자)과 취소(참가자) 시도
    ExecutorService executorService = Executors.newFixedThreadPool(2);
    CountDownLatch latch = new CountDownLatch(2);
    AtomicInteger approveSuccess = new AtomicInteger(0);
    AtomicInteger leaveSuccess = new AtomicInteger(0);

    final Long participationId = participations.get(0).getId();
    final Long participantId = participations.get(0).getParticipant().getId();

    // Thread 1: 승인 시도
    executorService.submit(
        () -> {
          try {
            Thread.sleep(10); // 약간의 딜레이
            participationService.approveUser(author.getId(), participationId);
            approveSuccess.incrementAndGet();
          } catch (Exception e) {
            System.out.println("승인 실패: " + e.getMessage());
          } finally {
            latch.countDown();
          }
        });

    // Thread 2: 취소 시도
    executorService.submit(
        () -> {
          try {
            participationService.leavePosting(participantId, posting.getId());
            leaveSuccess.incrementAndGet();
          } catch (Exception e) {
            System.out.println("취소 실패: " + e.getMessage());
          } finally {
            latch.countDown();
          }
        });

    latch.await();
    executorService.shutdown();

    // then - 둘 중 하나만 성공해야 함
    assertThat(approveSuccess.get() + leaveSuccess.get()).isEqualTo(1);

    // 승인이 성공했으면 APPROVED 상태여야 함
    if (approveSuccess.get() == 1) {
      Participation result = participationRepository.findById(participationId).orElseThrow();
      assertThat(result.getStatus()).isEqualTo(ParticipationStatus.APPROVED);
    }

    // 취소가 성공했으면 삭제되었어야 함
    if (leaveSuccess.get() == 1) {
      assertThat(participationRepository.findById(participationId)).isEmpty();
    }
  }

  @Test
  @DisplayName("여러 참가자를 동시에 승인해도 최대 인원을 초과하지 않는다")
  void concurrentApproveMultipleUsers() throws InterruptedException {
    // given - 미리 10000명 참가 신청
    for (int i = 0; i < 1000; i++) {
      participationService.joinPosting(participants.get(i).getId(), posting.getId());
    }
    List<Participation> allParticipations = participationRepository.findByPostingId(posting.getId());
    assertThat(allParticipations).hasSize(1000); // 1000명 모두 참가 성공

    // when - 5명을 동시에 승인
    ExecutorService executorService = Executors.newFixedThreadPool(1000);
    CountDownLatch latch = new CountDownLatch(1000);
    AtomicInteger approveSuccessCount = new AtomicInteger(0);

    for (Participation p : allParticipations) {
      executorService.submit(
          () -> {
            try {
              participationService.approveUser(author.getId(), p.getId());
              approveSuccessCount.incrementAndGet();
            } catch (Exception e) {
              // 동시성으로 인한 실패 가능
            } finally {
              latch.countDown();
            }
          });
    }

    latch.await();
    executorService.shutdown();

    // then
    int approvedCount =
        participationRepository.countByPostingAndStatus(posting, ParticipationStatus.APPROVED);
    assertThat(approvedCount).isLessThanOrEqualTo(5); // 최대 인원 초과 안 함
    assertThat(approvedCount).isEqualTo(approveSuccessCount.get());
  }
}

