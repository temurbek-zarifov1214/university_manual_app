package uz.doc.test.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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

public class SearchFragment extends Fragment {

    private EditText etSearch;
    private RecyclerView rvSearchResults;
    private LinearLayout emptyState;
    private DocumentAdapter documentAdapter;

    private FileManager fileManager;
    private SharedPrefsHelper prefsHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        initViews(view);
        setupRecyclerView();
        setupSearch();

        return view;
    }

    private void initViews(View view) {
        etSearch = view.findViewById(R.id.et_search);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        emptyState = view.findViewById(R.id.empty_state);

        fileManager = FileManager.getInstance(requireContext());
        prefsHelper = SharedPrefsHelper.getInstance(requireContext());
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        rvSearchResults.setLayoutManager(layoutManager);

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

        rvSearchResults.setAdapter(documentAdapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void performSearch(String query) {
        if (query.trim().isEmpty()) {
            // Show empty state when no query
            emptyState.setVisibility(View.VISIBLE);
            rvSearchResults.setVisibility(View.GONE);
            return;
        }

        List<Document> results = fileManager.searchDocuments(query);

        if (results.isEmpty()) {
            // Show empty state when no results
            emptyState.setVisibility(View.VISIBLE);
            rvSearchResults.setVisibility(View.GONE);
        } else {
            // Show results
            emptyState.setVisibility(View.GONE);
            rvSearchResults.setVisibility(View.VISIBLE);
            documentAdapter.setDocuments(results);
        }
    }

    private void openDocument(Document document) {
        // Add to recent files
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