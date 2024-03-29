package com.example.dailychallenge.controller.comment;

import static com.example.dailychallenge.util.fixture.TokenFixture.AUTHORIZATION;
import static com.example.dailychallenge.util.fixture.TokenFixture.EMAIL;
import static com.example.dailychallenge.util.fixture.TokenFixture.PASSWORD;
import static com.example.dailychallenge.util.fixture.TokenFixture.TOKEN_PREFIX;
import static com.example.dailychallenge.util.fixture.user.UserFixture.createOtherUser;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.removeHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestPartFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.dailychallenge.dto.ChallengeDto;
import com.example.dailychallenge.dto.CommentDto;
import com.example.dailychallenge.dto.UserDto;
import com.example.dailychallenge.entity.Heart;
import com.example.dailychallenge.entity.challenge.Challenge;
import com.example.dailychallenge.entity.challenge.ChallengeCategory;
import com.example.dailychallenge.entity.challenge.ChallengeDuration;
import com.example.dailychallenge.entity.challenge.ChallengeLocation;
import com.example.dailychallenge.entity.comment.Comment;
import com.example.dailychallenge.entity.users.User;
import com.example.dailychallenge.exception.users.UserNotFound;
import com.example.dailychallenge.repository.HeartRepository;
import com.example.dailychallenge.service.challenge.ChallengeService;
import com.example.dailychallenge.service.comment.CommentService;
import com.example.dailychallenge.service.users.UserService;
import com.example.dailychallenge.util.fixture.TestDataSetup;
import com.example.dailychallenge.utils.JwtTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.dailychallenge.com", uriPort = 443)
@ExtendWith(RestDocumentationExtension.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Import({TestDataSetup.class})
public class CommentControllerDocTest {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserService userService;
    @Autowired
    private ChallengeService challengeService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    protected AuthenticationManager authenticationManager;
    @Autowired
    protected JwtTokenUtil jwtTokenUtil;

    @Autowired
    private HeartRepository heartRepository;
    @Autowired
    private TestDataSetup testDataSetup;

    private MockMultipartFile createMultipartFiles() {
        String path = "commentDtoImg";
        String imageName = "commentDtoImg.jpg";
        return new MockMultipartFile(path, imageName,
                "image/jpg", new byte[]{1, 2, 3, 4});
    }

    public UserDto createUser() {
        UserDto userDto = new UserDto();
        userDto.setEmail(EMAIL);
        userDto.setUserName("홍길동");
        userDto.setInfo("testInfo");
        userDto.setPassword(PASSWORD);
        return userDto;
    }
    public Challenge createChallenge() throws Exception {
        User savedUser = userService.saveUser(createUser(), passwordEncoder);
        ChallengeDto challengeDto = ChallengeDto.builder()
                .title("제목입니다.")
                .content("내용입니다.")
                .challengeCategory(ChallengeCategory.STUDY.getDescription())
                .challengeLocation(ChallengeLocation.INDOOR.getDescription())
                .challengeDuration(ChallengeDuration.WITHIN_TEN_MINUTES.getDescription())
                .build();
        MultipartFile challengeImg = createMultipartFiles();
        List<MultipartFile> challengeImgFiles = List.of(challengeImg);
        return challengeService.saveChallenge(challengeDto, challengeImgFiles, savedUser);
    }

    public Comment createComment() throws Exception {
        Challenge challenge = createChallenge();
        User user = userService.findByEmail(EMAIL).orElseThrow(UserNotFound::new);
        List<MultipartFile> commentDtoImgFiles = new ArrayList<>();
        commentDtoImgFiles.add(createMultipartFiles());
        CommentDto commentDto = CommentDto.builder()
                .content("댓글 내용")
                .build();

        return commentService.saveComment(commentDto, commentDtoImgFiles, user, challenge);
    }

    @Test
    @DisplayName("댓글 생성 테스트")
    public void createCommentTest() throws Exception {
        Challenge challenge = createChallenge();
        User user = challenge.getUsers();
        testDataSetup.saveUserBadgeEvaluation(user);
        testDataSetup.saveBadgesAndUserBadges(user);
        CommentDto commentDto = CommentDto.builder()
                .content("댓글 내용")
                .build();
        String json = objectMapper.writeValueAsString(commentDto);
        MockMultipartFile mockCommentDto = new MockMultipartFile("commentDto",
                "commentDto",
                "application/json", json.getBytes(UTF_8));

        Long challengeId = challenge.getId();
        String token = generateToken();
        mockMvc.perform(RestDocumentationRequestBuilders
                        .multipart("/{challengeId}/comment/new", challengeId)
                        .file(mockCommentDto)
                        .part(new MockPart("commentImgFiles", "commentImgFiles", createMultipartFiles().getBytes()))
                        .part(new MockPart("commentImgFiles", "commentImgFiles", createMultipartFiles().getBytes()))
                        .header(AUTHORIZATION, token)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value(commentDto.getContent()))
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andDo(print())
                .andDo(document("comment-create",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(
                                removeHeaders("Vary", "X-Content-Type-Options", "X-XSS-Protection", "Pragma", "Expires",
                                        "Cache-Control", "Strict-Transport-Security", "X-Frame-Options"),
                                prettyPrint()),
                        pathParameters(
                                parameterWithName("challengeId").description("챌린지 아이디")
                        ),
                        requestParts(
                                partWithName("commentDto").description("댓글 데이터(JSON)").optional()
                                        .attributes(key("type").value("JSON")),
                                partWithName("commentImgFiles").description("댓글 이미지 파일(FILE)").optional()
                                        .attributes(key("type").value(".jpg"))
                        ),
                        requestPartFields("commentDto",
                                fieldWithPath("content").description("댓글 내용")
                                        .attributes(key("format").value("\"\", \" \"은 허용하지 않습니다."))
                        )
                ));
    }

    @Test
    @DisplayName("댓글 수정 테스트")
    public void updateCommentTest() throws Exception {
        Comment savedComment = createComment();
        CommentDto commentDto = CommentDto.builder()
                .content("댓글 수정")
                .build();
        String json = objectMapper.writeValueAsString(commentDto);
        MockMultipartFile mockCommentDto = new MockMultipartFile("commentDto",
                "commentDto",
                "application/json", json.getBytes(UTF_8));

        Long challengeId = savedComment.getChallenge().getId();
        Long commentId = savedComment.getId();
        String token = generateToken();
        mockMvc.perform(RestDocumentationRequestBuilders
                        .multipart("/{challengeId}/comment/{commentId}", challengeId, commentId)
                        .file(mockCommentDto)
                        .part(new MockPart("commentImgFiles", "commentImgFiles", createMultipartFiles().getBytes()))
                        .part(new MockPart("commentImgFiles", "commentImgFiles", createMultipartFiles().getBytes()))
                        .header(AUTHORIZATION, token)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("comment-update",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(
                                removeHeaders("Vary", "X-Content-Type-Options", "X-XSS-Protection", "Pragma", "Expires",
                                        "Cache-Control", "Strict-Transport-Security", "X-Frame-Options"),
                                prettyPrint()),
                        pathParameters(
                                parameterWithName("challengeId").description("챌린지 아이디"),
                                parameterWithName("commentId").description("댓글 아이디")
                        ),
                        requestParts(
                                partWithName("commentDto").description("댓글 데이터(JSON)").optional()
                                        .attributes(key("type").value("JSON")),
                                partWithName("commentImgFiles").description("댓글 이미지 파일(FILE)").optional()
                                        .attributes(key("type").value(".jpg"))
                        ),
                        requestPartFields("commentDto",
                                fieldWithPath("content").description("댓글 내용")
                                        .attributes(key("format").value("\"\", \" \"은 허용하지 않습니다."))
                        )
                ));
    }

    @Test
    @DisplayName("댓글 삭제 테스트")
    public void deleteCommentTest() throws Exception {
        Comment savedComment = createComment();
        Long challengeId = savedComment.getChallenge().getId();
        String token = generateToken();
        mockMvc.perform(RestDocumentationRequestBuilders
                        .delete("/{challengeId}/comment/{commentId}",challengeId,savedComment.getId())
                        .header(AUTHORIZATION, token)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("comment-delete",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(
                                removeHeaders("Vary", "X-Content-Type-Options", "X-XSS-Protection", "Pragma", "Expires",
                                        "Cache-Control", "Strict-Transport-Security", "X-Frame-Options"),
                                prettyPrint()),
                        pathParameters(
                                parameterWithName("challengeId").description("챌린지 아이디"),
                                parameterWithName("commentId").description("댓글 아이디")
                        )
                ));
    }

    @Test
    @DisplayName("좋아요 테스트")
    public void isLikeTest() throws Exception {
        Comment savedComment = createComment();
        int beforeLikes = savedComment.getHearts().size();
        String token = generateToken();
        mockMvc.perform(RestDocumentationRequestBuilders
                        .post("/{commentId}/like?isLike=1", savedComment.getId())
                        .header(AUTHORIZATION, token)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isLike").value(beforeLikes +1))
                .andDo(document("comment-like",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(
                                removeHeaders("Vary", "X-Content-Type-Options", "X-XSS-Protection", "Pragma", "Expires",
                                        "Cache-Control", "Strict-Transport-Security", "X-Frame-Options"),
                                prettyPrint()),
                        requestParameters(
                                parameterWithName("isLike").description("좋아요(1)/좋아요 취소(0)")
                        ),
                        pathParameters(
                                parameterWithName("commentId").description("댓글 아이디")
                        )
                ));
    }

    @Test
    @DisplayName("특정 챌린지의 댓글들 조회 테스트")
    public void searchCommentsByChallengeId() throws Exception {
        Challenge challenge = createChallenge();
        User otherUser = userService.saveUser(createOtherUser(), passwordEncoder);
        for (int i = 0; i < 5; i++) {
            List<MultipartFile> commentDtoImgFiles = new ArrayList<>();
            commentDtoImgFiles.add(createMultipartFiles());
            CommentDto commentDto = CommentDto.builder()
                    .content("댓글 내용" + i)
                    .build();
            commentService.saveComment(commentDto, commentDtoImgFiles, otherUser, challenge);
        }
        Long challengeId = challenge.getId();

        mockMvc.perform(get("/{challengeId}/comment", challengeId)
                        .param("size", "20")
                        .param("page", "0")
                        .param("sort", "time")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("search-comments-by-challengeId",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(
                                removeHeaders("Vary", "X-Content-Type-Options", "X-XSS-Protection", "Pragma", "Expires",
                                        "Cache-Control", "Strict-Transport-Security", "X-Frame-Options"),
                                prettyPrint()),
                        pathParameters(
                                parameterWithName("challengeId").description("챌린지 아이디")
                        ),
                        requestParameters(
                                parameterWithName("size").description("기본값: 10").optional(),
                                parameterWithName("page").description("기본값: 0, 0번부터 시작합니다.").optional(),
                                parameterWithName("sort").description("기본값: time-내림차순").optional()
                        ),
                        relaxedResponseFields(
                                fieldWithPath("content[*].id").description("댓글 id"),
                                fieldWithPath("content[*].content").description("댓글 내용"),
                                fieldWithPath("content[*].likes").description("댓글 좋아요 갯수"),
                                fieldWithPath("content[*].createdAt").description("댓글 생성 시간"),
                                fieldWithPath("content[*].commentImgUrls").description("댓글 이미지들 url"),
                                fieldWithPath("content[*].commentOwnerUser").description("댓글 소유자 정보"),
                                fieldWithPath("content[*].commentLikeUsersInfo").description("댓글 좋아요한 유저들의 정보")
                        )
                ));
    }

    @Test
    @DisplayName("특정 챌린지의 댓글들 조회 좋아요 순으로 정렬 테스트")
    public void searchCommentsByChallengeIdSortByLikes() throws Exception {
        Challenge challenge = createChallenge();
        User otherUser = userService.saveUser(createOtherUser(), passwordEncoder);
        for (int i = 0; i < 5; i++) {
            List<MultipartFile> commentDtoImgFiles = new ArrayList<>();
            commentDtoImgFiles.add(createMultipartFiles());
            CommentDto commentDto = CommentDto.builder()
                    .content("댓글 내용" + i)
                    .build();
            Comment comment = commentService.saveComment(commentDto, commentDtoImgFiles, otherUser, challenge);

            // 좋아요 누르기
            for (int j = 0; j <= i; j++) {
                User user = testDataSetup.saveUser("testName" + j, "testEmail" + j + "test.com", PASSWORD);
                heartRepository.save(Heart.builder()
                        .comment(comment)
                        .users(user)
                        .build());
            }
        }
        Long challengeId = challenge.getId();

        mockMvc.perform(get("/{challengeId}/comment", challengeId)
                        .param("size", "20")
                        .param("page", "0")
                        .param("sort", "likes")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("search-comments-by-challengeId-sort-by-likes",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(
                                removeHeaders("Vary", "X-Content-Type-Options", "X-XSS-Protection", "Pragma", "Expires",
                                        "Cache-Control", "Strict-Transport-Security", "X-Frame-Options"),
                                prettyPrint()),
                        pathParameters(
                                parameterWithName("challengeId").description("챌린지 아이디")
                        ),
                        requestParameters(
                                parameterWithName("size").description("기본값: 10").optional(),
                                parameterWithName("page").description("기본값: 0, 0번부터 시작합니다.").optional(),
                                parameterWithName("sort").description("기본값: time-내림차순").optional()
                        ),
                        relaxedResponseFields(
                                fieldWithPath("content[*].id").description("댓글 id"),
                                fieldWithPath("content[*].content").description("댓글 내용"),
                                fieldWithPath("content[*].likes").description("댓글 좋아요 갯수"),
                                fieldWithPath("content[*].createdAt").description("댓글 생성 시간"),
                                fieldWithPath("content[*].commentImgUrls").description("댓글 이미지들 url"),
                                fieldWithPath("content[*].commentOwnerUser").description("댓글 소유자 정보"),
                                fieldWithPath("content[*].commentLikeUsersInfo").description("댓글 좋아요한 유저들의 정보")
                        )
                ));
    }

    @Test
    @DisplayName("유저가 작성한 챌린지의 댓글들 조회 테스트")
    public void searchCommentsByUserId() throws Exception {
        Challenge challenge = createChallenge();
        User savedUser = challenge.getUsers();
        for (int i = 0; i < 5; i++) {
            List<MultipartFile> commentDtoImgFiles = new ArrayList<>();
            commentDtoImgFiles.add(createMultipartFiles());
            CommentDto commentDto = CommentDto.builder()
                    .content("댓글 내용" + i)
                    .build();
            commentService.saveComment(commentDto, commentDtoImgFiles, savedUser, challenge);
        }
        ChallengeDto challengeDto = ChallengeDto.builder()
                .title("다른 제목입니다.")
                .content("다른 내용입니다.")
                .challengeCategory(ChallengeCategory.STUDY.getDescription())
                .challengeLocation(ChallengeLocation.INDOOR.getDescription())
                .challengeDuration(ChallengeDuration.WITHIN_TEN_MINUTES.getDescription())
                .build();
        MultipartFile challengeImg = createMultipartFiles();
        List<MultipartFile> challengeImgFiles = List.of(challengeImg);
        Challenge otherChallenge = challengeService.saveChallenge(challengeDto, challengeImgFiles, savedUser);
        for (int i = 5; i < 8; i++) {
            List<MultipartFile> commentDtoImgFiles = new ArrayList<>();
            commentDtoImgFiles.add(createMultipartFiles());
            CommentDto commentDto = CommentDto.builder()
                    .content("다른 댓글 내용" + i)
                    .build();
            commentService.saveComment(commentDto, commentDtoImgFiles, savedUser, otherChallenge);
        }
        Long userId = savedUser.getId();

        String token = generateToken();
        mockMvc.perform(get("/user/{userId}/comment", userId)
                        .header(AUTHORIZATION, token)
                        .param("size", "20")
                        .param("page", "0")
                        .param("sort", "time")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("search-comments-by-userId",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(
                                removeHeaders("Vary", "X-Content-Type-Options", "X-XSS-Protection", "Pragma", "Expires",
                                        "Cache-Control", "Strict-Transport-Security", "X-Frame-Options"),
                                prettyPrint()),
                        pathParameters(
                                parameterWithName("userId").description("유저 아이디")
                        ),
                        requestParameters(
                                parameterWithName("size").description("기본값: 10").optional(),
                                parameterWithName("page").description("기본값: 0, 0번부터 시작합니다.").optional(),
                                parameterWithName("sort").description("기본값: time-내림차순").optional()
                        ),
                        relaxedResponseFields(
                                fieldWithPath("content[*].id").description("댓글 id"),
                                fieldWithPath("content[*].content").description("댓글 내용"),
                                fieldWithPath("content[*].likes").description("댓글 좋아요 갯수"),
                                fieldWithPath("content[*].createdAt").description("댓글 생성 시간"),
                                fieldWithPath("content[*].commentImgUrls").description("댓글 이미지들 url"),
                                fieldWithPath("content[*].challengeId").description("챌린지 id"),
                                fieldWithPath("content[*].challengeTitle").description("챌린지 제목")
                        )
                ));
    }

    @Test
    @DisplayName("특정 챌린지의 유저가 작성한 댓글들 이미지 조회 테스트")
    public void searchCommentsByUserIdByChallengeId() throws Exception {
        Challenge challenge = createChallenge();
        User savedUser = challenge.getUsers();
        for (int i = 0; i < 5; i++) {
            List<MultipartFile> commentDtoImgFiles = new ArrayList<>();
            commentDtoImgFiles.add(createMultipartFiles());
            CommentDto commentDto = CommentDto.builder()
                    .content("댓글 내용" + i)
                    .build();
            commentService.saveComment(commentDto, commentDtoImgFiles, savedUser, challenge);
        }
        ChallengeDto challengeDto = ChallengeDto.builder()
                .title("다른 제목입니다.")
                .content("다른 내용입니다.")
                .challengeCategory(ChallengeCategory.STUDY.getDescription())
                .challengeLocation(ChallengeLocation.INDOOR.getDescription())
                .challengeDuration(ChallengeDuration.WITHIN_TEN_MINUTES.getDescription())
                .build();
        MultipartFile challengeImg = createMultipartFiles();
        List<MultipartFile> challengeImgFiles = List.of(challengeImg);
        Challenge otherChallenge = challengeService.saveChallenge(challengeDto, challengeImgFiles, savedUser);
        for (int i = 5; i < 8; i++) {
            List<MultipartFile> commentDtoImgFiles = new ArrayList<>();
            commentDtoImgFiles.add(createMultipartFiles());
            CommentDto commentDto = CommentDto.builder()
                    .content("다른 댓글 내용" + i)
                    .build();
            commentService.saveComment(commentDto, commentDtoImgFiles, savedUser, otherChallenge);
        }
        Long challengeId = challenge.getId();

        String token = generateToken();
        mockMvc.perform(get("/challenge/{challengeId}/comment", challengeId)
                        .header(AUTHORIZATION, token)
                        .param("size", "20")
                        .param("page", "0")
                        .param("sort", "time")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andDo(document("search-comments-by-userId-by-challengeId",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(
                                removeHeaders("Vary", "X-Content-Type-Options", "X-XSS-Protection", "Pragma", "Expires",
                                        "Cache-Control", "Strict-Transport-Security", "X-Frame-Options"),
                                prettyPrint()),
                        pathParameters(
                                parameterWithName("challengeId").description("챌린지 아이디")
                        ),
                        requestParameters(
                                parameterWithName("size").description("기본값: 10").optional(),
                                parameterWithName("page").description("기본값: 0, 0번부터 시작합니다.").optional(),
                                parameterWithName("sort").description("기본값: time-내림차순").optional()
                        ),
                        relaxedResponseFields(
                                fieldWithPath("content[*].id").description("댓글 id"),
                                fieldWithPath("content[*].commentImgUrls").description("댓글 이미지들 url")
                        )
                ));
    }

    private String generateToken() {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(EMAIL, PASSWORD));
        if (auth.isAuthenticated()) {
            UserDetails userDetails = userService.loadUserByUsername(EMAIL);
            return TOKEN_PREFIX + jwtTokenUtil.generateToken(userDetails.getUsername());
        }

        throw new IllegalArgumentException("token 생성 오류");
    }
}
