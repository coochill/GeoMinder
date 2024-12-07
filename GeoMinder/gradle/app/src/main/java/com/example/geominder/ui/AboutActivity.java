package com.example.geominder.ui;




import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;




import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;




import com.example.geominder.R;
import com.example.geominder.auth.SignUpActivity;




public class AboutActivity extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_about);




        // Find the "Get Started" button by ID
        Button getStartedButton = findViewById(R.id.goButton);




        // Set an OnClickListener to handle button clicks
        getStartedButton.setOnClickListener(v -> {
            // Start SignUpActivity when the button is clicked
            Intent intent = new Intent(AboutActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }
}







