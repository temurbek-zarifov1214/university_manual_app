package uz.doc.test.viewer;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import uz.doc.test.R;
import uz.doc.test.manager.FileManager;
import uz.doc.test.model.Document;
import uz.doc.test.utils.Constants;
import uz.doc.test.utils.SharedPrefsHelper;

import java.io.File;
import java.io.InputStream;

public class DocumentViewerActivity extends AppCompatActivity {

    private static final String TAG = "DocumentViewerActivity";

    private Toolbar toolbar;
    private PDFView pdfView;
    private WebView webView;
    private ProgressBar progressBar;
    private LinearLayout errorState;
    private FloatingActionButton fabFavorite;

    private Document document;
    private FileManager fileManager;
    private SharedPrefsHelper prefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_viewer);

        initViews();
        getDocumentFromIntent();
        setupToolbar();
        setupFavoriteButton();
        loadDocument();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        pdfView = findViewById(R.id.pdf_view);
        webView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progress_bar);
        errorState = findViewById(R.id.error_state);
        fabFavorite = findViewById(R.id.fab_favorite);

        fileManager = FileManager.getInstance(this);
        prefsHelper = SharedPrefsHelper.getInstance(this);
    }

    private void getDocumentFromIntent() {
        document = (Document) getIntent().getSerializableExtra(Constants.EXTRA_DOCUMENT);

        if (document == null) {
            Toast.makeText(this, "Xatolik yuz berdi", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(document.getTitle());
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupFavoriteButton() {
        // Check if document is favorite
        boolean isFavorite = prefsHelper.isFavorite(document.getId());
        updateFavoriteIcon(isFavorite);

        fabFavorite.setOnClickListener(v -> {
            boolean newState = !prefsHelper.isFavorite(document.getId());

            if (newState) {
                prefsHelper.addFavorite(document.getId());
                Toast.makeText(this, "Sevimlilarga qo'shildi", Toast.LENGTH_SHORT).show();
            } else {
                prefsHelper.removeFavorite(document.getId());
                Toast.makeText(this, "Sevimlilardan o'chirildi", Toast.LENGTH_SHORT).show();
            }

            updateFavoriteIcon(newState);
        });
    }

    private void updateFavoriteIcon(boolean isFavorite) {
        if (isFavorite) {
            fabFavorite.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            fabFavorite.setImageResource(R.drawable.ic_favorite_border);
        }
    }

    private void loadDocument() {
        if (document.getType() == Document.DocumentType.PDF) {
            loadPDF();
        } else {
            loadPPTX();
        }
    }

    private void loadPDF() {
        try {
            showLoading();

            InputStream inputStream = fileManager.getAssetInputStream(document.getFilePath());

            if (inputStream == null) {
                showError();
                return;
            }

            pdfView.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);

            pdfView.fromStream(inputStream)
                    .defaultPage(0)
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .enableAnnotationRendering(false)
                    .scrollHandle(new DefaultScrollHandle(this))
                    .spacing(10)
                    .onLoad(new OnLoadCompleteListener() {
                        @Override
                        public void loadComplete(int nbPages) {
                            hideLoading();
                            Log.d(TAG, "PDF loaded successfully. Pages: " + nbPages);
                        }
                    })
                    .onError(new OnErrorListener() {
                        @Override
                        public void onError(Throwable t) {
                            hideLoading();
                            showError();
                            Log.e(TAG, "Error loading PDF", t);
                        }
                    })
                    .load();

        } catch (Exception e) {
            hideLoading();
            showError();
            Log.e(TAG, "Error loading PDF", e);
        }
    }

    private void loadPPTX() {
        try {
            showLoading();

            // Copy file to internal storage first
            File file = fileManager.copyAssetToInternalStorage(document.getFilePath());

            if (file == null || !file.exists()) {
                showError();
                return;
            }

            // Use Google Docs Viewer in WebView
            pdfView.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);

            setupWebView();

            // Create HTML content to display PPTX using Google Docs Viewer
            String fileUrl = "file://" + file.getAbsolutePath();
            String googleDocsUrl = "https://docs.google.com/gview?embedded=true&url=" + fileUrl;

            // For offline mode, we'll display a simple message
            // In Step 6, we'll add proper PPTX rendering
            String html = createPPTXPlaceholderHTML();
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);

            hideLoading();

        } catch (Exception e) {
            hideLoading();
            showError();
            Log.e(TAG, "Error loading PPTX", e);
        }
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        webView.setBackgroundColor(Color.WHITE);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                hideLoading();
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                hideLoading();
                showError();
            }
        });
    }

    private String createPPTXPlaceholderHTML() {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; padding: 20px; text-align: center; background: #f5f5f5; }" +
                ".container { background: white; padding: 40px; border-radius: 12px; margin: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }" +
                ".icon { font-size: 64px; color: #FF9800; margin-bottom: 20px; }" +
                "h2 { color: #333; margin-bottom: 10px; }" +
                "p { color: #666; line-height: 1.6; }" +
                ".filename { font-weight: bold; color: #1976D2; margin-top: 20px; padding: 10px; background: #E3F2FD; border-radius: 4px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='icon'>📊</div>" +
                "<h2>PowerPoint Taqdimoti</h2>" +
                "<p>Ushbu PowerPoint faylini ko'rish uchun tizimda maxsus ko'rish vositasi kerak.</p>" +
                "<p>Fayl muvaffaqiyatli yuklandi va offline rejimda saqlanmoqda.</p>" +
                "<div class='filename'>" + document.getTitle() + ".pptx</div>" +
                "<p style='margin-top: 20px; font-size: 14px; color: #999;'>" +
                "Keyingi yangilanishda PPTX fayllarini to'liq ko'rish imkoniyati qo'shiladi.</p>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        errorState.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showError() {
        progressBar.setVisibility(View.GONE);
        pdfView.setVisibility(View.GONE);
        webView.setVisibility(View.GONE);
        errorState.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (webView.getVisibility() == View.VISIBLE && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}