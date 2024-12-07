package com.example.geominder.services;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class GeofenceBroadcastReceiver extends BroadcastReceiver {


    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();


    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e("GeofenceReceiver", "Error in geofence transition: " + geofencingEvent.getErrorCode());
            return;
        }


        int transition = geofencingEvent.getGeofenceTransition();
        if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Set<String> taskIds = new HashSet<>();
            List<String> reminderIntervals = new ArrayList<>();


            // Loop through each geofence that triggered the event
            for (Geofence geofence : geofencingEvent.getTriggeringGeofences()) {
                String requestId = geofence.getRequestId();
                String[] parts = requestId.split("::"); // Split the ID into taskId, taskTitle, and reminderInterval


                if (parts.length >= 3) {
                    String taskId = parts[0];  // Extract taskId
                    String reminderInterval = parts[2]; // Extract reminderInterval


                    taskIds.add(taskId);  // Add the taskId to the set (ensures unique task IDs)
                    reminderIntervals.add(reminderInterval); // Collect reminder intervals


                    Log.d("GeofenceReceiver", "Geofence transition for ID: " + taskId + " with interval: " + reminderInterval);
                } else {
                    Log.w("GeofenceReceiver", "Malformed requestId: " + requestId);
                }
            }


            // Convert the Set to a list and pass it to the service
            Intent serviceIntent = new Intent(context, LocationService.class);
            serviceIntent.putStringArrayListExtra("taskIds", new ArrayList<>(taskIds));  // Pass unique taskIds
            serviceIntent.putStringArrayListExtra("reminderIntervals", new ArrayList<>(reminderIntervals)); // Pass reminder intervals
            serviceIntent.putExtra("transition", transition);
            context.startService(serviceIntent);  // Start service to show notification


            // Update Firestore with the current notification time
            if (mAuth.getCurrentUser() != null) {
                String userEmail = mAuth.getCurrentUser().getEmail();
                db.collection("users")
                        .whereEqualTo("email", userEmail)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                                String userId = task.getResult().getDocuments().get(0).getId();


                                for (String taskId : taskIds) {
                                    // Access the specific task document using taskId
                                    db.collection("users")
                                            .document(userId)
                                            .collection("tasks")
                                            .document(taskId)
                                            .update("lastNotificationTime", System.currentTimeMillis())
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d("GeofenceReceiver", "lastNotificationTime updated successfully for task: " + taskId);
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("GeofenceReceiver", "Error updating lastNotificationTime for task: " + taskId, e);
                                            });
                                }
                            } else {
                                Log.w("GeofenceReceiver", "User not found or query returned no results.");
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("GeofenceReceiver", "Error querying user by email: " + userEmail, e);
                        });
            }
        }
    }
}



