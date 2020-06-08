package com.theagriculture.app.Admin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.UploadProgressListener;
import com.obsez.android.lib.filechooser.ChooserDialog;
import com.theagriculture.app.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import dmax.dialog.SpotsDialog;

import static com.theagriculture.app.AppNotificationChannels.CHANNEL_2_ID;

public class upload_fragment extends Fragment {

    private String url_location = "http://18.224.202.135/api/upload/locations/";
    private String url_bulk = "http://18.224.202.135/api/upload/mail/";
    private String token;
    private static final String TAG = "UploadFragment";
    private File csvFile;
    private AlertDialog uploadingDialog;
    private NotificationManagerCompat manager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.upload_fragment, container, false);
        CardView uploadCard = view.findViewById(R.id.card1);
        CardView uploadcsv= view.findViewById(R.id.card2);
        SharedPreferences prefs = getActivity().getSharedPreferences("tokenFile", Context.MODE_PRIVATE);
        token = prefs.getString("token", "");
        Log.d("url", "onCreateView: " );
        uploadCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //change the url  variable here
                openCsvPicker(url_location);

            }
        });
        uploadcsv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //change the url variable here
                openCsvPicker(url_bulk);
            }
        });
        manager = NotificationManagerCompat.from(getActivity());
        return view;
    }


    private void openCsvPicker(final String url) {
        File file = Environment.getExternalStorageDirectory();
        String start = file.getAbsolutePath();
        new ChooserDialog(getActivity())
                .withStartFile(start)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String s, File file) {
                        csvFile = file;
                        uploadingDialog = new SpotsDialog.Builder().setContext(getActivity())
                                .setMessage("Uploading Csv...")
                                .setCancelable(false)
                                .setTheme(R.style.CustomDialog)
                                .build();
                        uploadingDialog.show();
                        uploadCsv(url);
                    }
                })
                .withOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        dialogInterface.cancel();
                    }
                })
                .build()
                .show();
    }

    private void uploadCsv(String url) {
        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getActivity(), CHANNEL_2_ID)
                .setSmallIcon(R.drawable.ic_upload)
                .setContentTitle("Uploading Csv")
                .setContentText("0/100")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setProgress((int) csvFile.length(), 0, false);
        manager.notify(2, notificationBuilder.build());
        AndroidNetworking.upload(url)
                .addHeaders("Authorization", "Token " + token)
                .addMultipartFile("location_csv", csvFile)
                .setTag("Upload Csv")
                .setPriority(Priority.HIGH)
                .build()
                .setUploadProgressListener(new UploadProgressListener() {
                    @Override
                    public void onProgress(long bytesUploaded, long totalBytes) {
                        Log.d(TAG, "onProgress: " + bytesUploaded);
                        notificationBuilder.setContentText(((int) (bytesUploaded / totalBytes) * 100) + "/100")
                                .setProgress((int) totalBytes, (int) ((bytesUploaded / totalBytes) * 100), false);
                        manager.notify(2, notificationBuilder.build());

                    }
                })
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: " + response);
                        String count;
                        try {
                            JSONObject rootObject = new JSONObject(String.valueOf(response));
                            count = rootObject.getString("count");
                            Toast.makeText(getActivity(), "Successfully Uploaded " + count + " locations", Toast.LENGTH_LONG).show();
                            uploadingDialog.dismiss();
                            notificationBuilder.setContentText("Upload Successful!")
                                    .setProgress(0, 0, false)
                                    .setOngoing(false);
                            manager.notify(2, notificationBuilder.build());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            uploadingDialog.dismiss();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d(TAG, "onError: " + anError.getErrorDetail() + " " + anError.getErrorBody() +
                                " " + anError.getMessage() + " " + anError.getErrorCode());
                        Toast.makeText(getActivity(), "Sorry something went wrong, please try again!",
                                Toast.LENGTH_LONG).show();
                        notificationBuilder.setContentText("Upload Failed!")
                                .setProgress(0, 0, false)
                                .setOngoing(false);
                        manager.notify(2, notificationBuilder.build());
                        uploadingDialog.dismiss();
                    }
                });
    }
}
