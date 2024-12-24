package com.example.firebasetodo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class TaskAdapter extends RecyclerView.Adapter<TaskViewHolder> {

    ArrayList<Task> tasks;

    public TaskAdapter(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.titleEditText.setText(tasks.get(position).getTitle());
        holder.descriptionEditText.setText(tasks.get(position).getDescription());
        holder.checkBox.setChecked(tasks.get(position).isCompleted());

        if(tasks.get(position).isCompleted()) {
            holder.deleteButton.setVisibility(View.VISIBLE);
        } else {
            holder.deleteButton.setVisibility(View.GONE);
        }

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateTask(holder, isChecked);
        });

        holder.deleteButton.setOnClickListener(v -> {
            deleteTask(holder);
        });

        int color = 0; // Use a fallback color for light mode if needed
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            color = ContextCompat.getColor(holder.itemView.getContext(),
                    holder.itemView.getContext().getResources().getConfiguration().isNightModeActive()
                            ? android.R.color.white
                            : android.R.color.black);
        }
        holder.deleteButton.setColorFilter(color);
    }

    public void deleteTask(@NonNull RecyclerView.ViewHolder holder) {

        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());

        builder.setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Delete the task
                    int deletePosition = holder.getBindingAdapterPosition();
                    Task task = tasks.get(deletePosition);

                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("tasks").child(uid);

                    dbRef.child(task.getKey()).removeValue()
                            .addOnCompleteListener(task1 -> {
                                if (!task1.isSuccessful()) {
                                    Toast.makeText(holder.itemView.getContext(), "Failed to delete task", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Notify the adapter about the removed item
                                    holder.itemView.post(() -> {
                                        notifyItemRemoved(deletePosition);
                                        notifyItemRangeChanged(deletePosition, tasks.size());
                                    });
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(holder.itemView.getContext(), "Failed to delete task", Toast.LENGTH_SHORT).show();
                            });

                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Dismiss the dialog
                    notifyItemChanged(holder.getBindingAdapterPosition());
                    dialog.dismiss();
                });

        builder.create().show();
    }

    public void updateTask(@NonNull RecyclerView.ViewHolder holder, boolean isChecked) {
        int updatePosition = holder.getBindingAdapterPosition();
        Task task = tasks.get(updatePosition);
        task.setCompleted(isChecked);
        tasks.set(updatePosition, task);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("tasks").child(uid);

        dbRef.child(task.getKey()).setValue(task)
                .addOnCompleteListener(task1 -> {
                    if (!task1.isSuccessful()) {
                        Toast.makeText(holder.itemView.getContext(), "Failed to update task", Toast.LENGTH_SHORT).show();
                        task.setCompleted(!isChecked);
                        tasks.set(updatePosition, task);

                        holder.itemView.post(() -> notifyItemChanged(updatePosition));
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(holder.itemView.getContext(), "Failed to update task", Toast.LENGTH_SHORT).show();
                    task.setCompleted(!isChecked);
                    tasks.set(updatePosition, task);

                    holder.itemView.post(() -> notifyItemChanged(updatePosition));
                });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(ArrayList<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }
}
