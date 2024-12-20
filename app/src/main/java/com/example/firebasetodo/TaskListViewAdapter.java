package com.example.firebasetodo;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class TaskListViewAdapter extends BaseAdapter {
    List<Task> tasks;

    public TaskListViewAdapter(List<Task> tasks) {
        this.tasks = tasks;
    }

    @Override
    public int getCount() {
        return tasks.size();
    }

    @Override
    public Task getItem(int i) {
        return tasks.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.task_item, viewGroup, false);
        }

        TextView titleTextView = view.findViewById(R.id.tv_title);
        TextView descriptionTextView = view.findViewById(R.id.tv_desc);
        CheckBox checkBox = view.findViewById(R.id.cb_done);
        ImageView deleteButton = view.findViewById(R.id.btn_delete);

        titleTextView.setText(tasks.get(i).getTitle());
        descriptionTextView.setText(tasks.get(i).getDescription());
        checkBox.setChecked(tasks.get(i).isCompleted());

        if(tasks.get(i).isCompleted()) {
            deleteButton.setVisibility(View.VISIBLE);
        } else {
            deleteButton.setVisibility(View.GONE);
        }

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateTask(i, isChecked, viewGroup.getContext());
        });

        deleteButton.setOnClickListener(v -> {
            deleteTask(i, viewGroup.getContext());
        });

        int color = 0; // Use a fallback color for light mode if needed
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            color = ContextCompat.getColor(view.getContext(),
                    view.getContext().getResources().getConfiguration().isNightModeActive()
                            ? android.R.color.white
                            : android.R.color.black);
        }
        deleteButton.setColorFilter(color);

        return view;
    }

    public void deleteTask(int position, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Task task = tasks.get(position);

                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("tasks");

                    dbRef.child(task.getKey()).removeValue()
                            .addOnCompleteListener(task1 -> {
                                if (!task1.isSuccessful()) {
                                    Toast.makeText(context, "Failed to delete task", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Remove the task locally and notify the adapter
                                    notifyDataSetChanged();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to delete task", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Dismiss the dialog
                    dialog.dismiss();
                });

        builder.create().show();
    }

    public void updateTask(int position, boolean isChecked, Context context) {
        Task task = tasks.get(position);
        task.setCompleted(isChecked);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("tasks");

        dbRef.child(task.getKey()).setValue(task)
                .addOnCompleteListener(task1 -> {
                    if (!task1.isSuccessful()) {
                        Toast.makeText(context, "Failed to update task", Toast.LENGTH_SHORT).show();
                        // Revert the change locally if update fails
                        task.setCompleted(!isChecked);
                        tasks.set(position, task);
                        notifyDataSetChanged();
                    }
                    else {
                        // Notify the adapter about the change
                        tasks.set(position, task);
                        notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update task", Toast.LENGTH_SHORT).show();
                    // Revert the change locally if update fails
                    task.setCompleted(!isChecked);
                    tasks.set(position, task);
                    notifyDataSetChanged();
                });
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }
}
