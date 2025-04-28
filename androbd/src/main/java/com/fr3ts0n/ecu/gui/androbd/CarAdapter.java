package com.fr3ts0n.ecu.gui.androbd;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

    public interface OnCarActionListener {
        void onUseVehicle(int position);

        void onRemoveCar(int position);
    }

    private List<Vehicle> carList;
    private OnCarActionListener listener;

    public CarAdapter(List<Vehicle> carList, OnCarActionListener listener) {
        this.carList = carList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_car, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        Vehicle vehicle = carList.get(position);
        holder.tvLicensePlate.setText(vehicle.vin);
        holder.btnUseVehicle.setOnClickListener(v -> listener.onUseVehicle(position));
        holder.btnRemoveCar.setOnClickListener(v -> listener.onRemoveCar(position));
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    static class CarViewHolder extends RecyclerView.ViewHolder {
        TextView tvLicensePlate;
        Button btnUseVehicle, btnRemoveCar;

        CarViewHolder(View itemView) {
            super(itemView);
            tvLicensePlate = itemView.findViewById(R.id.tvLicensePlate);
            btnUseVehicle = itemView.findViewById(R.id.btnUseVehicle);
            btnRemoveCar = itemView.findViewById(R.id.btnRemoveCar);
        }
    }
}