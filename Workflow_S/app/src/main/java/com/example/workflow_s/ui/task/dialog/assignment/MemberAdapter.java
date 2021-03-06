package com.example.workflow_s.ui.task.dialog.assignment;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.workflow_s.R;
import com.example.workflow_s.model.User;
import com.example.workflow_s.utils.SharedPreferenceUtils;

import java.util.List;

/**
 * Workflow_S
 * Created by TinhPV on 2019-07-06
 * Copyright © 2019 TinhPV. All rights reserved
 **/


public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {


    EventListener listener;

    public interface EventListener {
        void onEvent(String userId);
    }

    private List<User> mUserList;
    private String checklistUserId;
    private RecyclerView mRecyclerView;
    private Context mContext;


    public void setUserList(List<User> userList) {
        mUserList = userList;
        notifyDataSetChanged();
    }

    public void setChecklistUserId(String checklistUserId) {
        this.checklistUserId = checklistUserId;
    }


    public MemberAdapter(EventListener listener, Context context) {
        this.listener = listener;
        this.mContext = context;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }


    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        int layoutId = R.layout.recyclerview_item_user_assigning;
        View view = layoutInflater.inflate(layoutId, viewGroup, false);
        MemberViewHolder viewHolder = new MemberViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder memberViewHolder, final int i) {
        memberViewHolder.mMemberName.setText(mUserList.get(i).getName());
        memberViewHolder.mEmail.setText(mUserList.get(i).getEmail());

        String profileUrlString = mUserList.get(i).getAvatar();
        if (profileUrlString ==  null) {
            memberViewHolder.mAvatar.setImageDrawable(ContextCompat.getDrawable(memberViewHolder.view.getContext(), R.drawable.default_avatar));
        } else {
            Glide.with(memberViewHolder.view.getContext()).load(profileUrlString).into(memberViewHolder.mAvatar);
        }


        String userId = SharedPreferenceUtils.retrieveData(mContext, mContext.getString(R.string.pref_userId));
        if (!userId.equals(checklistUserId)) {
            memberViewHolder.btUnassign.setVisibility(View.GONE);

        } else {
            String id = mUserList.get(i).getId();
            if (id.equals(checklistUserId) || id.equals(userId)) { // if this user is checklist's owner
                memberViewHolder.btUnassign.setVisibility(View.GONE);
            } else {
                memberViewHolder.btUnassign.setVisibility(View.VISIBLE);
                memberViewHolder.btUnassign.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // DONE - HANDLE UNASSIGN USER HERE
                        //int index = mRecyclerView.getChildLayoutPosition(v);
                        listener.onEvent(mUserList.get(i).getId());
                    }
                });
            }
        }
    }


    @Override
    public int getItemCount() {
        if (null == mUserList) {
            return 0;
        }
        return mUserList.size();
    }



    public class MemberViewHolder extends RecyclerView.ViewHolder {

        public TextView mMemberName;
        public TextView mEmail;
        public ImageView mAvatar;
        public ImageButton btUnassign;
        public View view;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            mMemberName = itemView.findViewById(R.id.tv_user_display_name);
            mEmail = itemView.findViewById(R.id.tv_user_email);
            mAvatar = itemView.findViewById(R.id.img_user_avatar);
            btUnassign = itemView.findViewById(R.id.bt_unassign);
        }
    }
}
