package com.example.firebasetodo.fragments;

import static androidx.core.util.TypedValueCompat.dpToPx;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
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
import java.util.Comparator;

public class UndoneTasksFragment extends Fragment {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private ArrayList<Task> tasks = new ArrayList<>();
    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("tasks");
    private ValueEventListener eventListener;

    private TextView statusTextView;

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
        statusTextView = view.findViewById(R.id.tv_status);

        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        taskViewModel.getSearchQuery().observe(getViewLifecycleOwner(), this::searchTask);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TaskAdapter(tasks);
        recyclerView.setAdapter(adapter);

        statusTextView.setText("Loading tasks...");
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

        setupRecyclerViewSwipe();

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

    private void setupRecyclerViewSwipe() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                // No move functionality here, return false
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Handle the swipe action here
                if (direction == ItemTouchHelper.LEFT) {
                    adapter.deleteTask(viewHolder);
                } else {
                    adapter.updateTask(viewHolder, true);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    View itemView = viewHolder.itemView; // Get the swiped item's view
                    Paint paint = new Paint();          // Set paint color to red

                    float top = (float) itemView.getTop() + dpToPx(6f, getResources().getDisplayMetrics());
                    float bottom = (float) itemView.getBottom() - dpToPx(6f, getResources().getDisplayMetrics());


                    if (dX < 0) { // Swiping left
                        paint.setColor(Color.RED);
                        c.drawRect((float) itemView.getRight() + dX, top, (float) itemView.getRight(), bottom, paint);
                    } else { // Swiping right
                        paint.setColor(Color.GREEN);
                        c.drawRect((float) itemView.getLeft(), top,(float) itemView.getLeft() + dX, bottom, paint);
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        // Attach the ItemTouchHelper to the RecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
}