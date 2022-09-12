package com.shubham.attendance_maintainance_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;

import android.net.Uri;
import android.os.Bundle;

import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SheetActivity extends AppCompatActivity {
    private static final String TAG = SheetActivity.class.getSimpleName();
    private int FILE_SELECTOR_CODE = 10000;
    private List<Map<Integer, Object>> readExcelList = new ArrayList<>();
    private int DIR_SELECTOR_CODE = 20000;

    Toolbar toolbar;
    private EditText editTextExcel;
    private File filePath = new File(Environment.getExternalStorageDirectory() + "/Demo.xls");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sheet);


        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
        showTable();
        setToolbar();



    }




    private void setToolbar() {
        toolbar = findViewById(R.id.toolbar);
        TextView title = toolbar.findViewById(R.id.title_toolbar);
        TextView subtitle = toolbar.findViewById(R.id.subtitle_toolbar);
        ImageButton back = toolbar.findViewById(R.id.back);
        ImageButton save = toolbar.findViewById(R.id.save);

        String month = getIntent().getStringExtra("month");
        title.setText("Attendance");
        subtitle.setText(month);
        save.setImageResource(R.drawable.ic_baseline_cloud_download_24);
        back.setOnClickListener(v -> onBackPressed());

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFolderSelector();
//                Toast.makeText(SheetActivity.this, "Soon!", Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.student_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.cancel_btn) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    // Show Table
    public void showTable() {
        //show table
        DbHelper dbHelper = new DbHelper(this);
        TableLayout tableLayout = findViewById(R.id.tableLayout);
        long[] idArray = getIntent().getLongArrayExtra("idArray");
        int[] rollArray = getIntent().getIntArrayExtra("rollArray");
        String[] nameArray = getIntent().getStringArrayExtra("nameArray");
        String month = getIntent().getStringExtra("month");

        int DAY_IN_MONTH = getDayInMonth(month);
        //row setup
        int rowSize = idArray.length + 1;

        TableRow[] rows = new TableRow[rowSize];
        TextView[] roll_tvs = new TextView[rowSize];
        TextView[] name_tvs = new TextView[rowSize];
        TextView[][] status_tvs = new TextView[rowSize][DAY_IN_MONTH + 1];

        Map<Integer, Object> map = new HashMap<Integer, Object>();


        for(int i=0;i<rowSize;i++)
        {
            roll_tvs[i] = new TextView(this);
            name_tvs[i] = new TextView(this);
            for(int j=1;j<=DAY_IN_MONTH;j++)
            {
                status_tvs[i][j] = new TextView(this);
            }
        }

        //header
        roll_tvs[0].setText("Roll");
        name_tvs[0].setText("Name");
        roll_tvs[0].setTypeface(roll_tvs[0].getTypeface(), Typeface.BOLD);
        name_tvs[0].setTypeface(name_tvs[0].getTypeface(), Typeface.BOLD);
        map.put(0,"Roll");
        map.put(1,"Name");


        for(int i=1;i<=DAY_IN_MONTH;i++)
        {
            status_tvs[0][i].setText(String.valueOf(i));
            status_tvs[0][i].setTypeface(status_tvs[0][i].getTypeface(), Typeface.BOLD);
            map.put(i+1,String.valueOf(i));
        }
        readExcelList.add(0, map);

        //other rows
        for(int i=1;i<rowSize;i++)
        {
            Map<Integer, Object> map1 = new HashMap<Integer, Object>();
            roll_tvs[i].setText(String.valueOf(rollArray[i-1]));
            name_tvs[i].setText(nameArray[i-1]);
            map1.put(0, String.valueOf(rollArray[i-1]));
            map1.put(1, nameArray[i-1]);

            for(int j=1;j<=DAY_IN_MONTH;j++)
            {
                //01.09.2021
                String day = String.valueOf(j);
                if(day.length() == 1) day = "0"+day;

                String date = day+"."+month;
                String status = dbHelper.getStatus(idArray[i-1], date);
                status_tvs[i][j].setText(status);
                if(status==null)
                {
                    map1.put(j+1, "-");
                }
                else
                {
                    map1.put(j+1, status);
                }


            }
            readExcelList.add(i, map1);

        }

        for(int i=0;i<rowSize;i++)
        {
//            if(i%2 != 0)
//            {
//                rows[i].setBackgroundColor(Color.parseColor("#ffffff"));
//            }
//            else
//            {
//                rows[i].setBackgroundColor(Color.parseColor("#ecf0f3"));
//            }
            rows[i] = new TableRow(this);

            roll_tvs[i].setPadding(16, 16, 16, 16);
            name_tvs[i].setPadding(16, 16, 16, 16);


            rows[i].addView(roll_tvs[i]);
            rows[i].addView(name_tvs[i]);

            for(int j=1;j<= DAY_IN_MONTH;j++)
            {
                status_tvs[i][j].setPadding(16, 16, 16, 16);
                rows[i].addView(status_tvs[i][j]);
            }
            tableLayout.addView(rows[i]);
        }
        tableLayout.setShowDividers(TableLayout.SHOW_DIVIDER_MIDDLE);



    }

    private int getDayInMonth(String month) {

        int monthIndex = Integer.parseInt(month.substring(0, 2))-1;
        int year = Integer.parseInt(month.substring(3));

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, monthIndex);
        calendar.set(Calendar.YEAR, year);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
    private void openFolderSelector() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("application/*");
        intent.putExtra(Intent.EXTRA_TITLE,
                System.currentTimeMillis() + ".xlsx");
        startActivityForResult(intent, DIR_SELECTOR_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DIR_SELECTOR_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if (uri == null) return;
            Log.i(TAG, "onActivityResult: " + "filePath：" + uri.getPath());
            Toast.makeText(SheetActivity.this, "Exporting...", Toast.LENGTH_SHORT).show();
            //you can modify readExcelList, then write to excel.
            ExcelUtil.writeExcelNew(this, readExcelList , uri);
        }
    }


}