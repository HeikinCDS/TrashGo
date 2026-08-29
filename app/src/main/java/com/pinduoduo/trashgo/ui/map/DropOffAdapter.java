package com.pinduoduo.trashgo.ui.map;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.pinduoduo.trashgo.R;
import com.pinduoduo.trashgo.data.model.DropOffPoint;
import com.pinduoduo.trashgo.data.repository.DropOffRepository;
import com.pinduoduo.trashgo.util.GeoUtils;

import java.util.ArrayList;
import java.util.List;

public class DropOffAdapter extends RecyclerView.Adapter<DropOffAdapter.ViewHolder> {

    public interface OnPointClickListener {
        void onPointClicked(@NonNull DropOffPoint point);
    }

    private final List<DropOffPoint> points = new ArrayList<>();
    private final OnPointClickListener listener;

    @Nullable private Double userLat;
    @Nullable private Double userLng;

    public DropOffAdapter(@NonNull OnPointClickListener listener) {
        this.listener = listener;
    }

    public void submit(@NonNull List<DropOffPoint> newPoints) {
        points.clear();
        points.addAll(newPoints);
        notifyDataSetChanged();
    }

    public void setUserLocation(@Nullable Double lat, @Nullable Double lng) {
        this.userLat = lat;
        this.userLng = lng;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dropoff, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DropOffPoint p = points.get(position);

        holder.name.setText(p.getName());
        holder.types.setText(DropOffRepository.acceptedLabel(p));

        if (userLat != null && userLng != null) {
            holder.distance.setText(GeoUtils.format(GeoUtils.distanceMetres(
                    userLat, userLng, p.getLatitude(), p.getLongitude())));
        } else {
            holder.distance.setText("—");
        }

        holder.itemView.setOnClickListener(v -> listener.onPointClicked(p));
    }

    @Override
    public int getItemCount() {
        return points.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name, types, distance;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.dropoff_name);
            types = itemView.findViewById(R.id.dropoff_types);
            distance = itemView.findViewById(R.id.dropoff_distance);
        }
    }
}