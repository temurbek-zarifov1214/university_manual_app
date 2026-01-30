package uz.doc.test.utils;

public class Constants {
    // Asset folder paths
    /**
     * Base folder inside /assets.
     *
     * Your current project keeps category folders directly under /assets
     * (e.g. assets/maruzalar, assets/adabiyotlar, ...), so this must be empty.
     */
    public static final String ASSETS_BASE_PATH = "";

    public static final String CATEGORY_MARUZALAR = "maruzalar";
    public static final String CATEGORY_ADABIYOTLAR = "adabiyotlar";
    public static final String CATEGORY_LABARATORIYA = "labaratoriya";
    // Folder name in assets is "amaliy"
    public static final String CATEGORY_AMALIY_MASHGULOT = "amaliy";
    public static final String CATEGORY_MASALALAR = "masalalar";
    public static final String CATEGORY_SILLABUS = "sillabus";

    // SharedPreferences keys
    public static final String PREFS_NAME = "TestAppPrefs";
    public static final String PREFS_FAVORITES = "favorites";
    public static final String PREFS_RECENT = "recent_files";

    // File extensions
    public static final String EXT_PDF = ".pdf";
    public static final String EXT_PPTX = ".pptx";
    public static final String EXT_PPT = ".ppt";

    // Intent extras
    public static final String EXTRA_DOCUMENT = "extra_document";
    public static final String EXTRA_CATEGORY = "extra_category";
    public static final String EXTRA_CATEGORY_ID = "extra_category_id";

    /**
     * Build an AssetManager-compatible relative path (no leading slash).
     */
    public static String assetCategoryPath(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) return "";
        if (ASSETS_BASE_PATH == null || ASSETS_BASE_PATH.trim().isEmpty()) return categoryId;
        return ASSETS_BASE_PATH + "/" + categoryId;
    }
}