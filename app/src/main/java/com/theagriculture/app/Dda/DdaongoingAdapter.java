package com.theagriculture.app.Dda;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.theagriculture.app.Ado.ReviewReport;
import com.theagriculture.app.R;

import java.util.ArrayList;

public class DdaongoingAdapter extends RecyclerView.Adapter<DdaongoingAdapter.ViewHolderOngoingDda> {
    private ArrayList<String> mtextview1;
    private ArrayList<String> mtextview2;
    private ArrayList<String> mtextview3;
    private ArrayList<String> mDates;
    private Context mcontext;
    public boolean showongoingshimmer = true;
    private int shimmer_item_count = 4;

    public DdaongoingAdapter(Context mcontext, ArrayList<String> mtextview1, ArrayList<String> mtextview2,ArrayList<String> mtextview3,ArrayList<String> mDtaes) {
        this.mcontext = mcontext;
        this.mtextview1 = mtextview1;
        this.mtextview2 = mtextview2;
        this.mtextview3 = mtextview3;
        this.mDates = mDtaes;
    }

    @NonNull
    @Override
    public ViewHolderOngoingDda onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mcontext).inflate(R.layout.ddaongoinglist, parent, false);
        final ViewHolderOngoingDda viewHolderOngoingDda = new ViewHolderOngoingDda(view);
        viewHolderOngoingDda.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!showongoingshimmer) {
                    Intent intent = new Intent(mcontext, ReviewReport.class);
                    int pos = viewHolderOngoingDda.getAdapterPosition();
                    intent.putExtra("id", mtextview1.get(pos));
                    intent.putExtra("isDdo", true);
                    mcontext.startActivity(intent);
                }
            }
        });
        return viewHolderOngoingDda;
    }

    @Override
    public void onBindViewHolder(@NonNull DdaongoingAdapter.ViewHolderOngoingDda holder, int position) {

        if(showongoingshimmer){
            holder.shimmerongoing.startShimmer();
        }
        else {
            holder.shimmerongoing.stopShimmer();
            holder.shimmerongoing.setShimmer(null);
            holder.tv2.setBackground(null);
            holder.tv3.setBackground(null);
            holder.Dates.setBackground(null);
            holder.tv2.setText(mtextview3.get(position).toUpperCase());
            holder.tv3.setText(mtextview2.get(position).toUpperCase());
            holder.Dates.setText(mDates.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return showongoingshimmer ? shimmer_item_count : mtextview1.size();
    }

    public class ViewHolderOngoingDda extends RecyclerView.ViewHolder {

        TextView tv2;
        TextView tv3;
        TextView Dates;
        ShimmerFrameLayout shimmerongoing;
        RelativeLayout relativeLayout;

        public ViewHolderOngoingDda(@NonNull View itemView) {
            super(itemView);
            mcontext = itemView.getContext();
            tv2 = itemView.findViewById(R.id.adoongoingname);
            tv3 = itemView.findViewById(R.id.adoongoingaddress);
            shimmerongoing = itemView.findViewById(R.id.shimmer_ongoing);
            relativeLayout = itemView.findViewById(R.id.ongoing_parent);
            Dates = itemView.findViewById(R.id.adoongoingdates);
        }

    }
}
