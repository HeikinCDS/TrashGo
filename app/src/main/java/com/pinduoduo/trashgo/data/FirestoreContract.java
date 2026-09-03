package com.pinduoduo.trashgo.data;

public final class FirestoreContract {
    private FirestoreContract() {}

    public static final class DropOffPoints {
        public static final String COLLECTION = "dropOffPoints";
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String ACCEPTED_CATEGORIES = "acceptedCategories";
        public static final String OPENING_HOURS = "openingHours";
        public static final String QR_PAYLOAD = "qrPayload";

        private DropOffPoints() {}
    }
}
