package org.example.todayprobe;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Ordinary Android UI; only its exported providers participate in the Today SDK. */
public final class ProbeActivity extends Activity {
    private TextView counter,selected,status;
    @Override protected void onCreate(Bundle saved){
        setTheme(Build.VERSION.SDK_INT>=21?android.R.style.Theme_Material_Light:android.R.style.Theme_Holo_Light);
        super.onCreate(saved);
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);
        int inset=Math.round(16*getResources().getDisplayMetrics().density);body.setPadding(inset,inset,inset,inset);
        TextView title=new TextView(this);title.setText("Today plugin probe");title.setTextSize(24);body.addView(title);
        selected=new TextView(this);selected.setId(android.R.id.text2);selected.setTag("probe_selected");body.addView(selected);
        counter=new TextView(this);counter.setId(android.R.id.text1);counter.setTag("probe_counter");counter.setTextSize(20);body.addView(counter);
        TextView theme=new TextView(this);theme.setText(Build.VERSION.SDK_INT>=21?"Android Material Activity":"Android Holo Activity");body.addView(theme);
        Button increment=new Button(this);increment.setId(android.R.id.button1);increment.setTag("probe_increment");increment.setText("Increment counter");increment.setContentDescription("Increment counter");
        increment.setOnClickListener(view->{
            SharedPreferences data=ProbeProvider.data(this);int old=data.getInt(ProbeProvider.COUNT,0);
            int next=old==Integer.MAX_VALUE?old:old+1;
            if(data.edit().putInt(ProbeProvider.COUNT,next).commit()){
                ProbeProvider.notifyRows(this);status.setText("Today plugins notified");refresh();
            }else status.setText("Could not save counter");
        });body.addView(increment,new LinearLayout.LayoutParams(-1,-2));
        status=new TextView(this);status.setTag("probe_status");body.addView(status);setContentView(body);
        if(saved==null)recordIntent(getIntent());refresh();
    }
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);recordIntent(intent);refresh();}
    @Override protected void onResume(){super.onResume();refresh();}
    private void recordIntent(Intent intent){
        String row=intent==null?null:intent.getStringExtra(ProbeProvider.ROW_EXTRA);
        if(!"counter".equals(row)&&!"status".equals(row))row="none";
        SharedPreferences data=ProbeProvider.data(this);int old=data.getInt(ProbeProvider.LAUNCHES,0);
        if(data.edit().putString(ProbeProvider.LAST_ROW,row).putInt(ProbeProvider.LAUNCHES,old==Integer.MAX_VALUE?old:old+1).commit())ProbeProvider.notifyRows(this);
        else status.setText("Could not save selected module");
    }
    private void refresh(){
        if(counter==null)return;SharedPreferences data=ProbeProvider.data(this);
        counter.setText("Count: "+data.getInt(ProbeProvider.COUNT,0));selected.setText("Selected module: "+data.getString(ProbeProvider.LAST_ROW,"none"));
    }
}
