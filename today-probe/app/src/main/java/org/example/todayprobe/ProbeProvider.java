package org.example.todayprobe;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.MatrixCursor;
import me.ancal.wmdroid.sdk.TodayContract;
import me.ancal.wmdroid.sdk.TodayProvider;

/** Two public synthetic Today plugins; no personal data, permissions or WM Activity. */
public abstract class ProbeProvider extends TodayProvider {
    public static final String PACKAGE="org.example.todayprobe";
    public static final String COUNTER_AUTHORITY=PACKAGE+".counter",STATUS_AUTHORITY=PACKAGE+".status";
    public static final String ROW_EXTRA="me.ancal.wmdroid.sdk.ROW";
    public static final String PREFS="today_probe",COUNT="count",LAST_ROW="last_row",LAUNCHES="launches";
    private final String row;
    protected ProbeProvider(String row){this.row=row;}
    @Override protected final String getAuthority(){return PACKAGE+"."+row;}
    @Override protected final void fillRows(MatrixCursor rows,String language){
        SharedPreferences data=data(getContext());int count=data.getInt(COUNT,0);boolean english="en".equals(language);
        String title,detail;
        if("counter".equals(row)){
            title=english?"Native app counter":"일반 앱 카운터";
            detail=(english?"Count: ":"카운터: ")+count;
        }else{
            title=english?"Native app status":"일반 앱 상태";
            detail="count="+count+";last_row="+data.getString(LAST_ROW,"none")+";launches="+data.getInt(LAUNCHES,0);
        }
        // The host owns WmToday.Row typography, logical height, selection and dividers.
        int icon="counter".equals(row)?R.drawable.today_probe_marker:0;
        rows.addRow(new Object[]{row,title,detail,icon,PACKAGE+".ProbeActivity"});
    }
    static SharedPreferences data(Context context){return context.getSharedPreferences(PREFS,Context.MODE_PRIVATE);}
    static void notifyRows(Context context){
        context.getContentResolver().notifyChange(TodayContract.rows(COUNTER_AUTHORITY),null);
        context.getContentResolver().notifyChange(TodayContract.rows(STATUS_AUTHORITY),null);
    }
    public static final class Counter extends ProbeProvider { public Counter(){super("counter");} }
    public static final class Status extends ProbeProvider { public Status(){super("status");} }
}
