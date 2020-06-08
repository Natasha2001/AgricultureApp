package com.theagriculture.app.Ado;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.theagriculture.app.R;

import java.util.ArrayList;


public class ReviewPicsRecyclerviewAdapter extends RecyclerView.Adapter<ReviewPicsRecyclerviewAdapter.customViewHolder> {
    private Context mContext;
    private ArrayList<String> imagesUrl;

    public ReviewPicsRecyclerviewAdapter(Context mContext, ArrayList<String> imagesUrl) {
        this.mContext = mContext;
        this.imagesUrl = imagesUrl;
    }

    @NonNull
    @Override
    public customViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.ado_report_image_item, parent, false);
        return new customViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return imagesUrl.size();
    }

    @Override
    public void onBindViewHolder(@NonNull customViewHolder holder, int position) {
        Glide.with(mContext)
                .load(imagesUrl.get(imagesUrl.size() - position - 1))
                .into(holder.imageView);
    }

    public class customViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public customViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ado_report_image);
        }
    }
}
