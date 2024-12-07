package com.example.geominder.ui;








import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.view.ViewGroup;
import android.widget.Toast;








import androidx.appcompat.app.AppCompatActivity;








import com.example.geominder.R;
import com.example.geominder.services.LocationService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;








import java.util.ArrayList;








public class DecisionActivity extends AppCompatActivity {








    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListView taskListView;
    private ArrayList<Task> taskList; // List to hold Task objects








    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decision);








        // Initialize Firestore and FirebaseAuth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();








        // Initialize UI components
        taskListView = findViewById(R.id.taskListView);








        // Initialize the ArrayList for tasks
        taskList = new ArrayList<>();








        // Set up a custom adapter
        taskListView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return taskList.size();
            }








            @Override
            public Object getItem(int position) {
                return taskList.get(position);
            }








            @Override
            public long getItemId(int position) {
                return position;
            }








            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(DecisionActivity.this).inflate(R.layout.task_item, parent, false);
                }








                TextView taskTitleView = convertView.findViewById(R.id.taskTitle);
                RadioGroup radioGroup = convertView.findViewById(R.id.radioGroup);
                Button saveButton = convertView.findViewById(R.id.saveButton);








                Task task = taskList.get(position);  // Task object is already retrieved here
                taskTitleView.setText(task.getTaskTitle());










                radioGroup.setVisibility(View.GONE);
                saveButton.setVisibility(View.GONE);








                // Toggle visibility of the RadioGroup and Save button on task title click
                taskTitleView.setOnClickListener(v -> {
                    if (radioGroup.getVisibility() == View.GONE) {
                        radioGroup.setVisibility(View.VISIBLE);
                        saveButton.setVisibility(View.VISIBLE);
                    } else {
                        radioGroup.setVisibility(View.GONE);
                        saveButton.setVisibility(View.GONE);
                    }
                });








                saveButton.setOnClickListener(v -> {
                    int selectedId = radioGroup.getCheckedRadioButtonId();




                    if (selectedId == R.id.radioButton1) {
                        performActionForOption1(task);
                    } else if (selectedId == R.id.radioButton2) {
                        performActionForOption2(task);
                    } else if (selectedId == R.id.radioButton3) {
                        performActionForOption3(task);
                    } else if (selectedId == R.id.radioButton4) {
                        performActionForOption4(task);
                    } else {
                        Toast.makeText(DecisionActivity.this, "Please select an option", Toast.LENGTH_SHORT).show();
                        return; // Don't proceed if no option is selected
                    }




                    // Remove the task from the list
                    taskList.remove(position);




                    // Notify the adapter that the data set has changed
                    ((BaseAdapter) taskListView.getAdapter()).notifyDataSetChanged();




                    // Optionally show a confirmation message




                    // Check if the task list is empty
                    if (taskList.isEmpty()) {
                        // Navigate to the MainActivity
                        Intent intent = new Intent(DecisionActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish(); // Optionally finish the current activity to prevent going back
                    }
                });




                return convertView;
            }








            private void performActionForOption(Task task, String actionField, Object actionValue) {
                String userEmail = mAuth.getCurrentUser().getEmail();




                if (userEmail != null) {
                    // Step 2: Query the Firestore 'users' collection for the current user's email
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("users")
                            .whereEqualTo("email", userEmail)
                            .get()
                            .addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful() && !task1.getResult().isEmpty()) {
                                    // User found, now proceed to check the task in the 'tasks' subcollection
                                    DocumentSnapshot userDoc = task1.getResult().getDocuments().get(0); // Assuming only one match
                                    String userId = userDoc.getId();




                                    // Step 3: Look in the 'tasks' subcollection of the matched user
                                    db.collection("users")
                                            .document(userId)
                                            .collection("tasks")
                                            .get()
                                            .addOnCompleteListener(task2 -> {
                                                if (task2.isSuccessful()) {
                                                    boolean taskFound = false;




                                                    // Step 4: Iterate over tasks to find the one with the matching document ID
                                                    for (DocumentSnapshot taskDoc : task2.getResult()) {
                                                        if (taskDoc.getId().equals(task.getTaskId())) {
                                                            // Task ID matches the document ID
                                                            taskFound = true;




                                                            // Update the specified field with the provided value
                                                            taskDoc.getReference().update(actionField, actionValue)
                                                                    .addOnSuccessListener(aVoid -> {
                                                                        Log.d("Action", "Task updated for Task ID: " + task.getTaskId());
                                                                    })
                                                                    .addOnFailureListener(e -> {
                                                                        Log.e("Action", "Error updating task: ", e);
                                                                    });
                                                            break;
                                                        }
                                                    }




                                                    if (!taskFound) {
                                                        Log.d("Action", "No matching task found for Task ID: " + task.getTaskId());
                                                    }
                                                } else {
                                                    Log.e("Action", "Error retrieving tasks for user: ", task2.getException());
                                                }
                                            });
                                } else {
                                    Log.d("Action", "No user found with email: " + userEmail);
                                }
                            });
                } else {
                    Log.d("Action", "User is not logged in.");
                }
            }




            // Usage for Option 1
            private void performActionForOption1(Task task) {
                performActionForOption(task, "isCompleted", true);
            }




            // Usage for Option 2
            private void performActionForOption2(Task task) {
                performActionForOption(task, "isDeleted", true);
            }




            // Usage for Option 3
            private void performActionForOption3(Task task) {
                performActionForOption(task, "reminderInterval", "2 hours");
                performActionForOption(task, "ongoingDelay", false);
            }




            private void performActionForOption4(Task task) {
                performActionForOption(task, "notify", "off");
            }
        });








        // Cancel the geofence notification when this activity is opened
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(LocationService.GEOFENCE_NOTIFICATION_ID);
        }




        // Retrieve taskIds from the intent
        Intent intent = getIntent();
        ArrayList<String> taskIds = intent.getStringArrayListExtra("taskIds");








// Log the taskIds retrieved
        if (taskIds != null && !taskIds.isEmpty()) {
            Log.d("TaskIdsRetrieved", "Task IDs retrieved: " + taskIds.toString());
        } else {
            Log.d("TaskIdsRetrieved", "No Task IDs retrieved.");
        }
        retrieveTasksFromFirestore(taskIds);
    }








    private void retrieveTasksFromFirestore(ArrayList<String> taskIds) {
        String userEmail = mAuth.getCurrentUser().getEmail();
        if (userEmail == null) {
            Log.e("DecisionActivity", "User email is null");
            return;
        }








        db.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot documents = task.getResult();
                        if (documents != null && !documents.isEmpty()) {
                            String userId = documents.getDocuments().get(0).getId();
                            Log.d("Firestore", "User ID: " + userId);
                            retrieveUserTasks(userId, taskIds);
                        } else {
                            Log.e("Firestore", "No user found with the given email.");
                        }
                    } else {
                        Log.e("Firestore", "Error fetching user data: ", task.getException());
                    }
                });
    }








    private void retrieveUserTasks(String userId, ArrayList<String> taskIds) {
        CollectionReference tasksRef = db.collection("users").document(userId).collection("tasks");








        for (String taskId : taskIds) {
            tasksRef.document(taskId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        String taskTitle = task.getResult().getString("taskTitle");
                        if (taskTitle != null) {
                            taskList.add(new Task(taskId, taskTitle));  // Add Task object with ID and Title
                            ((BaseAdapter) taskListView.getAdapter()).notifyDataSetChanged();
                        }
                    } else {
                        Log.d("Firestore", "No task found for ID: " + taskId);
                    }
                } else {
                    Log.e("Firestore", "Error getting document: ", task.getException());
                }
            });
        }
    }








    // Task class to store both taskId and taskTitle
    public static class Task {
        private String taskId;
        private String taskTitle;








        public Task(String taskId, String taskTitle) {
            this.taskId = taskId;
            this.taskTitle = taskTitle;
        }








        public String getTaskId() {
            return taskId;
        }








        public String getTaskTitle() {
            return taskTitle;
        }
    }
}









