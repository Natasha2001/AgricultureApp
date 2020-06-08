package com.theagriculture.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import static io.fabric.sdk.android.services.concurrency.AsyncTask.init;

public class RegistrationActivity extends AppCompatActivity {
    //private WebView webView;

    TextView tvName,tvEnd;
    EditText etEmail,etType,etConfirm;
    Button btnRegister;
    ImageButton typeButton, confirmButton;
    public boolean typeHide = false, confirmHide = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        if(Build.VERSION.SDK_INT>=21){
            View decorView = getWindow().getDecorView();
            //int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN| View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            int option = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            //getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        init();

        tvName = findViewById(R.id.tvName);
        etEmail = findViewById(R.id.etEmail);
        etConfirm = findViewById(R.id.etConfirm);
        etType = findViewById(R.id.etType);
        btnRegister = findViewById(R.id.btnRegister);
        tvEnd = findViewById(R.id.textView5);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etEmail.getText().toString().isEmpty() || etConfirm.getText().toString().isEmpty() || etType.getText().toString().isEmpty())
                {
                    Toast.makeText(RegistrationActivity.this, "Please enter all details!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if (etType.getText().toString().equals(etConfirm.getText().toString()))
                    {
                        Toast.makeText(RegistrationActivity.this, "Successfully registered \n Signing up", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(RegistrationActivity.this, "Password does not match! \n Please enter again", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        //to redirect back to login page
        //String text = tvEnd.getText().toString().trim();
        String text = "Don't have an account Login";
        SpannableString ss = new SpannableString(text);
        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                //Toast.makeText(RegistrationActivity.this,"You clicked login",Toast.LENGTH_LONG).show();
                Intent intent1 = new Intent(RegistrationActivity.this,login_activity.class);
                startActivity(intent1);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.RED);
            }
        };
        ss.setSpan(clickableSpan1,22,27, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvEnd.setText(ss);
        tvEnd.setMovementMethod(LinkMovementMethod.getInstance());

        //to chnage colour of register button when text is filled in all fields
        etEmail.addTextChangedListener(registerTextWatcher);
        etType.addTextChangedListener(registerTextWatcher);
        etConfirm.addTextChangedListener(registerTextWatcher);

        //to view data in type password field
        typeButton = findViewById(R.id.imageView3);
        typeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(typeHide==true){
                    int start=etType.getSelectionStart();//get position of cursor
                    int end= etType.getSelectionEnd();
                    etType.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    typeHide=false;
                    typeButton.setImageResource(R.drawable.show_password);
                    etType.setSelection(start,end);//added so that cursor does not jump to start
                }
                else{
                    int start= etType.getSelectionStart();
                    int end= etType.getSelectionEnd();
                    etType.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    typeHide=true;
                    typeButton.setImageResource(R.drawable.hide_password);
                    etType.setSelection(start,end);
                }
            }
        });

        //to view data in confirm password field
        confirmButton = findViewById(R.id.imageView4);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(confirmHide==true){
                    int start = etConfirm.getSelectionStart();
                    int end= etConfirm.getSelectionEnd();
                    etConfirm.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    confirmHide=false;
                    confirmButton.setImageResource(R.drawable.show_password);
                    etConfirm.setSelection(start,end);
                }
                else{
                    int start = etConfirm.getSelectionStart();
                    int end = etConfirm.getSelectionEnd();
                    etConfirm.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    confirmHide=true;
                    confirmButton.setImageResource(R.drawable.hide_password);
                    etConfirm.setSelection(start,end);
                }
            }
        });
        //end of onCreate fuction
    }

    //function to change colour of register button when text is entered
    public TextWatcher registerTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String username = etEmail.getText().toString().trim();
            String password = etType.getText().toString().trim();
            String confirm_password = etConfirm.getText().toString().trim();
            if(!username.isEmpty() && !password.isEmpty() && !confirm_password.isEmpty())
            {
                btnRegister.setBackgroundResource(R.drawable.buttons);
            }
            else{
                btnRegister.setBackgroundResource(R.drawable.buttons_before_text);//this condition is added for when text was first added but now it is cleared
            }
        }
        @Override
        public void afterTextChanged(Editable s) {

        }
    };
        /*
        webView = findViewById(R.id.reg_webview);
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        boolean isAdo = intent.getBooleanExtra("isAdo", false);
        if (isAdo)
            getSupportActionBar().setTitle("Sign Up as ADO");
        else
            getSupportActionBar().setTitle("Sign Up as DDA");
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadUrl(url);
    }
        */
}
