package com.example.dailychallenge.entity.challenge;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "challenge")
@Getter
@NoArgsConstructor
public class Challenge {

    @Id
    @Column(name = "challenge_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String content;
    @Enumerated(value = EnumType.STRING)
    private ChallengeCategory challengeCategory;
    @Enumerated(value = EnumType.STRING)
    private ChallengeLocation challengeLocation;
    @Enumerated(value = EnumType.STRING)
    private ChallengeDuration challengeDuration;

    // 이미지 업로드 기능 추가하기
    @OneToOne(mappedBy = "challenge", cascade = CascadeType.ALL)
    private ChallengeImg challengeImg;

    @OneToMany(mappedBy = "challenge")
    private List<UserChallenge> userChallenges = new ArrayList<>();

    @Builder
    public Challenge(String title, String content, ChallengeCategory challengeCategory,
                     ChallengeLocation challengeLocation,
                     ChallengeDuration challengeDuration) {
        this.title = title;
        this.content = content;
        this.challengeCategory = challengeCategory;
        this.challengeLocation = challengeLocation;
        this.challengeDuration = challengeDuration;
    }

    public void setChallengeImg(ChallengeImg challengeImg) {
        this.challengeImg = challengeImg;
    }
}