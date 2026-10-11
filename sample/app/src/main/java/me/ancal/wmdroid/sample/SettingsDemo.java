package me.ancal.wmdroid.sample;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.io.IOException;
import me.ancal.wmdroid.sdk.*;

/** Generic caller-owned metadata; callbacks never change Android or launcher settings. */
final class SettingsDemo extends LinearLayout implements WmControls.BackHandler,WmControls.LanguageListener,WmControls.RestyleListener {
    final WmSettingsView grid;
    String lastKey="";
    int actions;
    private String language="ko";
    private final Runnable close;
    private final java.util.List<String[]> artwork=new java.util.ArrayList<>();
    private String artworkRevision;
    SettingsDemo(Context context,Typeface font,int selection,int face,String language,Runnable close){
        super(context);this.close=close;this.language=WmStrings.language(context,language);setOrientation(VERTICAL);
        grid=new WmSettingsView(context,font,language,selection,face,new WmSettingsView.Category[]{
            category("개인",new String[][]{{"fonts","글꼴","imageres_icon_res_129"},{"theme","테마 색","imageres_icon_res_197"},{"app_icons","Android 앱 아이콘","imageres_icon_res_15"}}),
            category("시스템",new String[][]{{"finger_mouse","핑거마우스","main_icon_res_100"},{"keyboard","키보드","main_icon_res_200"},{"date","날짜 및 시간","timedate_icon_res_50"},{"display","화면 표시","imageres_icon_res_197"},{"tools","도구","main_icon_res_500"},{"lock","잠금","main_icon_res_607"},{"fonts_system","글꼴","imageres_icon_res_129"},{"apps","프로그램","imageres_icon_res_15"},{"calendar","일정","timedate_icon_res_201"},{"pointer","포인터","main_icon_res_104"},{"typing","입력","main_icon_res_200"},{"folders","폴더","main_icon_res_606"},{"theme_system","테마 색","imageres_icon_res_197"}}),
            category("연결",new String[][]{{"network","네트워크","netshell_icon_0"},{"internet","인터넷","imageres_icon_res_73"},{"connections","연결","imageres_icon_res_143"}})
        },()->{},key->{lastKey=key;if(actions<Integer.MAX_VALUE)actions++;Toast.makeText(context,text("설정 요청 예제: ")+key,Toast.LENGTH_SHORT).show();});
        addView(grid,new LinearLayout.LayoutParams(-1,-1));artworkRevision=WmResources.revision(context);
    }
    private WmSettingsView.Category category(String title,String[][] metadata){
        WmSettingsView.Item[] items=new WmSettingsView.Item[metadata.length];
        for(int i=0;i<items.length;i++){artwork.add(metadata[i]);items[i]=new WmSettingsView.Item(metadata[i][0],metadata[i][1],icon(metadata[i][2]));}
        return new WmSettingsView.Category(title,items);
    }
    private android.graphics.drawable.Drawable icon(String id){
        try{return WmAssets.loadDrawable(getContext(),id,64);}
        catch(IOException missing){return new android.graphics.drawable.BitmapDrawable((android.content.res.Resources)null,WmResources.emojiBitmap("⚙",64,64));}
    }
    @Override public void onWmRestyle(Typeface regular,Typeface bold,int selection,int face){
        String revision=WmResources.revision(getContext());if(revision.equals(artworkRevision))return;
        for(String[] item:artwork)grid.setIcon(item[0],icon(item[2]));artworkRevision=revision;
    }
    private String text(String authored){return WmStrings.text(getContext(),language,authored);}
    private String text(String authored,String key){return WmStrings.text(getContext(),language,authored,key);}
    void saveState(Bundle out){out.putBoolean("settings",true);out.putBundle("settings_state",grid.saveInteractionState());out.putString("settings_key",lastKey);out.putInt("settings_actions",actions);}
    void restoreState(Bundle saved){
        String key=saved.getString("settings_key","");lastKey=key==null?"":key;actions=Math.max(0,saved.getInt("settings_actions",0));
        try{grid.restoreInteractionState(saved.getBundle("settings_state"));}
        catch(IllegalArgumentException invalid){Toast.makeText(getContext(),text("설정 예제 화면 상태를 복원하지 못했습니다."),Toast.LENGTH_SHORT).show();}
    }
    @Override public void onWmLanguageChanged(String language){this.language=WmStrings.language(getContext(),language);}
    @Override public boolean back(){close.run();return true;}
}
