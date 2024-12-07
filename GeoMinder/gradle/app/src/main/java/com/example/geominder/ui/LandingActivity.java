package com.example.geominder.ui;




import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.widget.Button;
import android.widget.TextView;
import androidx.core.text.HtmlCompat;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;




import com.example.geominder.R;
import com.example.geominder.auth.LoginActivity;
import com.example.geominder.auth.SignUpActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;




import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;




public class LandingActivity extends AppCompatActivity {




    private TextView featuresTextView;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_landing);




        // Find the "Get Started" button by ID
        Button getStartedButton = findViewById(R.id.getStartedButton);
        featuresTextView = findViewById(R.id.featuresTextView);




        // Set an OnClickListener to handle button clicks
        getStartedButton.setOnClickListener(v -> {
            // Start AboutActivity when the button is clicked
            Intent intent = new Intent(LandingActivity.this, AboutActivity.class);
            startActivity(intent);
        });




        // Load features text from the raw resource and set it to TextView
        String features = loadFeaturesFromFile();




        // Convert Markdown-like formatting to HTML
        String formattedFeatures = convertMarkdownToHtml(features);




        // Apply HTML formatting to the text before setting it
        SpannableString spannableText = new SpannableString(HtmlCompat.fromHtml(formattedFeatures, HtmlCompat.FROM_HTML_MODE_LEGACY));




        // Emojis to make bigger (15dp)
        makeEmojisLarger(spannableText);




        // Set the formatted text with larger emojis
        featuresTextView.setText(spannableText);




        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();






    }




    private String loadFeaturesFromFile() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.features); // raw/features.txt
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }




    private String convertMarkdownToHtml(String text) {
        // Replace ## with <h2> for heading and new line
        text = text.replaceAll("##(.*?)##", "<h2><strong>$1</strong></h2>");




        // Replace ** with <strong> for bold text on the same line
        text = text.replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>");




        // Replace * with <strong> for bold text on the same line
        text = text.replaceAll("\\*(.*?)\\*", "<strong>$1</strong>");




        // Replace newlines \n with <br> for line breaks
        text = text.replaceAll("\n", "<br>");




        return text;
    }




    private void makeEmojisLarger(SpannableString spannableText) {
        String[] emojis = {"🗺️", "⏰", "📍", "✅"};
        for (String emoji : emojis) {
            int start = spannableText.toString().indexOf(emoji);
            while (start >= 0) {
                int end = start + emoji.length();
                // Apply the relative size span to increase emoji size by 1.5 (15dp equivalent)
                spannableText.setSpan(new RelativeSizeSpan(1.5f), start, end, 0);
                start = spannableText.toString().indexOf(emoji, end); // Find next emoji
            }
        }
    }
}









