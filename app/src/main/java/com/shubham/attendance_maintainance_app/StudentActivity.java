package com.shubham.attendance_maintainance_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class StudentActivity extends AppCompatActivity {
    Toolbar toolbar;
    private String className, subjectName;
    private int position;
    private RecyclerView recyclerView;
    private StudentAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<StudentItem> studentItems = new ArrayList<>();
    private DbHelper dbHelper;
    private long cid;
    private MyCalender calender;
    private TextView subtitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        calender = new MyCalender();
        dbHelper = new DbHelper(this);
        Intent intent = getIntent();
        className = intent.getStringExtra("className");
        subjectName = intent.getStringExtra("subjectName");
        position = intent.getIntExtra("position", -1);

        cid = intent.getLongExtra("cid", -1);

        setToolbar();
        loadData();
        recyclerView = findViewById(R.id.student_recycler);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new StudentAdapter(this, studentItems);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(position -> changeStatus(position));

        loadStatusData();

        try{
            String s, s2;
            s = getIntent().getExtras().getString("list");
            s2 = getIntent().getExtras().getString("list2");
            int size=getIntent().getIntExtra("size", 0);

            if(s!=null && s2 !=null)
            {
                int i=0,j=0, k=0;
                while(j<s2.length() && k<s.length())
                {
                    String name = "";
                    if (s2.charAt(j) == ',' || s2.charAt(j) == '[') {
                        j++;
                    }

                    while (s2.charAt(j) != ',' && j < s2.length() - 1) {
                        name = name + s2.charAt(j);
                        j++;
                    }
                    if(name=="")
                    {
                        break;
                    }

                    String roll = "";
                    if (s.charAt(k) == ',' || s.charAt(k) == '[') {
                        k++;
                    }
                    while (s.charAt(k) != ',' && k < s.length() - 1) {
                        roll = roll + s.charAt(k);
                        k++;
                    }
//                Toast.makeText(StudentActivity.this, roll + "", Toast.LENGTH_SHORT).show();
                    float f = Float.parseFloat(roll);
                    int r = (int)f;
                    long sid = dbHelper.addStudent(cid, r, name + "");
                    StudentItem studentItem = new StudentItem(sid, r, name + "");
                    studentItems.add(studentItem);
                    adapter.notifyDataSetChanged();
                    i++;
                }

            }



        }
        catch(Exception e)
        {
            adapter.notifyDataSetChanged();
        }



    }

    private void loadData() {
        Cursor cursor = dbHelper.getStudentTable(cid);
        Log.i("1234567890", "loadData: "+cid);
        studentItems.clear();
        while(cursor.moveToNext())
        {
            long sid = cursor.getLong(cursor.getColumnIndex(DbHelper.S_ID));
            int roll = cursor.getInt(cursor.getColumnIndex(DbHelper.STUDENT_ROLL_KEY));
            String name = cursor.getString(cursor.getColumnIndex(DbHelper.STUDENT_NAME_KEY));
            studentItems.add(new StudentItem(sid, roll, name));
        }
        cursor.close();
    }

    private void changeStatus(int position) {
        String status = studentItems.get(position).getStatus();
        if(status.equals("P")) status = "A";
        else status = "P";

        studentItems.get(position).setStatus(status);
        adapter.notifyItemChanged(position);
    }

    private void setToolbar() {
        toolbar = findViewById(R.id.toolbar);
        TextView title = toolbar.findViewById(R.id.title_toolbar);
        subtitle = toolbar.findViewById(R.id.subtitle_toolbar);
        ImageButton back = toolbar.findViewById(R.id.back);
        ImageButton save = toolbar.findViewById(R.id.save);

        save.setOnClickListener(v->saveStatus());

        title.setText(className);
        subtitle.setText(subjectName+" | "+calender.getDate());
        if (subtitle.toString() == null)
        {
            subtitle.setText("Subject");
        }
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        toolbar.inflateMenu(R.menu.student_menu);
        toolbar.setOnMenuItemClickListener(menuItem -> onMenuItemClick(menuItem));

    }

    private void saveStatus() {
        for(StudentItem studentItem : studentItems)
        {
            String status = studentItem.getStatus();
            if(!status.equals("P")) status = "A";
            long value = dbHelper.addStatus(studentItem.getSid(),cid, calender.getDate(), status);
            if(value == -1) dbHelper.updateStatus(studentItem.getSid(), calender.getDate(), status);

        }
        Toast.makeText(this, "Status saved for "+calender.getDate(), Toast.LENGTH_SHORT).show();
    }

    private void loadStatusData()
    {
        for(StudentItem studentItem : studentItems)
        {
            String status = dbHelper.getStatus(studentItem.getSid(), calender.getDate());
            if (status != null) studentItem.setStatus(status);
            else studentItem.setStatus("");

        }
        adapter.notifyDataSetChanged();
    }

    private boolean onMenuItemClick(MenuItem menuItem) {

        if(menuItem.getItemId() == R.id.add_student)
        {
            showAddStudentDialog();
        }
        else if(menuItem.getItemId() == R.id.show_calender)
        {
            showCalender();
        }
        else if(menuItem.getItemId() == R.id.show_attendance_sheet)
        {
            openSheetList();
        }
        else if(menuItem.getItemId() == R.id.import_excel)
        {
            Intent intent = new Intent(this, ImportExcel.class);
            startActivity(intent);
        }
        return true;
    }

    private void openSheetList() {
        long[] idArray = new long[studentItems.size()];
        int[] rollArray = new int[studentItems.size()];
        String[] nameArray = new String[studentItems.size()];

        for(int i=0;i<idArray.length;i++)
            idArray[i] = studentItems.get(i).getSid();
        for(int i=0;i<rollArray.length;i++)
            rollArray[i] = studentItems.get(i).getRoll();
        for(int i=0;i<nameArray.length;i++)
            nameArray[i] = studentItems.get(i).getName();

        Intent intent=new Intent(this, SheetListActivity.class);
        intent.putExtra("cid", cid);
        intent.putExtra("idArray", idArray);
        intent.putExtra("rollArray", rollArray);
        intent.putExtra("nameArray", nameArray);
        startActivity(intent);
    }

    private void showCalender() {

        calender.show(getSupportFragmentManager(), "");
        calender.setOnCalenderOkClickListener((this::onCalenderOkClicked));
    }

    private void onCalenderOkClicked(int year, int month, int day) {
        calender.setDate(year, month, day);
        subtitle.setText(subjectName+" | "+calender.getDate());
        loadStatusData();
    }

    private void showAddStudentDialog() {
        MyDialog dialog = new MyDialog();
        dialog.show(getSupportFragmentManager(), MyDialog.STUDENT_ADD_DIALOG);
        dialog.setListener((roll, name)-> addStudent(roll, name));


    }

    private void addStudent(String roll_string, String name) {
        try {
            if (roll_string.equals("")) {
                Toast.makeText(this, "Roll no must not be Empty", Toast.LENGTH_LONG).show();
            } else if (name.equals("")) {
                Toast.makeText(this, "Name must not be Empty", Toast.LENGTH_LONG).show();

            } else {
                int roll = Integer.parseInt(roll_string);
                long sid = dbHelper.addStudent(cid, roll, name);
                StudentItem studentItem = new StudentItem(sid, roll, name);
                studentItems.add(studentItem);
                adapter.notifyDataSetChanged();
            }
        }
        catch (Exception e)
        {
            Toast.makeText(StudentActivity.this, "Roll Number must be an Integer", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case 0:
                showUpdateStudentDialog(item.getGroupId());
                break;
            case 1:
                deleteStudent(item.getGroupId());
        }
        return super.onContextItemSelected(item);
    }

    private void showUpdateStudentDialog(int position) {
        MyDialog dialog = new MyDialog(studentItems.get(position).getRoll(), studentItems.get(position).getName());
        dialog.show(getSupportFragmentManager(), MyDialog.STUDENT_UPDATE_DIALOG);
        dialog.setListener((roll_string, name)->updateStudent(position, name));
    }

    private void updateStudent(int position, String name) {
        if(name.equals("")){
            Toast.makeText(this, "Name must not be Empty", Toast.LENGTH_LONG).show();
        }
        else {
            dbHelper.updateStudent(studentItems.get(position).getSid(), name);
            studentItems.get(position).setName(name);
            adapter.notifyItemChanged(position);
        }
    }

    private void deleteStudent(int position) {
        dbHelper.deleteStudent(studentItems.get(position).getSid());
        studentItems.remove(position);
        adapter.notifyItemRemoved(position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.student_menu, menu);

        for(int i = 0; i < menu.size(); i++){
            Drawable drawable = menu.getItem(i).getIcon();
            if(drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(getResources().getColor(R.color.textColor), PorterDuff.Mode.SRC_ATOP);
            }
        }

        return true;
    }

}