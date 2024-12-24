package com.example.firebasetodo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView registerBtn = findViewById(R.id.tv_action);

        EditText name = findViewById(R.id.et_name);
        EditText email = findViewById(R.id.et_email);
        EditText password = findViewById(R.id.et_password);
        EditText confirmPassword = findViewById(R.id.et_confirm_password);

        registerBtn.setOnClickListener(v -> {
            navigateToLoginActivity();
        });

        registerButton = findViewById(R.id.btn_register);

        registerButton.setOnClickListener(v -> {
            String nameStr = name.getText().toString();
            String emailStr = email.getText().toString();
            String passwordStr = password.getText().toString();
            String confirmPasswordStr = confirmPassword.getText().toString();

            if (nameStr.isEmpty() || emailStr.isEmpty() || passwordStr.isEmpty() || confirmPasswordStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!passwordStr.equals(confirmPasswordStr)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailStr).matches()) {
                Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            // Register user
            registerUser(nameStr, emailStr, passwordStr);
        });
    }

    private void registerUser(String name, String email, String password) {
        registerButton.setEnabled(false);
        // Register user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this,  task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user != null) {
                                user.updateProfile(new UserProfileChangeRequest.Builder()
                                                .setDisplayName(name)
                                                .build())
                                        .addOnCompleteListener(profileTask -> {
                                            if (profileTask.isSuccessful()) {
                                                Toast.makeText(RegisterActivity.this, "Registration successful",
                                                        Toast.LENGTH_SHORT).show();
                                                registerButton.setEnabled(true);
                                                navigateToLoginActivity();
                                            } else {
                                                Toast.makeText(RegisterActivity.this, "Failed to set name",
                                                        Toast.LENGTH_SHORT).show();
                                                registerButton.setEnabled(true);
                                                navigateToLoginActivity();
                                            }
                                        });
                            }
                            else {
                                Toast.makeText(RegisterActivity.this, "Failed to set name",
                                        Toast.LENGTH_SHORT).show();
                                registerButton.setEnabled(true);
                                navigateToLoginActivity();
                            }

                        } else {
                            Toast.makeText(RegisterActivity.this, "Registration failed",
                                    Toast.LENGTH_SHORT).show();
                            registerButton.setEnabled(true);
                        }
                    }
                );
    }

    void navigateToLoginActivity() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}