package com.theagriculture.app.Admin;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.theagriculture.app.R;

import java.util.ArrayList;

public class RecyclerViewAdapter_district extends RecyclerView.Adapter<RecyclerViewAdapter_district.DistrictCustomViewHolder> {
    private Context mContext;
    private ArrayList<String> mDistrictNames;

    public RecyclerViewAdapter_district(Context mContext, ArrayList<String> mDistrictNames) {
        this.mContext = mContext;
        this.mDistrictNames = mDistrictNames;
    }

    @NonNull
    @Override
    public DistrictCustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.district_recycler, parent, false);
        final DistrictCustomViewHolder viewHolder = new DistrictCustomViewHolder(view);
        viewHolder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, adoListofDistrict.class);
                intent.putExtra("district", mDistrictNames.get(viewHolder.getAdapterPosition()));
                mContext.startActivity(intent);
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull DistrictCustomViewHolder holder, int position) {
        holder.textView.setText(mDistrictNames.get(position));
    }

    @Override
    public int getItemCount() {
        return mDistrictNames.size();
    }

    public class DistrictCustomViewHolder extends RecyclerView.ViewHolder{
        TextView textView;
        RelativeLayout itemLayout;
        public DistrictCustomViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.dist);
            itemLayout = itemView.findViewById(R.id.dist_item);
        }
    }
}
