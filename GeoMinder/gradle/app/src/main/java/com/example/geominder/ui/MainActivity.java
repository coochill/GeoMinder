package com.example.geominder.ui;




import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;




import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationManagerCompat;




import com.example.geominder.R;
import com.example.geominder.services.GeofenceBroadcastReceiver;
import com.example.geominder.services.LocationService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;




import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.ToggleButton;
import com.example.geominder.auth.LoginActivity;



import androidx.core.app.ActivityCompat;




import com.google.android.gms.location.GeofencingClient;
import android.os.Handler;
import android.os.Looper;




public class MainActivity extends AppCompatActivity {




    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ListView taskListView;
    private ArrayList<String[]> taskTitles; // List to hold task titles
    private Spinner categorySpinner;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;
    private static final int FOREGROUND_LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 1002;
    private boolean isRedirectedToSettingsPermission = false;
    private boolean isGPSenabled = false;
    private boolean isRedirectedToNotifSettings = false;
    private List<Map<String, Object>> taskLocationDataList = new ArrayList<>();
    private ImageButton logoutButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();




// Find the logout button
        logoutButton = findViewById(R.id.logout_button);


// Set a click listener on the logout button
        logoutButton.setOnClickListener(view -> {
            // Log the user out
            mAuth.signOut();


            // Redirect to the login activity (or wherever you want)
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);  // Replace LoginActivity with your login screen
            startActivity(intent);
            finish();  // Optional: finish the current activity to prevent going back
        });


        // Initialize the ListView and ArrayList
        // Initialize the BottomNavigationView
        BottomNavigationView navView = findViewById(R.id.nav_view);


// Set click listeners for each menu item in BottomNavigationView
        navView.findViewById(R.id.main).setOnClickListener(v -> {
            // Set the selected color to green for the Main item
            MenuItem mainItem = navView.getMenu().findItem(R.id.main);
            mainItem.setIconTintList(ColorStateList.valueOf(Color.GREEN));


            // Navigate to Main Page
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
        });






        navView.findViewById(R.id.navigation_history).setOnClickListener(v -> {
            // Navigate to History Page
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });


        taskListView = findViewById(R.id.taskListView);
        taskTitles = new ArrayList<String[]>();  // List of String arrays




        FloatingActionButton navigationInput = findViewById(R.id.navigation_input);
        categorySpinner = findViewById(R.id.categorySpinner);




        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geofencingClient = LocationServices.getGeofencingClient(this);
        geofencePendingIntent = null; // Initialized when needed




        // Inside the onCreate() method, after populating the spinner
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Retrieve the list of category document IDs stored in the Spinner's tag
                ArrayList<String> categoryDocIds = (ArrayList<String>) categorySpinner.getTag();
                // Get the selected category's document ID
                String selectedCategoryId = position == 0 ? null : categoryDocIds.get(position - 1);




                // Filter tasks based on the selected category
                filterTasksByCategory(selectedCategoryId);
            }




            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Optional: Handle cases where no category is selected
            }
        });




        navigationInput.setOnClickListener(v -> {
            // Check if GPS is enabled
            if (!isGPSEnabled()) {
                // Show an AlertDialog asking the user if they want to enable GPS
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("GPS is off")
                        .setMessage("Your GPS is turned off. Do you want to go to settings and turn it on?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Open the location settings to enable GPS
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Display a message if the user chooses not to turn on GPS
                                Toast.makeText(MainActivity.this, "You need to turn on your GPS to proceed", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .create()
                        .show();
            } else {
                // GPS is enabled, start InputActivity
                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                startActivity(intent);
            }
        });




        View rootView = findViewById(android.R.id.content);
        rootView.setBackgroundColor(getResources().getColor(android.R.color.white));




        navView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.main) {
                Toast.makeText(this, "Home Selected", Toast.LENGTH_SHORT).show();
                return true;
            } else if (item.getItemId() == R.id.navigation_history) {
                Toast.makeText(this, "Notifications Selected", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });








// Define colors for selected/unselected states programmatically
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked}, // Selected state
                new int[]{-android.R.attr.state_checked} // Unselected state
        };




        int[] colors = new int[]{
                getResources().getColor(R.color.darkgreen),  // Color for selected
                getResources().getColor(com.google.android.libraries.places.R.color.quantum_grey) // Color for unselected
        };




        ColorStateList colorStateList = new ColorStateList(states, colors);
        navView.setItemIconTintList(colorStateList);
        navView.setItemTextColor(colorStateList);




// Set ListView item click listener
        taskListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedTaskId = taskTitles.get(position)[0];
            String selectedTaskTitle = taskTitles.get(position)[1];
            showPopupContainer(selectedTaskId, selectedTaskTitle);
        });




        // Retrieve tasks for the signed-in user
        populateSpinner();
        getUsernameAndUpdateTextView();
    }








    private void showPopupContainer(String taskId, String taskTitle) {
        // Inflate the custom layout for the popup
        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.popup_container, null);




        // Get references to the UI elements
        TextView taskDetailsTextView = popupView.findViewById(R.id.taskDetailsTextView);
        Button editButton = popupView.findViewById(R.id.editButton);
        Button deleteButton = popupView.findViewById(R.id.deleteButton);
        SwitchCompat notifySwitch = popupView.findViewById(R.id.notifySwitch);




        // Retrieve task details from Firestore
        if (mAuth.getCurrentUser() != null) {
            String userEmail = mAuth.getCurrentUser().getEmail();
            db.collection("users")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                            String userId = task.getResult().getDocuments().get(0).getId();




                            // Access the specific task document using taskId
                            db.collection("users")
                                    .document(userId)
                                    .collection("tasks")
                                    .document(taskId)
                                    .get()
                                    .addOnCompleteListener(taskDetails -> {
                                        if (taskDetails.isSuccessful() && taskDetails.getResult() != null) {
                                            DocumentSnapshot taskSnapshot = taskDetails.getResult();




                                            // Retrieve fields from Firestore
                                            String placeAddress = taskSnapshot.getString("placeAddress");
                                            String placeName = taskSnapshot.getString("placeName");




                                            // Set the initial state based on Firestore value
                                            String notify = taskSnapshot.getString("notify");
                                            Double latitude = taskSnapshot.getDouble("latitude");
                                            Double longitude = taskSnapshot.getDouble("longitude");
                                            String category = taskSnapshot.getString("category");
                                            String reminderInterval = taskSnapshot.getString("reminderInterval");




                                            // Display the other details in taskDetailsTextView
                                            if (taskDetailsTextView != null) {
                                                String details = "Place Address: " + placeAddress + "\n" +
                                                        "Place Name: " + placeName + "\n";
                                                taskDetailsTextView.setText(details);
                                            }


                                            notifySwitch.setChecked(notify != null && notify.equalsIgnoreCase("on"));


                                            notifySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                                                String newNotify = isChecked ? "on" : "off";
                                                taskSnapshot.getReference().update("notify", newNotify)
                                                        .addOnSuccessListener(aVoid -> {
                                                            Toast.makeText(MainActivity.this, "Notify updated to: " + newNotify, Toast.LENGTH_SHORT).show();
                                                            finish(); // Close the current activity
                                                            startActivity(getIntent()); // Restart the activity to refresh
                                                        })
                                                        .addOnFailureListener(e ->
                                                                Toast.makeText(MainActivity.this, "Failed to update notify", Toast.LENGTH_SHORT).show());
                                            });


                                            // Set up click listener for edit button after data is fetched
                                            editButton.setOnClickListener(v -> {
                                                // Create an Intent to navigate to the InputActivity
                                                Intent intent = new Intent(MainActivity.this, InputActivity.class);


                                                // Pass the task details as extras
                                                intent.putExtra("taskId", taskId); // Include taskId to update Firestore later
                                                intent.putExtra("taskTitle",taskTitle);
                                                intent.putExtra("placeAddress", placeAddress);
                                                intent.putExtra("placeName", placeName);
                                                intent.putExtra("category", category);
                                                intent.putExtra("latitude", latitude);
                                                intent.putExtra("longitude", longitude);


                                                // Start the InputActivity
                                                startActivity(intent);
                                            });




                                        } else {
                                            Log.e("TaskDetails", "Failed to retrieve task details", taskDetails.getException());
                                        }
                                    });
                        } else {
                            Log.e("UserDetails", "User not found or failed to retrieve user details", task.getException());
                        }
                    });
        }




        // Set up click listener for delete button
        deleteButton.setOnClickListener(v ->
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete Task")
                        .setMessage("Are you sure you want to delete this task?")
                        .setPositiveButton("Yes", (dialog, which) -> deleteTask(taskId))
                        .setNegativeButton("No", null)
                        .show()
        );








        // Create and show the AlertDialog with the inflated layout
        new AlertDialog.Builder(this)
                .setView(popupView)
                .setCancelable(true)
                .show();
    }


    private void getUsernameAndUpdateTextView() {
        if (mAuth.getCurrentUser() != null) {
            String userEmail = mAuth.getCurrentUser().getEmail();
            db.collection("users")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                            // Get the document ID (username)
                            String userId = task.getResult().getDocuments().get(0).getId();


                            // Now you have the userId (username) from the document ID
                            Log.d("Username", "User's document ID (username): " + userId);


                            // Use the userId for further operations, like accessing specific documents
                            db.collection("users")
                                    .document(userId)
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            // Access the 'username' field from the document
                                            String username = documentSnapshot.getString("username");
                                            Log.d("Username", "Username from document: " + username);


                                            // Update the TextView with "Hi, username!"
                                            TextView userNameTextView = findViewById(R.id.user_name);
                                            userNameTextView.setText("Hi, " + username + "!");
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Error", "Error fetching user document: " + e.getMessage());
                                    });
                        } else {
                            Log.e("Error", "No user found with the given email.");
                        }
                    });
        } else {
            Log.e("Error", "User is not authenticated.");
        }
    }


    private void deleteTask(String taskId) {
        if (mAuth.getCurrentUser() != null) {
            String userEmail = mAuth.getCurrentUser().getEmail();
            db.collection("users")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                            String userId = task.getResult().getDocuments().get(0).getId();
                            // Directly use the taskId as the document ID to update the task
                            db.collection("users")
                                    .document(userId)
                                    .collection("tasks")
                                    .document(taskId) // Use the taskId directly here
                                    .update("isDeleted", true) // Update the isDeleted field to true
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(MainActivity.this, "Task marked as deleted", Toast.LENGTH_SHORT).show();
                                        // Reload or refresh the activity to update the task list
                                        finish(); // Close the current activity
                                        startActivity(getIntent()); // Restart the activity to refresh
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(MainActivity.this, "Failed to mark task as deleted", Toast.LENGTH_SHORT).show());
                        }
                    });
        }
    }




    private void checkAndPromptGPS() {
        // Check if GPS is enabled
        if (!isGPSEnabled()) {
            // Show AlertDialog to prompt the user
            new AlertDialog.Builder(this)
                    .setTitle("GPS is off")
                    .setMessage("Your GPS is turned off. Do you want to go to settings and turn it on?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Open location settings
                            isGPSenabled = true;
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Notify the user about GPS requirement
                            Toast.makeText(MainActivity.this, "You need to turn on your GPS to proceed.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .create()
                    .show();
        } else {
            checkAndEnableNotifications();
        }
    }




    private boolean isGPSEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }








    @Override
    protected void onResume() {
        super.onResume();
        if (isGPSenabled) {
            checkAndPromptGPS();
            isGPSenabled = false;
        }
        if (isRedirectedToNotifSettings) {
            checkAndEnableNotifications();
            isRedirectedToNotifSettings = false;
        }
        // If the user has been redirected to settings, check permissions again and reset the flag
        if (isRedirectedToSettingsPermission) {
            checkAndPromptLocationPermission();
            isRedirectedToSettingsPermission = false; // Reset flag after checking permissions
        }
    }








    private void showPermissionDeniedDialog() {
        // Create an AlertDialog to prompt user to go to settings
        new AlertDialog.Builder(this)
                .setTitle("Location Permission Required")
                .setMessage("Location permission is required to access location services. Please click 'Allow all the time' in the settings.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Set flag to indicate user has been redirected to settings
                    isRedirectedToSettingsPermission = true;




                    // Open the app settings
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // When No is clicked, show the location permission prompt again
                    checkAndPromptLocationPermission();
                })
                .show();
    }








    private void filterTasksByCategory(String selectedCategoryId) {
        // Ensure the user is signed in
        if (mAuth.getCurrentUser() != null) {
            String userEmail = mAuth.getCurrentUser().getEmail();




            // Query the user's document
            db.collection("users")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                // Assuming one user document for the email
                                String userId = querySnapshot.getDocuments().get(0).getId();




                                // Query the user's tasks subcollection
                                db.collection("users")
                                        .document(userId)
                                        .collection("tasks")
                                        .get()
                                        .addOnCompleteListener(tasksTask -> {
                                            if (tasksTask.isSuccessful()) {
                                                QuerySnapshot tasksSnapshot = tasksTask.getResult();
                                                if (tasksSnapshot != null && !tasksSnapshot.isEmpty()) {
                                                    taskTitles.clear(); // Clear previous data
                                                    for (QueryDocumentSnapshot document : tasksSnapshot) {
                                                        Boolean isCompleted = document.getBoolean("isCompleted");
                                                        Boolean isDeleted = document.getBoolean("isDeleted");
                                                        String notify = document.getString("notify");
                                                        String reminderInterval = document.getString("reminderInterval");
                                                        String category = document.getString("category"); // Category ID in tasks
                                                        String taskTitle = document.getString("taskTitle");
                                                        String taskId = document.getId();
                                                        double latitude = document.getDouble("latitude");
                                                        double longitude = document.getDouble("longitude");
                                                        Float radius = document.getDouble("radius") != null ? document.getDouble("radius").floatValue() : 100f;
                                                        Map<String, Object> locationData = new HashMap<>();
                                                        locationData.put("reminderInterval", reminderInterval);
                                                        locationData.put("taskTitle", taskTitle);
                                                        locationData.put("taskId", taskId);
                                                        locationData.put("latitude", latitude);
                                                        locationData.put("longitude", longitude);
                                                        locationData.put("radius", radius);




                                                        // Check if task matches selected category and is not completed
                                                        if (!Boolean.TRUE.equals(isCompleted) && !Boolean.TRUE.equals(isDeleted) &&
                                                                (selectedCategoryId == null || selectedCategoryId.equals(category))) {




                                                            if (taskTitle != null) {
                                                                taskTitles.add(new String[]{taskId, taskTitle}); // Add task title
                                                            }




                                                            if ("on".equals(notify) && locationData != null) {
                                                                taskLocationDataList.add(locationData); // Add task location data only if notify is true
                                                            }
                                                        }




                                                    }
                                                    checkAndPromptGPS();
                                                    // Update ListView
                                                    if (!taskTitles.isEmpty()) {
                                                        TaskAdapter adapter = new TaskAdapter(MainActivity.this, R.layout.task_card, taskTitles);
                                                        taskListView.setAdapter(adapter);
                                                        taskListView.setVisibility(View.VISIBLE);
                                                    } else {
                                                        taskListView.setVisibility(View.GONE); // Hide if no tasks
                                                    }
                                                } else {
                                                    taskListView.setVisibility(View.GONE); // Hide if no tasks
                                                }
                                            } else {
                                                taskListView.setVisibility(View.GONE); // Hide if failed
                                            }
                                        });
                            } else {
                                taskListView.setVisibility(View.GONE); // Hide if no user found
                            }
                        } else {
                            taskListView.setVisibility(View.GONE); // Hide if failed to load user
                        }
                    });
        } else {
            Toast.makeText(MainActivity.this, "User not signed in", Toast.LENGTH_SHORT).show();
        }
    }




    // Create a custom adapter class for the task list
    public class TaskAdapter extends ArrayAdapter<String[]> {
        private Context context;
        private ArrayList<String[]> taskTitles;




        public TaskAdapter(Context context, int resource, ArrayList<String[]> taskTitles) {
            super(context, resource, taskTitles);
            this.context = context;
            this.taskTitles = taskTitles;
        }




        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Reuse the view if it's already inflated
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.task_card, parent, false);
            }




            // Get the task data (taskId and taskTitle)
            String[] task = taskTitles.get(position);
            String taskId = task[0];  // taskId is at index 0
            String taskTitle = task[1];  // taskTitle is at index 1




            // Set the task title in the TextView
            TextView taskTitleTextView = convertView.findViewById(R.id.taskTitle);
            taskTitleTextView.setText(taskTitle);




            return convertView;
        }
    }








    private void checkAndPromptLocationPermission() {
        // Check if foreground location is granted
        boolean isForegroundLocationGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;








        // Check if background location is granted
        boolean isBackgroundLocationGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;




        // If neither foreground nor background location permissions are granted, show the dialog
        if (!isForegroundLocationGranted && !isBackgroundLocationGranted) {
            showPermissionDeniedDialog();
        } else if (isForegroundLocationGranted && !isBackgroundLocationGranted) {
            // If foreground permission is granted but background location permission is not, show the dialog
            showPermissionDeniedDialog();
        } else {
            // If both foreground and background location permissions are granted, start the LocationService
            Intent serviceIntent = new Intent(this, LocationService.class);
            startService(serviceIntent);
            Log.d("LocationPermission", "Permissions granted. Starting LocationService.");




            // Create a list to hold all the geofences
            List<Geofence> geofences = new ArrayList<>();




            if (taskLocationDataList == null || taskLocationDataList.isEmpty()) {
                Log.d("Geofence", "No task locations available. Skipping geofence setup.");
                return;
            }
            for (Map<String, Object> locationData : taskLocationDataList) {
                String taskTitle = (String) locationData.get("taskTitle");
                String taskId = (String) locationData.get("taskId");
                String reminderInterval = (String) locationData.get("reminderInterval");
                double latitude = (double) locationData.get("latitude");
                double longitude = (double) locationData.get("longitude");
                float radius = ((Number) locationData.get("radius")).floatValue();


                if ("none".equals(reminderInterval)) {
                    // Create geofence immediately for tasks with no reminder interval
                    Geofence geofence = createGeofence(taskTitle, taskId, latitude, longitude, radius, reminderInterval);
                    geofences.add(geofence);
                } else if ("2 hours".equals(reminderInterval)) {
                    // Handle tasks with a 2-hour reminder interval
                    if (mAuth.getCurrentUser() != null) {
                        String userEmail = mAuth.getCurrentUser().getEmail();
                        db.collection("users")
                                .whereEqualTo("email", userEmail)
                                .get()
                                .addOnCompleteListener(userTask -> {
                                    if (userTask.isSuccessful() && userTask.getResult() != null && !userTask.getResult().isEmpty()) {
                                        String userId = userTask.getResult().getDocuments().get(0).getId();


                                        // Retrieve task details
                                        db.collection("users")
                                                .document(userId)
                                                .collection("tasks")
                                                .document(taskId)
                                                .get()
                                                .addOnCompleteListener(taskSnapshot -> {
                                                    if (taskSnapshot.isSuccessful() && taskSnapshot.getResult() != null) {
                                                        Map<String, Object> taskData = taskSnapshot.getResult().getData();
                                                        if (taskData != null) {
                                                            long currentTime = System.currentTimeMillis();
                                                            long lastNotificationTime = taskData.containsKey("lastNotificationTime")
                                                                    ? (long) taskData.get("lastNotificationTime") : 0;


                                                            if (currentTime - lastNotificationTime > 2 * 60 * 1000) { // 2 minutes
                                                                // Create the geofence if sufficient time has passed
                                                                Geofence geofence = createGeofence(taskTitle, taskId, latitude, longitude, radius, reminderInterval);
                                                                geofences.add(geofence);


                                                                // Update reminderInterval to "none" in Firestore
                                                                db.collection("users")
                                                                        .document(userId)
                                                                        .collection("tasks")
                                                                        .document(taskId)
                                                                        .update("reminderInterval", "none")
                                                                        .addOnSuccessListener(aVoid ->
                                                                                Log.d("Firestore", "Updated reminder interval to 'none' for task: " + taskTitle))
                                                                        .addOnFailureListener(e ->
                                                                                Log.e("Firestore", "Failed to update reminder interval for task: " + taskTitle, e));
                                                            } else {
                                                                // Handle ongoing delay or schedule delayed geofencing setup
                                                                boolean ongoingDelay = taskData.containsKey("ongoingDelay") &&
                                                                        Boolean.TRUE.equals(taskData.get("ongoingDelay"));


                                                                if (ongoingDelay) {
                                                                    Log.d("Geofence", "Ongoing delay for task: " + taskTitle);
                                                                } else {
                                                                    Log.d("Interval", "Too soon to set geofence for task: " + taskTitle);


                                                                    db.collection("users")
                                                                            .document(userId)
                                                                            .collection("tasks")
                                                                            .document(taskId)
                                                                            .update("ongoingDelay", true)
                                                                            .addOnSuccessListener(aVoid ->
                                                                                    Log.d("Firestore", "Updated reminder interval to 'none' for task: " + taskTitle))
                                                                            .addOnFailureListener(e ->
                                                                                    Log.e("Firestore", "Failed to update reminder interval for task: " + taskTitle, e));


                                                                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                                                        Geofence geofence = createGeofence(taskTitle, taskId, latitude, longitude, radius, reminderInterval);
                                                                        geofences.add(geofence);


                                                                        // Add geofences after the delay
                                                                        addGeofences(geofences);


                                                                        db.collection("users")
                                                                                .document(userId)
                                                                                .collection("tasks")
                                                                                .document(taskId)
                                                                                .update("reminderInterval", "none")
                                                                                .addOnSuccessListener(aVoid ->
                                                                                        Log.d("Firestore", "Updated reminder interval to 'none' for task: " + taskTitle))
                                                                                .addOnFailureListener(e ->
                                                                                        Log.e("Firestore", "Failed to update reminder interval for task: " + taskTitle, e));


                                                                        db.collection("users")
                                                                                .document(userId)
                                                                                .collection("tasks")
                                                                                .document(taskId)
                                                                                .update("ongoingDelay", false)
                                                                                .addOnSuccessListener(aVoid ->
                                                                                        Log.d("Firestore", "Updated reminder interval to 'none' for task: " + taskTitle))
                                                                                .addOnFailureListener(e ->
                                                                                        Log.e("Firestore", "Failed to update reminder interval for task: " + taskTitle, e));
                                                                    }, 2 * 60 * 1000);
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        Log.e("Firestore", "Failed to fetch task data for taskId: " + taskId);
                                                    }
                                                })
                                                .addOnFailureListener(e -> Log.e("Firestore", "Error retrieving task document for taskId: " + taskId, e));
                                    } else {
                                        Log.e("Firestore", "Failed to fetch user data for email: " + userEmail);
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("Firestore", "Error querying user by email: " + userEmail, e));
                    } else {
                        Log.e("Auth", "User not authenticated.");
                    }
                }
            }


// Add all geofences in a single batch
            if (!geofences.isEmpty()) {
                addGeofences(geofences);
            } else {
                Log.d("Geofence", "No geofences to add.");
            }


        }
    }




    private void addGeofences(List<Geofence> geofences) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }




        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofences)
                .build();




        geofencingClient.addGeofences(geofencingRequest, getGeofencePendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("Geofence", "Geofences added successfully.");
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Geofence", "Failed to add geofences: " + e.getMessage());
                    }
                });
    }




    public Geofence createGeofence(String taskTitle, String taskId, double latitude, double longitude, float radius, String reminderInterval) {
        Log.d("Geofence", "Creating geofence for task: " + taskTitle);


        // Combine taskId, taskTitle, and reminderInterval in the requestId
        String requestId = taskId + "::" + taskTitle + "::" + reminderInterval;


        return new Geofence.Builder()
                .setRequestId(requestId)
                .setCircularRegion(latitude, longitude, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER) // Only enter transition
                .build();
    }






    private PendingIntent getGeofencePendingIntent() {
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        return geofencePendingIntent;
    }




    private void populateSpinner() {
        // Reference to the placetypes collection in Firestore
        db.collection("placetypes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<String> categoryNames = new ArrayList<>();
                        final ArrayList<String> categoryDocIds = new ArrayList<>();




                        categoryNames.add("All");




                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String categoryName = document.getString("name");
                            if (categoryName != null) {
                                categoryNames.add(categoryName);
                                categoryDocIds.add(document.getId());
                            }
                        }




                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                                android.R.layout.simple_spinner_item, categoryNames) {
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                View view = super.getView(position, convertView, parent);
                                TextView text = (TextView) view;
                                // Set the text color dynamically to black
                                text.setTextColor(Color.BLACK);
                                return view;
                            }




                            @Override
                            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                                View view = super.getDropDownView(position, convertView, parent);
                                TextView text = (TextView) view;
                                // Set the text color dynamically to black for the dropdown items
                                text.setTextColor(Color.BLACK);
                                return view;
                            }
                        };
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        categorySpinner.setAdapter(adapter);




                        // Store the document IDs as a tag to reference later
                        categorySpinner.setTag(categoryDocIds);
                    }
                });
    }




    private void checkAndEnableNotifications() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);




        // Check if notifications are enabled
        if (!notificationManager.areNotificationsEnabled()) {
            // Show alert dialog to prompt user to enable notifications
            new AlertDialog.Builder(this)
                    .setTitle("Enable Notifications")
                    .setMessage("This app requires notifications to be enabled. Would you like to enable them now?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        isRedirectedToNotifSettings = true;
                        // Open app notification settings
                        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                        startActivity(intent);
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // Show the dialog again
                        checkAndEnableNotifications();
                    })
                    .setCancelable(false) // Prevent the dialog from being dismissed without action
                    .show();
        } else {
            // Notifications are enabled, proceed to check location permission
            checkAndPromptLocationPermission();
        }
    }




}









