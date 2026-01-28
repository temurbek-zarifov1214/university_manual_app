package uz.doc.test.utils;
import android.content.Context;
import android.util.Log;

public class AnalyticsHelper {
    private static final String TAG = "Analytics";
    private static AnalyticsHelper instance;

    private AnalyticsHelper() {
    }

    public static synchronized AnalyticsHelper getInstance() {
        if (instance == null) {
            instance = new AnalyticsHelper();
        }
        return instance;
    }

    public void logEvent(String eventName, String... params) {
        // Placeholder for future analytics integration
        // TODO: Integrate Firebase Analytics or similar
        Log.d(TAG, "Event: " + eventName);
    }

    public void logDocumentOpen(String documentName, String category) {
        logEvent("document_open", "name", documentName, "category", category);
    }

    public void logSearch(String query, int resultsCount) {
        logEvent("search", "query", query, "results", String.valueOf(resultsCount));
    }

    public void logFavoriteAction(String action, String documentName) {
        logEvent("favorite_" + action, "document", documentName);
    }
}