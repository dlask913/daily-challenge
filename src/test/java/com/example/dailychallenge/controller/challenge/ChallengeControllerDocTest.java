package com.example.dailychallenge.controller.challenge;

import static com.example.dailychallenge.entity.challenge.ChallengeCategory.ECONOMY;
import static com.example.dailychallenge.entity.challenge.ChallengeCategory.STUDY;
import static com.example.dailychallenge.entity.challenge.ChallengeCategory.WORKOUT;
import static com.example.dailychallenge.entity.challenge.ChallengeDuration.OVER_ONE_HOUR;
import static com.example.dailychallenge.entity.challenge.ChallengeDuration.WITHIN_TEN_MINUTES;
import static com.example.dailychallenge.entity.challenge.ChallengeLocation.INDOOR;
import static com.example.dailychallenge.entity.challenge.ChallengeLocation.OUTDOOR;
import static com.example.dailychallenge.util.fixture.TokenFixture.AUTHORIZATION;
import static com.example.dailychallenge.util.fixture.TokenFixture.EMAIL;
import static com.example.dailychallenge.util.fixture.TokenFixture.PASSWORD;
import static com.example.dailychallenge.util.fixture.challenge.ChallengeImgFixture.createChallengeImgFiles;
import static com.example.dailychallenge.util.fixture.challenge.ChallengeImgFixture.updateChallengeImgFiles;
import static com.example.dailychallenge.util.fixture.user.UserFixture.USERNAME;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestPartFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.dailychallenge.dto.HashtagDto;
import com.example.dailychallenge.entity.challenge.Challenge;
import com.example.dailychallenge.entity.challenge.ChallengeCategory;
import com.example.dailychallenge.entity.challenge.ChallengeDuration;
import com.example.dailychallenge.entity.challenge.ChallengeLocation;
import com.example.dailychallenge.entity.challenge.ChallengeStatus;
import com.example.dailychallenge.entity.users.User;
import com.example.dailychallenge.util.RestDocsTest;
import com.example.dailychallenge.util.fixture.TestDataSetup;
import com.example.dailychallenge.vo.challenge.RequestCreateChallenge;
import com.example.dailychallenge.vo.challenge.RequestUpdateChallenge;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.web.multipart.MultipartFile;

public class ChallengeControllerDocTest extends RestDocsTest {
    @Autowired
    private TestDataSetup testDataSetup;

    private User user;
    private String token;
    private Challenge challenge1;

    @BeforeEach
    void beforeEach() {
        user = testDataSetup.saveUser(USERNAME, EMAIL, PASSWORD);
        token = generateToken(user);
    }

    private void initChallengeData() {
        challenge1 = testDataSetup.챌린지를_생성한다(
                "제목입니다.1",
                "내용입니다.1",
                STUDY.getDescription(),
                INDOOR.getDescription(),
                WITHIN_TEN_MINUTES.getDescription(),
                user);
        testDataSetup.챌린지에_참가한다(challenge1, user);
        testDataSetup.챌린지에_댓글을_단다(challenge1, user);
        testDataSetup.챌린지에_해시태그를_단다(challenge1, List.of("tag1", "tag2"));
    }

    private void initData() {
        initChallengeData();

        Challenge challenge2 = testDataSetup.챌린지를_생성한다(
                "제목입니다.2",
                "내용입니다.2",
                ECONOMY.getDescription(),
                OUTDOOR.getDescription(),
                OVER_ONE_HOUR.getDescription(),
                user
        );
        testDataSetup.챌린지에_참가한다(challenge2, user);
        testDataSetup.챌린지에_해시태그를_단다(challenge2, List.of("tag1", "tag2", "tag3"));

        Challenge challenge6 = null;

        for (int i = 3; i <= 10; i++) {
            Challenge challenge = testDataSetup.챌린지를_생성한다(
                    "제목입니다." + i,
                    "내용입니다." + i,
                    WORKOUT.getDescription(),
                    INDOOR.getDescription(),
                    WITHIN_TEN_MINUTES.getDescription(),
                    user
            );
            testDataSetup.챌린지에_참가한다(challenge, user);

            if (i == 6) {
                testDataSetup.챌린지에_해시태그를_단다(challenge, List.of("tag2", "tag4"));
                challenge6 = challenge;
            }
        }

        for (int i = 1; i <= 8; i++) {
            User otherUser = testDataSetup.saveUser(USERNAME + i, i + "@test.com", PASSWORD);
            if (i == 1) {
                testDataSetup.챌린지에_참가한다(challenge1, otherUser);
            }
            if (2 <= i && i <= 5) {
                testDataSetup.챌린지에_참가한다(challenge2, otherUser);
            }
            if (i == 6) {
                testDataSetup.챌린지에_참가한다(challenge6, otherUser);
            }
        }
    }

    @Test
    @DisplayName("챌린지 생성")
//    @WithAuthUser
    void createChallengeTest() throws Exception {
        testDataSetup.saveUserBadgeEvaluation(user);
        testDataSetup.saveBadgesAndUserBadges(user);

        RequestCreateChallenge requestCreateChallenge = RequestCreateChallenge.builder()
                .title("제목입니다.")
                .content("내용입니다.")
                .challengeCategory("공부")
                .challengeLocation("실내")
                .challengeDuration("10분 이내")
                .build();
        List<MultipartFile> challengeImgFiles = createChallengeImgFiles();

        String json = objectMapper.writeValueAsString(requestCreateChallenge);
        MockMultipartFile mockRequestCreateChallenge = new MockMultipartFile("requestCreateChallenge",
                "requestCreateChallenge",
                "application/json", json.getBytes(
                StandardCharsets.UTF_8));

        HashtagDto hashtagDto = HashtagDto.builder()
                .content(List.of("tag1", "tag2"))
                .build();
        String hashtagDtoJson = objectMapper.writeValueAsString(hashtagDto);
        MockMultipartFile mockHashtagDto = new MockMultipartFile("hashtagDto",
                "hashtagDto",
                "application/json", hashtagDtoJson.getBytes(UTF_8));

        String challengeCategoryDescriptions = String.join(", ", ChallengeCategory.getDescriptions());
        String challengeLocationDescriptions = String.join(", ", ChallengeLocation.getDescriptions());
        String challengeDurationDescriptions = String.join(", ", ChallengeDuration.getDescriptions());
        mockMvc.perform(multipart("/challenge/new")
                        .file(mockRequestCreateChallenge)
                        .part(new MockPart("challengeImgFiles", "challengeImgFile", challengeImgFiles.get(0).getBytes()))
                        .part(new MockPart("challengeImgFiles", "challengeImgFile", challengeImgFiles.get(1).getBytes()))
                        .part(new MockPart("challengeImgFiles", "challengeImgFile", challengeImgFiles.get(2).getBytes()))
                        .file(mockHashtagDto)
                        .header(AUTHORIZATION, token)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andDo(restDocs.document(
                        requestParts(
                                partWithName("requestCreateChallenge").description("챌린지 데이터(JSON)")
                                        .attributes(key("type").value("JSON")),
                                partWithName("challengeImgFiles").description("챌린지 이미지 파일들(FILE)").optional()
                                        .attributes(key("type").value(".jpg")),
                                partWithName("hashtagDto").description("해시태그 데이터(JSON)").optional()
                                        .attributes(key("type").value("JSON"))
                        ),
                        requestPartFields("requestCreateChallenge",
                                fieldWithPath("title").description("제목"),
                                fieldWithPath("content").description("내용"),
                                fieldWithPath("challengeCategory").description("카테고리")
                                        .attributes(key("format").value(
                                                challengeCategoryDescriptions)),
                                fieldWithPath("challengeLocation").description("장소")
                                        .attributes(key("format").value(
                                                challengeLocationDescriptions)),
                                fieldWithPath("challengeDuration").description("기간")
                                        .attributes(key("format").value(
                                                challengeDurationDescriptions))
                        ),
                        requestPartFields("hashtagDto",
                                fieldWithPath("content").description("해시태그 내용")
                                        .attributes(key("format").value("\"\", \" \"은 허용하지 않습니다."))
                        )
                ));
    }

    @Test
    @DisplayName("특정 챌린지 조회 테스트")
    void findChallengeByIdTest() throws Exception {
        initData();
        Long challenge1Id = challenge1.getId();

        mockMvc.perform(get("/challenge/{challengeId}", challenge1Id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseChallenge.title").value(challenge1.getTitle()))
                .andExpect(jsonPath("$.responseChallenge.content").value(challenge1.getContent()))
                .andExpect(jsonPath("$.responseChallenge.challengeCategory").value(
                        challenge1.getChallengeCategory().getDescription()))
                .andExpect(jsonPath("$.responseChallenge.challengeLocation").value(
                        challenge1.getChallengeLocation().getDescription()))
                .andExpect(jsonPath("$.responseChallenge.challengeDuration").value(
                        challenge1.getChallengeDuration().getDescription()))
                .andExpect(jsonPath("$.responseChallenge.created_at").value(challenge1.getFormattedCreatedAt()))
                .andExpect(
                        jsonPath("$.responseChallenge.challengeImgUrls[*]").value(challenge1.getImgUrls()))
                .andExpect(jsonPath("$.responseChallenge.howManyUsersAreInThisChallenge").value(2))
                .andExpect(jsonPath("$.responseChallenge.challengeOwnerUser.userName").value(user.getUserName()))
                .andExpect(jsonPath("$.responseChallenge.challengeOwnerUser.email").value(user.getEmail()))
                .andExpect(jsonPath("$.responseChallenge.challengeOwnerUser.userId").value(user.getId()))
                .andExpect(jsonPath("$.responseUserChallenges[*].challengeStatus",
                        hasItem(ChallengeStatus.TRYING.getDescription())))
                .andExpect(jsonPath("$.responseUserChallenges[*].participatedUser.userName",
                        contains(user.getUserName(), "홍길동1")))
                .andExpect(jsonPath("$.responseUserChallenges[*].participatedUser.email",
                        contains(user.getEmail(), "1@test.com")))
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("challengeId").description("찾고 싶은 챌린지 ID")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("responseChallenge").description("챌린지 정보"),
                                fieldWithPath("responseUserChallenges").description("찾은 챌린지에 참여한 사람들 정보")
                        )
                ));
    }

    @Test
    @DisplayName("모든 챌린지 조회하고 인기순으로 내림차순 정렬 테스트")
    public void searchAllChallengesSortByPopularTest() throws Exception {
        initData();

        mockMvc.perform(get("/challenge")
                        .param("size", "20")
                        .param("page", "0")
                        .param("sort", "popular")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].title", contains(
                        "제목입니다.2", "제목입니다.1", "제목입니다.6", "제목입니다.3", "제목입니다.4",
                        "제목입니다.5", "제목입니다.7", "제목입니다.8", "제목입니다.9", "제목입니다.10")))
                .andExpect(jsonPath("$.content[*].content", contains(
                        "내용입니다.2", "내용입니다.1", "내용입니다.6", "내용입니다.3", "내용입니다.4",
                        "내용입니다.5", "내용입니다.7", "내용입니다.8", "내용입니다.9", "내용입니다.10")))
                .andExpect(jsonPath("$.content[*].challengeCategory",
                        hasItems(ChallengeCategory.ECONOMY.getDescription(), ChallengeCategory.STUDY.getDescription(),
                                ChallengeCategory.WORKOUT.getDescription())))
                .andExpect(jsonPath("$.content[*].challengeLocation",
                        hasItems(ChallengeLocation.OUTDOOR.getDescription(),
                                ChallengeLocation.INDOOR.getDescription())))
                .andExpect(jsonPath("$.content[*].challengeDuration",
                        hasItems(ChallengeDuration.OVER_ONE_HOUR.getDescription(),
                                ChallengeDuration.WITHIN_TEN_MINUTES.getDescription())))
                .andExpect(jsonPath("$.content[*].challengeImgUrls",
                        hasItems(hasItem(startsWith("/images/")))))
                .andExpect(jsonPath("$.content[*].howManyUsersAreInThisChallenge",
                        contains(5, 2, 2, 1, 1, 1, 1, 1, 1, 1)))
                .andExpect(jsonPath("$.content[*].challengeOwnerUser.userName",
                        hasItem(user.getUserName())))
                .andExpect(jsonPath("$.content[*].challengeOwnerUser.email",
                        hasItem(user.getEmail())))
                .andExpect(jsonPath("$.content[*].challengeOwnerUser.userId",
                        hasItem(user.getId().intValue())))
                .andDo(restDocs.document(
                        requestParameters(
                                parameterWithName("size").description("기본값: 10").optional(),
                                parameterWithName("page").description("기본값: 0, 0번부터 시작합니다.").optional(),
                                parameterWithName("sort").description("기본값: popular-내림차순, popular 또는 time으로 정렬합니다.")
                                        .optional()
                        ),
                        relaxedResponseFields(
                                fieldWithPath("totalElements").description("DB에 있는 전체 Challenge 개수"),
                                fieldWithPath("totalPages").description("만들 수 있는 page 개수")
//                                subsectionWithPath("content").description("Challenge 데이터"),
//                                subsectionWithPath("pageable").description("페이징 정보"),
//                                subsectionWithPath("last").description("마지막 페이지인지"),
//                                subsectionWithPath("size").description("페이지당 나타낼수 있는 데이터 개수"),
//                                subsectionWithPath("number").description("현재 페이지 번호"),
//                                subsectionWithPath("first").description("첫번쨰 페이지 인지"),
//                                subsectionWithPath("sort").description("정렬 정보"),
//                                subsectionWithPath("numberOfElements").description("실제 데이터 개수 "),
//                                subsectionWithPath("empty").description("리스트가 비어있는지 여부")
                        )
                ));
    }

    @Test
    @DisplayName("모든 챌린지 조회하고 생성순으로 오름차순 정렬 테스트")
    public void searchAllChallengesSortByTimeTest() throws Exception {
        initData();

        mockMvc.perform(get("/challenge")
                        .param("size", "20")
                        .param("page", "0")
                        .param("sort", "time")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].title", contains(
                        "제목입니다.10", "제목입니다.9", "제목입니다.8", "제목입니다.7", "제목입니다.6", "제목입니다.5",
                        "제목입니다.4", "제목입니다.3", "제목입니다.2", "제목입니다.1")))
                .andDo(restDocs.document(
                        requestParameters(
                                parameterWithName("size").description("기본값: 10").optional(),
                                parameterWithName("page").description("기본값: 0, 0번부터 시작합니다.").optional(),
                                parameterWithName("sort").description("기본값: popular-내림차순, popular 또는 time으로 정렬합니다.")
                                        .optional()
                        ),
                        relaxedResponseFields(
                                fieldWithPath("totalElements").description("DB에 있는 전체 Challenge 개수"),
                                fieldWithPath("totalPages").description("만들 수 있는 page 개수")
                        )
                ));
    }

    @Test
    @DisplayName("해시태그로 검색한 챌린지 조회 테스트 - 시간순")
    void searchChallengesByHashtagSortByTimeTest() throws Exception {
        initData();

        mockMvc.perform(get("/challenge/hashtag")
                        .param("size", "10")
                        .param("page", "0")
                        .param("content", "tag2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].title", contains(
                        "제목입니다.1", "제목입니다.2", "제목입니다.6")))
                .andExpect(jsonPath("$.content[*].content", contains(
                        "내용입니다.1", "내용입니다.2", "내용입니다.6")))
                .andExpect(jsonPath("$.content[*].challengeCategory",
                        hasItems(ECONOMY.getDescription(), STUDY.getDescription(),
                                WORKOUT.getDescription())))
                .andExpect(jsonPath("$.content[*].challengeLocation",
                        hasItems(OUTDOOR.getDescription(),
                                INDOOR.getDescription())))
                .andExpect(jsonPath("$.content[*].challengeDuration",
                        hasItems(OVER_ONE_HOUR.getDescription(),
                                WITHIN_TEN_MINUTES.getDescription())))
                .andExpect(jsonPath("$.content[*].challengeImgUrls",
                        hasItems(hasItem(startsWith("/images/")))))
                .andExpect(jsonPath("$.content[*].howManyUsersAreInThisChallenge",
                        contains(2, 5, 2)))
                .andExpect(jsonPath("$.content[*].challengeOwnerUser.userName",
                        hasItem(user.getUserName())))
                .andExpect(jsonPath("$.content[*].challengeOwnerUser.email",
                        hasItem(user.getEmail())))
                .andExpect(jsonPath("$.content[*].challengeOwnerUser.userId",
                        hasItem(user.getId().intValue())))
                .andDo(restDocs.document(
                        requestParameters(
                                parameterWithName("size").description("기본값: 10").optional(),
                                parameterWithName("page").description("기본값: 0, 0번부터 시작합니다.").optional(),
                                parameterWithName("content").description("챌린지를 검색할 해시태그 내용(1개)"),
                                parameterWithName("sort").description("기본값: time 정렬, popular/time 중 택1")
                                        .optional()
                        ),
                        relaxedResponseFields(
                                fieldWithPath("totalElements").description("DB에 있는 전체 Challenge 개수"),
                                fieldWithPath("totalPages").description("만들 수 있는 page 개수")
                        )
                ));
    }

    @Test
    @DisplayName("해시태그로 검색한 챌린지 조회 테스트 - 인기순")
    void searchChallengesByHashtagSortByPopularTest() throws Exception {
        initData();

        mockMvc.perform(get("/challenge/hashtag")
                        .param("size", "10")
                        .param("page", "0")
                        .param("content", "tag2")
                        .param("sort","popular")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].title", contains(
                        "제목입니다.2", "제목입니다.1", "제목입니다.6")))
                .andExpect(jsonPath("$.content[*].content", contains(
                        "내용입니다.2", "내용입니다.1", "내용입니다.6")))
                .andExpect(jsonPath("$.content[*].challengeCategory",
                        hasItems(ECONOMY.getDescription(), STUDY.getDescription(),
                                WORKOUT.getDescription())))
                .andExpect(jsonPath("$.content[*].challengeLocation",
                        hasItems(OUTDOOR.getDescription(),
                                INDOOR.getDescription())))
                .andExpect(jsonPath("$.content[*].challengeDuration",
                        hasItems(OVER_ONE_HOUR.getDescription(),
                                WITHIN_TEN_MINUTES.getDescription())))
                .andExpect(jsonPath("$.content[*].challengeImgUrls",
                        hasItems(hasItem(startsWith("/images/")))))
                .andExpect(jsonPath("$.content[*].howManyUsersAreInThisChallenge",
                        contains(5, 2, 2)))
                .andExpect(jsonPath("$.content[*].challengeOwnerUser.userName",
                        hasItem(user.getUserName())))
                .andExpect(jsonPath("$.content[*].challengeOwnerUser.email",
                        hasItem(user.getEmail())))
                .andExpect(jsonPath("$.content[*].challengeOwnerUser.userId",
                        hasItem(user.getId().intValue())))
                .andDo(restDocs.document(
                        requestParameters(
                                parameterWithName("size").description("기본값: 10").optional(),
                                parameterWithName("page").description("기본값: 0, 0번부터 시작합니다.").optional(),
                                parameterWithName("content").description("챌린지를 검색할 해시태그 내용(1개)"),
                                parameterWithName("sort").description("기본값: time 정렬, popular/time 중 택1")
                                        .optional()
                        ),
                        relaxedResponseFields(
                                fieldWithPath("totalElements").description("DB에 있는 전체 Challenge 개수"),
                                fieldWithPath("totalPages").description("만들 수 있는 page 개수")
                        )
                ));
    }

    @Test
    @DisplayName("챌린지들을 검색 조건으로 조회하고 인기순으로 오름차순 정렬 테스트")
    public void searchChallengesByConditionSortByPopularTest() throws Exception {
        initData();

        mockMvc.perform(get("/challenge/condition")
                        .characterEncoding(UTF_8)
                        .param("title", "")
                        .param("category", ChallengeCategory.WORKOUT.getDescription())
                        .param("size", "20")
                        .param("page", "0")
                        .param("sort", "popular")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].title",
                        contains("제목입니다.6", "제목입니다.3", "제목입니다.4", "제목입니다.5", "제목입니다.7", "제목입니다.8",
                                "제목입니다.9", "제목입니다.10")))
                .andExpect(jsonPath("$.content[*].content",
                        contains("내용입니다.6", "내용입니다.3", "내용입니다.4", "내용입니다.5", "내용입니다.7", "내용입니다.8",
                                "내용입니다.9", "내용입니다.10")))
                .andExpect(jsonPath("$.content[*].challengeCategory",
                        hasItem(ChallengeCategory.WORKOUT.getDescription())))
                .andExpect(jsonPath("$.content[*].challengeLocation",
                        hasItem(ChallengeLocation.INDOOR.getDescription())))
                .andExpect(jsonPath("$.content[*].challengeDuration",
                        hasItem(ChallengeDuration.WITHIN_TEN_MINUTES.getDescription())))
                .andExpect(jsonPath("$.content[*].challengeImgUrls",
                        hasItem(hasItem(startsWith("/images/")))))
                .andExpect(jsonPath("$.content[*].howManyUsersAreInThisChallenge",
                        contains(2, 1, 1, 1, 1, 1, 1, 1)))
                .andExpect(jsonPath("$.content[*].challengeOwnerUser.userName",
                        hasItem(user.getUserName())))
                .andExpect(jsonPath("$.content[*].challengeOwnerUser.email",
                        hasItem(user.getEmail())))
                .andExpect(jsonPath("$.content[*].challengeOwnerUser.userId",
                        hasItem(user.getId().intValue())))
                .andDo(restDocs.document(
                        requestParameters(
                                parameterWithName("title").description("찾고 싶은 Challenge 제목").optional(),
                                parameterWithName("category").description("찾고 싶은 Challenge 카테고리").optional(),
                                parameterWithName("size").description("기본값: 10").optional(),
                                parameterWithName("page").description("기본값: 0, 0번부터 시작합니다.").optional(),
                                parameterWithName("sort").description("기본값: popular-내림차순, popular 또는 time으로 정렬합니다.")
                                        .optional()
                        ),
                        relaxedResponseFields(
                                fieldWithPath("totalElements").description("DB에 있는 전체 Challenge 개수"),
                                fieldWithPath("totalPages").description("만들 수 있는 page 개수")
                        )
                ));
    }

    @Test
    @DisplayName("챌린지들을 검색 조건으로 조회하고 생성순으로 내림차순 정렬 테스트")
    public void searchChallengesByConditionSortByTimeTest() throws Exception {
        initData();

        mockMvc.perform(get("/challenge/condition")
                        .param("title", "")
                        .param("category", ChallengeCategory.WORKOUT.getDescription())
                        .param("size", "20")
                        .param("page", "0")
                        .param("sort", "time")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].title",
                        contains("제목입니다.10", "제목입니다.9", "제목입니다.8", "제목입니다.7", "제목입니다.6", "제목입니다.5",
                                "제목입니다.4", "제목입니다.3")))
                .andDo(restDocs.document(
                        requestParameters(
                                parameterWithName("title").description("찾고 싶은 Challenge 제목").optional(),
                                parameterWithName("category").description("찾고 싶은 Challenge 카테고리").optional(),
                                parameterWithName("size").description("기본값: 10").optional(),
                                parameterWithName("page").description("기본값: 0, 0번부터 시작합니다.").optional(),
                                parameterWithName("sort").description("기본값: popular-내림차순, popular 또는 time으로 정렬합니다.")
                                        .optional()
                        ),
                        relaxedResponseFields(
                                fieldWithPath("totalElements").description("DB에 있는 전체 Challenge 개수"),
                                fieldWithPath("totalPages").description("만들 수 있는 page 개수")
                        )
                ));
    }

    @Test
    @DisplayName("챌린지들을 질문으로 조회하는 테스트")
    public void searchChallengesByQuestionTest() throws Exception {
        initData();

        mockMvc.perform(get("/challenge/question")
                        .param("challengeLocationIndex", "1")
                        .param("challengeDurationIndex", "0")
                        .param("challengeCategoryIndex", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        requestParameters(
                                parameterWithName("challengeCategoryIndex").description("챌린지 카테고리 번호"),
                                parameterWithName("challengeDurationIndex").description("챌린지 기간 번호"),
                                parameterWithName("challengeLocationIndex").description("챌린지 장소 번호")
                        )
                ));
    }

    @Test
    @DisplayName("챌린지들을 질문으로 찾을 수 없는 경우 랜덤 챌린지를 반환하는 테스트")
    public void searchChallengesByQuestionWithNotMatchTest() throws Exception {
        initData();

        mockMvc.perform(get("/challenge/question")
                        .param("challengeLocationIndex", "1")
                        .param("challengeDurationIndex", "3")
                        .param("challengeCategoryIndex", "4")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        requestParameters(
                                parameterWithName("challengeCategoryIndex").description("챌린지 카테고리 번호"),
                                parameterWithName("challengeDurationIndex").description("챌린지 기간 번호"),
                                parameterWithName("challengeLocationIndex").description("챌린지 장소 번호")
                        )
                ));
    }

    @Test
    @DisplayName("챌린지들을 해시태그들로 조회하는 테스트")
    public void searchChallengesByHashtagsTest() throws Exception {
        initData();

        mockMvc.perform(get("/challenge/hashtags")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        relaxedResponseFields(
                                fieldWithPath("[].hashtagId").description("해시태그 ID"),
                                fieldWithPath("[].hashtagContent").description("해시태그 내용"),
                                fieldWithPath("[].hashtagTagCount").description("해시태그 개수"),
                                fieldWithPath("[].recommendedChallenges").description("해당 해시태그를 가지고 있는 챌린지들")
                        )
                ));
    }

    @Test
    @DisplayName("챌린지를 랜덤으로 조회하는 테스트")
    public void searchChallengeByRandomTest() throws Exception {
        initData();

        mockMvc.perform(get("/challenge/random")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(restDocs.document());
    }

    @Test
    @DisplayName("챌린지 수정 테스트")
    void updateChallenge() throws Exception {
        initChallengeData();

        RequestUpdateChallenge requestUpdateChallenge = RequestUpdateChallenge.builder()
                .title("수정된 제목")
                .content("수정된 내용")
                .challengeCategory(ChallengeCategory.WORKOUT.getDescription())
                .build();
        List<MultipartFile> updateChallengeImgFiles = updateChallengeImgFiles();

        String json = objectMapper.writeValueAsString(requestUpdateChallenge);
        MockMultipartFile mockRequestUpdateChallenge = new MockMultipartFile("requestUpdateChallenge",
                "requestUpdateChallenge",
                "application/json", json.getBytes(UTF_8));

        HashtagDto hashtagDto = HashtagDto.builder()
                .content(List.of("editTag1", "editTag2"))
                .build();
        String hashtagDtoJson = objectMapper.writeValueAsString(hashtagDto);
        MockMultipartFile mockHashtagDto = new MockMultipartFile("hashtagDto",
                "hashtagDto",
                "application/json", hashtagDtoJson.getBytes(UTF_8));

        String challengeCategoryDescriptions = String.join(", ", ChallengeCategory.getDescriptions());
        Long challenge1Id = challenge1.getId();
        mockMvc.perform(multipart("/challenge/{challengeId}", challenge1Id)
                        .file(mockRequestUpdateChallenge)
                        .part(new MockPart("updateChallengeImgFiles", "updateChallengeImgFiles",
                                updateChallengeImgFiles.get(0).getBytes()))
                        .part(new MockPart("updateChallengeImgFiles", "updateChallengeImgFiles",
                                updateChallengeImgFiles.get(1).getBytes()))
                        .file(mockHashtagDto)
                        .header(AUTHORIZATION, token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(requestUpdateChallenge.getTitle()))
                .andExpect(jsonPath("$.content").value(requestUpdateChallenge.getContent()))
                .andExpect(jsonPath("$.challengeCategory").value(requestUpdateChallenge.getChallengeCategory()))
                .andExpect(jsonPath("$.challengeLocation").value(challenge1.getChallengeLocation().getDescription()))
                .andExpect(jsonPath("$.challengeDuration").value(challenge1.getChallengeDuration().getDescription()))
                .andExpect(jsonPath("$.created_at").value(challenge1.getFormattedCreatedAt()))
                .andExpect(jsonPath("$.updated_at").isNotEmpty())
                .andExpect(jsonPath("$.challengeImgUrls[*]", hasItem(startsWith("/images/"))))
                .andExpect(jsonPath("$.challengeHashtags[*]", contains("editTag1", "editTag2")))
                .andExpect(jsonPath("$.challengeImgUrls", hasSize(2)))
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("challengeId").description("수정하고 싶은 챌린지 ID")
                        ),
                        requestParts(
                                partWithName("requestUpdateChallenge").description("챌린지 데이터(JSON)")
                                        .attributes(key("type").value("JSON")),
                                partWithName("updateChallengeImgFiles").description("챌린지 이미지 파일들(FILE)").optional()
                                        .attributes(key("type").value(".jpg")),
                                partWithName("hashtagDto").description("해시태그 데이터(JSON)").optional()
                                        .attributes(key("type").value("JSON"))
                        ),
                        requestPartFields("requestUpdateChallenge",
                                fieldWithPath("title").description("제목"),
                                fieldWithPath("content").description("내용"),
                                fieldWithPath("challengeCategory").description("카테고리")
                                        .attributes(key("format").value(challengeCategoryDescriptions))
                        ),
                        requestPartFields("hashtagDto",
                                fieldWithPath("content").description("해시태그 내용")
                                        .attributes(key("format").value("\"\", \" \"은 허용하지 않습니다."))
                        ),
                        responseFields(
                                fieldWithPath("id").description("챌린지 ID"),
                                fieldWithPath("title").description("제목"),
                                fieldWithPath("content").description("내용"),
                                fieldWithPath("challengeCategory").description("카테고리"),
                                fieldWithPath("challengeLocation").description("장소"),
                                fieldWithPath("challengeDuration").description("기간"),
                                fieldWithPath("created_at").description("생성일"),
                                fieldWithPath("updated_at").description("수정일"),
                                fieldWithPath("challengeImgUrls").description("사진 url들"),
                                fieldWithPath("challengeHashtags").description("해시태그들")
                        )
                ));
    }

    @Test
    @DisplayName("챌린지 삭제 테스트")
    void deleteChallenge() throws Exception {
        initChallengeData();

        mockMvc.perform(delete("/challenge/{challengeId}", challenge1.getId())
                        .header(AUTHORIZATION, token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andDo(restDocs.document(
                        pathParameters(parameterWithName("challengeId").description("챌린지 ID"))));
    }
}