package com.fr3ts0n.ecu.gui.androbd;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class VehicleHealthAdapter extends RecyclerView.Adapter<VehicleHealthAdapter.VehicleViewHolder> {

    public interface OnHealthClickListener {
        void onViewHealth(int position);
    }

    private List<Vehicle> carList;
    private OnHealthClickListener listener;

    public VehicleHealthAdapter(List<Vehicle> carList, OnHealthClickListener listener) {
        this.carList = carList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vehicle_health, parent, false);
        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        Vehicle vehicle = carList.get(position);
        holder.tvLicensePlate.setText(vehicle.vin);
        holder.btnViewHealth.setOnClickListener(v -> listener.onViewHealth(position));
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    static class VehicleViewHolder extends RecyclerView.ViewHolder {
        TextView tvLicensePlate;
        Button btnViewHealth;

        VehicleViewHolder(View itemView) {
            super(itemView);
            tvLicensePlate = itemView.findViewById(R.id.tvLicensePlate);
            btnViewHealth = itemView.findViewById(R.id.btnViewHealth);
        }
    }
}