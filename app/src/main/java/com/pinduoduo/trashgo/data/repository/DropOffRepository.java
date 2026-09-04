package com.pinduoduo.trashgo.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.pinduoduo.trashgo.data.FirestoreContract;
import com.pinduoduo.trashgo.data.model.DropOffPoint;
import com.pinduoduo.trashgo.data.model.WasteCategory;
import com.pinduoduo.trashgo.util.GeoUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DropOffRepository {
    public interface PointsCallback {
        void onLoaded(@NonNull List<DropOffPoint> points);
        void onError(@NonNull Exception e);
    }

    public interface PointCallback {
        void onLoaded(@Nullable DropOffPoint point);
    }

    private final FirebaseFirestore db;

    public DropOffRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void fetchAll(@NonNull PointsCallback callback) {
        db.collection(FirestoreContract.DropOffPoints.COLLECTION)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<DropOffPoint> points = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        points.add(doc.toObject(DropOffPoint.class));
                    }
                    callback.onLoaded(points);
                })
                .addOnFailureListener(callback::onError);
    }

    public void fetchById(@NonNull String id, @NonNull PointCallback callback) {
        db.collection(FirestoreContract.DropOffPoints.COLLECTION)
                .document(id)
                .get()
                .addOnSuccessListener(doc ->
                        callback.onLoaded(doc.exists() ? doc.toObject(DropOffPoint.class) : null))
                .addOnFailureListener(e -> callback.onLoaded(null));
    }

    public static boolean accepts(@NonNull DropOffPoint p, @Nullable WasteCategory category) {
        return category != null
                && p.getAcceptedCategories() != null
                && p.getAcceptedCategories().contains(category);
    }

    @NonNull
    public static String acceptedLabel(@NonNull DropOffPoint p) {
        if (p.getAcceptedCategories() == null || p.getAcceptedCategories().isEmpty()) {
            return "No materials listed";
        }
        StringBuilder sb = new StringBuilder();
        for (WasteCategory c : p.getAcceptedCategories()) {
            if (sb.length() > 0) sb.append(", ");
            String n = c.name();
            sb.append(n.charAt(0)).append(n.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    @NonNull
    public static List<DropOffPoint> filterByCategory(@NonNull List<DropOffPoint> points,
                                                      @Nullable WasteCategory category) {
        if (category == null) return new ArrayList<>(points);
        List<DropOffPoint> out = new ArrayList<>();
        for (DropOffPoint p : points) {
            if (accepts(p, category)) out.add(p);
        }
        return out;
    }

    @NonNull
    public static List<DropOffPoint> sortByDistance(@NonNull List<DropOffPoint> points,
                                                    @Nullable Double lat, @Nullable Double lng) {
        List<DropOffPoint> out = new ArrayList<>(points);
        if (lat == null || lng == null) return out;
        Collections.sort(out, new Comparator<DropOffPoint>() {
            @Override public int compare(DropOffPoint a, DropOffPoint b) {
                return Double.compare(
                        GeoUtils.distanceMetres(lat, lng, a.getLatitude(), a.getLongitude()),
                        GeoUtils.distanceMetres(lat, lng, b.getLatitude(), b.getLongitude()));
            }
        });
        return out;
    }
}
