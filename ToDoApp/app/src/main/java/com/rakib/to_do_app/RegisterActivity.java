package com.rakib.to_do_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText edtName, edtPhone, edtBio, edtEmail, edtPassword;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);

        // Initialize all views
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone); // NEW
        edtBio = findViewById(R.id.edtBio);     // NEW
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);

        MaterialButton btnRegister = findViewById(R.id.btnRegister);
        TextView tvLogin = findViewById(R.id.tvLogin);

        btnRegister.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim(); // NEW
            String bio = edtBio.getText().toString().trim();     // NEW
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Name, Email and Password are required", Toast.LENGTH_SHORT).show();
            } else {
                // Pass all fields to registerUser
                if (dbHelper.registerUser(name, email, password, phone, bio)) {
                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();
                    // Go to Login
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }
}