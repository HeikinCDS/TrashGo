package com.pinduoduo.trashgo.data.verify;

public enum VerificationResult {
    OK,
    BAD_PAYLOAD,
    WRONG_POINT,
    CATEGORY_NOT_ACCEPTED,
    NO_LOCATION,
    MOCK_LOCATION,
    TOO_FAR,
    COOLDOWN;

    public boolean isSuccess() {
        return this == OK;
    }
}