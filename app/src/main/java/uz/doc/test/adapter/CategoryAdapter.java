package uz.doc.test.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import uz.doc.test.R;
import uz.doc.test.manager.FileManager;
import uz.doc.test.model.Category;
import uz.doc.test.model.Document;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Context context;
    private List<Category> categories;
    private OnCategoryClickListener listener;
    private FileManager fileManager;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(Context context, OnCategoryClickListener listener) {
        this.context = context;
        this.categories = new ArrayList<>();
        this.listener = listener;
        this.fileManager = FileManager.getInstance(context);
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivCategoryIcon;
        private TextView tvCategoryName;
        private TextView tvFileCount;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);

            ivCategoryIcon = itemView.findViewById(R.id.iv_category_icon);
            tvCategoryName = itemView.findViewById(R.id.tv_category_name);
            tvFileCount = itemView.findViewById(R.id.tv_file_count);
        }

        public void bind(Category category) {
            // Set category icon
            ivCategoryIcon.setImageResource(category.getIconResId());

            // Set category name (Uzbek)
            tvCategoryName.setText(category.getNameUz());

            // Count files in this category
            List<Document> documents = fileManager.getDocumentsFromCategory(category.getId());
            int fileCount = documents.size();

            String fileCountText = fileCount + " ta fayl";
            tvFileCount.setText(fileCountText);

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(category);
                }
            });
        }
    }
}