package wvw.mobile.rules;

import android.os.Bundle;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;

import com.hp.hpl.jena.rdf.model.Statement;
import wvw.mobile.rules.explanation.ExplanationRunner;
import wvw.mobile.rules.explanation.ModelFactory;

public class WebviewReasonActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview_reason);

        WebView webview = findViewById(R.id.webview);
        
        // Get a default statement from the model
        Statement defaultStatement = ModelFactory.getFoodRecommendationInfModel()
            .listStatements().nextStatement();

        String explanation = ExplanationRunner.getExplanation(
            "food-recommendation",
            "trace-based",
            defaultStatement  // Add the required Statement parameter
        );

        webview.loadData(explanation, "text/html", "UTF-8");
    }
}