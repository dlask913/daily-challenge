package com.example.dailychallenge.controller;

import com.example.dailychallenge.dto.CommentDto;
import com.example.dailychallenge.entity.badge.Badge;
import com.example.dailychallenge.entity.challenge.Challenge;
import com.example.dailychallenge.entity.comment.Comment;
import com.example.dailychallenge.entity.users.User;
import com.example.dailychallenge.exception.users.UserNotFound;
import com.example.dailychallenge.service.HeartService;
import com.example.dailychallenge.service.badge.UserBadgeEvaluationService;
import com.example.dailychallenge.service.challenge.ChallengeService;
import com.example.dailychallenge.service.comment.CommentService;
import com.example.dailychallenge.service.users.UserService;
import com.example.dailychallenge.vo.ResponseChallengeComment;
import com.example.dailychallenge.vo.ResponseChallengeCommentImg;
import com.example.dailychallenge.vo.ResponseComment;
import com.example.dailychallenge.vo.ResponseUserComment;
import com.example.dailychallenge.vo.badge.ResponseCommentWriteBadge;
import com.example.dailychallenge.vo.badge.ResponseCreateBadge;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final UserService userService;
    private final ChallengeService challengeService;
    private final UserBadgeEvaluationService userBadgeEvaluationService;
    private final HeartService heartService;

    @PostMapping(value = "/{challengeId}/comment/new")
    public ResponseEntity<Object> createComment(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user,
            @PathVariable("challengeId") Long challengeId,
            @RequestPart(required = false) @Valid CommentDto commentDto,
            @RequestPart(required = false) List<MultipartFile> commentImgFiles) {

        User findUser = userService.findByEmail(user.getUsername()).orElseThrow(UserNotFound::new);
        Challenge challenge = challengeService.findById(challengeId);

        commentService.checkCommentDateDuplicate(challengeId, findUser.getId());

        Comment comment = commentService.saveComment(commentDto, commentImgFiles, findUser, challenge);

        Optional<ResponseCommentWriteBadge> optionalResponseCommentWriteBadge = isBadgeCreated(findUser, comment);
        if (optionalResponseCommentWriteBadge.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(optionalResponseCommentWriteBadge);
        }

        ResponseComment responseComment = ResponseComment.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getSpecificCreatedAt())
                .userId(comment.getUsers().getId())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(responseComment);
    }

    private Optional<ResponseCommentWriteBadge> isBadgeCreated(User user, Comment comment) {

        Optional<Badge> optionalBadge = userBadgeEvaluationService.createCommentWriteBadgeIfFollowStandard(user);
        if (optionalBadge.isPresent()) {
            Badge badge = optionalBadge.get();
            ResponseCommentWriteBadge responseCommentWriteBadge = ResponseCommentWriteBadge.builder()
                    .id(comment.getId())
                    .content(comment.getContent())
                    .createdAt(comment.getSpecificCreatedAt())
                    .userId(comment.getUsers().getId())
                    .responseCreateBadge(ResponseCreateBadge.create(badge))
                    .build();
            return Optional.of(responseCommentWriteBadge);
        }
        return Optional.empty();
    }

    @PostMapping("/{challengeId}/comment/{commentId}")
    public void updateComment(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user,
            @PathVariable("challengeId") Long challengeId,
            @PathVariable("commentId") Long commentId,
            @RequestPart(required = false) @Valid CommentDto commentDto,
            @RequestPart(required = false) List<MultipartFile> commentImgFiles) {
        String userEmail = user.getUsername();
        User findUser = userService.findByEmail(userEmail).orElseThrow(UserNotFound::new);

        commentService.updateComment(challengeId, commentId, commentDto, commentImgFiles, findUser);
    }

    @DeleteMapping("/{challengeId}/comment/{commentId}")
    public ResponseEntity<?> deleteComment(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user,
            @PathVariable("challengeId") Long challengeId,
            @PathVariable("commentId") Long commentId){
        String userEmail = user.getUsername();
        User findUser = userService.findByEmail(userEmail).orElseThrow(UserNotFound::new);

        commentService.deleteComment(challengeId, commentId, findUser);
        return ResponseEntity.status(HttpStatus.OK).body("댓글이 삭제되었습니다.");
    }

    @PostMapping("/{commentId}/like")
    public ResponseEntity<?> likeComment(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user,
            @PathVariable("commentId") Long commentId,
            @RequestParam Integer isLike) {

        heartService.updateHeart(isLike,commentId,user.getUsername());
        Long heartOfComment = heartService.getHeartOfComment(commentId);

        Map<String, Long> responseMap = new HashMap<>();
        responseMap.put("isLike", heartOfComment);

        return ResponseEntity.status(HttpStatus.OK).body(responseMap);
    }

    @GetMapping("/{challengeId}/comment")
    public ResponseEntity<Page<ResponseChallengeComment>> searchCommentsByChallengeId(
            @PathVariable Long challengeId,
            @PageableDefault(page = 0, size = 10, sort = "time", direction = Sort.Direction.DESC) Pageable pageable) {

        Challenge challenge = challengeService.findById(challengeId);
        Page<ResponseChallengeComment> result = commentService.searchCommentsByChallengeId(challenge, pageable);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/user/{userId}/comment")
    public ResponseEntity<Page<ResponseUserComment>> searchCommentsByUserId(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user,
            @PathVariable Long userId,
            @PageableDefault(page = 0, size = 10, sort = "time", direction = Sort.Direction.DESC) Pageable pageable) {

        String loginUserEmail = user.getUsername();
        userService.validateUser(loginUserEmail, userId);

        Page<ResponseUserComment> result = commentService.searchCommentsByUserId(userId, pageable);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/challenge/{challengeId}/comment")
    public ResponseEntity<Page<ResponseChallengeCommentImg>> searchCommentsByUserIdByChallengeId(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user,
            @PathVariable Long challengeId,
            @PageableDefault(page = 0, size = 10, sort = "time", direction = Sort.Direction.DESC) Pageable pageable) {

        String userEmail = user.getUsername();
        User findUser = userService.findByEmail(userEmail).orElseThrow(UserNotFound::new);

        Challenge challenge = challengeService.findById(challengeId);
        Page<ResponseChallengeCommentImg> result = commentService.searchCommentsByUserIdByChallengeId(findUser, challenge, pageable);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
