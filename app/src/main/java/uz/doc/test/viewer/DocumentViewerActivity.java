package uz.doc.test.viewer;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xslf.usermodel.XMLSlideShow;

import uz.doc.test.R;
import uz.doc.test.manager.FileManager;
import uz.doc.test.model.Document;
import uz.doc.test.utils.Constants;
import uz.doc.test.utils.SharedPrefsHelper;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DocumentViewerActivity extends AppCompatActivity {

    private static final String TAG = "DocumentViewerActivity";

    private Toolbar toolbar;
    private RecyclerView pdfRecyclerView;
    private WebView webView;
    private ProgressBar progressBar;
    private LinearLayout errorState;
    private FloatingActionButton fabFavorite;

    private Document document;
    private FileManager fileManager;
    private SharedPrefsHelper prefsHelper;
    private ParcelFileDescriptor pdfFileDescriptor;
    private PdfRenderer pdfRenderer;
    private PdfPageAdapter pdfPageAdapter;
    private PptxSlideAdapter pptxSlideAdapter;
    private final ExecutorService pptxExecutor = Executors.newSingleThreadExecutor();

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
        pdfRecyclerView = findViewById(R.id.pdf_viewer);
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

            File file;
            if (document.isFromAssets()) {
                // Asset file - copy from assets
                file = fileManager.copyAssetToInternalStorage(document.getFilePath());
            } else {
                // User-uploaded file - use directly
                file = new File(document.getFilePath());
            }

            if (file == null || !file.exists()) {
                showError();
                return;
            }

            pdfFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            pdfRenderer = new PdfRenderer(pdfFileDescriptor);
            pdfPageAdapter = new PdfPageAdapter(this, pdfRenderer);

            pdfRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            pdfRecyclerView.setAdapter(pdfPageAdapter);
            pdfRecyclerView.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
            hideLoading();

        } catch (Exception e) {
            hideLoading();
            showError();
            Log.e(TAG, "Error loading PDF", e);
        }
    }

    private void loadPPTX() {
        try {
            showLoading();

            File file;
            if (document.isFromAssets()) {
                // Copy file from assets to internal storage first
                file = fileManager.copyAssetToInternalStorage(document.getFilePath());
            } else {
                // User-uploaded file - use directly
                file = new File(document.getFilePath());
            }

            if (file == null || !file.exists()) {
                showError();
                return;
            }

            if (document.getType() == Document.DocumentType.PPTX) {
                loadPPTXInsideApp(file);
            } else {
                // Legacy PPT is not supported for in-app rendering yet
                pdfRecyclerView.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                setupWebView();
                String html = createPPTXPlaceholderHTML();
                webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
                hideLoading();
            }

        } catch (Exception e) {
            hideLoading();
            showError();
            Log.e(TAG, "Error loading PPTX", e);
        }
    }

    private void loadPPTXInsideApp(File file) {
        pdfRecyclerView.setVisibility(View.VISIBLE);
        webView.setVisibility(View.GONE);
        pdfRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        pptxExecutor.execute(() -> {
            List<PptxSlideAdapter.SlideItem> slides = new ArrayList<>();
            try (FileInputStream fis = new FileInputStream(file);
                 XMLSlideShow ppt = new XMLSlideShow(fis)) {
                List<XSLFSlide> pptSlides = ppt.getSlides();
                for (int i = 0; i < pptSlides.size(); i++) {
                    XSLFSlide slide = pptSlides.get(i);

                    StringBuilder text = new StringBuilder();
                    byte[] firstImage = null;

                    for (XSLFShape shape : slide.getShapes()) {
                        if (shape instanceof XSLFTextShape) {
                            String t = ((XSLFTextShape) shape).getText();
                            if (t != null) {
                                t = t.trim();
                                if (!t.isEmpty()) {
                                    if (text.length() > 0) text.append("\n\n");
                                    text.append(t);
                                }
                            }
                        } else if (firstImage == null && shape instanceof XSLFPictureShape) {
                            try {
                                firstImage = ((XSLFPictureShape) shape).getPictureData().getData();
                            } catch (Exception ignore) {
                                // keep null
                            }
                        }
                    }

                    slides.add(new PptxSlideAdapter.SlideItem(i + 1, text.toString(), firstImage));
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse PPTX", e);
            }

            runOnUiThread(() -> {
                if (isFinishing()) return;
                if (slides.isEmpty()) {
                    showError();
                    return;
                }
                pptxSlideAdapter = new PptxSlideAdapter(this);
                pptxSlideAdapter.setSlides(slides);
                pdfRecyclerView.setAdapter(pptxSlideAdapter);
                hideLoading();
            });
        });
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
        pdfRecyclerView.setVisibility(View.GONE);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_document_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_share) {
            shareDocument();
            return true;
        } else if (id == R.id.action_download) {
            downloadDocument();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void shareDocument() {
        try {
            File file;
            if (document.isFromAssets()) {
                file = fileManager.copyAssetToInternalStorage(document.getFilePath());
            } else {
                file = new File(document.getFilePath());
            }

            if (file != null && file.exists()) {
                Uri fileUri = FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".fileprovider",
                        file
                );

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType(document.getType() == Document.DocumentType.PDF
                        ? "application/pdf"
                        : "application/vnd.ms-powerpoint");
                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                startActivity(Intent.createChooser(shareIntent, "Ulashish"));
            } else {
                Toast.makeText(this, "Faylni ulashib bo'lmadi", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sharing document", e);
            Toast.makeText(this, "Xatolik yuz berdi", Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadDocument() {
        try {
            File file;
            if (document.isFromAssets()) {
                file = fileManager.copyAssetToInternalStorage(document.getFilePath());
            } else {
                file = new File(document.getFilePath());
            }

            if (file != null && file.exists()) {
                Toast.makeText(this, "Fayl saqlandi: " + file.getName(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Faylni saqlab bo'lmadi", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error downloading document", e);
            Toast.makeText(this, "Xatolik yuz berdi", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            pptxExecutor.shutdownNow();
        } catch (Exception ignore) {
        }
        try {
            if (pdfRenderer != null) {
                pdfRenderer.close();
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to close PdfRenderer", e);
        }
        try {
            if (pdfFileDescriptor != null) {
                pdfFileDescriptor.close();
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to close PDF file descriptor", e);
        }
    }
}