package uz.doc.test.model;

public class Category {
    private String id;
    private String name;
    private String nameUz; // Uzbek name
    private int iconResId;
    private String folderPath;

    public Category(String id, String name, String nameUz, int iconResId, String folderPath) {
        this.id = id;
        this.name = name;
        this.nameUz = nameUz;
        this.iconResId = iconResId;
        this.folderPath = folderPath;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNameUz() { return nameUz; }
    public void setNameUz(String nameUz) { this.nameUz = nameUz; }

    public int getIconResId() { return iconResId; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }

    public String getFolderPath() { return folderPath; }
    public void setFolderPath(String folderPath) { this.folderPath = folderPath; }
}