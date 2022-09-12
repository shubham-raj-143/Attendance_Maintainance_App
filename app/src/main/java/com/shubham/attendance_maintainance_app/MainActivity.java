package com.shubham.attendance_maintainance_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    Button fab_name;
    RecyclerView recyclerView;
    ClassAdapter classAdapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<ClassItem> classItems = new ArrayList<>();
    Toolbar toolbar;
    DbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DbHelper(this);
        fab_name = findViewById(R.id.fab_name);

        fab_name.setOnClickListener(view -> showDialog());

        loadData();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        classAdapter = new ClassAdapter(this, classItems);
        recyclerView.setAdapter(classAdapter);

        classAdapter.setOnItemClickListener(position -> gotoItemActivity(position));

        setToolbar();


    }

    private void loadData() {
        Cursor cursor = dbHelper.getClassTable();

        classItems.clear();
        while (cursor.moveToNext())
        {
            int id = cursor.getInt(cursor.getColumnIndex(DbHelper.C_ID));
            String className = cursor.getString(cursor.getColumnIndex(DbHelper.CLASS_NAME_KEY));
            String subjectName = cursor.getString(cursor.getColumnIndex(DbHelper.SUBJECT_NAME_KEY));

            classItems.add(new ClassItem(id, className, subjectName));
        }
    }

    private void setToolbar() {
        toolbar = findViewById(R.id.toolbar);
        TextView title = toolbar.findViewById(R.id.title_toolbar);
        TextView subtitle = toolbar.findViewById(R.id.subtitle_toolbar);
        ImageButton back = toolbar.findViewById(R.id.back);
        ImageButton save = toolbar.findViewById(R.id.save);

        title.setText("Attendance Maintenance");
        subtitle.setVisibility(View.GONE);
        back.setVisibility(View.INVISIBLE);
        save.setVisibility(View.INVISIBLE);
//        save.setImageResource(R.drawable.clear_icon);
//        save.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                finish();
//            }
//        });
    }

    private void gotoItemActivity(int position) {
        String list="";
        String list2="";
        int size = getIntent().getIntExtra("size", 0);
        try {

            list = getIntent().getExtras().getString("list");
            list2 = getIntent().getExtras().getString("list2");

        }
        catch (Exception e)
        {

        }
        int t=1;

        Intent intent = new Intent(this, StudentActivity.class);

        intent.putExtra("className", classItems.get(position).getClassName());
        intent.putExtra("subjectName", classItems.get(position).getSubjectName());
        intent.putExtra("position", position);
        intent.putExtra("list", list);
        intent.putExtra("list2", list2);
        intent.putExtra("size", size);
        intent.putExtra("cid", classItems.get(position).getCid());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("EXIT", true);
        startActivity(intent);

    }

    private void showDialog()
    {
        MyDialog dialog = new MyDialog();
        dialog.show(getSupportFragmentManager(), MyDialog.CLASS_ADD_DIALOG);
        dialog.setListener((className, subjectName)-> addClass(className, subjectName));
    }

    private void addClass(String className, String subjectName) {

        if(className.equals("")){
            Toast.makeText(this, "Class Name must not be Empty", Toast.LENGTH_LONG).show();
        }
        else if(subjectName.equals(""))
        {
            Toast.makeText(this, "Subject Name must not be Empty", Toast.LENGTH_LONG).show();

        }
        else {

            long cid = dbHelper.addClass(className, subjectName);
            ClassItem classItem = new ClassItem(cid, className, subjectName);
            classItems.add(classItem);
            classAdapter.notifyDataSetChanged();
        }


    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case 0:
                showUpdateDialog(item.getGroupId());
                break;
            case 1:
                deleteClass(item.getGroupId());
        }
        return super.onContextItemSelected(item);
    }

    private void showUpdateDialog(int position) {
        MyDialog dialog = new MyDialog();
        dialog.show(getSupportFragmentManager(), MyDialog.CLASS_UPDATE_DIALOG);
        dialog.setListener((className, subjectName)-> updateClass(position, className, subjectName));
    }

    private void updateClass(int position, String className, String subjectName) {
        if(className.equals("")){
            Toast.makeText(this, "Class Name must not be Empty", Toast.LENGTH_LONG).show();
        }
        else if(subjectName.equals(""))
        {
            Toast.makeText(this, "Subject Name must not be Empty", Toast.LENGTH_LONG).show();

        }
        else {
            dbHelper.updateClass(classItems.get(position).getCid(), className, subjectName);
            classItems.get(position).setClassName(className);
            classItems.get(position).setSubjectName(subjectName);
            classAdapter.notifyItemChanged(position);
        }
    }

    private void deleteClass(int position) {
        dbHelper.deleteClass(classItems.get(position).getCid());
        classItems.remove(position);
        classAdapter.notifyItemRemoved(position);
    }
}