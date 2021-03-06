package com.example.workflow_s.ui.checklistrunning;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.workflow_s.R;
import com.example.workflow_s.model.Checklist;
import com.example.workflow_s.model.Template;
import com.example.workflow_s.ui.task.task_checklist.ChecklistTaskFragment;
import com.example.workflow_s.utils.CommonUtils;
import com.example.workflow_s.utils.SharedPreferenceUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Workflow_S
 * Created by TinhPV on 2019-07-05
 * Copyright © 2019 TinhPV. All rights reserved
 **/


// WE NEED TEMPLATE NAME, TEMPLATE ID

public class ChecklistRunningFragment extends Fragment implements ChecklistRunningContract.ChecklistRunningView, View.OnClickListener {

    View view;
    private int templateId;
    private String templateName, checklistName, templateUserId, orgId, userId;
    private TextView templateNameTextView;
    private EditText checklistNameEditText;
    private Button confirmRunButton;

    private ChecklistRunningContract.ChecklistRunningPresenter mRunningPresenter;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_checklistrunning, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getDataFromParent();
        initView();
    }

    private void getDataFromParent() {
        Bundle args = getArguments();
        templateId = args.getInt("templateId");
        templateName = args.getString("templateName");
        templateUserId = args.getString("templateUserId");

        orgId = SharedPreferenceUtils.retrieveData(getContext(), getString(R.string.pref_orgId));
        userId = SharedPreferenceUtils.retrieveData(getContext(), getString(R.string.pref_userId));
    }

    private void initView() {
        templateNameTextView = view.findViewById(R.id.tv_template_name);
        templateNameTextView.setText(templateName);
        checklistNameEditText = view.findViewById(R.id.edt_checklist_name);
        confirmRunButton = view.findViewById(R.id.bt_confirm);
        confirmRunButton.setOnClickListener(this);

        mRunningPresenter = new ChecklistRunningPresenterImpl(this, new ChecklistRunningInteractor());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_confirm) {
            handleRunChecklist();
        }
    }

    private void handleRunChecklist() {
        checklistName = checklistNameEditText.getText().toString();
        if (checklistName.isEmpty()) {
            Animation shakeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.shake_animation);
            checklistNameEditText.startAnimation(shakeAnimation);
        } else {
            // fixed - hardcode
            mRunningPresenter.getTemplateObject(String.valueOf(templateId), templateUserId, orgId);
        }
    }

    private String getTimeToRunChecklist() {
        Date oldDate = new Date(); // oldDate == current time
        final long hoursInMillis = 60L * 60L * 1000L;
        Date newDate = new Date(oldDate.getTime() +
                (24L * hoursInMillis)); // Adds 2 hours

        SimpleDateFormat sm = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        return sm.format(newDate);
    }

    @Override
    public void finishGetTemplateObject(Template template) {
        if (null != template) {
            template.setName(checklistName);
            template.setDueTime(getTimeToRunChecklist());
            mRunningPresenter.runChecklist(userId, template);
        }
    }

    @Override
    public void finishedRunChecklist(Checklist checklist) {
        Bundle args = new Bundle();

        String checklistId = String.valueOf(checklist.getId());
        args.putString("checklistId", checklistId);
        args.putInt("location", 1);
        args.putSerializable("listMember", checklist.getChecklistMembers());

        ((AppCompatActivity) getActivity()).getSupportActionBar().show();


        this.getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
        CommonUtils.replaceFragments_2(getContext(), ChecklistTaskFragment.class, args, true);
    }
}
