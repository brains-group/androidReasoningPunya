package wvw.mobile.rules;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.text.method.ScrollingMovementMethod;

import androidx.appcompat.app.AppCompatActivity;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import wvw.mobile.rules.explanation.ExplanationRunner;
import wvw.mobile.rules.explanation.ModelFactory;

public class MainActivity extends AppCompatActivity {
    private Spinner inspectionModelSpinner;
    private Spinner viewTypeSpinner;
    private Spinner modelSpinner;
    private Spinner subjectSpinner;
    private Spinner predicateSpinner;
    private Spinner objectSpinner;
    private Spinner explanationSpinner;
    private TextView explanationText;
    private ArrayAdapter<Resource> subjectAdapter;
    private ArrayAdapter<Property> predicateAdapter;
    private ArrayAdapter<RDFNode> objectAdapter;
    
    private static final String TAG = "MainActivity-Explanation";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize explanationText and set scrolling
        explanationText = findViewById(R.id.explanationText);
        explanationText.setMovementMethod(new ScrollingMovementMethod());

        // Initialize inspection section
        inspectionModelSpinner = findViewById(R.id.inspectionModelSpinner);
        viewTypeSpinner = findViewById(R.id.viewTypeSpinner);
        Button inspectionButton = findViewById(R.id.inspectionButton);

        // Initialize explanation section
        modelSpinner = findViewById(R.id.modelSpinner);
        subjectSpinner = findViewById(R.id.subjectSpinner);
        predicateSpinner = findViewById(R.id.predicateSpinner);
        objectSpinner = findViewById(R.id.objectSpinner);
        explanationSpinner = findViewById(R.id.explanationSpinner);
        explanationText = findViewById(R.id.explanationText);
        Button generateButton = findViewById(R.id.generateButton);

        // Set up inspection model spinner
        ArrayAdapter<String> inspectionModelAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item,
            new String[]{"loan-eligibility", "food-recommendation", "transitive"});
        inspectionModelSpinner.setAdapter(inspectionModelAdapter);

        // Set up view type spinner
        ArrayAdapter<String> viewTypeAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item,
            new String[]{"base-model", "rules", "inference-model"});
        viewTypeSpinner.setAdapter(viewTypeAdapter);

        // Set up explanation spinners
        ArrayAdapter<String> modelAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item,
            new String[]{"loan-eligibility", "food-recommendation", "transitive"});
        modelSpinner.setAdapter(modelAdapter);

        // Initialize triple selection spinners
        subjectSpinner = findViewById(R.id.subjectSpinner);
        predicateSpinner = findViewById(R.id.predicateSpinner);
        objectSpinner = findViewById(R.id.objectSpinner);

        // Create adapters for triple components
        subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        predicateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        objectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());

        subjectSpinner.setAdapter(subjectAdapter);
        predicateSpinner.setAdapter(predicateAdapter);
        objectSpinner.setAdapter(objectAdapter);

        ArrayAdapter<String> explanationAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item,
            new String[]{"trace-based", "contextual", "contrastive", "counterfactual"});
        explanationSpinner.setAdapter(explanationAdapter);

        // Set up listeners
        modelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateTripleComponents(modelSpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        inspectionButton.setOnClickListener(v -> showModelInspection());
        generateButton.setOnClickListener(v -> generateExplanation());
    }

    private void showModelInspection() {
        String model = inspectionModelSpinner.getSelectedItem().toString();
        String viewType = viewTypeSpinner.getSelectedItem().toString();
        
        String content = "";
        switch (viewType) {
            case "base-model":
                content = getBaseModel(model);
                break;
            case "rules":
                content = getRules(model);
                break;
            case "inference-model":
                content = getInferenceModel(model);
                break;
        }
        explanationText.setText(content);
    }

    private String getBaseModel(String model) {
        Log.d("ModelViewer", "Viewing base model for: " + model);
        Model baseModel;
        switch(model) {
            case "loan-eligibility":
                baseModel = ModelFactory.getLoanEligibilityBaseModel();
                Log.d("ModelViewer", "Created loan eligibility base model");
                break;
            case "food-recommendation":
                baseModel = ModelFactory.getFoodRecommendationBaseModel();
                Log.d("ModelViewer", "Created food recommendation base model");
                break;
            case "transitive":
                baseModel = ModelFactory.getTransitiveBaseModel();
                Log.d("ModelViewer", "Created transitive base model");
                break;
            default:
                Log.e("ModelViewer", "Invalid model selected: " + model);
                return "Invalid model selected";
        }
        String formattedModel = formatModelTriples(baseModel);
        // Split long output into multiple log messages
        Log.d("ModelViewer", "Full model content:");
        int chunkSize = 1000;
        for (int i = 0; i < formattedModel.length(); i += chunkSize) {
            int end = Math.min(i + chunkSize, formattedModel.length());
            Log.d("ModelViewer", formattedModel.substring(i, end));
        }
        return formattedModel;
    }

    private String getRules(String model) {
        Log.d("ModelViewer", "Getting rules for model: " + model);
        String rules;
        switch(model) {
            case "loan-eligibility":
                rules = ModelFactory.getLoanEligibilityRules();
                Log.d("ModelViewer", "Retrieved loan eligibility rules");
                break;
            case "food-recommendation":
                rules = ModelFactory.getFoodRecommendationRules();
                Log.d("ModelViewer", "Retrieved food recommendation rules");
                break;
            case "transitive":
                rules = ModelFactory.getTransitiveRules();
                Log.d("ModelViewer", "Retrieved transitive rules");
                break;
            default:
                Log.e("ModelViewer", "Invalid model selected: " + model);
                return "Invalid model selected";
        }
        
        // Split long rules into multiple log messages
        Log.d("ModelViewer", "Full rules content:");
        int chunkSize = 1000;
        for (int i = 0; i < rules.length(); i += chunkSize) {
            int end = Math.min(i + chunkSize, rules.length());
            Log.d("ModelViewer", rules.substring(i, end));
        }
        
        return rules;
    }

    private String getInferenceModel(String model) {
        Log.d("ModelViewer", "Getting inference model for: " + model);
        InfModel infModel;
        switch(model) {
            case "loan-eligibility":
                infModel = ModelFactory.getLoanEligibilityInfModel();
                Log.d("ModelViewer", "Created loan eligibility inference model");
                break;
            case "food-recommendation":
                infModel = ModelFactory.getFoodRecommendationInfModel();
                Log.d("ModelViewer", "Created food recommendation inference model");
                break;
            case "transitive":
                infModel = ModelFactory.getTransitiveInfModel();
                Log.d("ModelViewer", "Created transitive inference model");
                break;
            default:
                Log.e("ModelViewer", "Invalid model selected: " + model);
                return "Invalid model selected";
        }
        String formattedModel = formatModelTriples(infModel);
        // Split long output into multiple log messages
        Log.d("ModelViewer", "Full inference model content:");
        int chunkSize = 1000;
        for (int i = 0; i < formattedModel.length(); i += chunkSize) {
            int end = Math.min(i + chunkSize, formattedModel.length());
            Log.d("ModelViewer", formattedModel.substring(i, end));
        }
        return formattedModel;
    }

    private String formatModelTriples(Model model) {
        StringBuilder formatted = new StringBuilder();
        StmtIterator iter = model.listStatements();
        
        while (iter.hasNext()) {
            Statement stmt = iter.next();
            formatted.append(formatTriple(stmt)).append("\n");
        }
        
        return formatted.toString();
    }

    private String formatTriple(Statement stmt) {
        String subject = stmt.getSubject().toString();
        String predicate = stmt.getPredicate().toString();
        String object = stmt.getObject().toString();
        
        // Remove URI prefixes for cleaner display
        subject = subject.substring(subject.lastIndexOf("/") + 1);
        predicate = predicate.substring(predicate.lastIndexOf("/") + 1);
        object = object.substring(object.lastIndexOf("/") + 1);
        
        return String.format("%s -> %s -> %s", subject, predicate, object);
    }

    private void updateTripleComponents(String model) {
        InfModel infModel;
        switch(model) {
            case "loan-eligibility":
                infModel = ModelFactory.getLoanEligibilityInfModel();
                break;
            case "food-recommendation":
                infModel = ModelFactory.getFoodRecommendationInfModel();
                break;
            case "transitive":
                infModel = ModelFactory.getTransitiveInfModel();
                break;
            default:
                Log.e(TAG, "Invalid model selected: " + model);
                return;
        }

        // Get unique subjects, predicates, and objects
        Set<Resource> subjects = new HashSet<>();
        Set<Property> predicates = new HashSet<>();
        Set<RDFNode> objects = new HashSet<>();

        StmtIterator itr = infModel.listStatements();
        while (itr.hasNext()) {
            Statement stmt = itr.next();
            subjects.add(stmt.getSubject());
            predicates.add(stmt.getPredicate());
            objects.add(stmt.getObject());
        }

        // Update adapters
        subjectAdapter.clear();
        predicateAdapter.clear();
        objectAdapter.clear();

        subjectAdapter.addAll(subjects);
        predicateAdapter.addAll(predicates);
        objectAdapter.addAll(objects);

        subjectAdapter.notifyDataSetChanged();
        predicateAdapter.notifyDataSetChanged();
        objectAdapter.notifyDataSetChanged();
    }

    private void generateExplanation() {
        String model = modelSpinner.getSelectedItem().toString();
        String explanationType = explanationSpinner.getSelectedItem().toString();
        
        // Create statement from selected components
        Resource subject = (Resource) subjectSpinner.getSelectedItem();
        Property predicate = (Property) predicateSpinner.getSelectedItem();
        RDFNode object = (RDFNode) objectSpinner.getSelectedItem();
        
        Statement selectedTriple = ResourceFactory.createStatement(subject, predicate, object);
        
        String explanation = runExplanationTest(model, explanationType, selectedTriple);
        
        // Log the explanation to Logcat
        Log.d(TAG, "Selected Model: " + model);
        Log.d(TAG, "Selected Explanation Type: " + explanationType);
        Log.d(TAG, "Selected Triple: " + selectedTriple.toString());
        Log.d(TAG, "Generated Explanation: \n" + explanation);
        
        // Set text and make scrollable
        explanationText.setText(explanation);
        explanationText.setMovementMethod(new ScrollingMovementMethod());
        explanationText.scrollTo(0, 0); // Reset scroll position to top
    }

    private String runExplanationTest(String model, String explanationType, Statement triple) {
        try {
            return ExplanationRunner.getExplanation(model, explanationType, triple);
        } catch (Exception e) {
            return "Error generating explanation: " + e.getMessage();
        }
    }
} 