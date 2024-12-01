package wvw.mobile.rules;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.webkit.WebView;
import wvw.mobile.rules.explanation.ExplanationRunner;

public class WebviewReasonActivity extends AppCompatActivity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview_reason);

        webView = findViewById(R.id.webView);
        
        // Instead of calling run(), let's get a specific explanation
        String explanation = ExplanationRunner.getExplanation(
            "loan-eligibility", 
            "trace-based"  // or whatever default explanation type you want
        );
        
        // Display the explanation in the WebView
        String htmlContent = "<html><body><pre>" + explanation + "</pre></body></html>";
        webView.loadData(htmlContent, "text/html", "UTF-8");
    }
}