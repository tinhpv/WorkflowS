package com.example.workflow_s.utils;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.example.workflow_s.R;

/**
 * Workflow_S
 * Created by TinhPV on 2019-06-30
 * Copyright © 2019 TinhPV. All rights reserved
 **/


public class CommonUtils {

    public static void replaceFragments(Context fragmentContext, Class fragmentClass, Bundle args) {

        Fragment fragment = null;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Insert the fragment by replacing any existing fragment
        if (null != args) {
            fragment.setArguments(args);
        }

        FragmentManager fragmentManager = ((FragmentActivity)fragmentContext).getSupportFragmentManager();

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_in_right);

        transaction
                .add(R.id.flContent, fragment)
                .addToBackStack(null)
                .commit();

//        fragmentManager
//                .beginTransaction()
//                .add(R.id.flContent, fragment)
//                .addToBackStack(null)
//                .commit();

    }

}
