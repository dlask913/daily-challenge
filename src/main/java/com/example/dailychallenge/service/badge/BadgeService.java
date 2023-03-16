package com.example.dailychallenge.service.badge;

import com.example.dailychallenge.dto.BadgeDto;
import com.example.dailychallenge.entity.badge.Badge;
import com.example.dailychallenge.repository.badge.BadgeRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BadgeService {
    @Value("${defaultBadgeImgLocation}")
    private String badgeImgLocation;
    @Value("${badgeImgFileExtension}")
    private String badgeImgFileExtension;

    private final BadgeRepository badgeRepository;

    public List<Badge> createBadges(List<BadgeDto> badgeDtos) {
        List<Badge> badges = badgeDtos.stream()
                .map(badgeDto -> Badge.builder()
                        .name(badgeDto.getBadgeName())
                        .imgUrl(badgeImgLocation + badgeDto.getBadgeImgFileName() + badgeImgFileExtension)
                        .build())
                .collect(Collectors.toUnmodifiableList());

        return badgeRepository.saveAll(badges);
    }

    public List<Badge> findAll() {
        return badgeRepository.findAll();
    }
}
