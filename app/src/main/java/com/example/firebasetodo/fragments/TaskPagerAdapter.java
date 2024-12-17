package com.example.firebasetodo.fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TaskPagerAdapter extends FragmentStateAdapter {

    private final String[] titles = new String[]{"All", "Pending", "Done"};

    public TaskPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return UndoneTasksFragment.newInstance();
            case 1:
                return DoneTasksFragment.newInstance();
            default:
                return AllTasksFragment.newInstance();
        }
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }
}
