package uz.doc.test.ui.favorites;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FavoritesFragment extends Fragment {

    private RecyclerView rvFavorites;
    private LinearLayout emptyState;
    private DocumentAdapter documentAdapter;

    private FileManager fileManager;
    private SharedPrefsHelper prefsHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        initViews(view);
        setupRecyclerView();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void initViews(View view) {
        rvFavorites = view.findViewById(R.id.rv_favorites);
        emptyState = view.findViewById(R.id.empty_state);

        fileManager = FileManager.getInstance(requireContext());
        prefsHelper = SharedPrefsHelper.getInstance(requireContext());
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        rvFavorites.setLayoutManager(layoutManager);

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

        rvFavorites.setAdapter(documentAdapter);
    }

    private void loadFavorites() {
        Set<String> favoriteIds = prefsHelper.getFavorites();
        List<Document> allDocuments = fileManager.getAllDocuments();
        List<Document> favoriteDocuments = new ArrayList<>();

        // Filter documents that are in favorites
        for (Document doc : allDocuments) {
            if (favoriteIds.contains(doc.getId())) {
                doc.setFavorite(true);
                favoriteDocuments.add(doc);
            }
        }

        if (favoriteDocuments.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvFavorites.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvFavorites.setVisibility(View.VISIBLE);
            documentAdapter.setDocuments(favoriteDocuments);
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
            // Refresh the list
            loadFavorites();
        }
    }
}