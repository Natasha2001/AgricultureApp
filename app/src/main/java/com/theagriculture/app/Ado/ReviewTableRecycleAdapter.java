package com.theagriculture.app.Ado;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.theagriculture.app.R;

import java.util.ArrayList;

public class ReviewTableRecycleAdapter extends RecyclerView.Adapter<ReviewTableRecycleAdapter.viewHolder> {

    Context context;
    ArrayList<String> list1;
    ArrayList<String> list2;
    ArrayList<String> list3;
    ArrayList<String> list4;

    public ReviewTableRecycleAdapter(Context context, ArrayList<String> list1, ArrayList<String> list2, ArrayList<String> list3, ArrayList<String> list4) {
        this.context = context;
        this.list1 = list1;
        this.list2 = list2;
        this.list3 = list3;
        this.list4 = list4;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.tablelist, parent, false);
        viewHolder holder = new viewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        if (position == 0) {
            holder.tv1.setBackground(null);
            holder.tv2.setBackground(null);
            holder.tv3.setBackground(null);
            holder.tv4.setBackground(null);
        }
        holder.tv1.setText(list1.get(position));
        holder.tv2.setText(list2.get(position));
        holder.tv3.setText(list3.get(position));
        holder.tv4.setText(list4.get(position));


    }

    @Override
    public int getItemCount() {
        return list1.size();
    }

    class viewHolder extends RecyclerView.ViewHolder {
        TextView tv1;
        TextView tv2;
        TextView tv3;
        TextView tv4;
        LinearLayout parent;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            tv1 = itemView.findViewById(R.id.tv1);
            tv2 = itemView.findViewById(R.id.tv2);
            tv3 = itemView.findViewById(R.id.tv3);
            tv4 = itemView.findViewById(R.id.tv4);
            parent = itemView.findViewById(R.id.parent);

        }
    }
}
