package com.example.firebasetodo.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;

public class AllTasksFragment extends Fragment {

    private ListView listView;
    private TaskListViewAdapter adapter;
    private ArrayList<Task> tasks = new ArrayList<>();
    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("tasks");
    private ValueEventListener eventListener;

    private TextView statusTextView;

    TaskViewModel taskViewModel;

    public AllTasksFragment() {
        // Required empty public constructor
    }

    public static AllTasksFragment newInstance() {
        return new AllTasksFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_tasks, container, false);

        listView = view.findViewById(R.id.lv_all_tasks);
        statusTextView = view.findViewById(R.id.tv_status);

        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        taskViewModel.getSearchQuery().observe(getViewLifecycleOwner(), this::searchTask);

        adapter = new TaskListViewAdapter(tasks);
        listView.setAdapter(adapter);

        statusTextView.setText("Loading tasks...");
        eventListener = dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tasks.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Task task = dataSnapshot.getValue(Task.class);
                    if (task != null) task.setKey(dataSnapshot.getKey());
                    tasks.add(task);
                }
                tasks.sort(Comparator.comparing(Task::getCreatedAt).reversed());
                adapter.notifyDataSetChanged();
                if(tasks.isEmpty()) {
                    statusTextView.setText("No tasks found");
                } else {
                    statusTextView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Failed to read tasks " + error.getMessage(), Toast.LENGTH_SHORT).show();
                statusTextView.setText("Failed to read tasks");
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