package com.gvvp.roadcrackdetector;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


public class SignupActivity extends AppCompatActivity {
    TextInputLayout fullname_txt, username_txt, email_txt, phoneno_txt, password_txt;
    ProgressBar progress_bar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        fullname_txt = findViewById(R.id.fullname_text_field_design);
        username_txt = findViewById(R.id.signup_username_text_field_design);
        password_txt = findViewById(R.id.signup_password_text_field_design);
        email_txt = findViewById(R.id.email_text_field_design);
        phoneno_txt = findViewById(R.id.phoneno_text_field_design);
        progress_bar = findViewById(R.id.r_progressbar);

        progress_bar.setVisibility(View.GONE);
    }

    public void loginbuttonclick(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    private Boolean validateName() {
        String val = fullname_txt.getEditText().getText().toString();
        if (val.isEmpty()) {
            fullname_txt.setError("Field cannot be empty");
            return false;
        }
        else {
            fullname_txt.setError(null);
            fullname_txt.setErrorEnabled(false);
            return true;
        }
    }

    private Boolean validateUsername() {
        String val = username_txt.getEditText().getText().toString();
        String noWhiteSpace = "\\A\\w{4,20}\\z";
        if (val.isEmpty()) {
            username_txt.setError("Field cannot be empty");
            return false;
        } else if (val.length() >= 20) {
            username_txt.setError("Username too long");
            return false;
        } else if (!val.matches(noWhiteSpace)) {
            username_txt.setError("White Spaces are not allowed");
            return false;
        } else {
            username_txt.setError(null);
            username_txt.setErrorEnabled(false);
            return true;
        }
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

    private Boolean validatePhoneNo() {
        String val = phoneno_txt.getEditText().getText().toString();
        if (val.isEmpty()) {
            phoneno_txt.setError("Field cannot be empty");
            return false;
        } else {
            phoneno_txt.setError(null);
            phoneno_txt.setErrorEnabled(false);
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

    public void registerbuttonclick(View view) {
        if(!validateName() | !validatePassword() | !validatePhoneNo() | !validateEmail() | !validateUsername()){
            return;
        }
        else {
            String fullname = fullname_txt.getEditText().getText().toString();
            String username = username_txt.getEditText().getText().toString();
            String email = email_txt.getEditText().getText().toString();
            String phoneno = phoneno_txt.getEditText().getText().toString();
            String password = password_txt.getEditText().getText().toString();
            progress_bar.setVisibility(view.VISIBLE);
            mAuth = FirebaseAuth.getInstance();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                storingdata user = new storingdata(username, fullname, email, phoneno);
                                FirebaseFirestore db  = FirebaseFirestore.getInstance();
                                DocumentReference newUserRef = db.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                newUserRef.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                        Toast.makeText(SignupActivity.this,"User has been registered successfully", Toast.LENGTH_LONG).show();
                                        progress_bar.setVisibility(view.VISIBLE);
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SignupActivity.this,"Failed to Register! Try Again!", Toast.LENGTH_LONG).show();
                                        progress_bar.setVisibility(view.GONE);
                                    }
                                });

                                /*FirebaseDatabase.getInstance().getReference("Users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                                    Toast.makeText(SignupActivity.this,"User has been registered successfully", Toast.LENGTH_LONG).show();
                                                    progress_bar.setVisibility(view.VISIBLE);
                                                    finish();
                                                }else{
                                                    Toast.makeText(SignupActivity.this,"Failed to Register! Try Again!", Toast.LENGTH_LONG).show();
                                                    progress_bar.setVisibility(view.GONE);
                                                }
                                            }
                                        });*/
                            }else {
                                Toast.makeText(SignupActivity.this, "User with same email already exists! Try using another email id", Toast.LENGTH_LONG).show();
                                progress_bar.setVisibility(view.GONE);
                            }
                        }
                    });
        }
    }
}