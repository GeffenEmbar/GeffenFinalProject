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

    public enum Mode {
        TOTAL, NOTES, INTERVALS, QUIZ
    }

    private List<User> userList = new ArrayList<>();
    private int startRank = 1;
    private Mode currentMode = Mode.TOTAL;

    public void setUserList(List<User> users){
        this.userList = users;
        notifyDataSetChanged();
    }

    public void setMode(Mode mode) {
        this.currentMode = mode;
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
        
        int score = 0;
        switch (currentMode) {
            case TOTAL: score = user.getCorrect_answers(); break;
            case NOTES: score = user.getNotesCorrect(); break;
            case INTERVALS: score = user.getIntervalsCorrect(); break;
            case QUIZ: score = user.getQuizCorrect(); break;
        }
        holder.tvScore.setText(String.valueOf(score));


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
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvScore = itemView.findViewById(R.id.tv_score);
        }
    }
    public void setStartRank(int startRank) {
        this.startRank = startRank;
    }
}