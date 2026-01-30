package uz.doc.test.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import uz.doc.test.R;
import uz.doc.test.adapter.DocumentAdapter;
import uz.doc.test.manager.FileManager;
import uz.doc.test.model.Category;
import uz.doc.test.model.Document;
import uz.doc.test.utils.Constants;
import uz.doc.test.utils.SharedPrefsHelper;
import uz.doc.test.viewer.DocumentViewerActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class CategoryFilesActivity extends AppCompatActivity {

    private static final String TAG = "CategoryFilesActivity";

    private Toolbar toolbar;
    private RecyclerView rvFiles;
    private LinearLayout emptyState;
    private FloatingActionButton fabAddFile;
    private DocumentAdapter documentAdapter;

    private FileManager fileManager;
    private SharedPrefsHelper prefsHelper;
    private Category category;

    private ActivityResultLauncher<String> filePickerLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_files);

        initViews();
        getCategoryFromIntent();
        setupToolbar();
        setupRecyclerView();
        setupFileUpload();
        loadFiles();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvFiles = findViewById(R.id.rv_files);
        emptyState = findViewById(R.id.empty_state);
        fabAddFile = findViewById(R.id.fab_add_file);

        fileManager = FileManager.getInstance(this);
        prefsHelper = SharedPrefsHelper.getInstance(this);
    }

    private void getCategoryFromIntent() {
        category = (Category) getIntent().getSerializableExtra(Constants.EXTRA_CATEGORY);

        if (category == null) {
            Toast.makeText(this, "Xatolik yuz berdi", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(category.getNameUz());
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvFiles.setLayoutManager(layoutManager);

        documentAdapter = new DocumentAdapter(this, new DocumentAdapter.OnDocumentClickListener() {
            @Override
            public void onDocumentClick(Document document) {
                openDocument(document);
            }

            @Override
            public void onFavoriteClick(Document document, boolean isFavorite) {
                handleFavoriteClick(document, isFavorite);
            }
        });

        rvFiles.setAdapter(documentAdapter);
    }

    private void setupFileUpload() {
        // Initialize file picker
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        handleFileSelected(uri);
                    }
                }
        );

        // Initialize permission launcher
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openFilePicker();
                    } else {
                        Toast.makeText(this, "Fayl tanlash uchun ruxsat kerak", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // FAB click listener
        fabAddFile.setOnClickListener(v -> requestFilePermissionAndPick());
    }

    private void requestFilePermissionAndPick() {
        // For Android 13+ we don't need READ_EXTERNAL_STORAGE
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            openFilePicker();
        } else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openFilePicker();
        } else {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private void openFilePicker() {
        filePickerLauncher.launch("application/pdf");
    }

    private void handleFileSelected(Uri uri) {
        try {
            // Get file name
            String fileName = getFileNameFromUri(uri);
            if (fileName == null || !fileName.toLowerCase().endsWith(".pdf")) {
                Toast.makeText(this, "Faqat PDF fayllar qo'shish mumkin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Copy file to app's internal storage in category folder
            File categoryDir = new File(getFilesDir(), "user_files/" + category.getId());
            if (!categoryDir.exists()) {
                categoryDir.mkdirs();
            }

            File destFile = new File(categoryDir, fileName);

            // Check if file already exists
            if (destFile.exists()) {
                Toast.makeText(this, "Bu nomdagi fayl allaqachon mavjud", Toast.LENGTH_SHORT).show();
                return;
            }

            // Copy file
            InputStream inputStream = getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(destFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            Toast.makeText(this, "Fayl qo'shildi: " + fileName, Toast.LENGTH_SHORT).show();

            // Reload files
            loadFiles();

        } catch (Exception e) {
            Log.e(TAG, "Error uploading file", e);
            Toast.makeText(this, "Faylni qo'shishda xatolik", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void loadFiles() {
        List<Document> documents = fileManager.getDocumentsFromCategory(category.getId());

        if (documents.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvFiles.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvFiles.setVisibility(View.VISIBLE);
            documentAdapter.setDocuments(documents);
        }
    }

    private void openDocument(Document document) {
        // Add to recent files
        prefsHelper.addRecentFile(document.getFilePath());

        // Open viewer activity
        Intent intent = new Intent(this, DocumentViewerActivity.class);
        intent.putExtra(Constants.EXTRA_DOCUMENT, document);
        startActivity(intent);
    }

    private void handleFavoriteClick(Document document, boolean isFavorite) {
        if (isFavorite) {
            prefsHelper.addFavorite(document.getId());
            Toast.makeText(this, "Sevimlilarga qo'shildi", Toast.LENGTH_SHORT).show();
        } else {
            prefsHelper.removeFavorite(document.getId());
            Toast.makeText(this, "Sevimlilardan o'chirildi", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload files when returning to this activity (in case favorites changed)
        loadFiles();
    }
}