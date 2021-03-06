package com.example.workflow_s.ui.checklist.adapter;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.workflow_s.ui.checklist.fragments.AllChecklistFragment;
import com.example.workflow_s.ui.checklist.fragments.CurrentChecklistFragment;

public class ChecklistPagerAdapter extends FragmentPagerAdapter {

    private int numberOfTabs, status;

    public void setStatus(int status) {
        this.status = status;
    }

    public ChecklistPagerAdapter(FragmentManager fm, int numberOfTabs) {
        super(fm);
        this.numberOfTabs = numberOfTabs;
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
//                Bundle args = new Bundle();
//                args.putInt("status_checklist_current", status);
//                CurrentChecklistFragment.newInstance().setArguments(args);
                return CurrentChecklistFragment.newInstance(status);
            case 1:
                return AllChecklistFragment.newInstance();
            default:
                    return null;
        } //end switch
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "My Checklist";
            case 1:
                return "Other";
                default:
                    return null;
        }
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }



}
