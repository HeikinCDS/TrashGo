package com.pinduoduo.trashgo.data.seed;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.pinduoduo.trashgo.data.FirestoreContract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DropOffPointSeeder {
    private static final String TAG = "DropOffPointSeeder";

    private static final double CAMPUS_LATITUDE = 4.3366214;
    private static final double CAMPUS_LONGITUDE = 101.1421110;

    private DropOffPointSeeder() {}

    public static void seedIfEmpty() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection(FirestoreContract.DropOffPoints.COLLECTION)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        Log.d(TAG, "Drop-off collection already contains data; seed skipped.");
                        return;
                    }

                    WriteBatch batch = firestore.batch();
                    for (Map<String, Object> point : simulatedPoints()) {
                        String id = (String) point.get(FirestoreContract.DropOffPoints.ID);
                        batch.set(
                                firestore.collection(FirestoreContract.DropOffPoints.COLLECTION)
                                        .document(id),
                                point
                        );
                    }
                    batch.commit()
                            .addOnSuccessListener(unused ->
                                    Log.i(TAG, "Eight simulated drop-off points created."))
                            .addOnFailureListener(error ->
                                    Log.e(TAG, "Unable to seed drop-off points.", error));
                })
                .addOnFailureListener(error ->
                        Log.e(TAG, "Unable to check drop-off collection.", error));
    }

    private static List<Map<String, Object>> simulatedPoints() {
        List<Map<String, Object>> points = new ArrayList<>();
        points.add(point(
                "kpr_heritage_hall",
                "Heritage Hall Recycling Hub (Simulated)",
                0.00000,
                0.00000,
                "Mon–Fri, 8:00 AM–6:00 PM",
                "PLASTIC", "PAPER", "GLASS", "METAL"
        ));
        points.add(point(
                "kpr_library_entrance",
                "Library Entrance Recycling Station (Simulated)",
                0.00058,
                -0.00054,
                "Daily, 8:00 AM–10:00 PM",
                "PLASTIC", "PAPER", "METAL"
        ));
        points.add(point(
                "kpr_student_pavilion",
                "Student Pavilion Recycling Point (Simulated)",
                0.00032,
                0.00018,
                "Mon–Sat, 8:00 AM–8:00 PM",
                "PLASTIC", "PAPER", "GLASS", "METAL", "GENERAL"
        ));
        points.add(point(
                "kpr_cafeteria",
                "Campus Cafeteria Waste Station (Simulated)",
                -0.00041,
                0.00036,
                "Daily, 7:00 AM–9:00 PM",
                "PLASTIC", "METAL", "ORGANIC", "GENERAL"
        ));
        points.add(point(
                "kpr_fict_block",
                "FICT Block E-Waste Counter (Simulated)",
                0.00074,
                0.00043,
                "Mon–Fri, 9:00 AM–5:00 PM",
                "EWASTE"
        ));
        points.add(point(
                "kpr_sports_complex",
                "Sports Complex Recycling Bin (Simulated)",
                -0.00077,
                -0.00031,
                "Daily, 7:00 AM–10:00 PM",
                "PLASTIC", "PAPER", "METAL", "GENERAL"
        ));
        points.add(point(
                "kpr_south_gate",
                "South Gate Community Drop-off (Simulated)",
                -0.00104,
                0.00067,
                "Daily, 7:00 AM–7:00 PM",
                "PLASTIC", "PAPER", "GLASS", "METAL", "EWASTE"
        ));
        points.add(point(
                "kpr_main_bus_stop",
                "Main Bus Stop Recycling Bin (Simulated)",
                0.00013,
                -0.00088,
                "Daily, 6:30 AM–11:00 PM",
                "PLASTIC", "PAPER", "METAL", "GENERAL"
        ));
        return points;
    }

    private static Map<String, Object> point(
            String id,
            String name,
            double latitudeOffset,
            double longitudeOffset,
            String openingHours,
            String... categories
    ) {
        Map<String, Object> point = new HashMap<>();
        point.put(FirestoreContract.DropOffPoints.ID, id);
        point.put(FirestoreContract.DropOffPoints.NAME, name);
        point.put(FirestoreContract.DropOffPoints.LATITUDE,
                CAMPUS_LATITUDE + latitudeOffset);
        point.put(FirestoreContract.DropOffPoints.LONGITUDE,
                CAMPUS_LONGITUDE + longitudeOffset);
        point.put(FirestoreContract.DropOffPoints.ACCEPTED_CATEGORIES,
                Arrays.asList(categories));
        point.put(FirestoreContract.DropOffPoints.OPENING_HOURS, openingHours);
        point.put(FirestoreContract.DropOffPoints.QR_PAYLOAD, "TRASHGO:DROP_OFF:" + id);
        return point;
    }
}
