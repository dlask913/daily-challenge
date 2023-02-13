package com.example.dailychallenge.util.fixture;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class ChallengeImgFixture {

    @Value("${userImgLocation}")
    private static String challengeImgLocation;

    public static List<MultipartFile> createChallengeImgFiles() {
        List<MultipartFile> result = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            String path = challengeImgLocation +"/";
            String imageName = "challengeImage" + i + ".jpg";
            result.add(new MockMultipartFile(path, imageName, "image/jpg", new byte[]{1, 2, 3, 4}));
        }
        return result;
    }

    public static List<MultipartFile> updateChallengeImgFiles() {
        List<MultipartFile> updateChallengeImgFiles = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            String path = challengeImgLocation +"/";
            String imageName = "updatedChallengeImage" + i + ".jpg";
            updateChallengeImgFiles.add(new MockMultipartFile(path, imageName, "image/jpg", new byte[]{1, 2, 3, 4}));
        }
        return updateChallengeImgFiles;
    }
}