package com.theagriculture.app.Dda;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.theagriculture.app.R;

import java.util.ArrayList;

public class VillagesListRecyAdapter extends RecyclerView.Adapter<VillagesListRecyAdapter.VillagesViewHolder> {

    private ArrayList<String> villagesList;
    private Context mContext;

    public VillagesListRecyAdapter(Context mContext, ArrayList<String> villagesList) {
        this.mContext = mContext;
        this.villagesList = villagesList;
    }

    @NonNull
    @Override
    public VillagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.villages_list_item, parent, false);
        return new VillagesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VillagesViewHolder holder, int position) {
        holder.villageName.setText(villagesList.get(position));
    }

    @Override
    public int getItemCount() {
        return villagesList.size();
    }

    public class VillagesViewHolder extends RecyclerView.ViewHolder {
        TextView villageName;

        public VillagesViewHolder(@NonNull View itemView) {
            super(itemView);
            villageName = itemView.findViewById(R.id.village_name);
        }
    }
}
