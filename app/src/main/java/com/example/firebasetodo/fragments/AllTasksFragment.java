package com.example.firebasetodo.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firebasetodo.R;
import com.example.firebasetodo.Task;
import com.example.firebasetodo.TaskAdapter;
import com.example.firebasetodo.TaskListViewAdapter;
import com.example.firebasetodo.TaskViewModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;

public class AllTasksFragment extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<Task, TaskViewHolder> adapter;
    private DatabaseReference dbRef;

    public AllTasksFragment() {
        // Required empty public constructor
    }

    public static AllTasksFragment newInstance() {
        return new AllTasksFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_tasks, container, false);

        recyclerView = view.findViewById(R.id.rv_all_tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Firebase Database reference
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef = FirebaseDatabase.getInstance().getReference("tasks").child(uid);

        // FirebaseRecyclerOptions
        FirebaseRecyclerOptions<Task> options = new FirebaseRecyclerOptions.Builder<Task>()
                .setQuery(dbRef, Task.class)
                .build();

        // FirebaseRecyclerAdapter
        adapter = new FirebaseRecyclerAdapter<Task, TaskViewHolder>(options) {
            @NonNull
            @Override
            public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.task_item, parent, false);
                return new TaskViewHolder(itemView);
            }

            @Override
            protected void onBindViewHolder(@NonNull TaskViewHolder holder, int position, @NonNull Task model) {
                holder.bind(model, dbRef.child(getRef(position).getKey()));
            }
        };

        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    // ViewHolder class for RecyclerView
    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView descriptionTextView;
        private final CheckBox checkBox;
        private final ImageView deleteButton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.tv_title);
            descriptionTextView = itemView.findViewById(R.id.tv_desc);
            checkBox = itemView.findViewById(R.id.cb_done);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(Task task, DatabaseReference taskRef) {
            titleTextView.setText(task.getTitle());
            descriptionTextView.setText(task.getDescription());
            checkBox.setChecked(task.isCompleted());

            // Delete button visibility
            deleteButton.setVisibility(task.isCompleted() ? View.VISIBLE : View.GONE);

            // Update task completion
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                task.setCompleted(isChecked);
                taskRef.setValue(task).addOnFailureListener(e ->
                        Toast.makeText(itemView.getContext(), "Failed to update task", Toast.LENGTH_SHORT).show());
            });

            // Delete task
            deleteButton.setOnClickListener(v -> new AlertDialog.Builder(itemView.getContext())
                    .setMessage("Are you sure you want to delete this task?")
                    .setPositiveButton("Yes", (dialog, which) -> taskRef.removeValue()
                            .addOnFailureListener(e ->
                                    Toast.makeText(itemView.getContext(), "Failed to delete task", Toast.LENGTH_SHORT).show()))
                    .setNegativeButton("Cancel", null)
                    .show());
        }
    }
}
