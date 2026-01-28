package uz.doc.test.manager;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import uz.doc.test.model.Document;
import uz.doc.test.utils.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class FileManager {
    private static final String TAG = "FileManager";
    private static FileManager instance;
    private Context context;
    private AssetManager assetManager;
    private Map<String, List<Document>> categoryCache;
    private FileManager(Context context) {
        this.context = context.getApplicationContext();
        this.assetManager = context.getAssets();
        this.categoryCache = new HashMap<>();
    }

    public static synchronized FileManager getInstance(Context context) {
        if (instance == null) {
            instance = new FileManager(context);
        }
        return instance;
    }

    /**
     * Get all documents from a specific category folder
     */
    public List<Document> getDocumentsFromCategory(String categoryId) {
        if (categoryCache.containsKey(categoryId)) {
            return new ArrayList<>(Objects.requireNonNull(categoryCache.get(categoryId)));
        }
        List<Document> documents = new ArrayList<>();
        String folderPath = Constants.ASSETS_BASE_PATH + "/" + categoryId;

        try {
            String[] files = assetManager.list(folderPath);

            if (files != null) {
                for (String fileName : files) {
                    // Skip hidden files and directories
                    if (fileName.startsWith(".")) {
                        continue;
                    }

                    Document.DocumentType type = getDocumentType(fileName);
                    if (type != null) {
                        Document doc = new Document();
                        doc.setId(UUID.randomUUID().toString());
                        doc.setTitle(getFileNameWithoutExtension(fileName));
                        doc.setFilePath(folderPath + "/" + fileName);
                        doc.setCategoryId(categoryId);
                        doc.setType(type);

                        documents.add(doc);
                    }
                }
            }

            // Sort by title
            documents.sort((d1, d2) -> d1.getTitle().compareToIgnoreCase(d2.getTitle()));
            categoryCache.put(categoryId, documents);
        } catch (IOException e) {
            Log.e(TAG, "Error reading files from category: " + categoryId, e);
        }

        return documents;
    }

    /**
     * Get all documents from all categories (for search)
     */
    public List<Document> getAllDocuments() {
        List<Document> allDocuments = new ArrayList<>();

        String[] categories = {
                Constants.CATEGORY_MARUZALAR,
                Constants.CATEGORY_ADABIYOTLAR,
                Constants.CATEGORY_LABARATORIYA,
                Constants.CATEGORY_AMALIY_MASHGULOT,
                Constants.CATEGORY_MASALALAR,
                Constants.CATEGORY_SILLABUS
        };

        for (String categoryId : categories) {
            allDocuments.addAll(getDocumentsFromCategory(categoryId));
        }

        return allDocuments;
    }

    /**
     * Search documents by title
     */
    public List<Document> searchDocuments(String query) {
        List<Document> allDocs = getAllDocuments();
        List<Document> results = new ArrayList<>();

        String lowerQuery = query.toLowerCase().trim();

        for (Document doc : allDocs) {
            if (doc.getTitle().toLowerCase().contains(lowerQuery)) {
                results.add(doc);
            }
        }

        return results;
    }

    /**
     * Copy file from assets to internal storage (for offline access)
     */
    public File copyAssetToInternalStorage(String assetPath) {
        try {
            String fileName = assetPath.substring(assetPath.lastIndexOf("/") + 1);
            File outputDir = new File(context.getFilesDir(), "documents");

            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            File outputFile = new File(outputDir, fileName);

            // If file already exists, return it
            if (outputFile.exists()) {
                return outputFile;
            }

            // Copy from assets to internal storage
            InputStream in = assetManager.open(assetPath);
            OutputStream out = new FileOutputStream(outputFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            in.close();
            out.close();

            Log.d(TAG, "File copied successfully: " + outputFile.getAbsolutePath());
            return outputFile;

        } catch (IOException e) {
            Log.e(TAG, "Error copying asset to internal storage", e);
            return null;
        }
    }

    /**
     * Get InputStream from asset file
     */
    public InputStream getAssetInputStream(String assetPath) {
        try {
            return assetManager.open(assetPath);
        } catch (IOException e) {
            Log.e(TAG, "Error opening asset: " + assetPath, e);
            return null;
        }
    }

    /**
     * Check if file exists in assets
     */
    public boolean assetExists(String assetPath) {
        try {
            InputStream stream = assetManager.open(assetPath);
            stream.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Get document type from file extension
     */
    private Document.DocumentType getDocumentType(String fileName) {
        String lowerFileName = fileName.toLowerCase();

        if (lowerFileName.endsWith(Constants.EXT_PDF)) {
            return Document.DocumentType.PDF;
        } else if (lowerFileName.endsWith(Constants.EXT_PPTX)) {
            return Document.DocumentType.PPTX;
        } else if (lowerFileName.endsWith(Constants.EXT_PPT)) {
            return Document.DocumentType.PPT;
        }

        return null;
    }

    /**
     * Get file name without extension
     */
    private String getFileNameWithoutExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0) {
            return fileName.substring(0, lastDotIndex);
        }
        return fileName;
    }

    /**
     * Get file size in human-readable format
     */
    public String getFileSizeString(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}