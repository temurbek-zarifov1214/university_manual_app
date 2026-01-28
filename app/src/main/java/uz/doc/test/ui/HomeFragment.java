package uz.doc.test.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import uz.doc.test.R;
import uz.doc.test.adapter.CategoryAdapter;  // ← ADD THIS IMPORT
import uz.doc.test.manager.CategoryManager;
import uz.doc.test.model.Category;

import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView rvCategories;
    private CategoryAdapter categoryAdapter;
    private CategoryManager categoryManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        setupRecyclerView();
        loadCategories();

        return view;
    }

    private void initViews(View view) {
        rvCategories = view.findViewById(R.id.rv_categories);
        categoryManager = CategoryManager.getInstance(requireContext());
    }

    private void setupRecyclerView() {
        // 2 columns grid
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        rvCategories.setLayoutManager(layoutManager);

        categoryAdapter = new CategoryAdapter(requireContext(), category -> {
            // Open CategoryFilesActivity
            Intent intent = new Intent(requireContext(), CategoryFilesActivity.class);
            intent.putExtra(uz.doc.test.utils.Constants.EXTRA_CATEGORY, category);
            startActivity(intent);
        });

        rvCategories.setAdapter(categoryAdapter);
    }

    private void loadCategories() {
        List<Category> categories = categoryManager.getAllCategories();
        categoryAdapter.setCategories(categories);
    }
}