package uz.doc.test.ui.search;

import android.content.Intent;
import android.os.Bundle;
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
import uz.doc.test.viewer.DocumentViewerActivity;
import uz.doc.test.utils.Constants;
import uz.doc.test.utils.SharedPrefsHelper;

import java.util.List;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        initViews(view);
        setupSearchBar();
        setupRecyclerView();

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
    }

    private void setupSearchBar() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();

                if (query.isEmpty()) {
                    ivClearSearch.setVisibility(View.GONE);
                    showInitialState();
                } else {
                    ivClearSearch.setVisibility(View.VISIBLE);
                    performSearch(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ivClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            etSearch.clearFocus();
        });
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

    private void performSearch(String query) {
        List<Document> results = fileManager.searchDocuments(query);

        if (results.isEmpty()) {
            showNoResults(query);
        } else {
            showResults(results);
        }
    }

    private void showInitialState() {
        initialState.setVisibility(View.VISIBLE);
        noResultsState.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.GONE);
    }

    private void showResults(List<Document> results) {
        initialState.setVisibility(View.GONE);
        noResultsState.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.VISIBLE);
        documentAdapter.setDocuments(results);
    }

    private void showNoResults(String query) {
        initialState.setVisibility(View.GONE);
        noResultsState.setVisibility(View.VISIBLE);
        rvSearchResults.setVisibility(View.GONE);
        tvSearchQuery.setText("\"" + query + "\" bo'yicha hech narsa topilmadi");
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
    }
}