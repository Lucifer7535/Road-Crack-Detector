package com.gvvp.roadcrackdetector;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button signupbtn,loginbtn,forgetpasswordbtn;
    private TextInputLayout email_txt,password_txt;
    private ProgressBar progressbar;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            // User is already logged in, start the DashboardActivity
            Intent intent = new Intent(this, Dashboard.class);
            startActivity(intent);
            finish(); // Optional: close the LoginActivity so it's not on the back stack
        }

        signupbtn = findViewById(R.id.signup_button);
        signupbtn.setOnClickListener(this);

        loginbtn = findViewById(R.id.login_button);
        loginbtn.setOnClickListener(this);

        forgetpasswordbtn = findViewById(R.id.forget_password_button);
        forgetpasswordbtn.setOnClickListener(this);

        email_txt = findViewById(R.id.login_email_text_field_design);
        password_txt = findViewById(R.id.password_text_field_design);
        progressbar = findViewById(R.id.progressbar);

        progressbar.setVisibility(View.GONE);
    }

    private Boolean validateEmail() {
        String val = email_txt.getEditText().getText().toString();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if (val.isEmpty()) {
            email_txt.setError("Field cannot be empty");
            return false;
        } else if (!val.matches(emailPattern)) {
            email_txt.setError("Invalid email address");
            return false;
        } else {
            email_txt.setError(null);
            email_txt.setErrorEnabled(false);
            return true;
        }
    }

    String passwordVal = "^" +
            "(?=.*[0-9])" +         //at least 1 digit
            "(?=.*[a-z])" +         //at least 1 lower case letter
            "(?=.*[A-Z])" +         //at least 1 upper case letter
            "(?=.*[@#$%^&+=])" +    //at least 1 special character
            "(?=\\S+$)" +           //no white spaces
            ".{4,}" +               //at least 4 characters
            "$";

    private Boolean validatePassword() {
        String val = password_txt.getEditText().getText().toString();
        String passwordVal = "^" +
                //"(?=.*[0-9])" +         //at least 1 digit
                //"(?=.*[a-z])" +         //at least 1 lower case letter
                //"(?=.*[A-Z])" +         //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +      //any letter
                "(?=.*[@#$%^&+=])" +    //at least 1 special character
                "(?=\\S+$)" +           //no white spaces
                ".{4,}" +               //at least 4 characters
                "$";
        if (val.isEmpty()) {
            password_txt.setError("Field cannot be empty");
            return false;
        } else if (!val.matches(passwordVal)) {
            password_txt.setError("Password is too weak");
            return false;
        } else {
            password_txt.setError(null);
            password_txt.setErrorEnabled(false);
            return true;
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.signup_button:
                startActivity(new Intent(this, SignupActivity.class));
                finish();
                break;
            case R.id.login_button:
                userlogin();
                break;
            case R.id.forget_password_button:
                startActivity(new Intent(this, ForgotPassword.class));
                finish();
        }
    }

    private void userlogin() {
        String email = email_txt.getEditText().getText().toString().trim();
        String password = password_txt.getEditText().getText().toString().trim();

        if (!validatePassword() | !validateEmail()) {
            return;
        } else {
            final Context context = this;
            mAuth = FirebaseAuth.getInstance();
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        progressbar.setVisibility(View.VISIBLE);
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.apply();

                        startActivity(new Intent(MainActivity.this, Dashboard.class));
                        finish();
                    }else{
                        Toast.makeText(MainActivity.this, "Failed to login! Please Check your Credentials", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}