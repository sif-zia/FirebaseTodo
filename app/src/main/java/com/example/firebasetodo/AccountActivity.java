package com.example.firebasetodo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountActivity extends AppCompatActivity {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = mAuth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView name = findViewById(R.id.tv_name);
        TextView email = findViewById(R.id.tv_email);
        TextView uid = findViewById(R.id.tv_uid);

        name.setText(user.getDisplayName() == null || user.getDisplayName().isEmpty() ? "Unknown" : user.getDisplayName());
        email.setText(user.getEmail());
        uid.setText(user.getUid());

        Button logoutButton = findViewById(R.id.btn_logout);
        Button deleteButton = findViewById(R.id.btn_delete);

        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            navigateToLoginActivity();
        });

        deleteButton.setOnClickListener(v -> {
            mAuth.signOut();
            user.delete();
            navigateToLoginActivity();
        });
    }

    private void navigateToLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}