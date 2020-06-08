package com.theagriculture.app.Dda;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.theagriculture.app.R;

import java.util.ArrayList;

public class DdapendingassignedAdapter extends RecyclerView.Adapter<DdapendingassignedAdapter.ViewHolderAssignedDda> {
    private static final String TAG = "DdapendingassignedAdapt";
    ArrayList<String> mtextview1;
    ArrayList<String> mtextview2;
    ArrayList<String> mtextview3;
    ArrayList<String> mDates;
    private ArrayList<String> mAdoIds;
    Context mcontext;
    public boolean showassignedshimmer = true;
    private int shimmer_item_count = 6;
    private AlertDialog dialog;
    private String adoId;

    public DdapendingassignedAdapter(Context mcontext, ArrayList<String> mtextview1, ArrayList<String> mtextview2, ArrayList<String> mtextview3, ArrayList<String> mAdoIds,ArrayList<String> mDates) {
        this.mcontext = mcontext;
        this.mtextview1 = mtextview1;
        this.mtextview2 = mtextview2;
        this.mtextview3 = mtextview3;
        this.mAdoIds = mAdoIds;
        this.mDates = mDates;
    }

    @NonNull
    @Override
    public DdapendingassignedAdapter.ViewHolderAssignedDda onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mcontext).inflate(R.layout.ddaadoassignedlist,parent,false);
        final ViewHolderAssignedDda viewHolderAssignedDda = new ViewHolderAssignedDda(view);
        viewHolderAssignedDda.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!showassignedshimmer) {
                    dialog = showdialogbox("Reassign Location", "Are you sure?", "Yes",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(mcontext, DdaselectAdo.class);
                                    int pos = viewHolderAssignedDda.getAdapterPosition();
                                    intent.putExtra("Id_I_Need", mtextview1.get(pos));
                                    intent.putExtra("adoId", mAdoIds.get(pos));
                                    mcontext.startActivity(intent);
                                    dialog.dismiss();
                                }
                            }, "No",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialog.dismiss();
                                }
                            }, true);
                }
            }
        });
        return viewHolderAssignedDda;
    }

    @Override
    public void onBindViewHolder(@NonNull DdapendingassignedAdapter.ViewHolderAssignedDda holder, int position) {

        if(showassignedshimmer){
            holder.shimmerassigned.startShimmer();
        }
        else {
            holder.shimmerassigned.stopShimmer();
            holder.shimmerassigned.setShimmer(null);

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
        return showassignedshimmer ? shimmer_item_count : mtextview1.size();
    }

    private AlertDialog showdialogbox(String title, String msg, String positiveLabel, DialogInterface.OnClickListener positiveOnclick,
                                      String negativeLabel, DialogInterface.OnClickListener negativeOnclick,
                                      boolean isCancelable) {

        AlertDialog.Builder builder = new AlertDialog.Builder(mcontext);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton(positiveLabel, positiveOnclick);
        builder.setNegativeButton(negativeLabel, negativeOnclick);
        builder.setCancelable(isCancelable);
        AlertDialog alert = builder.create();
        Log.d(TAG, "showdialogbox: " + alert);
        alert.show();
        return alert;
    }

    public class ViewHolderAssignedDda extends RecyclerView.ViewHolder{

        TextView tv2;
        TextView tv3;
        TextView Dates;
        RelativeLayout parent;
        ShimmerFrameLayout shimmerassigned;

        public ViewHolderAssignedDda(@NonNull View itemView) {
            super(itemView);
            mcontext = itemView.getContext();
            parent = itemView.findViewById(R.id.card_assigned);
            tv2 = itemView.findViewById(R.id.adoassignedname);
            tv3 = itemView.findViewById(R.id.adoassignedaddress);
            Dates = itemView.findViewById(R.id.adoassigneddate);
            shimmerassigned = itemView.findViewById(R.id.shimmer_assigned);

        }

    }
}
