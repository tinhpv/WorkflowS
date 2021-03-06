package com.example.workflow_s.ui.task.task_template;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.example.workflow_s.R;
import com.example.workflow_s.model.Task;
import com.example.workflow_s.ui.checklistrunning.ChecklistRunningFragment;
import com.example.workflow_s.ui.task.TaskContract;
import com.example.workflow_s.ui.task.TaskInteractor;
import com.example.workflow_s.ui.task.TaskStatusPresenterImpl;
import com.example.workflow_s.ui.task.adapter.TemplateTaskAdapter;
import com.example.workflow_s.utils.CommonUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

/**
 * Workflow_S
 * Created by TinhPV on 2019-07-03
 * Copyright © 2019 TinhPV. All rights reserved
 **/


public class TemplateTaskFragment extends Fragment implements TaskContract.TemplateView, View.OnClickListener {

    private static final String TAG = "TEMPLATE_TASK_FRAGMENT";

    View view;
    private FloatingActionButton btnRunChecklist;
    private TextView mTemplateDescription, mTemplateName;
    private RecyclerView templateTaskRecyclerView;
    private TemplateTaskAdapter mTemplateTaskAdapter;
    private RecyclerView.LayoutManager taskLayoutManager;

    private TaskContract.TaskPresenter mPresenter;
    private int templateId;
    private String templateName, templateDescription, templateUserId;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_task_template, container, false);
        getActivity().setTitle("Template's Tasks");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initRV();
        initData();
        initUI();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }


    private void initData() {
        // GET NECESSARY DATA FROM PARENT
        Bundle arguments = getArguments();
        templateId = Integer.parseInt(arguments.getString("templateId"));
        templateName = arguments.getString("templateName");
        templateDescription = arguments.getString("templateDescription");
        templateUserId = arguments.getString("templateUserId");

        // request data from server
        mPresenter = new TaskStatusPresenterImpl(this, new TaskInteractor());
        mPresenter.loadTasks(templateId);
    }

    private void initUI() {
        btnRunChecklist = view.findViewById(R.id.bt_run_checklist);
        btnRunChecklist.setOnClickListener(this);
        mTemplateDescription = view.findViewById(R.id.tv_template_description);
        mTemplateDescription.setText(templateDescription);
        mTemplateName = view.findViewById(R.id.tv_template_name);
        mTemplateName.setText(templateName);
    }

    private void initRV() {
        templateTaskRecyclerView = view.findViewById(R.id.rv_template_task);
        templateTaskRecyclerView.setHasFixedSize(true);
        taskLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        templateTaskRecyclerView.setLayoutManager(taskLayoutManager);

        mTemplateTaskAdapter = new TemplateTaskAdapter(this);
        templateTaskRecyclerView.setAdapter(mTemplateTaskAdapter);
    }

    @Override
    public void setDataToTaskRecyclerView(ArrayList<Task> datasource) {
        mTemplateTaskAdapter.setTemplateTaskList(datasource);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_run_checklist) {
            Bundle args = new Bundle();
            args.putInt("templateId", templateId);
            args.putString("templateName", templateName);
            args.putString("templateUserId", templateUserId);

//            ChecklistRunningFragment fragment = new ChecklistRunningFragment();
//            fragment.setDataFromParent(args);
//            this.getChildFragmentManager().beginTransaction().replace(R.id.parent_container, fragment).commit();
            CommonUtils.replaceFragments_2(getActivity(), ChecklistRunningFragment.class, args, true);
        } // end if
    }

}
