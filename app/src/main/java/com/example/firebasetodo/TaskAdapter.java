package com.example.firebasetodo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
            int updatePosition = holder.getBindingAdapterPosition();
            Task task = tasks.get(updatePosition);
            task.setCompleted(isChecked);
            tasks.set(updatePosition, task);

            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("tasks");

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
        });

        holder.deleteButton.setOnClickListener(v -> {
            int deletePosition = holder.getBindingAdapterPosition();
            Task task = tasks.get(deletePosition);

            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("tasks");

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
