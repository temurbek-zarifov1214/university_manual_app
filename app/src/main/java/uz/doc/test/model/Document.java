package uz.doc.test.model;

import java.io.Serializable;

public class Document implements Serializable {
    private String id;
    private String title;
    private String filePath;
    private String categoryId;
    private DocumentType type;
    private long fileSize;
    private long lastModified;
    private boolean isFavorite;

    public enum DocumentType {
        PDF, PPTX, PPT
    }

    public Document() {
        this.isFavorite = false;
    }

    public Document(String id, String title, String filePath, String categoryId, DocumentType type) {
        this.id = id;
        this.title = title;
        this.filePath = filePath;
        this.categoryId = categoryId;
        this.type = type;
        this.isFavorite = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public DocumentType getType() { return type; }
    public void setType(DocumentType type) { this.type = type; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public long getLastModified() { return lastModified; }
    public void setLastModified(long lastModified) { this.lastModified = lastModified; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public String getFileExtension() {
        return type.name().toLowerCase();
    }
}