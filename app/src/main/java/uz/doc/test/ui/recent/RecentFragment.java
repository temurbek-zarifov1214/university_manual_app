package uz.doc.test.ui.recent;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import uz.doc.test.R;
import uz.doc.test.adapter.DocumentAdapter;
import uz.doc.test.manager.FileManager;
import uz.doc.test.model.Document;
import uz.doc.test.viewer.DocumentViewerActivity;
import uz.doc.test.utils.Constants;
import uz.doc.test.utils.SharedPrefsHelper;

import java.util.ArrayList;
import java.util.List;

public class RecentFragment extends Fragment {

    private RecyclerView rvRecent;
    private LinearLayout emptyState;
    private Button btnClearRecent;
    private DocumentAdapter documentAdapter;

    private FileManager fileManager;
    private SharedPrefsHelper prefsHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recent, container, false);

        initViews(view);
        setupRecyclerView();
        setupClearButton();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecentFiles();
    }

    private void initViews(View view) {
        rvRecent = view.findViewById(R.id.rv_recent);
        emptyState = view.findViewById(R.id.empty_state);
        btnClearRecent = view.findViewById(R.id.btn_clear_recent);

        fileManager = FileManager.getInstance(requireContext());
        prefsHelper = SharedPrefsHelper.getInstance(requireContext());
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        rvRecent.setLayoutManager(layoutManager);

        documentAdapter = new DocumentAdapter(requireContext(), new DocumentAdapter.OnDocumentClickListener() {
            @Override
            public void onDocumentClick(Document document) {
                openDocument(document);
            }

            @Override
            public void onFavoriteClick(Document document, boolean isFavorite) {
                handleFavoriteClick(document, isFavorite);
            }
        });

        rvRecent.setAdapter(documentAdapter);
    }

    private void setupClearButton() {
        btnClearRecent.setOnClickListener(v -> {
            prefsHelper.clearRecentFiles();
            loadRecentFiles();
            Toast.makeText(requireContext(), "So'nggi fayllar tozalandi", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadRecentFiles() {
        List<String> recentPaths = prefsHelper.getRecentFiles();
        List<Document> allDocuments = fileManager.getAllDocuments();
        List<Document> recentDocuments = new ArrayList<>();

        // Find documents by their file paths
        for (String path : recentPaths) {
            for (Document doc : allDocuments) {
                if (doc.getFilePath().equals(path)) {
                    recentDocuments.add(doc);
                    break;
                }
            }
        }

        if (recentDocuments.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvRecent.setVisibility(View.GONE);
            btnClearRecent.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvRecent.setVisibility(View.VISIBLE);
            btnClearRecent.setVisibility(View.VISIBLE);
            documentAdapter.setDocuments(recentDocuments);
        }
    }

    private void openDocument(Document document) {
        prefsHelper.addRecentFile(document.getFilePath());

        Intent intent = new Intent(requireContext(), DocumentViewerActivity.class);
        intent.putExtra(Constants.EXTRA_DOCUMENT, document);
        startActivity(intent);
    }

    private void handleFavoriteClick(Document document, boolean isFavorite) {
        if (isFavorite) {
            prefsHelper.addFavorite(document.getId());
            Toast.makeText(requireContext(), "Sevimlilarga qo'shildi", Toast.LENGTH_SHORT).show();
        } else {
            prefsHelper.removeFavorite(document.getId());
            Toast.makeText(requireContext(), "Sevimlilardan o'chirildi", Toast.LENGTH_SHORT).show();
        }
        loadRecentFiles();
    }
}