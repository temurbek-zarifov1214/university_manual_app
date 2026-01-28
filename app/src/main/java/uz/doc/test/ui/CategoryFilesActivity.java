package uz.doc.test.ui;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import uz.doc.test.R;
import uz.doc.test.adapter.DocumentAdapter;
import uz.doc.test.manager.FileManager;
import uz.doc.test.model.Category;
import uz.doc.test.model.Document;
import uz.doc.test.utils.Constants;
import uz.doc.test.utils.SharedPrefsHelper;
import uz.doc.test.viewer.DocumentViewerActivity;

import java.util.List;

public class CategoryFilesActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rvFiles;
    private LinearLayout emptyState;
    private DocumentAdapter documentAdapter;

    private FileManager fileManager;
    private SharedPrefsHelper prefsHelper;
    private Category category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_files);

        initViews();
        getCategoryFromIntent();
        setupToolbar();
        setupRecyclerView();
        loadFiles();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvFiles = findViewById(R.id.rv_files);
        emptyState = findViewById(R.id.empty_state);

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
}