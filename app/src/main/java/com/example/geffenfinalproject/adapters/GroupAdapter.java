package com.example.geffenfinalproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geffenfinalproject.R;
import com.example.geffenfinalproject.models.Group;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

    private List<Group> groupList = new ArrayList<>();

    public void setGroupList(List<Group> groups){
        this.groupList = groups;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_item_group, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {

        Group group = groupList.get(position);

        holder.tvGroupName.setText(group.getGroupName());
        holder.tvGroupScore.setText("Correct Answers: " + group.getTotalQuestions());

        int memberCount = 0;
        if(group.getMembers() != null)
            memberCount = group.getMembers().size();

        holder.tvGroupMembers.setText("Members: " + memberCount);

        if(group.getGroupName() != null && !group.getGroupName().isEmpty())
            holder.tvGroupInitial.setText(group.getGroupName().substring(0,1));
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    static class GroupViewHolder extends RecyclerView.ViewHolder{

        TextView tvGroupName;
        TextView tvGroupScore;
        TextView tvGroupMembers;
        TextView tvGroupInitial;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);

            tvGroupName = itemView.findViewById(R.id.tv_group_name);
            tvGroupScore = itemView.findViewById(R.id.tv_group_score);
            tvGroupMembers = itemView.findViewById(R.id.tv_group_members);
            tvGroupInitial = itemView.findViewById(R.id.tv_group_initial);
        }
    }
}
