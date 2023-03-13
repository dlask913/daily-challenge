package com.example.dailychallenge.exception.badge;

import com.example.dailychallenge.exception.DailyChallengeException;

public class BadgeNotFound extends DailyChallengeException {

    private static final String MESSAGE = "존재하지 않는 뱃지입니다.";

    public BadgeNotFound() {
        super(MESSAGE);
    }

    public BadgeNotFound(Throwable cause) {
        super(MESSAGE, cause);
    }

    @Override
    public String getMessage() {
        return MESSAGE;
    }

    @Override
    public int getStatusCode() {
        return 400;
    }
}
