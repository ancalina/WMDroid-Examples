package me.ancal.wmdroid.widgets;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.ImageView;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import android.widget.RemoteViews;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.ancal.wmdroid.sdk.TodayContract;
import me.ancal.wmdroid.sdk.WmFonts;
import me.ancal.wmdroid.sdk.WmFontFile;
import me.ancal.wmdroid.sdk.WmMetrics;
import me.ancal.wmdroid.sdk.WmToday;

/** Provider-side text rasterization. No app View or module code runs inside the host.
 * Each module keeps only its latest immutable rendering key/bitmap; no Context is retained.
 */
final class WidgetRenderer {
    private WidgetRenderer(){}
    private static final String[] KINDS={"weather","transit","splatnet","battery"};
    private static final Map<String,Cached> CACHE=new HashMap<String,Cached>();
    static int defaultHeight(String kind){return WmMetrics.todayHeight(3);}
    private static final class Cached {
        final List<Object> key;final Bitmap bitmap;final int height;
        Cached(List<Object> key,Bitmap bitmap,int height){this.key=Collections.unmodifiableList(new ArrayList<Object>(key));this.bitmap=bitmap;this.height=height;}
    }
    static TodayContract.Widget render(Context context,String kind,Bundle options,WidgetData.Snapshot snapshot){
        int kindIndex=Arrays.asList(KINDS).indexOf(kind);if(kindIndex<0)throw new IllegalArgumentException("Unknown widget kind");
        SharedPreferences prefs=context.getSharedPreferences("widgets",0);String prefix=kind+".";
        int height=clamp(prefs.getInt(prefix+"height",defaultHeight(kind)),64,480),size=clamp(prefs.getInt(prefix+"text_size",WmMetrics.TODAY_FONT),16,40);
        boolean auto=prefs.getBoolean(prefix+"auto_height",!prefs.contains(prefix+"height"));
        int padding=clamp(prefs.getInt(prefix+"padding",WmMetrics.INSET),0,32),pixels=clamp(options.getInt("pixel_width",480),480,Build.VERSION.SDK_INT<21?720:1080);
        boolean heavy=prefs.getBoolean(prefix+"bold",false),showTitle=prefs.getBoolean(prefix+"show_title",true),showTime=prefs.getBoolean(prefix+"show_time",true);
        boolean custom=prefs.getBoolean(prefix+"custom_colors",false),hybrid=options.getBoolean("hybrid",true);
        int color=custom?prefs.getInt(prefix+"text_color",Color.WHITE):options.getInt("text_color",Color.WHITE);
        int background=custom?prefs.getInt(prefix+"background",Color.TRANSPARENT):Color.TRANSPARENT;
        String align=prefs.getString(prefix+"align","left");if(!"center".equals(align)&&!"right".equals(align))align="left";
        String language=me.ancal.wmdroid.sdk.WmStrings.language(context,options.getString("language"));
        String override=prefs.getString(prefix+"title","");String title=override==null||override.trim().isEmpty()?snapshot.title:override;
        String font=fontPath(options.getString("font")),fontBold=options.getString("font_bold");
        if(fontBold==null||!fontBold.equals(fontPath(fontBold)))fontBold="";
        String time= snapshot.updated>0 ? me.ancal.wmdroid.sdk.WmStrings.text(context,language,"갱신 ")+android.text.format.DateFormat.getTimeFormat(context).format(new Date(snapshot.updated)) : me.ancal.wmdroid.sdk.WmStrings.text(context,language,"업데이트 대기");
        List<Object> key=Arrays.<Object>asList(kind,height,auto,size,padding,pixels,heavy,showTitle,showTime,custom,hybrid,color,background,align,language,title,font,fontBold,
                options.getInt("accent",0xff149ca7),snapshot.title,snapshot.detail,snapshot.source,snapshot.updated,snapshot.loading,time,me.ancal.wmdroid.sdk.WmResources.revision(context));
        Cached cached;
        synchronized(CACHE){cached=CACHE.get(kind);if(cached!=null&&!cached.key.equals(key))cached=null;}
        if(cached==null){
            // Load fonts outside UI rendering and outside the bitmap cache lock.
            final Typeface selected=face(context,font,fontBold,hybrid,heavy);
            final String text=content(title,snapshot.detail,time,showTitle,showTime);
            final int alignment="center".equals(align)?Gravity.CENTER_HORIZONTAL:"right".equals(align)?Gravity.RIGHT:Gravity.LEFT;
            FutureTask<Cached> task=new FutureTask<>(()->{
                WmToday.TextStyle typography=new WmToday.TextStyle(size,padding,WmMetrics.TEXT_LEFT,WmMetrics.ICON_INSET,alignment);
                WmToday.Row row=new WmToday.Row(context,new ImageView(context),selected,new WmToday.Style(color,color,Color.TRANSPARENT,Color.TRANSPARENT),text,typography);
                int fitted=auto?clamp(row.getLayoutParams().height,WmMetrics.TODAY_MIN,TodayContract.MAX_HEIGHT):height;
                row.setBackgroundColor(background);
                return new Cached(key,WmToday.rasterize(row,pixels,fitted),fitted);
            });
            if(Looper.myLooper()==Looper.getMainLooper())task.run();else new Handler(Looper.getMainLooper()).post(task);
            try{cached=task.get(5,TimeUnit.SECONDS);}
            catch(InterruptedException interrupted){task.cancel(false);Thread.currentThread().interrupt();throw new IllegalStateException("Today rendering interrupted",interrupted);}
            catch(TimeoutException timeout){task.cancel(false);throw new IllegalStateException("Today rendering timed out",timeout);}
            catch(ExecutionException failure){throw new IllegalStateException("Today rendering failed",failure.getCause());}
            synchronized(CACHE){CACHE.put(kind,cached);}
            // Do not recycle replaced bitmaps: an in-flight RemoteViews may still own them.
        }
        RemoteViews views=new RemoteViews(context.getPackageName(),R.layout.today_widget);
        views.setImageViewBitmap(R.id.widget_image,cached.bitmap);
        // RemoteViews gained this action in API15; API14 uses the host row description.
        if(Build.VERSION.SDK_INT>=15)views.setContentDescription(R.id.widget_image,(showTitle?title+"\n":"")+snapshot.detail+(showTime?"\n"+time:""));
        // The provider row activity handles the whole-row tap through the host's SDK launch path.
        // A duplicate image PendingIntent bypasses that path and opens a separate task.
        return new TodayContract.Widget(views,cached.height);
    }
    private static int clamp(int value,int min,int max){return Math.max(min,Math.min(max,value));}
    private static String fontPath(String requested){
        if(WmFontFile.isSource(requested)||"fonts/samsung/samsung-gothic.ttf".equals(requested)||"fonts/samsung/ssgd.ttf".equals(requested)||"fonts/tahoma/regular.ttf".equals(requested)||"fonts/tahoma/bold.ttf".equals(requested)||"fonts/malgun/bold.ttf".equals(requested))return requested;
        return "fonts/malgun/regular.ttf";
    }
    private static Typeface face(Context context,String path,String boldPath,boolean hybrid,boolean heavy){
        String selected=!heavy?path:!boldPath.isEmpty()?boldPath:"fonts/malgun/regular.ttf".equals(path)?"fonts/malgun/bold.ttf":"fonts/tahoma/regular.ttf".equals(path)?"fonts/tahoma/bold.ttf":path;
        // Samsung/imported regular outlines are unchanged when no bold face is supplied.
        try{return WmFonts.load(context,selected,hybrid,heavy);}catch(RuntimeException unavailable){return WmFonts.load(context,heavy?"fonts/malgun/bold.ttf":"fonts/malgun/regular.ttf",hybrid,heavy);}
    }
    private static String content(String title,String detail,String time,boolean showTitle,boolean showTime){
        List<String> lines=new ArrayList<>();if(showTitle&&title!=null&&!title.isEmpty())lines.add(title);
        if(detail!=null&&!detail.isEmpty())lines.add(detail);if(showTime&&!time.isEmpty())lines.add(time);
        StringBuilder text=new StringBuilder();for(String line:lines){if(text.length()>0)text.append('\n');text.append(line);}return text.toString();
    }
}
