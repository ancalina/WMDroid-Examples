package me.ancal.wmdroid.widgets;

import android.content.*;
import android.database.ContentObserver;
import android.graphics.Color;
import android.os.*;
import android.text.*;
import android.view.View;
import android.widget.*;
import java.util.*;
import me.ancal.wmdroid.sdk.*;

/** One independent SDK app; each Today module keeps its own settings and provider. */
public final class WidgetsActivity extends WmApplet {
    private static final String[] KINDS={"weather","transit","splatnet","battery"};
    private SharedPreferences prefs;private String kind="",renderedLanguage="";private int tab;
    private final java.util.concurrent.ExecutorService renderer=java.util.concurrent.Executors.newSingleThreadExecutor();
    private java.util.concurrent.Future<?> renderJob;private int renderGeneration;private boolean destroyed;
    private LinearLayout form;private FrameLayout preview;private TextView status;
    private final Handler ui=new Handler(Looper.getMainLooper());private boolean observing;
    private final Runnable update=this::refreshPreview;
    private final ContentObserver observer=new ContentObserver(ui){public void onChange(boolean self){ui.removeCallbacks(update);ui.post(update);}};
    private final SharedPreferences.OnSharedPreferenceChangeListener changes=(store,key)->{
        if(key==null||!(key.matches("(weather|transit|splatnet|battery)\\.(height|auto_height|text_size|padding|bold|align|show_title|show_time|custom_colors|text_color|background|title)")||Arrays.asList("weather_lat","weather_lon","weather_label","transit_provider","transit_key","transit_station_id","transit_city_code","transit_route_id","transit_station_name","transit_allow_http","splatnet_mode").contains(key)))return;
        ui.removeCallbacks(update);ui.postDelayed(update,100);WidgetData.notifyWidgets(this);
    };
    private String language(){return WmStrings.language(this,getIntent().getStringExtra(LANGUAGE));}
    private String t(String authored){return WmStrings.text(this,language(),authored);}
    private String t(String authored,String key){return WmStrings.text(this,language(),authored,key);}
    private String title(String id){switch(id){case "weather":return t("날씨");case "transit":return t("버스·지하철");case "splatnet":return t("스플래툰 3 스테이지");default:return t("배터리");}}
    private static boolean known(String id){return Arrays.asList(KINDS).contains(id);}
    @Override public void onCreate(Bundle saved){super.onCreate(saved);prefs=getSharedPreferences("widgets",0);prefs.registerOnSharedPreferenceChangeListener(changes);String requested=saved==null?getIntent().getStringExtra("me.ancal.wmdroid.sdk.ROW"):saved.getString("kind");if(known(requested))kind=requested;tab=saved==null?0:saved.getInt("tab",0);show();}
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);String requested=intent.getStringExtra("me.ancal.wmdroid.sdk.ROW");kind=known(requested)?requested:"";tab=0;show();}
    @Override protected void onResume(){super.onResume();if(prefs==null)return;for(String id:KINDS)getContentResolver().registerContentObserver(TodayContract.rows("me.ancal.wmdroid.widgets."+id),false,observer);observing=true;refreshPreview();}
    @Override protected void onPause(){if(observing){getContentResolver().unregisterContentObserver(observer);observing=false;}ui.removeCallbacks(update);renderGeneration++;if(renderJob!=null)renderJob.cancel(true);super.onPause();}
    @Override protected void onDestroy(){destroyed=true;renderer.shutdownNow();if(prefs!=null)prefs.unregisterOnSharedPreferenceChangeListener(changes);ui.removeCallbacks(update);super.onDestroy();}
    @Override protected void onSaveInstanceState(Bundle out){out.putString("kind",kind);out.putInt("tab",tab);super.onSaveInstanceState(out);}
    @Override protected void onShellStateChanged(WmShell.State state){if(prefs!=null){if(!state.language.equals(renderedLanguage))show();else refreshPreview();}}
    private void show(){
        renderGeneration++;renderedLanguage=language();
        content.removeAllViews();preview=null;status=null;caption(kind.isEmpty()?t("WMDroid 위젯"):title(kind));closeButton(false,null);
        form=new LinearLayout(this);form.setOrientation(LinearLayout.VERTICAL);form.setPadding(12,4,12,8);
        content.addView(new WmScrollView(this,form,-2,WmPalette.face(accent)),new LinearLayout.LayoutParams(-1,0,1));
        if(kind.isEmpty()){
            note(t("오늘 → 항목에서 표시와 순서를 설정하세요."));
            for(String id:KINDS){TextView item=entry(title(id),()->{kind=id;tab=0;show();});form.addView(item,new LinearLayout.LayoutParams(-1,52));}
            note(t("외부 서비스 인증키는 이 앱 안에서만 저장합니다."));
            softKeys(t("새로 고침"),()->{for(String id:KINDS)WidgetData.refresh(this,id,true);refreshPreview();},t("닫기"),this::finish);return;
        }
        preview=new FrameLayout(this);preview.setBackgroundColor(0xff176b76);form.addView(preview,new LinearLayout.LayoutParams(-1,128));
        status=label("");status.setAutoLinkMask(android.text.util.Linkify.WEB_URLS);status.setLinksClickable(true);status.setPadding(0,8,0,8);form.addView(status,new LinearLayout.LayoutParams(-1,-2));
        if(tab==0)dataSettings();else styleSettings();
        WmTabStrip tabs=new WmTabStrip(this,WmPalette.face(accent));TextView data=tabs.addTab(t("내용"),regular,()->{tab=0;show();}),style=tabs.addTab(t("모양"),regular,()->{tab=1;show();});WmControls.tab(data,tab==0,WmPalette.face(accent));WmControls.tab(style,tab==1,WmPalette.face(accent));content.addView(tabs);
        softKeys(t("새로 고침"),()->{WidgetData.refresh(this,kind,true);refreshPreview();},t("뒤로"),()->{kind="";show();});refreshPreview();
    }
    private void note(String value){TextView text=label(value);text.setPadding(0,8,0,8);form.addView(text,new LinearLayout.LayoutParams(-1,-2));}
    private void field(String label,String key,String fallback,boolean secret){
        note(label);EditText edit=new EditText(this);WmControls.input(edit,regular);edit.setSingleLine(true);edit.setInputType(secret?android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD:android.text.InputType.TYPE_CLASS_TEXT);edit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(secret?512:160)});edit.setText(prefs.getString(key,fallback));form.addView(edit,new LinearLayout.LayoutParams(-1,44));
        edit.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int f){}public void onTextChanged(CharSequence s,int a,int b,int c){}public void afterTextChanged(Editable s){prefs.edit().putString(key,s.toString()).apply();}});
    }
    private EditText number(String label,String key,int fallback,int min,int max){
        note(label+" ("+min+"–"+max+")");EditText edit=new EditText(this);WmControls.input(edit,regular);edit.setSingleLine(true);edit.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);edit.setText(String.valueOf(prefs.getInt(key,fallback)));form.addView(edit,new LinearLayout.LayoutParams(-1,44));
        edit.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int f){}public void onTextChanged(CharSequence s,int a,int b,int c){}public void afterTextChanged(Editable s){try{int value=Integer.parseInt(s.toString());if(value>=min&&value<=max)prefs.edit().putInt(key,value).apply();}catch(NumberFormatException invalid){}}});
        return edit;
    }
    private void check(String label,String key,boolean fallback){CheckBox box=new CheckBox(this);WmControls.check(box,regular);box.setText(label);box.setChecked(prefs.getBoolean(key,fallback));box.setOnCheckedChangeListener((v,yes)->prefs.edit().putBoolean(key,yes).apply());form.addView(box,new LinearLayout.LayoutParams(-1,52));}
    private void choice(String label,String key,String fallback,String[] values,String[] captions){
        String selected=prefs.getString(key,fallback);int index=Math.max(0,Arrays.asList(values).indexOf(selected));
        form.addView(entry(label+": "+captions[index],()->showCommandMenu(true,captions,index,chosen->{prefs.edit().putString(key,values[chosen]).apply();show();})),new LinearLayout.LayoutParams(-1,52));
    }
    private void color(String label,String key,int fallback){
        note(label+" (#RRGGBB / #AARRGGBB)");EditText edit=new EditText(this);WmControls.input(edit,regular);edit.setSingleLine(true);edit.setText(String.format(Locale.ROOT,"#%08X",prefs.getInt(key,fallback)));form.addView(edit,new LinearLayout.LayoutParams(-1,44));
        edit.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int f){}public void onTextChanged(CharSequence s,int a,int b,int c){}public void afterTextChanged(Editable s){String value=s.toString();if(value.matches("#[0-9a-fA-F]{6}|#[0-9a-fA-F]{8}"))prefs.edit().putInt(key,Color.parseColor(value)).apply();}});
    }
    private void styleSettings(){String k=kind+".";
        field(t("사용자 제목 (비우면 기본값)"),k+"title","",false);
        CheckBox automatic=new CheckBox(this);WmControls.check(automatic,regular);automatic.setText(t("내용에 맞춰 높이"));
        automatic.setChecked(prefs.getBoolean(k+"auto_height",!prefs.contains(k+"height")));form.addView(automatic,new LinearLayout.LayoutParams(-1,52));
        EditText height=number(t("높이 · VGA 픽셀"),k+"height",WidgetRenderer.defaultHeight(kind),64,480);height.setEnabled(!automatic.isChecked());
        automatic.setOnCheckedChangeListener((view,enabled)->{height.setEnabled(!enabled);prefs.edit().putBoolean(k+"auto_height",enabled).apply();});
        number(t("글자 크기"),k+"text_size",WmMetrics.TODAY_FONT,16,40);number(t("위아래 여백"),k+"padding",WmMetrics.INSET,0,32);
        check(t("제목 표시"),k+"show_title",true);check(t("갱신 시각 표시"),k+"show_time",true);check(t("굵은 글꼴","widgets.bold_face"),k+"bold",false);
        choice(t("정렬"),k+"align","left",new String[]{"left","center","right"},new String[]{t("왼쪽"),t("가운데"),t("오른쪽")});
        check(t("사용자 색상 사용"),k+"custom_colors",false);color(t("글자 색"),k+"text_color",Color.WHITE);color(t("배경 색 (알파 00 = 투명)"),k+"background",Color.TRANSPARENT);
    }
    private void dataSettings(){switch(kind){
        case "weather":
            field(t("지역 이름"),"weather_label",t("서울"),false);field(t("위도"),"weather_lat","37.5665",false);field(t("경도"),"weather_lon","126.9780",false);
            note(t("Open-Meteo · 인증키 불필요 · 약 15분 간격. 좌표는 날씨 제공자에게 전송됩니다."));break;
        case "transit":
            choice(t("정보 제공자"),"transit_provider","gbis",new String[]{"seoul_bus","seoul_subway","gbis","tago"},new String[]{t("서울 버스"),t("서울·수도권 지하철"),t("경기 버스 GBIS"),t("전국 버스 TAGO")});
            field(t("API 인증키 (일반 Decoding 키)"),"transit_key","",true);
            String service=prefs.getString("transit_provider","gbis");
            if("seoul_subway".equals(service)){field(t("역 이름"),"transit_station_name","시청",false);note(t("서울 실시간 지하철 전용 인증키가 필요합니다. 서울 API에서 제공하는 역만 지원하며 일부 수도권 구간은 제외됩니다."));}
            else{field(t("정류소 ID"),"transit_station_id","",false);field(t("노선 ID (서울 버스 필수)"),"transit_route_id","",false);if("tago".equals(service))field(t("도시 코드"),"transit_city_code","",false);}
            if(service.startsWith("seoul_")){check(t("서울 API HTTP 연결 허용"),"transit_allow_http",false);note(t("서울 공식 HTTP API에 연결하면 인증키가 암호화되지 않습니다. 자동으로 HTTP로 전환하지 않습니다."));}
            note(t("실제 도착정보는 사용자 인증키·정류소 설정 후 표시됩니다. 하루 호출 한도는 제공자의 정책을 따릅니다."));break;
        case "splatnet":
            choice(t("모드"),"splatnet_mode","regular",new String[]{"regular","bankara_series","bankara_open","x"},new String[]{t("레귤러 매치"),t("카오폴리스 매치 챌린지"),t("카오폴리스 매치 오픈"),t("X 매치")});
            note(t("Splatoon3.ink 공개 스케줄 · 텍스트만 표시 · Nintendo 로그인 불필요. SplatNet 3 계정 API가 아닙니다. 제공자 조건에 따라 새로 고침도 1시간 캐시를 따릅니다."));break;
        default:break;
    }}
    private void refreshPreview(){if(destroyed||prefs==null||kind.isEmpty()||preview==null)return;
        final int ticket=++renderGeneration,theme=accent;final String selected=kind,language=language();final Context context=getApplicationContext();
        if(renderJob!=null)renderJob.cancel(true);
        renderJob=renderer.submit(()->{
            WidgetData.Snapshot snapshot=WidgetData.snapshot(context,selected,language);WmShell.State state=WmShell.state(context);Bundle options=new Bundle();options.putInt("width",480);options.putInt("pixel_width",context.getResources().getDisplayMetrics().widthPixels);options.putString("language",language);options.putInt("accent",theme);options.putInt("text_color",Color.WHITE);options.putString("font",state.font);options.putString("font_bold",state.fontBold);options.putBoolean("hybrid",state.hybrid);
            TodayContract.Widget widget=WidgetRenderer.render(context,selected,options,snapshot);
            ui.post(()->{if(destroyed||ticket!=renderGeneration||preview==null)return;View image=widget.view.apply(this,preview);image.setOnClickListener(null);image.setClickable(false);image.setContentDescription(snapshot.title+", "+snapshot.detail);preview.removeAllViews();preview.addView(image,new FrameLayout.LayoutParams(-1,-1));int previewWidth=preview.getWidth()>0?preview.getWidth():456;preview.setLayoutParams(new LinearLayout.LayoutParams(-1,Math.round(widget.height*previewWidth/480f)));status.setText(snapshot.detail+"\n"+snapshot.source);});
        });
    }
}
