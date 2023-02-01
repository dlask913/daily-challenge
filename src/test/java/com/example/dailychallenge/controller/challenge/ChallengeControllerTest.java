package com.example.dailychallenge.controller.challenge;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.dailychallenge.dto.UserDto;
import com.example.dailychallenge.entity.challenge.Challenge;
import com.example.dailychallenge.entity.challenge.ChallengeCategory;
import com.example.dailychallenge.entity.challenge.ChallengeDuration;
import com.example.dailychallenge.entity.challenge.ChallengeImg;
import com.example.dailychallenge.entity.challenge.ChallengeLocation;
import com.example.dailychallenge.entity.challenge.ChallengeStatus;
import com.example.dailychallenge.entity.users.User;
import com.example.dailychallenge.repository.ChallengeImgRepository;
import com.example.dailychallenge.repository.ChallengeRepository;
import com.example.dailychallenge.repository.UserChallengeRepository;
import com.example.dailychallenge.repository.UserRepository;
import com.example.dailychallenge.service.challenge.UserChallengeService;
import com.example.dailychallenge.service.users.UserService;
import com.example.dailychallenge.utils.JwtTokenUtil;
import com.example.dailychallenge.vo.RequestCreateChallenge;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class ChallengeControllerTest {
    private final static String TOKEN_PREFIX = "Bearer ";
    private final static String AUTHORIZATION = "Authorization";
    private final static String EMAIL = "test1234@test.com";
    private final static String PASSWORD = "1234";

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private UserService userService;
    @Autowired
    private ChallengeRepository challengeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserChallengeRepository userChallengeRepository;
    @Autowired
    private UserChallengeService userChallengeService;
    @Autowired
    private ChallengeImgRepository challengeImgRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @BeforeEach
    void beforeEach() {
        userChallengeRepository.deleteAll();
        userRepository.deleteAll();
        challengeRepository.deleteAll();
        challengeImgRepository.deleteAll();
    }


    public UserDto createUser() {
        UserDto userDto = new UserDto();
        userDto.setEmail(EMAIL);
        userDto.setUserName("홍길동");
        userDto.setInfo("testInfo");
        userDto.setPassword(PASSWORD);
        return userDto;
    }

    private static MockMultipartFile createMultipartFiles() {
        String path = "challengeImgFile";
        String imageName = "challengeImage.jpg";
        return new MockMultipartFile(path, imageName,
                "image/jpg", new byte[]{1, 2, 3, 4});
    }


    @Test
    @DisplayName("챌린지 생성 테스트")
    public void createChallengeTest() throws Exception {
        User savedUser = userService.saveUser(createUser(), passwordEncoder);
        RequestCreateChallenge requestCreatChallenge = RequestCreateChallenge.builder()
                .title("제목입니다.")
                .content("내용입니다.")
                .challengeCategory("공부")
                .challengeLocation("실내")
                .challengeDuration("10분 이내")
                .build();
        MockMultipartFile challengeImgFile = createMultipartFiles();

        String json = objectMapper.writeValueAsString(requestCreatChallenge);
        MockMultipartFile requestCreateChallenge = new MockMultipartFile("requestCreateChallenge",
                "requestCreateChallenge",
                "application/json", json.getBytes(UTF_8));

        MockPart tag1 = new MockPart("\"hashtagDto\"", "tag1".getBytes(UTF_8));
        MockPart tag2 = new MockPart("\"hashtagDto\"", "tag2".getBytes(UTF_8));

        String token = generateToken();
        mockMvc.perform(multipart("/challenge/new")
                        .file(requestCreateChallenge)
                        .file(challengeImgFile)
                        .part(tag1)
                        .part(tag2)
                        .header(AUTHORIZATION, token)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(requestCreatChallenge.getTitle()))
                .andExpect(jsonPath("$.content").value(requestCreatChallenge.getContent()))
                .andExpect(jsonPath("$.challengeCategory").value(requestCreatChallenge.getChallengeCategory()))
                .andExpect(jsonPath("$.challengeLocation").value(requestCreatChallenge.getChallengeLocation()))
                .andExpect(jsonPath("$.challengeDuration").value(requestCreatChallenge.getChallengeDuration()))
                .andExpect(jsonPath("$.challengeStatus").value(ChallengeStatus.TRYING.getDescription()))
                .andExpect(jsonPath("$.challengeImgUrl").isNotEmpty())
                .andExpect(jsonPath("$.challengeOwnerUser.userName").value(savedUser.getUserName()))
                .andExpect(jsonPath("$.challengeOwnerUser.email").value(savedUser.getEmail()))
                .andExpect(jsonPath("$.challengeOwnerUser.userId").value(savedUser.getId()))
                .andDo(print());
    }

    @Test
    @DisplayName("존재하지 않는 카테고리로 챌린지 생성 테스트")
    public void createChallengeByCategoryNotFoundTest() throws Exception {
        userService.saveUser(createUser(), passwordEncoder);
        RequestCreateChallenge requestCreatChallenge = RequestCreateChallenge.builder()
                .title("제목입니다.")
                .content("내용입니다.")
                .challengeCategory("error")
                .challengeLocation("실내")
                .challengeDuration("10분 이내")
                .build();
        String json = objectMapper.writeValueAsString(requestCreatChallenge);
        MockMultipartFile requestCreateChallenge = new MockMultipartFile("requestCreateChallenge",
                "requestCreateChallenge",
                "application/json", json.getBytes(UTF_8));

        String token = generateToken();
        mockMvc.perform(multipart("/challenge/new")
                        .file(requestCreateChallenge)
                        .header(AUTHORIZATION, token)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("존재하지 않는 챌린지 카테고리입니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("모든 챌린지 인기순으로 조회 테스트")
    public void searchAllChallengesByPopularTest() throws Exception {
        User savedUser = userService.saveUser(createUser(), passwordEncoder);
        Challenge challenge1 = Challenge.builder()
                .title("제목입니다.1")
                .content("내용입니다.1")
                .challengeCategory(ChallengeCategory.STUDY)
                .challengeLocation(ChallengeLocation.INDOOR)
                .challengeDuration(ChallengeDuration.WITHIN_TEN_MINUTES)
                .build();
        ChallengeImg challengeImg = new ChallengeImg();
        challengeImg.updateUserImg("oriImgName", "imgName", "imgUrl");
        challenge1.setChallengeImg(challengeImg);
        challenge1.setUser(savedUser);
        Challenge challenge2 = Challenge.builder()
                .title("제목입니다.2")
                .content("내용입니다.2")
                .challengeCategory(ChallengeCategory.ECONOMY)
                .challengeLocation(ChallengeLocation.OUTDOOR)
                .challengeDuration(ChallengeDuration.OVER_ONE_HOUR)
                .build();
        challenge2.setUser(savedUser);
        challengeRepository.save(challenge1);
        challengeImgRepository.save(challengeImg);
        challengeRepository.save(challenge2);
        userChallengeService.saveUserChallenge(challenge1, savedUser);
        userChallengeService.saveUserChallenge(challenge2, savedUser);
        for (int i = 1; i <= 8; i++) {
            User user =  User.builder()
                    .userName("홍길동" + i)
                    .email(i + "@test.com")
                    .password("1234")
                    .build();
            userRepository.save(user);
            if (i == 1) {
                userChallengeService.saveUserChallenge(challenge1, user);
            }
            if (2 <= i && i <= 5) {
                userChallengeService.saveUserChallenge(challenge2, user);
            }
        }

        String token = generateToken();
        mockMvc.perform(get("/challenge")
                        .header(AUTHORIZATION, token)
                        .param("size", "20")
                        .param("page", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value(challenge2.getTitle()))
                .andExpect(jsonPath("$.content[0].content").value(challenge2.getContent()))
                .andExpect(jsonPath("$.content[0].challengeCategory").value(challenge2.getChallengeCategory().getDescription()))
                .andExpect(jsonPath("$.content[0].challengeLocation").value(challenge2.getChallengeLocation().getDescription()))
                .andExpect(jsonPath("$.content[0].challengeDuration").value(challenge2.getChallengeDuration().getDescription()))
                .andExpect(jsonPath("$.content[0].challengeImgUrl").isEmpty())
                .andExpect(jsonPath("$.content[0].howManyUsersAreInThisChallenge").value(5))
                .andExpect(jsonPath("$.content[0].challengeOwnerUser.userName").value(savedUser.getUserName()))
                .andExpect(jsonPath("$.content[0].challengeOwnerUser.email").value(savedUser.getEmail()))
                .andExpect(jsonPath("$.content[0].challengeOwnerUser.userId").value(savedUser.getId()))
                .andExpect(jsonPath("$.content[1].title").value(challenge1.getTitle()))
                .andExpect(jsonPath("$.content[1].content").value(challenge1.getContent()))
                .andExpect(jsonPath("$.content[1].challengeCategory").value(challenge1.getChallengeCategory().getDescription()))
                .andExpect(jsonPath("$.content[1].challengeLocation").value(challenge1.getChallengeLocation().getDescription()))
                .andExpect(jsonPath("$.content[1].challengeDuration").value(challenge1.getChallengeDuration().getDescription()))
                .andExpect(jsonPath("$.content[1].challengeImgUrl").value(challengeImg.getImgUrl()))
                .andExpect(jsonPath("$.content[1].howManyUsersAreInThisChallenge").value(2))
                .andExpect(jsonPath("$.content[1].challengeOwnerUser.userName").value(savedUser.getUserName()))
                .andExpect(jsonPath("$.content[1].challengeOwnerUser.email").value(savedUser.getEmail()))
                .andExpect(jsonPath("$.content[1].challengeOwnerUser.userId").value(savedUser.getId()))
                .andDo(print());
    }

    static Stream<Arguments> generateData() {
        return Stream.of(
                Arguments.of("1", null),
                Arguments.of(null, "경제")
        );
    }

    @ParameterizedTest
    @MethodSource("generateData")
    @DisplayName("챌린지들을 검색 조건으로 찾고 인기순으로 정렬하는 테스트")
    public void searchChallengesByConditionSortByPopularTest(String title, String category) throws Exception {
        User savedUser = userService.saveUser(createUser(), passwordEncoder);
        for (int i = 1; i <= 10; i++) {
            ChallengeCategory challengeCategory = ChallengeCategory.STUDY;
            if (i >= 6) {
                challengeCategory = ChallengeCategory.ECONOMY;
            }
            Challenge challenge = Challenge.builder()
                    .title("제목입니다." + i)
                    .content("내용입니다." + i)
                    .challengeCategory(challengeCategory)
                    .challengeLocation(ChallengeLocation.INDOOR)
                    .challengeDuration(ChallengeDuration.WITHIN_TEN_MINUTES)
                    .build();
            challenge.setUser(savedUser);
            challengeRepository.save(challenge);
            userChallengeService.saveUserChallenge(challenge, savedUser);
        }

        String token = generateToken();
        mockMvc.perform(get("/challenge/condition")
                        .header(AUTHORIZATION, token)
                        .param("title", title)
                        .param("category", category)
                        .param("size", "20")
                        .param("page", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..['title']").exists())
                .andExpect(jsonPath("$..['challengeCategory']").exists())
                .andExpect(jsonPath("$..['challengeImgUrl']").exists())
                .andExpect(jsonPath("$..['challengeOwnerUser']").exists())
                .andDo(print());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "10, 2"
    })
    @DisplayName("모든 챌린지 인기순으로 조회 페이징 테스트")
    public void searchAllChallengesByPopularPagingTest(int totalElements, int totalPages) throws Exception {
        User savedUser = userService.saveUser(createUser(), passwordEncoder);
        for (int i = 0; i < totalElements; i++) {
            Challenge challenge = Challenge.builder()
                    .title("제목입니다." + i)
                    .content("내용입니다." + i)
                    .challengeCategory(ChallengeCategory.ECONOMY)
                    .challengeLocation(ChallengeLocation.OUTDOOR)
                    .challengeDuration(ChallengeDuration.OVER_ONE_HOUR)
                    .build();
            challenge.setUser(savedUser);
            challengeRepository.save(challenge);
            userChallengeService.saveUserChallenge(challenge, savedUser);
        }

        String token = generateToken();
        mockMvc.perform(get("/challenge")
                        .header(AUTHORIZATION, token)
                        .param("size", String.valueOf(totalPages))
                        .param("page", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(totalElements))
                .andExpect(jsonPath("$.totalPages").value(totalElements / totalPages))
                .andDo(print());
    }

    private String generateToken() {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(EMAIL, PASSWORD));
        if (auth.isAuthenticated()) {
            UserDetails userDetails = userService.loadUserByUsername(EMAIL);
            return TOKEN_PREFIX + jwtTokenUtil.generateToken(userDetails);
        }

        throw new IllegalArgumentException("token 생성 오류");
    }
}