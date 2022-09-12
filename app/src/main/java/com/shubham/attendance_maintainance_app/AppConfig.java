package com.shubham.attendance_maintainance_app;

import java.util.List;
import java.util.Map;

public class AppConfig {

    ImportExcel eu = new ImportExcel();

    List<Map<Integer, Object>> excelList = eu.readExcelList;

    String editValue, editValue2;
    private static final AppConfig ourInstance = new AppConfig();
    private List<String> list;
    private List<String> list2;

    public static AppConfig getInstance()
    {
        return ourInstance;
    }
    private AppConfig()
    {

    }
    public void setText(String editValue)
    {
        this.editValue = editValue;
    }


    public void setText1(List<String> list){

        this.list=list;
    }
    public void setText2(List<String> list2){

        this.list2=list2;
    }

    //    public String getText1()
//    {
//        return editValue;
//    }
    public String getText1()
    {
        return list.toString();
    }
    public String getText2()
    {
        return list2.toString();
    }
}
