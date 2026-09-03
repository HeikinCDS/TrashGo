package com.pinduoduo.trashgo.data;

public final class FirestoreContract {
    private FirestoreContract() {}

    public static final class Users {
        public static final String COLLECTION = "users";
        public static final String DISPLAY_NAME = "displayName";
        public static final String EMAIL = "email";
        public static final String TOTAL_POINTS = "totalPoints";
        public static final String ITEMS_RECYCLED = "itemsRecycled";
        public static final String CURRENT_STREAK = "currentStreak";
        public static final String LAST_SCAN_DATE = "lastScanDate";
        public static final String LAST_CLAIMS = "lastClaims";

        private Users() {}
    }

    public static final class Scans {
        public static final String COLLECTION = "scans";
        public static final String CATEGORY = "category";
        public static final String DROP_OFF_ID = "dropOffId";
        public static final String POINTS_AWARDED = "pointsAwarded";
        public static final String TIMESTAMP = "timestamp";

        private Scans() {}
    }

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
