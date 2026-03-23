package com.example.geffenfinalproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geffenfinalproject.R;
import com.example.geffenfinalproject.models.User;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private List<User> userList = new ArrayList<>();
    private int startRank = 1;

    public void setUserList(List<User> users){
        this.userList = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        User user = userList.get(position);

        holder.tvRank.setText("#" + (startRank + position));
        holder.tvUsername.setText(user.getFname());
        holder.tvScore.setText(String.valueOf(user.getCorrect_answers()));

        // Medal logic
        if(position == 0){
            holder.tvMedal.setText("🥇");
        }
        else if(position == 1){
            holder.tvMedal.setText("🥈");
        }
        else if(position == 2){
            holder.tvMedal.setText("🥉");
        }
        else{
            holder.tvMedal.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvRank;
        TextView tvMedal;
        TextView tvUsername;
        TextView tvScore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvRank = itemView.findViewById(R.id.tv_rank);
            tvMedal = itemView.findViewById(R.id.tv_medal);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvScore = itemView.findViewById(R.id.tv_score);
        }
    }
    public void setStartRank(int startRank) {
        this.startRank = startRank;
    }
}