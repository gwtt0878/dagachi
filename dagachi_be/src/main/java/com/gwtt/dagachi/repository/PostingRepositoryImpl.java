package com.gwtt.dagachi.repository;

import static com.gwtt.dagachi.entity.QPosting.posting;

import com.gwtt.dagachi.dto.PostingSearchCondition;
import com.gwtt.dagachi.entity.Posting;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class PostingRepositoryImpl implements PostingRepositoryCustom {
  private final JPAQueryFactory queryFactory;

  @Override
  public Page<Posting> searchPostings(PostingSearchCondition condition, Pageable pageable) {
    BooleanBuilder builder = getBooleanBuilder(condition);

    NumberTemplate<Double> orderByDistance = null;

    if (condition.isSortByDistance()
        && condition.getUserLatitude() != null
        && condition.getUserLongitude() != null) {
      orderByDistance = getDistanceTemplate(condition);
    }

    List<Posting> result =
        queryFactory
            .selectFrom(posting)
            .leftJoin(posting.author)
            .fetchJoin()
            .where(builder)
            .orderBy(orderByDistance != null ? orderByDistance.asc() : posting.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    Long count =
        queryFactory
            .select(posting.count())
            .from(posting)
            .leftJoin(posting.author)
            .where(builder)
            .fetchOne();

    return new PageImpl<>(result, pageable, count != null ? count : 0L);
  }

  private BooleanBuilder getBooleanBuilder(PostingSearchCondition condition) {
    BooleanBuilder builder = new BooleanBuilder();
    if (condition.getTitle() != null) {
      builder.and(posting.title.contains(condition.getTitle()));
    }
    if (condition.getType() != null) {
      builder.and(posting.type.eq(condition.getType()));
    }
    if (condition.getStatus() != null) {
      builder.and(posting.status.eq(condition.getStatus()));
    }
    if (condition.getAuthorNickname() != null) {
      builder.and(posting.author.nickname.contains(condition.getAuthorNickname()));
    }

    builder.and(posting.deletedAt.isNull());
    return builder;
  }

  private NumberTemplate<Double> getDistanceTemplate(PostingSearchCondition condition) {
    return Expressions.numberTemplate(
        Double.class,
        "6371 * acos(cos(radians({0})) * cos(radians({1})) * cos(radians({2}) - radians({3})) + sin(radians({0})) * sin(radians({1})))",
        condition.getUserLatitude(),
        posting.location.latitude,
        posting.location.longitude,
        condition.getUserLongitude());
  }
}
