package uz.doc.test;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import uz.doc.test.ui.HomeFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupBottomNavigation();

        // Load home fragment by default
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            String title = "";

            int itemId = item.getItemId();

            if (itemId == R.id.navigation_home) {
                fragment = new HomeFragment();
                title = "Test";
            } else if (itemId == R.id.navigation_favorites) {
                // TODO: Create FavoritesFragment in Step 5
                // fragment = new FavoritesFragment();
                title = "Sevimlilar";
                return true; // Temporary
            } else if (itemId == R.id.navigation_recent) {
                // TODO: Create RecentFragment in Step 5
                // fragment = new RecentFragment();
                title = "So'nggi fayllar";
                return true; // Temporary
            } else if (itemId == R.id.navigation_search) {
                // TODO: Create SearchFragment in Step 5
                // fragment = new SearchFragment();
                title = "Qidiruv";
                return true; // Temporary
            }

            if (fragment != null) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(title);
                }
                loadFragment(fragment);
                return true;
            }

            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}