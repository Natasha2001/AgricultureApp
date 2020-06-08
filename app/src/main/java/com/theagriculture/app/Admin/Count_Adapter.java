package com.theagriculture.app.Admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.theagriculture.app.R;

import java.util.ArrayList;

public class Count_Adapter extends RecyclerView.Adapter<Count_Adapter.MyviewHolder> {

    Context context;
    ArrayList<String> distlist;
    ArrayList<Integer> pending;
    ArrayList<Integer> ongoing;
    ArrayList<Integer> completed;

    public Count_Adapter(Context context, ArrayList<String> distlist, ArrayList<Integer> pending,
                         ArrayList<Integer> ongoing, ArrayList<Integer> completed)
    {
        this.context=context;
        this.distlist=distlist;
        this.pending=pending;
        this.completed=completed;
        this.ongoing=ongoing;


    }
    @NonNull
    @Override
    public MyviewHolder onCreateViewHolder(@NonNull ViewGroup parent , int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.count_recycler,parent,false);
        MyviewHolder vh=new MyviewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyviewHolder holder , int position) {
        holder.distval2.setText(distlist.get(position));
        holder.pendingval2.setText(pending.get(position).toString());
        holder.ongoingval2.setText(ongoing.get(position).toString());
        holder.completedval2.setText(completed.get(position).toString());
       // holder.setIsRecyclable(true);
        if(position%2==0){
           // holder.linearcount.setBackgroundColor(707070);
            holder.linearcount.setBackgroundColor(context.getResources().getColor(R.color.light_grey));
        }
        else{
            holder.linearcount.setBackgroundColor(context.getResources().getColor(R.color.white));
        }

    }

    @Override
    public int getItemCount() {
        return distlist.size();
    }

    public class MyviewHolder extends RecyclerView.ViewHolder {

        TextView distval2;
        TextView pendingval2;
        TextView ongoingval2;
        TextView completedval2;
        View linearcount;

        public MyviewHolder(@NonNull View itemView) {
            super(itemView);
            distval2=itemView.findViewById(R.id.distval2);
            pendingval2=itemView.findViewById(R.id.pendingval2);
            ongoingval2=itemView.findViewById(R.id.ongoingval2);
            completedval2=itemView.findViewById(R.id.completedval2);
            linearcount=itemView.findViewById(R.id.linearcount);
        }
    }
}

