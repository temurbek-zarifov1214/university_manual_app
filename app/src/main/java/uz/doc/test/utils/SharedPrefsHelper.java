package uz.doc.test.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SharedPrefsHelper {
    private static SharedPrefsHelper instance;
    private SharedPreferences prefs;
    private Gson gson;

    private SharedPrefsHelper(Context context) {
        prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static synchronized SharedPrefsHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefsHelper(context.getApplicationContext());
        }
        return instance;
    }

    // Favorites management
    public void addFavorite(String documentId) {
        Set<String> favorites = getFavorites();
        favorites.add(documentId);
        prefs.edit().putStringSet(Constants.PREFS_FAVORITES, favorites).apply();
    }

    public void removeFavorite(String documentId) {
        Set<String> favorites = getFavorites();
        favorites.remove(documentId);
        prefs.edit().putStringSet(Constants.PREFS_FAVORITES, favorites).apply();
    }

    public Set<String> getFavorites() {
        return new HashSet<>(prefs.getStringSet(Constants.PREFS_FAVORITES, new HashSet<>()));
    }

    public boolean isFavorite(String documentId) {
        return getFavorites().contains(documentId);
    }

    // Recent files management (stores file paths with timestamp)
    public void addRecentFile(String filePath) {
        List<String> recent = getRecentFiles();

        // Remove if already exists
        recent.remove(filePath);

        // Add to beginning
        recent.add(0, filePath);

        // Keep only last 20 items
        if (recent.size() > 20) {
            recent = recent.subList(0, 20);
        }

        String json = gson.toJson(recent);
        prefs.edit().putString(Constants.PREFS_RECENT, json).apply();
    }

    public List<String> getRecentFiles() {
        String json = prefs.getString(Constants.PREFS_RECENT, "[]");
        Type type = new TypeToken<List<String>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public void clearRecentFiles() {
        prefs.edit().remove(Constants.PREFS_RECENT).apply();
    }
}