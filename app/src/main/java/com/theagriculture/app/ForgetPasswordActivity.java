package com.theagriculture.app;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

public class ForgetPasswordActivity extends AppCompatActivity {

    private final String webViewurl="http://theagriculture.tk/forgotpassword.html";
    private WebView forget_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        forget_password = findViewById(R.id.forgetPass);

        WebSettings webSettings = forget_password.getSettings();
        webSettings.setJavaScriptEnabled(true);

        forget_password.loadUrl(webViewurl);




    }

    @Override
    public void onBackPressed() {
        if (forget_password.canGoBack())
            forget_password.goBack();
        else
            super.onBackPressed();
    }
}
