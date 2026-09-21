package com.example.householdfoodinventory.UI;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * StoragePagerAdapter
 *
 * Responsibilities:
 *  - Provide the correct StorageFragment for each tab position.
 *  - Expose the number of storage areas to ViewPager2.
 *
 * Architectural Role:
 *  - SRP: Only handles fragment creation for the tabbed interface.
 *  - Decouples MainActivity from fragment instantiation.
 *  - Enables scalable UI (adding new storage areas only requires updating AREAS).
 */
public class StoragePagerAdapter extends FragmentStateAdapter {

    // Storage areas represented by each tab
    private static final String[] AREAS = {"fridge", "freezer", "cupboard"};

    public StoragePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Create a new fragment instance with the correct area argument
        return StorageFragment.newInstance(AREAS[position]);
    }

    @Override
    public int getItemCount() {
        return AREAS.length; // Always 3
    }

    /**
     * Helper method used by MainActivity to determine which area
     * the FAB should pass to AddItemActivity.
     */
    public String getAreaForPosition(int position) {
        return AREAS[position];
    }
}
