package com.example.geominder.services;








import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;








import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;








import com.example.geominder.ui.DecisionActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;








import java.util.ArrayList;
import java.util.List;








public class LocationService extends android.app.Service {








    private static final int LOCATION_NOTIFICATION_ID = 1; // ID for location tracking notification
    public static final int GEOFENCE_NOTIFICATION_ID = 2; // ID for geofence alert notification
    private static final String CHANNEL_ID = "location_channel";








    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;








    @Override
    public void onCreate() {
        super.onCreate();








        Log.d("LocationService", "Service created.");








        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);








        // Initialize location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null && locationResult.getLocations().size() > 0) {
                    // Get the most recent location
                    android.location.Location location = locationResult.getLastLocation();
                    // Log the latitude and longitude
                    Log.d("LocationService", "Location updated: Latitude = " + location.getLatitude() + ", Longitude = " + location.getLongitude());
                }
            }
        };








        // Create notification channel for devices running Android O or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Service",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }








        // Start the foreground service with a notification for tracking location
        startForeground(LOCATION_NOTIFICATION_ID, createLocationNotification());
        Log.d("LocationService", "Foreground service started.");
    }








    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("LocationService", "Permissions not granted for location");
            return START_NOT_STICKY;
        }








        fusedLocationClient.requestLocationUpdates(
                new com.google.android.gms.location.LocationRequest()
                        .setInterval(30000) // Update every 30 seconds
                        .setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY),
                locationCallback,
                getMainLooper()
        );








        if (intent != null) {
            // Explicitly clear `taskIds` and ensure no data leaks
            ArrayList<String> taskIds = intent.getStringArrayListExtra("taskIds");
            if (taskIds == null) taskIds = new ArrayList<>();
            int transition = intent.getIntExtra("transition", -1);








            Log.d("LocationService", "Received taskIds: " + taskIds);
            if (transition != -1) {
                showGeofenceNotification(taskIds, transition);
            }
        }








        return START_STICKY;
    }
























    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("LocationService", "Service destroyed.");
    }








    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }








    private Notification createLocationNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Tracking Location")
                .setContentText("Your location is being tracked.")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setSilent(true)
                .build();
    }








    private void showGeofenceNotification(List<String> taskIds, int transition) {
        String transitionMessage = "";








        if (taskIds == null || taskIds.isEmpty()) {
            Log.e("LocationService", "No task IDs available for notification.");
            return;
        }








        int taskCount = taskIds.size(); // Get the number of tasks detected








        if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            transitionMessage = "You are entering a location, " + taskCount + " task(s) detected.";
        } else if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            transitionMessage = "You are exiting a location, " + taskCount + " task(s) detected.";
        } else {
            Log.w("LocationService", "Unknown geofence transition.");
            return; // Do nothing for unknown transitions
        }








        // Prepare the intent to send the task IDs to DecisionActivity
        Intent notificationIntent = new Intent(this, DecisionActivity.class);








        // Pass only the first occurrence of taskIds
        notificationIntent.putStringArrayListExtra("taskIds", new ArrayList<>(taskIds));
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);








        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                (int) System.currentTimeMillis(), // Unique requestCode
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? PendingIntent.FLAG_IMMUTABLE : 0)
        );
















        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("GeoMinder")
                .setContentText(transitionMessage)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent) // Open DecisionActivity on click
                .setAutoCancel(true)
                .setOngoing(true)
                .build();








        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(GEOFENCE_NOTIFICATION_ID, notification);
        }
    }
















}









