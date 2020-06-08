package com.theagriculture.app.Admin;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.theagriculture.app.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NotificationActivity extends AppCompatActivity {

    private EditText Title;
    private EditText Message;
    private String token;
    private final String TAG = "Notification activity";
    private JSONObject params;
    private Button button;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        Title = findViewById(R.id.title1);
        Message = findViewById(R.id.title2);

        final SharedPreferences preferences = this.getSharedPreferences("tokenFile", Context.MODE_PRIVATE);
        token = preferences.getString("token", "");

        button = findViewById(R.id.send_notification);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendNotifications();
            }
        });



    }



    private void sendNotifications() {

        params = new JSONObject();
        try {
            params.put("username", Title.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            params.put("password", Message.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = "http://18.224.202.135/api/trigger/sms/pending";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: sendNotifications " + response);
                        Toast.makeText(getApplicationContext(), "Notifications Successfully Sent!",
                                Toast.LENGTH_SHORT).show();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NoConnectionError)
                            Toast.makeText(getApplicationContext(), "Check Your Internt Connection Please!",
                                    Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getApplicationContext(), "Something went wrong, please try again!",
                                    Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onErrorResponse: sendNotifications " + error);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Token " + token);
                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        jsonObjectRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        requestQueue.add(jsonObjectRequest);
    }
}
