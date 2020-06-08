package com.theagriculture.app.Admin;


import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.theagriculture.app.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class count_fragment extends Fragment {

    private Button btndate;
    private RecyclerView pierecycler;
    private ArrayList<String> distlist;
    private ArrayList<Integer> pending;
    private String token ;
    private ArrayList<Integer> ongoing;
    private ArrayList<Integer> completed;
    private PieChart pie;
    private ArrayList<PieEntry> val;
    private ArrayList valstr;
    private String date;
    final String burl="http://18.224.202.135/api/count-reports/?date=";
    private String URL;
    private Count_Adapter adapter;
    private PieData pieData;
    private Button allStatButton;
    private TextView totalPendingTextView;
    private TextView totalOngoingTextView;
    private TextView totalCompletedTextView;
    private PieDataSet pieDataSet;
    private ArrayList<Integer> colors;
    private ArrayList<LegendEntry> legendEntries;

    public count_fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater , ViewGroup container ,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_count , container , false);
        pierecycler=view.findViewById(R.id.pierecycler);
        pie=view.findViewById(R.id.pie);
        btndate=view.findViewById(R.id.btndate);
        allStatButton = view.findViewById(R.id.total_stat_button);
        totalPendingTextView = view.findViewById(R.id.total_pending);
        totalOngoingTextView = view.findViewById(R.id.total_ongoing);
        totalCompletedTextView = view.findViewById(R.id.total_completed);
        distlist=new ArrayList<>();
        pending=new ArrayList<>();
        ongoing=new ArrayList<>();
        completed=new ArrayList<>();
        val=new ArrayList<>();
        valstr=new ArrayList<>();

        Calendar c= Calendar.getInstance();
        final int day=c.get(Calendar.DAY_OF_MONTH);
        final int month=c.get(Calendar.MONTH);
        final int year=c.get(Calendar.YEAR);

        final SharedPreferences preferences = getActivity().getSharedPreferences("tokenFile", Context.MODE_PRIVATE);
        token = preferences.getString("token", "");

        date = year + "-" + (month + 1) + "-" + (day - 1);
        btndate.setText(date);
        URL="http://18.224.202.135/api/count-reports/?date="+date;


        btndate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog=new DatePickerDialog(getActivity() , new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view , int year , int month , int dayOfMonth) {
                        month=month+1;
                        date=year+"-"+month+"-"+dayOfMonth;
                        btndate.setText(date);
                        URL="http://18.224.202.135/api/count-reports/?date="+date;
                        getData(URL);
                    }
                },year,month,day-1);
                datePickerDialog.show();
            }
        });

        RequestQueue mqueue= Volley.newRequestQueue(getActivity());
        JsonObjectRequest json= new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String data1=response.toString();
                    JSONObject object=new JSONObject(data1);
                    int pen_c=Integer.valueOf(object.getString("pending_count"));
                    int ong_c=Integer.valueOf(object.getString("ongoing_count"));
                    int cp=Integer.valueOf(object.getString("completed_count"));
                    if(pen_c>0) {
                        val.add(new PieEntry(pen_c,""));
                    }
                    if(ong_c>0){
                        val.add(new PieEntry(ong_c,""));
                    }
                    if(cp>0) {
                        val.add(new PieEntry(cp,""));
                    }
                    Log.d("PIE", "onResponse: VALUES " + pen_c + " " + ong_c + " " + cp);
                    totalPendingTextView.setText(String.valueOf(pen_c));
                    totalOngoingTextView.setText(String.valueOf(ong_c));
                    totalCompletedTextView.setText(String.valueOf(cp));
                    /*distlist.add("TOTAL");
                    pending.add(pen_c);
                    ongoing.add(ong_c);
                    completed.add(cp);*/
                    JSONObject resultsObject = object.getJSONObject("results");
                    Iterator<String> itr = resultsObject.keys();
                    while(itr.hasNext())
                    {
                        String place = itr.next();
                        Object districtObject = resultsObject.get(place);
                        Log.d("Logs", "onResponse: place" + place + "object " + districtObject);
                        int pendingCount = ((JSONObject)districtObject).getInt("pending");
                        int ongoingCount = ((JSONObject)districtObject).getInt("ongoing");
                        int completedCount = ((JSONObject)districtObject).getInt("completed");
                        distlist.add(place);
                        pending.add(pendingCount);
                        ongoing.add(ongoingCount);
                        completed.add(completedCount);
                    }
                    bindUI();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error2",error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Token " + token);
                return map;
            }
        };
        mqueue.add(json);

        pie.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e , Highlight h) {
                Toast.makeText(getActivity(),"Value: "+e.getY(),Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });

        allStatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "http://18.224.202.135/api/count-reports/";
              /*  Calendar c= Calendar.getInstance();
                int day=c.get(Calendar.DAY_OF_MONTH);
                int month=c.get(Calendar.MONTH);
                int year=c.get(Calendar.YEAR); */
                String date = year+"-"+(month + 1)+"-"+day;
                btndate.setText(date);
                getData(url);
               // updateUI();
            }
        });
        return view;
    }

    private void bindUI()
    {

        colors=new ArrayList<>();
        colors.add(getResources().getColor(R.color.pending));
        colors.add(getResources().getColor(R.color.ongoing));
        colors.add(getResources().getColor(R.color.completed));

        ArrayList<String> status=new ArrayList<>();
        status.add("Pending");
        status.add("Ongoing");
        status.add("Completed");

        legendEntries=new ArrayList<>();

        for(int i=0;i<status.size();i++)
        {
            LegendEntry entry=new LegendEntry();
            entry.formColor=colors.get(i);
            entry.label=status.get(i);
            legendEntries.add(entry);
        }

        Legend legend=pie.getLegend();

        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setCustom(legendEntries);

        class IntValue extends ValueFormatter implements IValueFormatter{

            @Override
            public String getFormattedValue(float value , Entry entry , int dataSetIndex , ViewPortHandler viewPortHandler) {
                return String.valueOf((int)value);
            }
        }

        PieDataSet pieDataSet=new PieDataSet(val,"");
       // pie.setDrawHoleEnabled(false);
        pieDataSet.setValueTextSize(15);
        pieDataSet.setValueFormatter(new IntValue());
        pieDataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

      //  pieDataSet.setValueTextColor(getActivity().getResources().getColor(R.color.peach));
        pieDataSet.setColors(colors);
        pie.getDescription().setText("");

        PieData pieData = new PieData(pieDataSet);
        pie.setData(pieData);
      //  pie.setDrawEntryLabels(false);
        pie.animateXY(2000,2000);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getActivity());
        pierecycler.setLayoutManager(linearLayoutManager);
        pierecycler.addItemDecoration(new DividerItemDecoration(getActivity(),DividerItemDecoration.VERTICAL));
        adapter=new Count_Adapter(getActivity(),distlist,pending,ongoing,completed);
        pierecycler.setAdapter(adapter);
    }

    private void getData(String url)
    {
        RequestQueue mqueue= Volley.newRequestQueue(getActivity());
        val.clear();
        distlist.clear();
        pending.clear();
        ongoing.clear();
        completed.clear();
       // pieData.clearValues();
        JsonObjectRequest json= new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String data1=response.toString();
                    JSONObject object=new JSONObject(data1);
                    int pen_c=Integer.valueOf(object.getString("pending_count"));
                    int ong_c=Integer.valueOf(object.getString("ongoing_count"));
                    int cp=Integer.valueOf(object.getString("completed_count"));
                    val.clear();
                    Log.d("clear","OnResponse "+val);
                    if(pen_c>0) {
                        val.add(new PieEntry(pen_c,""));
                    }
                    if(ong_c>0){
                        val.add(new PieEntry(ong_c,""));
                    }
                    if(cp>0) {
                        val.add(new PieEntry(cp,""));
                    }
                    Log.d("PIE", "onResponse: VALUES " + pen_c + " " + ong_c + " " + cp);
                    totalPendingTextView.setText(String.valueOf(pen_c));
                    totalOngoingTextView.setText(String.valueOf(ong_c));
                    totalCompletedTextView.setText(String.valueOf(cp));
                    /*distlist.add("TOTAL");
                    pending.add(pen_c);
                    ongoing.add(ong_c);
                    completed.add(cp);*/
                    JSONObject resultsObject = object.getJSONObject("results");
                    Iterator<String> itr = resultsObject.keys();
                    while(itr.hasNext())
                    {
                        String place = itr.next();
                        Object districtObject = resultsObject.get(place);
                      //  Log.d("Logs", "onResponse: place" + place + "object " + districtObject);
                        int pendingCount = ((JSONObject)districtObject).getInt("pending");
                        int ongoingCount = ((JSONObject)districtObject).getInt("ongoing");
                        int completedCount = ((JSONObject)districtObject).getInt("completed");
                        distlist.add(place);
                        pending.add(pendingCount);
                        ongoing.add(ongoingCount);
                        completed.add(completedCount);
                        Log.d("counts", "onResponse: value" + val);
                    }
                    updateUI();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error2",error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Token " + token);
                return map;
            }
        };
        mqueue.add(json);
    }

    private void updateUI()
    {
        colors=new ArrayList<>();
        colors.add(getResources().getColor(R.color.pending));
        colors.add(getResources().getColor(R.color.ongoing));
        colors.add(getResources().getColor(R.color.completed));

        ArrayList<String> status=new ArrayList<>();
        status.add("Pending");
        status.add("Ongoing");
        status.add("Completed");

        legendEntries=new ArrayList<>();

        for(int i=0;i<status.size();i++)
        {
            LegendEntry entry=new LegendEntry();
            entry.formColor=colors.get(i);
            entry.label=status.get(i);
            legendEntries.add(entry);
        }

        Legend legend=pie.getLegend();
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setCustom(legendEntries);

        PieDataSet pieDataSet=new PieDataSet(val,"");
        pieDataSet.setValueTextSize(15);
        pieDataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
       // pieDataSet.setValueTextColor(getActivity().getResources().getColor(R.color.peach));
        pieDataSet.setColors(colors);
        pie.getDescription().setText("");

        PieData pieData = new PieData(pieDataSet);
        pieData.addDataSet(pieDataSet);
        pie.setData(pieData);
        pie.animateXY(2000,2000);
        //pie.setDrawEntryLabels(false);
        adapter.notifyDataSetChanged();
        pie.notifyDataSetChanged();
        pie.invalidate();
    }
}

