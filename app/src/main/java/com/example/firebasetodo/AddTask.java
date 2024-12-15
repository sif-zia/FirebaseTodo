package com.example.firebasetodo;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDateTime;

public class AddTask extends AppCompatActivity {
    EditText titleEditText, descriptionEditText;
    Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        titleEditText = findViewById(R.id.et_title);
        descriptionEditText = findViewById(R.id.et_desc);
        saveButton = findViewById(R.id.btn_save);

        saveButton.setOnClickListener(v -> saveTask());
    }

    public void saveTask() {
        saveButton.setEnabled(false);

        String title = titleEditText.getText().toString();
        String desc = descriptionEditText.getText().toString();

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            saveButton.setEnabled(true);
            return;
        }

        Task task = new Task(title, desc, LocalDateTime.now().toString()); // Use a simpler timestamp

        FirebaseDatabase.getInstance().getReference("tasks")
                .push()
                .setValue(task)
                .addOnCompleteListener(task1 -> {
                    saveButton.setEnabled(true);
                    if (task1.isSuccessful()) {
                        Toast.makeText(this, "Task added successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to add task", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    saveButton.setEnabled(true);
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
