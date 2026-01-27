package uz.doc.test.manager;

import android.content.Context;

import uz.doc.test.R;
import uz.doc.test.model.Category;
import uz.doc.test.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class CategoryManager {
    private static CategoryManager instance;
    private Context context;
    private List<Category> categories;

    private CategoryManager(Context context) {
        this.context = context.getApplicationContext();
        initializeCategories();
    }

    public static synchronized CategoryManager getInstance(Context context) {
        if (instance == null) {
            instance = new CategoryManager(context);
        }
        return instance;
    }

    /**
     * Initialize all categories with their icons and folder paths
     */
    private void initializeCategories() {
        categories = new ArrayList<>();

        // Maruzalar (Lectures)
        categories.add(new Category(
                Constants.CATEGORY_MARUZALAR,
                "Lectures",
                "Ma'ruzalar",
                R.drawable.ic_lecture,
                Constants.ASSETS_BASE_PATH + "/" + Constants.CATEGORY_MARUZALAR
        ));

        // Adabiyotlar (Literature)
        categories.add(new Category(
                Constants.CATEGORY_ADABIYOTLAR,
                "Literature",
                "Adabiyotlar",
                R.drawable.ic_book,
                Constants.ASSETS_BASE_PATH + "/" + Constants.CATEGORY_ADABIYOTLAR
        ));

        // Labaratoriya (Laboratory)
        categories.add(new Category(
                Constants.CATEGORY_LABARATORIYA,
                "Laboratory",
                "Labaratoriya",
                R.drawable.ic_lab,
                Constants.ASSETS_BASE_PATH + "/" + Constants.CATEGORY_LABARATORIYA
        ));

        // Amaliy mashg'ulot (Practical classes)
        categories.add(new Category(
                Constants.CATEGORY_AMALIY_MASHGULOT,
                "Practical",
                "Amaliy mashg'ulot",
                R.drawable.ic_practise,
                Constants.ASSETS_BASE_PATH + "/" + Constants.CATEGORY_AMALIY_MASHGULOT
        ));

        // Masalalar (Problems/Exercises)
        categories.add(new Category(
                Constants.CATEGORY_MASALALAR,
                "Problems",
                "Masalalar",
                R.drawable.ic_problem,
                Constants.ASSETS_BASE_PATH + "/" + Constants.CATEGORY_MASALALAR
        ));

        // Sillabus (Syllabus)
        categories.add(new Category(
                Constants.CATEGORY_SILLABUS,
                "Syllabus",
                "Sillabus",
                R.drawable.ic_syllabus,
                Constants.ASSETS_BASE_PATH + "/" + Constants.CATEGORY_SILLABUS
        ));
    }

    /**
     * Get all categories
     */
    public List<Category> getAllCategories() {
        return new ArrayList<>(categories);
    }

    /**
     * Get category by ID
     */
    public Category getCategoryById(String categoryId) {
        for (Category category : categories) {
            if (category.getId().equals(categoryId)) {
                return category;
            }
        }
        return null;
    }

    /**
     * Get category name in Uzbek
     */
    public String getCategoryNameUz(String categoryId) {
        Category category = getCategoryById(categoryId);
        return category != null ? category.getNameUz() : "";
    }

    /**
     * Get category icon resource
     */
    public int getCategoryIcon(String categoryId) {
        Category category = getCategoryById(categoryId);
        return category != null ? category.getIconResId() : 0;
    }
}