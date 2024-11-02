package com.aican.tlcanalyzer.adapterClasses;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.aican.tlcanalyzer.R;
import com.aican.tlcanalyzer.dataClasses.ContourData;

import java.util.ArrayList;

public class ContourDataAdapter extends RecyclerView.Adapter<ContourDataAdapter.ViewHolder> {

    private final LayoutInflater mInflater;
    Context context;
    ArrayList<ContourData> arrayList;

    public ContourDataAdapter(Context context, ArrayList<ContourData> arrayList) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.contour_data, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ContourData contourData = arrayList.get(position);

        holder.contourId.setText(contourData.getId());
        holder.rfValue.setText(contourData.getRf());
        holder.areaValue.setText(contourData.getArea());
        holder.volumeValue.setText(contourData.getVolume());

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView contourId;
        TextView rfValue;
        TextView areaValue;
        TextView volumeValue;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            contourId = itemView.findViewById(R.id.contourId);
            rfValue = itemView.findViewById(R.id.rfValue);
            areaValue = itemView.findViewById(R.id.areaValue);
            volumeValue = itemView.findViewById(R.id.volumeValue);

        }
    }
}
