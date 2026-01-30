package uz.doc.test.ui.recent;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import uz.doc.test.utils.Constants;
import uz.doc.test.utils.SharedPrefsHelper;
import uz.doc.test.viewer.DocumentViewerActivity;

import java.util.List;

public class RecentFragment extends Fragment {

    private RecyclerView rvRecent;
    private LinearLayout emptyState;
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
        loadRecentFiles();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload recent files when fragment becomes visible
        loadRecentFiles();
    }

    private void initViews(View view) {
        rvRecent = view.findViewById(R.id.rv_recent);
        emptyState = view.findViewById(R.id.empty_state);

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

    private void loadRecentFiles() {
        List<String> recentPaths = prefsHelper.getRecentFiles();

        if (recentPaths.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvRecent.setVisibility(View.GONE);
        } else {
            List<Document> recentDocuments = fileManager.getDocumentsByPaths(recentPaths);

            if (recentDocuments.isEmpty()) {
                emptyState.setVisibility(View.VISIBLE);
                rvRecent.setVisibility(View.GONE);
            } else {
                emptyState.setVisibility(View.GONE);
                rvRecent.setVisibility(View.VISIBLE);
                documentAdapter.setDocuments(recentDocuments);
            }
        }
    }

    private void openDocument(Document document) {
        // Add to recent files (moves to top if already exists)
        prefsHelper.addRecentFile(document.getFilePath());

        // Open viewer activity
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
    }
}