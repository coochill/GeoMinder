package com.example.geominder.ui;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
































































import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.textfield.TextInputEditText;
































































import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
































































import com.example.geominder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
















import com.google.android.material.button.MaterialButton;
















































import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
































































import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
































































import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
































































import org.json.JSONArray;
import org.json.JSONObject;
































































import com.example.geominder.utils.BitmapUtils;


public class InputActivity extends AppCompatActivity implements OnMapReadyCallback {


    private TextView selectedPlaceText;
    private TextView placeAddressText;
    private TextInputEditText textInputEditText;
    private Button saveButton;
    private Spinner categorySpinner;




    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    private String selectedCategoryDocId = "";








    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;








    private List<String> placeTypes = new ArrayList<>();
    private Map<String, Object> selectedPlaceData = new HashMap<>();








    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);








        Button saveButton = findViewById(R.id.saveButton);
        View rootView = findViewById(android.R.id.content); // Get the root view
        rootView.setBackgroundColor(getResources().getColor(android.R.color.white));








        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();








        // Get references to the radio group, text input, and selected date text
        selectedPlaceText = findViewById(R.id.selectedPlaceText);
        placeAddressText = findViewById(R.id.placeAddressText);
        textInputEditText = findViewById(R.id.textInputEditText);  // Find the TextInputEditText
        saveButton = findViewById(R.id.saveButton);  // Find the save button
        categorySpinner = findViewById(R.id.categorySpinner);
        SearchView searchView = findViewById(R.id.searchView);








        Intent intent = getIntent();
        String prevTaskTitle = intent.getStringExtra("taskTitle");
        String prevSelectedDate = intent.getStringExtra("selectedDate");
        Log.d("initialize", "Selected Date: " + prevSelectedDate);
        String prevPlaceAddress = intent.getStringExtra("placeAddress");
        String prevPlaceName = intent.getStringExtra("placeName");
        String prevCategory = intent.getStringExtra("category");
        Double prevLatitude = intent.getDoubleExtra("latitude", 0.0);
        Double prevLongitude = intent.getDoubleExtra("longitude", 0.0);








        if (prevTaskTitle != null) {
            // Set the task title into the TextInputEditText
            textInputEditText.setText(prevTaskTitle);
        }


        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ArrayList<String> categoryDocIds = (ArrayList<String>) categorySpinner.getTag();
                String selectedCategoryId = null;




                if (categoryDocIds == null) {
                    Log.e("SpinnerError", "categoryDocIds is null. Ensure the spinner tag is set with the correct ArrayList.");
                    return; // Exit early if the tag is not initialized
                }




                // Check if prevCategory is not null
                if (prevCategory != null) {
                    // Find the position of prevCategory in the list of category IDs
                    int prevPosition = categoryDocIds.indexOf(prevCategory);




                    // If the prevCategory is found, set it as the selected item
                    if (prevPosition != -1) {
                        categorySpinner.setSelection(prevPosition);
                        return; // Exit early since we're just setting the previous category
                    }
                }




                // If prevCategory is null or not found, handle the current selection
                String selectedCategory = (String) parent.getItemAtPosition(position);




                // Get the ID for the selected category
                selectedCategoryId = categoryDocIds.get(position);




                // Log for debugging
                Log.d("spinner", "Selected Category: " + selectedCategory);
                Log.d("spinner", "Selected Category ID: " + selectedCategoryId);




                // If the map is initialized, fetch places
                if (mMap != null) {
                    LatLng currentCenter = mMap.getCameraPosition().target;
                    String placeType = getPlaceTypeFromCategory(selectedCategoryId); // Pass ID to the method
                    fetchPlacesFromAPI(currentCenter, placeType);
                }
            }




            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });


        // Customize SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Call a method to perform the search
                geocodeLocation(query);
                return true;
            }




            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });




// Adjust the width programmatically
        ViewGroup.LayoutParams params = searchView.getLayoutParams();
        params.width = 850; // Set a custom width (e.g., 650px)
        searchView.setLayoutParams(params); // Apply the updated layout parameters




// Change the text color and hint color to black
        @SuppressLint("RestrictedApi")
        SearchView.SearchAutoComplete searchAutoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        if (searchAutoComplete != null) {
            searchAutoComplete.setTextColor(Color.BLACK); // Set text color to black
            searchAutoComplete.setHintTextColor(Color.BLACK); // Set hint text color to black
        }




// Change the search icon color to black
        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        if (searchIcon != null) {
            Drawable drawable = searchIcon.getDrawable();
            if (drawable != null) {
                drawable.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP); // Change icon color to black
            }
        }


// Change the close button icon color to black
        ImageView closeButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        if (closeButton != null) {
            Drawable closeDrawable = closeButton.getDrawable();
            if (closeDrawable != null) {
                closeDrawable.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP); // Change close icon color to black
            }
        }


// Set the border for the SearchView
        int borderColor = Color.BLACK; // Define the border color
        int borderWidth = 2; // Set the border width (2px as an example)


// Set the background of the SearchView with a black border
        GradientDrawable borderDrawable = new GradientDrawable();
        borderDrawable.setShape(GradientDrawable.RECTANGLE);
        borderDrawable.setStroke(borderWidth, borderColor); // Set stroke (border)
        borderDrawable.setCornerRadius(8); // Optionally, you can set corner radius to make the edges rounded
        searchView.setBackground(borderDrawable); // Apply the border to the SearchView


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapContainer);
        mapFragment.getMapAsync(this); // The activity must implement OnMapReadyCallback


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        fetchPlaceTypesFromFirestore();


        // Set OnClickListener for the save button
        saveButton.setOnClickListener(v -> onSaveButtonClick());
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        // Check for location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }


        // Enable map features after spinner is populated
        populateSpinner(() -> {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);


            // Check for previous latitude and longitude values
            Intent intent = getIntent();
            String prevCategory = intent.getStringExtra("category");
            Double prevLatitude = intent.getDoubleExtra("latitude", 0.0);
            Double prevLongitude = intent.getDoubleExtra("longitude", 0.0);
            String prevPlaceAddress = intent.getStringExtra("placeAddress");
            String prevPlaceName = intent.getStringExtra("placeName");




            if (prevLatitude != 0.0 && prevLongitude != 0.0 && prevPlaceName != null && prevPlaceAddress != null) {
                // Set map to previous location
                selectedPlaceData.put("latitude", prevLatitude);
                selectedPlaceData.put("longitude", prevLongitude);
                selectedPlaceText.setText("Selected Place: " + prevPlaceName);
                placeAddressText.setText("Address: " + prevPlaceAddress);
                selectedPlaceData.put("name", prevPlaceName);
                selectedPlaceData.put("address", prevPlaceAddress);
                findViewById(R.id.selectedPlaceCard).setVisibility(View.VISIBLE);
                LatLng prevLocation = new LatLng(prevLatitude, prevLongitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(prevLocation, 15));




                // Optionally fetch places around the previous location
                if (prevCategory != null) {
                    fetchPlacesFromAPI(prevLocation, prevCategory);
                }
            } else {
                // Get the user's current location if no previous location is available
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, location -> {
                            if (location != null) {
                                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                                mMap.addMarker(new MarkerOptions().position(userLocation).title("You are here"));
                            }
                        });
            }




            // Handle map interactions like camera idle
            mMap.setOnCameraIdleListener(() -> {
                LatLng center = mMap.getCameraPosition().target; // Get the center of the map
                String selectedCategory = (String) categorySpinner.getSelectedItem();




                // Get the list of category document IDs stored as the spinner's tag
                ArrayList<String> categoryDocIds = (ArrayList<String>) categorySpinner.getTag();




                // Get the ID for the selected category
                String selectedCategoryId = categoryDocIds.get(categorySpinner.getSelectedItemPosition());




                // Pass the ID to the getPlaceTypeFromCategory method
                String placeType = getPlaceTypeFromCategory(selectedCategoryId);
                fetchPlacesFromAPI(center, placeType);
            });
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if (requestCode == 1) { // Check for the request code used in requestPermissions
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with location-related tasks
                onMapReady(mMap);  // You can call this again to refresh map if needed
            } else {
                // Permission denied, handle gracefully (e.g., show a message)
                Toast.makeText(this, "Location permission is required to show nearby places.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void geocodeLocation(String locationName) {
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng location = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.clear();  // Clear existing markers
                mMap.addMarker(new MarkerOptions().position(location).title(address.getFeatureName()));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));  // Zoom to the new location
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error geocoding location", Toast.LENGTH_SHORT).show();
        }
    }


    private void fetchPlaceTypesFromFirestore() {
        db.collection("placetypes")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    placeTypes.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        placeTypes.add(document.getId().toLowerCase());
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                    Toast.makeText(this, "Error fetching place types: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private String getPlaceTypeFromCategory(String category) {
        // Convert the category to lowercase for comparison
        String categoryLower = category.toLowerCase();


        // Check if the category exists in placeTypes and return the corresponding placeType
        String placeType = placeTypes.contains(categoryLower) ? categoryLower : "cafe";


        return placeType;
    }


    // Method to get the inputted text from the TextInputEditText
    private String getTextInputData() {
        return textInputEditText.getText().toString();
    }


    private void onSaveButtonClick() {
        String selectedCategoryName = (String) categorySpinner.getSelectedItem();
        ArrayList<String> categoryDocIds = (ArrayList<String>) categorySpinner.getTag();
        int selectedIndex = ((ArrayAdapter<String>) categorySpinner.getAdapter()).getPosition(selectedCategoryName);
        selectedCategoryDocId = categoryDocIds.get(selectedIndex);


        // Call saveTaskToFirestore after validation
        if (!areFieldsValid()) {
            // Show a toast message if any required field is null or empty
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;  // Exit the method to prevent saving and navigating
        }


        saveTaskToFirestore();


        // Navigate to MainActivity after saving the task
        Intent intent = new Intent(InputActivity.this, MainActivity.class);
        startActivity(intent);
    }


    private boolean areFieldsValid() {
        // Ensure taskTitle is not empty
        String taskTitle = getTextInputData();
        if (taskTitle.isEmpty()) {
            return false; // Task title is required
        }


        // Ensure selectedCategoryDocId is not null or empty
        if (selectedCategoryDocId == null || selectedCategoryDocId.isEmpty()) {
            return false; // Category is required
        }


        // Ensure selectedPlaceData is not null
        if (selectedPlaceData == null) {
            return false; // Place data is required
        }


        // Ensure place name and address are not null or empty
        String placeName = (String) selectedPlaceData.get("name");
        String placeAddress = (String) selectedPlaceData.get("address");
        if (placeName == null || placeName.isEmpty() || placeAddress == null || placeAddress.isEmpty()) {
            return false; // Place name and address are required
        }


        // Ensure latitude and longitude are not null
        Double latitude = (Double) selectedPlaceData.get("latitude");
        Double longitude = (Double) selectedPlaceData.get("longitude");
        if (latitude == null || longitude == null) {
            return false; // Latitude and longitude are required
        }


        return true; // All fields are valid
    }




    private void saveTaskToFirestore() {
        String taskTitle = getTextInputData();
        if (taskTitle.isEmpty()) {
            return;  // Do not save if title is empty
        }


        Intent intent = getIntent();
        String prevTaskId = intent.getStringExtra("taskId");
        String userEmail = mAuth.getCurrentUser().getEmail();
        if (userEmail != null) {
            // Query Firestore to get the document with the matching email
            db.collection("users")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Retrieve user document and category document IDs
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                for (QueryDocumentSnapshot document : querySnapshot) {
                                    String userDocId = document.getId();


                                    // Populate taskData
                                    Map<String, Object> taskData = new HashMap<>();
                                    taskData.put("taskTitle", taskTitle);
                                    taskData.put("isCompleted", false);
                                    taskData.put("category", selectedCategoryDocId);
                                    taskData.put("radius", 200);
                                    taskData.put("isDeleted", false);
                                    taskData.put("notify", "on");
                                    taskData.put("selectedDate", "anytime");
                                    taskData.put("reminderInterval", "none");
                                    taskData.put("placeName", selectedPlaceData.get("name"));
                                    taskData.put("placeAddress", selectedPlaceData.get("address"));
                                    taskData.put("latitude", selectedPlaceData.get("latitude"));
                                    taskData.put("longitude", selectedPlaceData.get("longitude"));


                                    if (prevTaskId == null) {
                                        // If prevTaskId is null, save the task as a new task
                                        db.collection("users")
                                                .document(userDocId)
                                                .collection("tasks")
                                                .add(taskData)
                                                .addOnSuccessListener(documentReference -> {
                                                    // Task successfully saved
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Handle the error
                                                });
                                    } else {
                                        // If prevTaskId is not null, update the existing task
                                        db.collection("users")
                                                .document(userDocId)
                                                .collection("tasks")
                                                .document(prevTaskId) // Use prevTaskId to find the document
                                                .get()
                                                .addOnCompleteListener(updateTask -> {
                                                    if (updateTask.isSuccessful()) {
                                                        DocumentSnapshot taskDoc = updateTask.getResult();
                                                        if (taskDoc.exists()) {
                                                            // Update the existing task with the new data
                                                            db.collection("users")
                                                                    .document(userDocId)
                                                                    .collection("tasks")
                                                                    .document(prevTaskId)
                                                                    .update(taskData)
                                                                    .addOnSuccessListener(aVoid -> {
                                                                        // Task successfully updated
                                                                    })
                                                                    .addOnFailureListener(e -> {
                                                                        // Handle the error
                                                                    });
                                                        } else {
                                                            // Handle case when task document with prevTaskId does not exist
                                                        }
                                                    } else {
                                                        // Handle the error when getting the task document fails
                                                    }
                                                });
                                    }
                                }
                            }
                        }
                    });
        }
    }




    // New populateSpinner method with the custom spinner logic
    private void populateSpinner(Runnable onComplete) {
        // Reference to the placetypes collection in Firestore
        db.collection("placetypes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<String> categoryNames = new ArrayList<>();
                        final ArrayList<String> categoryDocIds = new ArrayList<>();
                        final ArrayList<Integer> categoryIcons = new ArrayList<>(); // For storing icon resource IDs


                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String categoryName = document.getString("name");
                            if (categoryName != null) {
                                categoryNames.add(categoryName);
                                categoryDocIds.add(document.getId());


                                // Add corresponding icon resource (you can map this based on your data)
                                int iconResId = getIconForCategory(categoryName); // Implement this method
                                categoryIcons.add(iconResId);
                            }
                        }


                        // Create a custom adapter
                        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(InputActivity.this, categoryNames, categoryIcons);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        categorySpinner.setAdapter(adapter);


                        // Store the document IDs as a tag to reference later
                        categorySpinner.setTag(categoryDocIds);


                        // Execute the callback to indicate completion
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    }
                });
    }


    private class CustomSpinnerAdapter extends ArrayAdapter<String> {
        private Context context;
        private ArrayList<String> categoryNames;
        private ArrayList<Integer> categoryIcons;


        public CustomSpinnerAdapter(Context context, ArrayList<String> categoryNames, ArrayList<Integer> categoryIcons) {
            super(context, 0, categoryNames);
            this.context = context;
            this.categoryNames = categoryNames;
            this.categoryIcons = categoryIcons;
        }


        // For the default view (before selection)
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_item, parent, false);
            }


            TextView text = convertView.findViewById(android.R.id.text1);
            ImageView icon = new ImageView(context);


            // Set the icon image
            icon.setImageResource(categoryIcons.get(position));


            // Adjust icon size
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    (int) (25 * context.getResources().getDisplayMetrics().density),
                    (int) (25 * context.getResources().getDisplayMetrics().density)
            );
            icon.setLayoutParams(layoutParams);


            text.setText(categoryNames.get(position));


            // Set the text color to black
            text.setTextColor(Color.BLACK);


            // Combine icon and text in a layout
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.addView(icon);
            layout.addView(text);


            return layout;
        }


        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
            }


            // Set the background color of the dropdown container to green dynamically
            if (position == 0) { // Change condition based on your logic
                convertView.setBackgroundColor(Color.GREEN);
            } else {
                convertView.setBackgroundColor(Color.TRANSPARENT); // or default color
            }


            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.HORIZONTAL);


            ImageView icon = new ImageView(context);
            icon.setImageResource(categoryIcons.get(position));


            // Adjust icon size
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    (int) (30 * context.getResources().getDisplayMetrics().density),
                    (int) (30 * context.getResources().getDisplayMetrics().density)
            );
            icon.setLayoutParams(layoutParams);


            TextView text = new TextView(context);
            text.setText(categoryNames.get(position));


            // Set the text color to black for the dropdown view
            text.setTextColor(Color.BLACK);


            layout.addView(icon);
            layout.addView(text);


            return layout;
        }
    }


    // Method to map category name to an icon resource
    private int getIconForCategory(String categoryName) {
        switch (categoryName) {
            case "Atm":
                return R.drawable.icon_atm;
            case "Bakery":
                return R.drawable.icon_bakery;
            case "Bank":
                return R.drawable.icon_bank;
            case "Cafe":
                return R.drawable.icon_cafe;
            case "Cemetery":
                return R.drawable.icon_cemetery;
            case "Church":
                return R.drawable.icon_church;
            case "Drugstore":
                return R.drawable.icon_drugstore;
            case "Hospital":
                return R.drawable.icon_hospital;
            case "Library":
                return R.drawable.icon_library;
            case "Pet store":
                return R.drawable.icon_pet_store;
            case "Restaurant":
                return R.drawable.icon_restaurant;
            case "School":
                return R.drawable.icon_school;
            case "Store":
                return R.drawable.icon_store;
            case "Shopping mall":
                return R.drawable.icon_shopping_mall;
            case "University":
                return R.drawable.icon_university;
            default:
                return R.drawable.icon_default; // Default icon
        }
    }


    private void fetchPlacesFromAPI(LatLng location, String placeType) {
        // Build the Places API URL
        Log.d ("fetch", "placetype: " + placeType);
        String apiKey = getString(R.string.google_maps_key);
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + location.latitude + "," + location.longitude +
                "&radius=1000&type=" + placeType + "&key=" + apiKey;


        // Make an API call to fetch nearby places
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();


        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(InputActivity.this, "Error fetching places: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }


            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonObject = new JSONObject(responseData);
                        JSONArray results = jsonObject.getJSONArray("results");


                        runOnUiThread(() -> mMap.clear());


                        for (int i = 0; i < results.length(); i++) {
                            JSONObject place = results.getJSONObject(i);
                            JSONObject geometry = place.getJSONObject("geometry").getJSONObject("location");
                            String name = place.getString("name");


                            LatLng placeLocation = new LatLng(geometry.getDouble("lat"), geometry.getDouble("lng"));


                            // Add custom icon and location name as snippet
                            runOnUiThread(() -> {
                                // Get the screen density (e.g., for scaling the icon)
                                float density = getResources().getDisplayMetrics().density;


                                // Set the desired width and height for the icon
                                int iconWidth = (int) (40 * density);  // Icon width is fixed to 40 * density
                                int iconHeight = (int) (40 * density); // Icon height is fixed to 40 * density


                                try {
                                    // Construct the resource name dynamically based on placeType
                                    String iconFileName = placeType + "_icon"; // e.g., "cafe_icon", "shopping_mall_icon"


                                    // Get the resource ID of the icon
                                    int resourceId = getResources().getIdentifier(iconFileName, "raw", getPackageName());
                                    if (resourceId != 0) {
                                        InputStream inputStream = getResources().openRawResource(resourceId);


                                        // Call the new method in BitmapUtils to create the combined bitmap
                                        Bitmap combinedBitmap = BitmapUtils.createCustomMarkerIcon(inputStream, name, density, iconWidth, iconHeight);


                                        // Create a BitmapDescriptor from the combined bitmap
                                        BitmapDescriptor customIcon = BitmapDescriptorFactory.fromBitmap(combinedBitmap);


                                        // Add the marker with the combined custom icon
                                        Marker marker = mMap.addMarker(new MarkerOptions()
                                                .position(placeLocation)
                                                .icon(customIcon));


                                        marker.setTag(place); // Storing the place data with the marker
                                    } else {
                                        throw new Exception("Icon resource not found for: " + iconFileName);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(InputActivity.this, "Error loading SVG: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }


                        runOnUiThread(() -> mMap.setOnMarkerClickListener(marker -> {
                            // Retrieve the place data from the marker tag
                            JSONObject place = (JSONObject) marker.getTag();
                            try {
                                String placeName = place.getString("name");
                                String placeAddress = place.getString("vicinity");


                                selectedPlaceText.setText("Selected Place: " + placeName);
                                placeAddressText.setText("Address: " + placeAddress);


                                findViewById(R.id.selectedPlaceCard).setVisibility(View.VISIBLE);
                                // Retrieve latitude and longitude from the marker's position
                                double latitude = marker.getPosition().latitude;
                                double longitude = marker.getPosition().longitude;


                                selectedPlaceData.put("name", placeName);
                                selectedPlaceData.put("address", placeAddress);
                                selectedPlaceData.put("latitude", latitude);
                                selectedPlaceData.put("longitude", longitude);


                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return false; // Returning false to allow the default behavior (e.g., showing the info window)
                        }));


                    } catch (Exception e) {
                        runOnUiThread(() ->
                                Toast.makeText(InputActivity.this, "Error parsing places data", Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }


}



