package com.example.firebasetodo.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.firebasetodo.R;
import com.example.firebasetodo.Task;
import com.example.firebasetodo.TaskAdapter;
import com.example.firebasetodo.TaskViewModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UndoneTasksFragment extends Fragment {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private ArrayList<Task> tasks = new ArrayList<>();
    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("tasks");
    private ValueEventListener eventListener;

    TaskViewModel taskViewModel;

    public UndoneTasksFragment() {
        // Required empty public constructor
    }

    public static UndoneTasksFragment newInstance() {
        return new UndoneTasksFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_undone_tasks, container, false);

        recyclerView = view.findViewById(R.id.rv_pending_tasks);

        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        taskViewModel.getSearchQuery().observe(getViewLifecycleOwner(), this::searchTask);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TaskAdapter(tasks);
        recyclerView.setAdapter(adapter);

        eventListener = dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tasks.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Task task = dataSnapshot.getValue(Task.class);
                    if (task != null && !task.isCompleted()) {
                        task.setKey(dataSnapshot.getKey());
                        tasks.add(task);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to read tasks " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (eventListener != null) {
            dbRef.removeEventListener(eventListener);
        }
    }

    private void searchTask(String query) {
        ArrayList<Task> filteredTasks = new ArrayList<>();
        for (Task task : tasks) {
            if (task.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    task.getDescription().toLowerCase().contains(query.toLowerCase())) {
                filteredTasks.add(task);
            }
        }
        adapter.setTasks(filteredTasks);
    }
}