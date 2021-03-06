package com.example.workflow_s.ui.task.task_checklist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.workflow_s.R;
import com.example.workflow_s.model.Checklist;
import com.example.workflow_s.model.ChecklistMember;
import com.example.workflow_s.model.Task;
import com.example.workflow_s.model.TaskMember;
import com.example.workflow_s.model.User;
import com.example.workflow_s.ui.checklist.ChecklistFragment;
import com.example.workflow_s.ui.task.ItemTouchListener;
import com.example.workflow_s.ui.task.TaskContract;
import com.example.workflow_s.ui.task.TaskInteractor;
import com.example.workflow_s.ui.task.TaskStatusPresenterImpl;
import com.example.workflow_s.ui.task.adapter.ChecklistTaskAdapter;
import com.example.workflow_s.ui.task.adapter.SimpleItemTouchHelperCallback;
import com.example.workflow_s.ui.task.dialog.assignment.AssigningDialogFragment;
import com.example.workflow_s.ui.task.dialog.time_setting.TimeSettingDialogFragment;
import com.example.workflow_s.utils.CommonUtils;
import com.example.workflow_s.utils.DateUtils;
import com.example.workflow_s.utils.SharedPreferenceUtils;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Workflow_S
 * Created by TinhPV on 2019-06-30
 * Copyright © 2019 TinhPV. All rights reserved
 **/


public class ChecklistTaskFragment extends Fragment
        implements TaskContract.TaskView, ChecklistTaskAdapter.CheckboxListener,
                    View.OnClickListener,
                    ChecklistTaskAdapter.MenuListener, ChecklistTaskAdapter.DragDropListener {

    private static final String TAG = "TASK_FRAGMENT";

    View view;
    private ImageButton completeChecklistButton;
    private TextView mChecklistDescription, mChecklistName, mUsername, mTimeCreated, mChecklistCompletedAlert;
    private CircleImageView userImg;
    private RecyclerView checklistTaskRecyclerView;
    private ChecklistTaskAdapter mChecklistChecklistTaskAdapter;
    private RecyclerView.LayoutManager taskLayoutManager;

    private TaskContract.TaskPresenter mPresenter;
    private int checklistId;
    private String checklistName, checklistDescription, checklistUserId, userId, orgId, checklistStatus, timeCreatedString;

    private int totalTask, doneTask, location;
    private List<ChecklistMember> checklistMembers;
    private List<Task> tasks, tasksPriority;
    private HashMap<Integer, Integer> taskPositions;
    private boolean isChecklistMember;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_task_detail, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public void onDestroyView() {
        switch (location) {
            case 1:
                getActivity().setTitle("Home");
                break;
            case 2:
                getActivity().setTitle("Active checklist");
                break;
        }
        tasksPriority = new ArrayList<>();
        tasksPriority = mChecklistChecklistTaskAdapter.getTaskList();
        mPresenter.changePriorityTaskList(tasksPriority);
        super.onDestroyView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        switch (item.getItemId()) {
            case R.id.action_delete:
                if (userId.equals(checklistUserId)) {
                    handleShowConfirmDialog(checklistId);
                } else {
                    final Dialog errorDialog = new Dialog(getContext());
                    errorDialog.setContentView(R.layout.dialog_error_task);
                    errorDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    TextView msg = errorDialog.findViewById(R.id.tv_error_message);
                    msg.setText("You're not the owner, so cannot delete!");

                    Button btnOk = errorDialog.findViewById(R.id.btn_ok);
                    btnOk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            errorDialog.cancel();
                        }
                    });
                    errorDialog.show();
                }

                return true;
            case R.id.action_rename:
                if (userId.equals(checklistUserId)) {
                    prepareShowRenameDialog(checklistId, checklistName);
                } else {
                    final Dialog errorDialog = new Dialog(getContext());
                    errorDialog.setContentView(R.layout.dialog_error_task);
                    errorDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    TextView msg = errorDialog.findViewById(R.id.tv_error_message);
                    msg.setText("You're not the owner, so cannot rename!");

                    Button btnOk = errorDialog.findViewById(R.id.btn_ok);
                    btnOk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            errorDialog.cancel();
                        }
                    });
                    errorDialog.show();
                }

                return true;
            case R.id.action_set_time:
                TimeSettingDialogFragment settingDialogFragment
                        = TimeSettingDialogFragment.newInstance(checklistId, checklistUserId, checklistMembers);
                settingDialogFragment.show(fm, "fragment_set_time");
                return true;

            case R.id.action_assign:
                // convert List to ArrayList so that we can store it in Bundle
                AssigningDialogFragment assigningDialogFragment
                        = AssigningDialogFragment.newInstance(checklistUserId, checklistId);
                assigningDialogFragment.show(fm, "fragment_assign_user");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_task_cheklist, container, false);
        getActivity().setTitle("");
        //((AppCompatActivity) getActivity()).getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFFFFF")));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        completeChecklistButton = view.findViewById(R.id.bt_complete_checklist);
        completeChecklistButton.setOnClickListener(this);
        mChecklistDescription = view.findViewById(R.id.tv_task_description);
        mChecklistName = view.findViewById(R.id.tv_task_name);
        mChecklistCompletedAlert = view.findViewById(R.id.checklist_completed);
        userImg = view.findViewById(R.id.img_user_avatar);
        mUsername = view.findViewById(R.id.tv_user_display_name);
        mTimeCreated = view.findViewById(R.id.tv_time_created);

        setupTaskRV();
        initData();
    }

    private void setupTaskRV() {
        checklistTaskRecyclerView = view.findViewById(R.id.rv_checklist_task);
        checklistTaskRecyclerView.setHasFixedSize(true);
        taskLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        checklistTaskRecyclerView.setLayoutManager(taskLayoutManager);

        mChecklistChecklistTaskAdapter = new ChecklistTaskAdapter(getContext(), this, this, this);
        checklistTaskRecyclerView.setAdapter(mChecklistChecklistTaskAdapter);
        mChecklistChecklistTaskAdapter.setChecklistStatus("Checklist");
    }

    public void initData() {

        taskPositions = new HashMap<>();

        // GET NECESSARY DATA FROM PARENT
        Bundle arguments = getArguments();
        checklistId = Integer.parseInt(arguments.getString("checklistId"));
        location = arguments.getInt("location");
        checklistMembers = new ArrayList<>();
//        checklistMembers = (ArrayList<ChecklistMember>) arguments.getSerializable("listMember");

        // USER's DATA
        userId = SharedPreferenceUtils.retrieveData(getContext(), getString(R.string.pref_userId));
        orgId = SharedPreferenceUtils.retrieveData(getContext(), getString(R.string.pref_orgId));
        mChecklistChecklistTaskAdapter.setChecklistId(checklistId);

        // OK - HARDCODE HERE
        mPresenter = new TaskStatusPresenterImpl(this, new TaskInteractor());
        mPresenter.getUserList(Integer.parseInt(orgId));

    }


    @Override
    public void finishGetChecklist(Checklist checklist) {
        if (null != checklist) {
            checklistName = checklist.getName();
            checklistStatus = checklist.getTemplateStatus();
            checklistDescription = checklist.getDescription();
            checklistUserId = checklist.getUserId();
            timeCreatedString = checklist.getTimeCreated();
            totalTask = checklist.getTotalTask();
            doneTask = checklist.getDoneTask();

            mChecklistChecklistTaskAdapter.setChecklistStatus(checklistStatus);

            checklistMembers = checklist.getChecklistMembers();
            mChecklistChecklistTaskAdapter.setChecklistMembers(checklistMembers);

            mChecklistName.setText(checklistName);
            mChecklistDescription.setText(checklistDescription);

            mChecklistChecklistTaskAdapter.setChecklistUserId(checklistUserId);
            updateButtonAppearanceByStatus(checklistStatus);

            // invalidate data
            if ((doneTask != totalTask) && (checklistStatus.equals("Done"))) {
                handleCompleteChecklist("Checklist");
            }

            if (checklistStatus.equals("Done")) {
                mChecklistCompletedAlert.setVisibility(View.VISIBLE);
            } else {
                mChecklistCompletedAlert.setVisibility(View.GONE);
            }
            mPresenter.getUserInfor(checklistUserId);
        }
    }

    @Override
    public void finishedLoadAllTasks(List<Task> taskList) {
        if (null != taskList) {
            this.tasks = taskList;
            mChecklistChecklistTaskAdapter.setTaskList(tasks);
            addItemTouchCallback(checklistTaskRecyclerView);
        }
    }

    @Override
    public void finishGetUserList(List<User> userList) {
        if (userList != null) {
            mChecklistChecklistTaskAdapter.setUserList(userList);

            mPresenter.loadChecklistData(Integer.parseInt(orgId), checklistId);
            mPresenter.loadTasks(checklistId);
        }
    }

    @Override
    public void finishGetInfo(User user) {
        if (null != user) {
            Glide.with(getContext()).load(user.getAvatar()).into(userImg);
            mUsername.setText(user.getName());

            String timeCreated = timeCreatedString.split("T")[0];
            Date dateCreate = DateUtils.parseDate(timeCreated);
            String monthString = (String) DateFormat.format("MMM", dateCreate);
            String day = (String) DateFormat.format("dd", dateCreate);
            mTimeCreated.setText(monthString + " " + day);
        }

    }

    private void updateButtonAppearanceByStatus(String status) {
        if (status.equals("Done")) { // done
            completeChecklistButton.setVisibility(View.GONE);
        } else { // running
            completeChecklistButton.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onEventCheckBox(Boolean isSelected, int taskId, int position) {
        if (!checklistStatus.equals("Done")) {
            if (userId.equals(checklistUserId)) {
                handleTickTaskOnChecklistTaskFragment(taskId, isSelected, position);
            } else {
                taskPositions.put(taskId, position);
                mPresenter.getTaskMember(taskId, isSelected);
            } // end if
        } // end if
    }

    @Override
    public void finishRenameChecklist() {

    }

    @Override
    public void finishDeleteChecklist() {

        Bundle args = new Bundle();
        args.putInt("status_checklist", 0);
        CommonUtils.replaceFragments(getContext(), ChecklistFragment.class, args, false);
    }

    @Override
    public void onClickMenu(int taskId, String taskName, String action) {

        if (action.equals(getString(R.string.item_action_rename))) { // RENAME
            prepareDisplayRenameDialog(taskId, taskName);
        } else if (action.equals(getActivity().getString(R.string.item_action_skip))) { // SKIP
            prepareDisplaySkipTaskDialog(taskId);
        } else {
            prepareDisplayReactivateDialog(taskId);
        }
    }

    private void prepareDisplayReactivateDialog(final int taskId) {
        final AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_skip_task, null);
        Button confirmButton = dialogView.findViewById(R.id.btn_ok);
        Button cancelButton = dialogView.findViewById(R.id.btn_cancel);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.changeTaskStatus(userId, taskId, "Running");
                totalTask++;
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setView(dialogView);
        dialog.show();
    }

    private void prepareDisplaySkipTaskDialog(final int taskId) {
        final AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_skip_task, null);
        Button confirmButton = dialogView.findViewById(R.id.btn_ok);
        Button cancelButton = dialogView.findViewById(R.id.btn_cancel);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.changeTaskStatus(userId, taskId, "Failed");
                totalTask--;
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setView(dialogView);
        dialog.show();

    }

    private void prepareDisplayRenameDialog(final int taskId, final String oldTaskName) {
        final AlertDialog dialog = new AlertDialog.Builder(getContext()).create();
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_rename_task, null);
        final EditText editedTaskName = dialogView.findViewById(R.id.edt_task_name_editted);
        editedTaskName.setText(oldTaskName);
        Button confirmButton = dialogView.findViewById(R.id.btn_confirm);
        Button cancelButton = dialogView.findViewById(R.id.btn_cancel);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = editedTaskName.getText().toString();
                if (newName.trim().length() == 0) {
                    Animation shakeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.shake_animation);
                    editedTaskName.startAnimation(shakeAnimation);
                }  else if (!newName.trim().equals(oldTaskName.trim())) {
                    mPresenter.renameTask(taskId, newName);
                    dialog.dismiss();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });


        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setView(dialogView);
        dialog.show();

    }

    @Override
    public void finishRenameTask() {
        mPresenter.loadTasks(checklistId);
    }

    private boolean isChecklistMember() {
        for (ChecklistMember member : checklistMembers) {
            if (member.getUserId().equals(userId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void finishedChangeTaskStatus(String status) {
//        if (status.equals("Failed") || status.equals("Running")) {
////            mPresenter.loadChecklistData(Integer.parseInt(orgId), checklistId);
//
//        }
        mPresenter.loadTasks(checklistId);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void finishGetTaskMember(List<TaskMember> taskMemberList, boolean isSelected, int taskId) {

        boolean isTaskMember = false;

        for (TaskMember member : taskMemberList) {
            if (userId.equals(member.getUserId())) {
                isTaskMember = true;
                break;
            }
        }

        if (isTaskMember) {
            int position = taskPositions.get(taskId);
            handleTickTaskOnChecklistTaskFragment(taskId, isSelected, position);
        } else {
            CommonUtils.showDialog(getContext(), "You're not assigned to this task, so you cannot complete it");
            mChecklistChecklistTaskAdapter.setTaskList(tasks);
        }

    }

    @Override
    public void finishedChangePriority() {
        Log.i("priority", "tra ve roi");
    }

    private void handleTickTaskOnChecklistTaskFragment(int taskId, boolean isSelected, int position) {

        Task currentTask = tasks.get(position);
        mChecklistChecklistTaskAdapter.setTaskList(tasks);

        if (isSelected) {
            doneTask++;
            currentTask.setTaskStatus("Done");
            currentTask.setActionUser(userId);
            mPresenter.changeTaskStatus(userId, taskId, getActivity().getString(R.string.task_done));

        } else {
            doneTask--;
            currentTask.setTaskStatus("Failed");
            currentTask.setActionUser(userId);
            mPresenter.changeTaskStatus(userId, taskId, getActivity().getString(R.string.task_running));
        }

        if (doneTask == totalTask) {
            handleCompleteChecklist("Done");
        } else if (checklistStatus.equals("Done")){
            handleCompleteChecklist("Checklist");
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_complete_checklist) {
            if (isChecklistMember()) {
                handleCompleteChecklist("Done");
            } else {
                CommonUtils.showDialog(getContext(),"You're not assigned to this checklist, so you cannot complete it");
            }
        }
    }

    private void handleCompleteChecklist(String status) {
        updateButtonAppearanceByStatus(status);
        if (status.equals("Done")) {
            showDialogCongrats();
        }
        mPresenter.changeChecklistStatus(userId, checklistId, status);
    }

    private void showDialogCongrats() {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_completed_checklist);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        Button confirmButton = dialog.findViewById(R.id.btn_confirm);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v2) {
                dialog.dismiss();
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void finishChangeChecklistStatus(String status) {
        checklistStatus = status;
        updateButtonAppearanceByStatus(checklistStatus);
        if (checklistStatus.equals("Done")) {
            mChecklistCompletedAlert.setVisibility(View.VISIBLE);
        } else {
            mChecklistCompletedAlert.setVisibility(View.GONE);
        }
        mPresenter.loadTasks(checklistId);
        doneTask = totalTask;
    }

    //drag and drop task
    private void addItemTouchCallback(RecyclerView recyclerView) {
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(new ItemTouchListener() {
            @Override
            public void onMove(int oldPosition, int newPosition) {
                mChecklistChecklistTaskAdapter.onMove(oldPosition, newPosition);
            }
        });
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }


    @Override
    public void onChangePriority(List<Task> taskList) {
//        tasksPriority = new ArrayList<>();
//        tasksPriority = taskList;
//        mChecklistChecklistTaskAdapter.setTaskList(tasksPriority);
//        mChecklistChecklistTaskAdapter.notifyDataSetChanged();
    }

    private void prepareShowRenameDialog(final int checklistId, String checklistName) {
        final Dialog changeDialog = new Dialog(getContext());
        changeDialog.setContentView(R.layout.dialog_edit_checklist_name);
        changeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final EditText edtChecklistName = changeDialog.findViewById(R.id.edt_name);
        edtChecklistName.setText(checklistName);

        Button saveName = changeDialog.findViewById(R.id.bt_save);
        saveName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = edtChecklistName.getText().toString().trim();
                if (newName.length() == 0) {
                    Animation shakeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.shake_animation);
                    edtChecklistName.startAnimation(shakeAnimation);
                } else {
                    mChecklistName.setText(newName);
                    mPresenter.renameChecklist(checklistId, newName);
                    changeDialog.dismiss();
                }
            }
        });

        Button cancel = changeDialog.findViewById(R.id.bt_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeDialog.dismiss();
            }
        });

        changeDialog.show();
    }

    private void handleShowConfirmDialog(final int deletedChecklistId) {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_confirm_delete_checklist);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        Button confirmButton = dialog.findViewById(R.id.btn_confirm);
        Button cancelButton = dialog.findViewById(R.id.btn_cancel);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v2) {
                mPresenter.deleteChecklist(deletedChecklistId, userId);
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
}
