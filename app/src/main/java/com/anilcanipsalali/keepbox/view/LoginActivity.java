package com.anilcanipsalali.keepbox.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.anilcanipsalali.keepbox.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText emailET, passwordET;
    private TextView loginTV, forgotTV, registerTV;
    private ImageView facebookIV, microsoftIV, twitterIV, googleIV;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();
        clickListener();
    }

    private void init() {
        emailET = findViewById(R.id.emailET);
        passwordET = findViewById(R.id.passwordET);
        facebookIV = findViewById(R.id.facebookIV);
        microsoftIV = findViewById(R.id.microsoftIV);
        twitterIV = findViewById(R.id.twitterIV);
        googleIV = findViewById(R.id.googleIV);
        loginTV = findViewById(R.id.loginTV);
        forgotTV = findViewById(R.id.forgotTV);
        registerTV = findViewById(R.id.registerTV);
        progressBar = findViewById(R.id.progressBar);

        //Firebase
        auth = FirebaseAuth.getInstance();

    }

    private void clickListener() {
        loginTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailET.getText().toString();
                String password = passwordET.getText().toString();

                if(email.isEmpty()) {
                    emailET.setError("Enter valid email!");
                    return;
                }

                if(password.isEmpty()) {
                    passwordET.setError("Enter valid password!");
                    return;
                }

                signIn(email, password);
            }
        });

        registerTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });
    }

    private void signIn (String email, String password) {
        progressBar.setVisibility(View.VISIBLE);

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    user = auth.getCurrentUser();
                    assert user != null;
                    progressBar.setVisibility(View.GONE);
                    startActivity(new Intent(LoginActivity.this, NotesActivity.class));
                    finish();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}