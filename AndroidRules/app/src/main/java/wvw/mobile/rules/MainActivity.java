package wvw.mobile.rules;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import wvw.mobile.rules.explanation.ExplanationRunner;

public class MainActivity extends AppCompatActivity {
    private Spinner modelSpinner;
    private Spinner explanationSpinner;
    private TextView explanationText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        modelSpinner = findViewById(R.id.modelSpinner);
        explanationSpinner = findViewById(R.id.explanationSpinner);
        explanationText = findViewById(R.id.explanationText);
        Button generateButton = findViewById(R.id.generateButton);

        // Set up model spinner
        ArrayAdapter<String> modelAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item,
            new String[]{"loan-eligibility", "food-recommendation"});
        modelSpinner.setAdapter(modelAdapter);

        // Set up explanation spinner
        ArrayAdapter<String> explanationAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item,
            new String[]{"trace-based", "contextual", "contrastive", "counterfactual"});
        explanationSpinner.setAdapter(explanationAdapter);

        generateButton.setOnClickListener(v -> generateExplanation());
    }

    private void generateExplanation() {
        String model = modelSpinner.getSelectedItem().toString();
        String explanationType = explanationSpinner.getSelectedItem().toString();
        
        // Run the appropriate test based on selection
        String explanation = runExplanationTest(model, explanationType);
        explanationText.setText(explanation);
    }

    private String runExplanationTest(String model, String explanationType) {
        try {
            return ExplanationRunner.getExplanation(model, explanationType);
        } catch (Exception e) {
            return "Error generating explanation: " + e.getMessage();
        }
    }
} 