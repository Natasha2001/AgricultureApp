package com.theagriculture.app.Dda;

import android.content.Context;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.theagriculture.app.R;

import java.util.ArrayList;

public class VillagesUnderDistrictAdapter extends RecyclerView.Adapter<VillagesUnderDistrictAdapter.CustomViewHolder>
        implements Filterable {

    private ArrayList<String> villageNames;
    private ArrayList<Integer> villageIds;
    private ArrayList<String> villageNamesFiltered;
    private ArrayList<Integer> villageIdsFiltered;
    private Context mContext;
    private SparseBooleanArray sparseBooleanArray;
    private ArrayList<Integer> currentVillagesPos;

    public VillagesUnderDistrictAdapter(Context mContext, ArrayList<String> villageNames, ArrayList<Integer> villageIds) {
        this.villageNames = villageNames;
        this.mContext = mContext;
        this.villageIds = villageIds;
        villageNamesFiltered = villageNames;
        villageIdsFiltered = villageIds;
        sparseBooleanArray = new SparseBooleanArray();
        currentVillagesPos = new ArrayList<>();
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.village_under_district_item, parent, false);
        final CustomViewHolder viewHolder = new CustomViewHolder(view);
        viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isPressed()) {
                    int pos = viewHolder.getAdapterPosition();
                    int selectedVillageId = villageIdsFiltered.get(pos);
                    toggleSelection(selectedVillageId);
                }
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        holder.villageTextView.setText(villageNamesFiltered.get(position));
        holder.checkBox.setChecked(false);
        int villageId = villageIdsFiltered.get(position);
        if (currentVillagesPos.contains(villageId)) {
            holder.checkBox.setChecked(true);
            toggleSelection(villageId);
        }
    }

    @Override
    public int getItemCount() {
        return villageNamesFiltered.size();
    }

    private void toggleSelection(int pos) {
        Log.d("ADAPTER ", "toggleSelection: " + sparseBooleanArray.get(pos) + pos);
        selectView(pos, !sparseBooleanArray.get(pos));
    }

    private void selectView(int pos, boolean value) {
        if (value) {
            sparseBooleanArray.put(pos, value);
        } else {
            sparseBooleanArray.delete(pos);
        }
    }

    public SparseBooleanArray getSparseBooleanArray() {
        return sparseBooleanArray;
    }

    public void addtoCurrentVillagesPos(int pos) {
        currentVillagesPos.add(pos);
        Log.d("ADAPTER ", "addtoCurrentVillagesPos: " + pos);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView villageTextView;
        CheckBox checkBox;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            villageTextView = itemView.findViewById(R.id.vill_name);
            checkBox = itemView.findViewById(R.id.village_checkbox);
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    villageNamesFiltered = villageNames;
                    villageIdsFiltered = villageIds;
                } else {
                    ArrayList<String> filteredVillNames = new ArrayList<>();
                    ArrayList<Integer> filteredVillIds = new ArrayList<>();
                    for (int i = 0; i < villageNames.size(); i++) {
                        if (villageNames.get(i).toLowerCase().contains(charString.toLowerCase())) {
                            filteredVillNames.add(villageNames.get(i));
                            filteredVillIds.add(villageIds.get(i));
                        }
                    }

                    villageNamesFiltered = filteredVillNames;
                    villageIdsFiltered = filteredVillIds;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = villageNamesFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                villageNamesFiltered = (ArrayList<String>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
}
