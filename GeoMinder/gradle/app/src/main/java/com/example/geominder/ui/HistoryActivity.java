package com.example.geominder.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.geominder.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListView historyListView;
    private Spinner filterSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        historyListView = findViewById(R.id.history_list_view);
        filterSpinner = findViewById(R.id.filter_spinner);

        // Create an array of strings for the Spinner options
        String[] filterOptions = {"Completed", "Deleted"};

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filterOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(adapter);

        // Fetch tasks when spinner item changes
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Access the selected item directly from the filterOptions array
                String filterOption = filterOptions[position];  // Use the position to get the correct option
                fetchTasksBasedOnFilter(filterOption);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle the case where nothing is selected (optional)
            }
        });


    }

    private void fetchTasksBasedOnFilter(String filterOption) {
        // Get the current user
        if (mAuth.getCurrentUser() != null) {
            String userEmail = mAuth.getCurrentUser().getEmail();
            db.collection("users")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                            String userId = task.getResult().getDocuments().get(0).getId();

                            // Access the tasks collection
                            CollectionReference tasksRef = db.collection("users")
                                    .document(userId)
                                    .collection("tasks");

                            Query query;

                            // Filter tasks based on selected option
                            if (filterOption.equals("Completed")) {
                                query = tasksRef.whereEqualTo("isCompleted", true);
                            } else if (filterOption.equals("Deleted")) {
                                query = tasksRef.whereEqualTo("isDeleted", true);
                            } else { // "All"
                                query = tasksRef.whereEqualTo("isCompleted", true)
                                        .whereEqualTo("isDeleted", true);
                            }

                            // Fetch tasks from Firestore
                            query.get().addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    QuerySnapshot querySnapshot = task1.getResult();
                                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                        ArrayList<String> taskTitles = new ArrayList<>();
                                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                            Task taskItem = document.toObject(Task.class);  // Renamed to taskItem
                                            if (taskItem != null) {
                                                taskTitles.add(taskItem.getTaskTitle());
                                            }
                                        }
                                        // Display the task titles in the ListView
                                        ArrayAdapter<String> taskAdapter = new ArrayAdapter<>(this,
                                                android.R.layout.simple_list_item_1, taskTitles);
                                        historyListView.setAdapter(taskAdapter);
                                    } else {
                                        Toast.makeText(this, "No tasks found.", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(this, "Error fetching tasks.", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    });
        }
    }

    // Task model
    public static class Task {
        private String taskTitle;
        private boolean isCompleted;
        private boolean isDeleted;

        public Task() {
            // Default constructor required for Firestore
        }

        public Task(String taskTitle, boolean isCompleted, boolean isDeleted) {
            this.taskTitle = taskTitle;
            this.isCompleted = isCompleted;
            this.isDeleted = isDeleted;
        }

        public String getTaskTitle() {
            return taskTitle;
        }

        public boolean isCompleted() {
            return isCompleted;
        }

        public boolean isDeleted() {
            return isDeleted;
        }
    }
}