package com.example.geffenfinalproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geffenfinalproject.adapters.GroupAdapter;
import com.example.geffenfinalproject.models.Group;
import com.example.geffenfinalproject.services.DatabaseService;

import java.util.List;

public class group_table extends AppCompatActivity {

    private static final String TAG = "group_table";

    private GroupAdapter groupAdapter;
    private TextView tvGroupCount;
    private DatabaseService databaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_group_table);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseService = DatabaseService.getInstance();

        RecyclerView groupList = findViewById(R.id.rv_groups_list);
        tvGroupCount = findViewById(R.id.tv_group_count);

        groupList.setLayoutManager(new LinearLayoutManager(this));

        groupAdapter = new GroupAdapter();
        groupList.setAdapter(groupAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        databaseService.getGroupList(new DatabaseService.DatabaseCallback<List<Group>>() {
            @Override
            public void onCompleted(List<Group> groups) {

                groupAdapter.setGroupList(groups);

                tvGroupCount.setText("Total groups: " + groups.size());
            }

            @Override
            public void onFailed(Exception e) {
                Log.e(TAG, "Failed to get group list", e);
            }
        });
    }
}
