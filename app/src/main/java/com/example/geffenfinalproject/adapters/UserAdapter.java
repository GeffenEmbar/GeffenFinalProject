package com.example.geffenfinalproject.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.geffenfinalproject.R;
import com.example.geffenfinalproject.models.User;
import com.example.geffenfinalproject.utils.ImageUtil;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {


    public interface OnUserClickListener {
        void onUserClick(User user);
        void onLongUserClick(User user);
        void onKickClick(User user);
    }

    private final List<User> userList;
    private final OnUserClickListener onUserClickListener;
    private String currentUserId;
    private String ownerId;

    public UserAdapter(@Nullable final OnUserClickListener onUserClickListener) {
        userList = new ArrayList<>();
        this.onUserClickListener = onUserClickListener;
    }

    public void setIds(String currentUserId, String ownerId) {
        this.currentUserId = currentUserId;
        this.ownerId = ownerId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // לוקח את העיצוב המקורי מהאייטם
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }


    // ריסייקל וויו קורא לפעולה הזו כאשר צריך עוד שורה של משתמש בשביל הרשימה
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // לוקח את הרשימה ממסד הנתונים ושם את הנתןנים במקומות הנכונים לפי הid
        User user = userList.get(position);
        if (user == null) return;

        holder.tvName.setText(user.getFname());
        holder.tvEmail.setText(user.getEmail());
        holder.tvPhone.setText(user.getPhone());
        holder.tvRank.setText("#" + (position + 1));
        
        // Handle stats
        holder.tvNotes.setText("N: " + user.getNotesCorrect());
        holder.tvIntervals.setText("I: " + user.getIntervalsCorrect());
        holder.tvQuiz.setText("Q: " + user.getQuizCorrect());
        holder.tvChords.setText("C: " + user.getChordsCorrect());
        holder.tvComplexChords.setText("CC: " + user.getComplexChordsCorrect());
        
        // Handle profile image
        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            android.graphics.Bitmap bitmap = ImageUtil.convertFrom64base(user.getProfileImage());
            if (bitmap != null) {
                holder.ivProfile.setVisibility(View.VISIBLE);
                holder.tvInitials.setVisibility(View.GONE);
                Glide.with(holder.itemView.getContext())
                        .load(bitmap)
                        .circleCrop()
                        .into(holder.ivProfile);
            } else {
                showInitials(holder, user);
            }
        } else {
            showInitials(holder, user);
        }
        
        // Show admin chip if user is admin
        if (user.isAdmin()) {
            holder.chipRole.setVisibility(View.VISIBLE);
            holder.chipRole.setText("Admin");
        } else {
            holder.chipRole.setVisibility(View.GONE);
        }

        // Show kick button if current user is owner and this user is NOT the owner
        if (currentUserId != null && currentUserId.equals(ownerId) && !user.getId().equals(ownerId)) {
            holder.btnKick.setVisibility(View.VISIBLE);
            holder.btnKick.setOnClickListener(v -> {
                if (onUserClickListener != null) {
                    onUserClickListener.onKickClick(user);
                }
            });
        } else {
            holder.btnKick.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onUserClickListener != null) {
                onUserClickListener.onUserClick(user);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onUserClickListener != null) {
                onUserClickListener.onLongUserClick(user);
            }
            return true;
        });

    }
    // הראה בתמונה רק את הראשי תיבות
    private void showInitials(ViewHolder holder, User user) {
        holder.ivProfile.setVisibility(View.GONE);
        holder.tvInitials.setVisibility(View.VISIBLE);

        // Set initials
        String initials = "";
        if (user.getFname() != null && !user.getFname().isEmpty()) {
            initials += user.getFname().charAt(0);
        }
        if (user.getLname() != null && !user.getLname().isEmpty()) {
            initials += user.getLname().charAt(0);
        }
        holder.tvInitials.setText(initials.toUpperCase());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void setUserList(List<User> users) {
        userList.clear();
        userList.addAll(users);
        notifyDataSetChanged();
    }
    /*
    public void addUser(User user) {
        userList.add(user);
        notifyItemInserted(userList.size() - 1);
    }
    public void updateUser(User user) {
        int index = -1;
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getId().equals(user.getId())) {
                index = i;
                break;
            }
        }
        if (index == -1) return;
        userList.set(index, user);
        notifyItemChanged(index);
    }

    public void removeUser(User user) {
        int index = -1;
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getId().equals(user.getId())) {
                index = i;
                break;
            }
        }
        if (index == -1) return;
        userList.remove(index);
        notifyItemRemoved(index);
    }*/

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvPhone, tvInitials, chipRole, tvRank;
        TextView tvNotes, tvIntervals, tvQuiz, tvChords, tvComplexChords;
        ImageView ivProfile;
        View btnKick;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_item_user_name);
            tvEmail = itemView.findViewById(R.id.tv_item_user_email);
            tvPhone = itemView.findViewById(R.id.tv_item_user_phone);
            tvInitials = itemView.findViewById(R.id.tv_user_initials);
            ivProfile = itemView.findViewById(R.id.iv_user_profile);
            chipRole = itemView.findViewById(R.id.chip_user_role);
            tvRank = itemView.findViewById(R.id.tv_rank);
            
            tvNotes = itemView.findViewById(R.id.tv_item_user_notes);
            tvIntervals = itemView.findViewById(R.id.tv_item_user_intervals);
            tvQuiz = itemView.findViewById(R.id.tv_item_user_quiz);
            tvChords = itemView.findViewById(R.id.tv_item_user_chords);
            tvComplexChords = itemView.findViewById(R.id.tv_item_user_complex_chords);
            btnKick = itemView.findViewById(R.id.btn_kick);
        }
    }
}