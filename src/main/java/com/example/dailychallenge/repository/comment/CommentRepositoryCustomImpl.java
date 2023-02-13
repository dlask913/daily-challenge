package com.example.dailychallenge.repository.comment;

import static com.example.dailychallenge.entity.challenge.QChallenge.challenge;
import static com.example.dailychallenge.entity.comment.QComment.comment;

import com.example.dailychallenge.repository.challenge.OrderByNull;
import com.example.dailychallenge.vo.QResponseChallengeComment;
import com.example.dailychallenge.vo.ResponseChallengeComment;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import javax.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class CommentRepositoryCustomImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public CommentRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<ResponseChallengeComment> searchCommentsByChallengeId(Long challengeId, Pageable pageable) {

        List<ResponseChallengeComment> content = queryFactory
                .select(new QResponseChallengeComment(comment))
                .from(comment)
                .leftJoin(comment.challenge, challenge)
                .where(challengeIdEq(challengeId))
//                .groupBy(userChallenge.challenge)
                .orderBy(commentSort(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(comment)
                .from(comment)
                .leftJoin(comment.challenge, challenge)
                .where(challengeIdEq(challengeId))
//                .groupBy(userChallenge.challenge)
                .fetch()
                .size();

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression challengeIdEq(Long challengeId) {
        if (challengeId == null) {
            throw new IllegalArgumentException();
        }
        return comment.challenge.id.eq(challengeId);
    }

    private OrderSpecifier<?> commentSort(Pageable pageable) {
        if (pageable.getSort().isEmpty()) {
            return OrderByNull.getDefault();
        }
        for (Sort.Order order : pageable.getSort()) {
//            Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC; // 새로운 정렬 조건이 추가되면 처리하자
            if (order.getProperty().equals("time")) {
//                return new OrderSpecifier<>(direction, challenge.created_at);
                return new OrderSpecifier<>(Order.DESC, comment.created_at);
            }
            if (order.getProperty().equals("likes")) {
                return new OrderSpecifier<>(Order.DESC, comment.likes);
            }
        }
        return OrderByNull.getDefault();
    }
}
