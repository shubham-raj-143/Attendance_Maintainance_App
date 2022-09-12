package com.shubham.attendance_maintainance_app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class ImportExcel extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();
    private Context mContext;
    private int FILE_SELECTOR_CODE = 10000;
    private int DIR_SELECTOR_CODE = 20000;
    public List<Map<Integer, Object>> readExcelList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ExcelAdapter excelAdapter;
    private Button excel_load, next_btn;
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_excel);
        mContext = this;

        initViews();
        excel_load = findViewById(R.id.excel_load);
        next_btn = findViewById(R.id.next_btn);
        setToolbar();

        excel_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save_list();

            }
        });
        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ImportExcel.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
    void save_list()
    {
        excel_load.setVisibility(View.INVISIBLE);
        next_btn.setVisibility(View.VISIBLE);
//        System.out.println(readExcelList);
        int row_size = readExcelList.size();
        List<String> list = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        List<String> list3 = new ArrayList<>();
        for(int i = 1; i<row_size; i++)
        {
            if(Objects.requireNonNull(readExcelList.get(i).get(0)).toString().equals(""))
            {
                break;
            }
            list.add(readExcelList.get(i).get(0).toString());
            list2.add(readExcelList.get(i).get(1).toString());
            list3.add(readExcelList.get(i).get(2).toString());
        }
        System.out.println(list3);
        String rl = Objects.requireNonNull(readExcelList.get(0).get(0)).toString();
        AppConfig appConfig =  AppConfig.getInstance();
        appConfig.setText(rl);
        appConfig.setText1(list);
        appConfig.setText2(list2);
//        appConfig.setText3(list3);
        Intent intent = new Intent(ImportExcel.this, MainActivity.class);
        intent.putExtra("list", list.toString());
        intent.putExtra("list2", list2.toString());
//        intent.putExtra("list3", list3.toString());
        intent.putExtra("size", row_size);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }
    private void initViews() {
        recyclerView = findViewById(R.id.excel_content_rv);
        excelAdapter = new ExcelAdapter(readExcelList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(excelAdapter);
    }


    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.import_excel_btn:
                excel_load.setVisibility(View.VISIBLE);
                next_btn.setVisibility(View.INVISIBLE);
                openFileSelector();

                break;

//            case R.id.export_excel_btn:
//                if (readExcelList.size() > 0) {
//                    openFolderSelector();
//                } else {
//                    Toast.makeText(mContext, "please import excel first", Toast.LENGTH_SHORT).show();
//                }
//                break;
            default:
                break;
        }
    }

    /**
     * open local filer to select file
     */
    private void openFileSelector() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/*");
        startActivityForResult(intent, FILE_SELECTOR_CODE);
    }

//    /**
//     * open the local filer and select the folder
//     */
//    private void openFolderSelector() {
//        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
//        intent.setType("application/*");
//        intent.putExtra(Intent.EXTRA_TITLE,
//                System.currentTimeMillis() + ".xlsx");
//        startActivityForResult(intent, DIR_SELECTOR_CODE);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_SELECTOR_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if (uri == null) return;
            Log.i(TAG, "onActivityResult: " + "filePath：" + uri.getPath());
            //select file and import
            importExcelDeal(uri);
        } else if (requestCode == DIR_SELECTOR_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if (uri == null) return;
            Log.i(TAG, "onActivityResult: " + "filePath：" + uri.getPath());
            Toast.makeText(mContext, "Exporting...", Toast.LENGTH_SHORT).show();
            //you can modify readExcelList, then write to excel.
            ExcelUtil.writeExcelNew(this, readExcelList, uri);
        }
    }

    private void importExcelDeal(final Uri uri) {
        new Thread(() -> {
            Log.i(TAG, "doInBackground: Importing...");
            runOnUiThread(() -> Toast.makeText(mContext, "Importing...", Toast.LENGTH_SHORT).show());

            List<Map<Integer, Object>> readExcelNew = ExcelUtil.readExcelNew(mContext, uri, uri.getPath());

            Log.i(TAG, "onActivityResult:readExcelNew " + ((readExcelNew != null) ? readExcelNew.size() : ""));

            if (readExcelNew != null && readExcelNew.size() > 0) {
                readExcelList.clear();
                readExcelList.addAll(readExcelNew);
                updateUI();

                Log.i(TAG, "run: successfully imported");
                runOnUiThread(() -> Toast.makeText(mContext, "successfully imported", Toast.LENGTH_SHORT).show());

            } else {
                runOnUiThread(() -> Toast.makeText(mContext, "no data", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    /**
     * refresh RecyclerView
     */
    private void updateUI() {
        runOnUiThread(() -> {
            if (readExcelList != null && readExcelList.size() > 0) {
                excelAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setToolbar() {
        toolbar = findViewById(R.id.toolbar);
        TextView title = toolbar.findViewById(R.id.title_toolbar);
        TextView subtitle = toolbar.findViewById(R.id.subtitle_toolbar);
        ImageButton back = toolbar.findViewById(R.id.back);
        ImageButton save = toolbar.findViewById(R.id.save);

        title.setText("Import From Excel");
        subtitle.setVisibility(View.GONE);
        save.setVisibility(View.INVISIBLE);
        back.setOnClickListener(v-> onBackPressed());
    }

}

