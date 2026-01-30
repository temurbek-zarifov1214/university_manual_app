package uz.doc.test.ui.search;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchFragment extends Fragment {

    private EditText etSearch;
    private ImageView ivClearSearch;
    private RecyclerView rvSearchResults;
    private LinearLayout initialState;
    private LinearLayout noResultsState;
    private TextView tvSearchQuery;
    private DocumentAdapter documentAdapter;

    private FileManager fileManager;
    private SharedPrefsHelper prefsHelper;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService searchExecutor = Executors.newSingleThreadExecutor();
    private Runnable pendingSearchRunnable;
    private int searchToken = 0;

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
        ivClearSearch = view.findViewById(R.id.iv_clear_search);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        initialState = view.findViewById(R.id.initial_state);
        noResultsState = view.findViewById(R.id.no_results_state);
        tvSearchQuery = view.findViewById(R.id.tv_search_query);

        fileManager = FileManager.getInstance(requireContext());
        prefsHelper = SharedPrefsHelper.getInstance(requireContext());

        ivClearSearch.setOnClickListener(v -> etSearch.setText(""));
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
                scheduleSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Initial UI
        showInitialState();
    }

    private void scheduleSearch(String query) {
        final String trimmed = query == null ? "" : query.trim();

        // Clear icon visibility
        ivClearSearch.setVisibility(trimmed.isEmpty() ? View.GONE : View.VISIBLE);

        // Cancel previous scheduled search
        if (pendingSearchRunnable != null) {
            mainHandler.removeCallbacks(pendingSearchRunnable);
        }

        if (trimmed.isEmpty()) {
            showInitialState();
            return;
        }

        // Debounce typing (prevents UI stutter and repeated disk/asset scans)
        final int token = ++searchToken;
        pendingSearchRunnable = () -> runSearch(trimmed, token);
        mainHandler.postDelayed(pendingSearchRunnable, 250);
    }

    private void runSearch(String query, int token) {
        searchExecutor.execute(() -> {
            List<Document> results;
            try {
                results = fileManager.searchDocuments(query);
            } catch (Exception e) {
                results = new ArrayList<>();
            }

            final List<Document> finalResults = results;
            mainHandler.post(() -> {
                if (!isAdded() || token != searchToken) return;
                showResults(query, finalResults);
            });
        });
    }

    private void showInitialState() {
        initialState.setVisibility(View.VISIBLE);
        noResultsState.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.GONE);
    }

    private void showResults(String query, List<Document> results) {
        if (results == null || results.isEmpty()) {
            initialState.setVisibility(View.GONE);
            rvSearchResults.setVisibility(View.GONE);
            noResultsState.setVisibility(View.VISIBLE);
            if (tvSearchQuery != null) {
                tvSearchQuery.setText("\"" + query + "\"");
            }
        } else {
            noResultsState.setVisibility(View.GONE);
            initialState.setVisibility(View.GONE);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (pendingSearchRunnable != null) {
            mainHandler.removeCallbacks(pendingSearchRunnable);
            pendingSearchRunnable = null;
        }
        searchToken++;
    }
}