package me.ancal.wmdroid.sample;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.util.*;
import me.ancal.wmdroid.sdk.*;

/** Interactive SDK fixtures. Edits affect this example, never personal records. */
public final class GalleryActivity extends WmApplet {
    private String language="en",selected="home";
    private View live;
    private WmBrowserView browser;
    private WmMediaView media;
    private ImageView logo;
    private final ArrayList<String[]> demos=new ArrayList<>();
    private static final String[][] CATALOG={
        {"old_week","주 보기","Week view"},{"old_agenda","일정 목록","Agenda"},{"old_year","연도 보기","Year view"},
        {"old_appointment","약속 편집","Appointment editor"},{"old_recurrence","되풀이 편집","Recurrence editor"},
        {"old_phone","전화","Phone"},{"old_contacts","연락처","Contacts"},{"old_notes","메모","Notes"},
        {"old_settings","설정 그리드","Settings grid"},{"old_pwi","PWI 문서","PWI document"},
        {"month","월 보기","Month view"},{"month_calendar","월 선택 달력","Month calendar"},
        {"datefield","날짜 입력","Date input"},{"favorites","즐겨찾기 목록","Favorites list"},
        {"folders","문서 폴더","Document folders"},{"pointer","핑거마우스","Finger mouse"},
        {"controls","공통 컨트롤","Common controls"},{"choice","선택기","Choices"},
        {"dates","날짜 및 시간 입력","Date and time input"},{"categories","범주","Categories"},
        {"boxes","다중 선택","Multiple selection"},{"tabs","탭","Tabs"},
        {"wizard","마법사","Wizard"},{"input","입력 대화상자","Input dialog"},
        {"message","메시지 상자","Message box"},{"menus","메뉴 상태","Menu states"},
        {"scroll","스크롤","Scrolling"},{"today","오늘 행","Today rows"},
        {"clock","시계 패널","Clock panel"},{"task","작업 편집","Task editor"},
        {"reminder","미리 알림","Reminders"},{"calendar_general","일정 일반 옵션","Calendar general options"},
        {"calendar_appointments","약속 옵션","Appointment options"},
        {"touch","터치음 옵션","Touch sound options"},{"speeddial","단축 다이얼","Speed dial"},
        {"subcard","연락처 상세 카드","Contact subcard"},{"documents","문서 속성","Document properties"},
        {"browser","브라우저","Browser"},{"favorite","즐겨찾기 편집","Favorite editor"},
        {"folder","폴더 편집","Folder editor"},{"media","미디어 플레이어","Media player"},
        {"playlist","재생 목록","Playlist"},{"library","미디어 라이브러리","Media library"},
        {"media_properties","미디어 속성","Media properties"},{"icons","아이콘과 글꼴","Icons and fonts"},
        {"utilities","날짜와 색상 계산","Date and color calculations"},{"api","전체 API","All APIs"}
    };
    @Override public void onCreate(Bundle state){super.onCreate(state);language=WmStrings.language(this,getIntent().getStringExtra(LANGUAGE));Collections.addAll(demos,CATALOG);show(state==null?getIntent().getStringExtra("demo"):state.getString("demo","home"));}
    @Override protected void onShellStateChanged(WmShell.State state){language=state.language;if(live!=null){WmControls.restyleTree(live,regular,bold,shade(0xff149ca7),shade(0xffc9e7e9));WmControls.localizeTree(live,language);if("home".equals(selected))localizeHome(live);caption(title(selected));}}
    @Override protected void onResourcesChanged(){if(logo!=null)logo.setImageDrawable(new WmLogo(this));}
    @Override protected void onSaveInstanceState(Bundle out){out.putString("demo",selected);super.onSaveInstanceState(out);}
    @Override public void onBackPressed(){if(!"home".equals(selected)){if(live!=null&&WmControls.back(live))return;show("home");}else super.onBackPressed();}
    @Override protected void onPause(){if(browser!=null)browser.pause();super.onPause();}
    @Override protected void onResume(){super.onResume();if(browser!=null)browser.resume();}
    @Override protected void onDestroy(){release();super.onDestroy();}
    private void localizeHome(View view){if(view instanceof TextView&&view.getTag() instanceof String[]){String[] d=(String[])view.getTag();((TextView)view).setText(t(d[1],d[2]));}if(view instanceof android.view.ViewGroup){android.view.ViewGroup group=(android.view.ViewGroup)view;for(int i=0;i<group.getChildCount();i++)localizeHome(group.getChildAt(i));}}
    private void release(){logo=null;if(browser!=null){browser.close();browser=null;}if(media!=null){media.close();media=null;}}
    private String t(String ko,String en){return "ko".equals(language)?ko:en;}
    private String title(String id){for(String[] d:CATALOG)if(d[0].equals(id))return t(d[1],d[2]);return t("UI 실험실","UI laboratory");}
    private Locale locale(){return WmStrings.locale(language);}
    private LinearLayout column(){LinearLayout v=new LinearLayout(this);v.setOrientation(LinearLayout.VERTICAL);return v;}
    private void row(LinearLayout parent,View v,int height){parent.addView(v,new LinearLayout.LayoutParams(-1,height));}
    private TextView button(String value,Runnable action){TextView v=label(value);WmControls.button(v,shade(0xff149ca7));v.setGravity(android.view.Gravity.CENTER);v.setOnClickListener(w->action.run());return v;}
    private android.net.Uri sampleAudio(){
        java.io.File file=new java.io.File(getCacheDir(),"sdk-tone.wav");
        try(java.io.InputStream in=getAssets().open("sdk-tone.wav");java.io.FileOutputStream out=new java.io.FileOutputStream(file)){byte[] buffer=new byte[4096];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);}
        catch(java.io.IOException failure){throw new IllegalStateException("Missing example audio",failure);}
        return android.net.Uri.parse("content://"+getPackageName()+".media/sdk-tone");
    }
    private void notice(String value){Toast.makeText(this,value,Toast.LENGTH_SHORT).show();}
    private WmDateTimeField.Value now(){Calendar c=Calendar.getInstance();return new WmDateTimeField.Value(c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DAY_OF_MONTH),9,30);}
    private void popup(String name,View body){showPopup(name,body,444,240);}
    private void show(String id){
        if(id!=null&&id.startsWith("old_")){startActivity(new Intent(this,SampleActivity.class).putExtra("example",id.substring(4)).putExtra(LANGUAGE,language));overridePendingTransition(0,0);return;}
        release();dismissPopup();selected=id==null?"home":id;boolean known="home".equals(selected);for(String[] d:CATALOG)known|=d[0].equals(selected);if(!known)selected="home";
        content.removeAllViews();caption(title(selected));closeButton(false,()->{if("home".equals(selected))finish();else show("home");});
        softKeys(t("뒤로","Back"),()->{if("home".equals(selected))finish();else show("home");},t("메뉴","Menu"),null);
        softKeyMenu(true,t("메뉴","Menu"),()->showCommandMenu(true,new WmMenuItem[]{WmMenuItem.command(1,t("실험실","Laboratory")),WmMenuItem.command(2,t("전체 API","All APIs")),WmMenuItem.command(3,t("닫기","Close"))},i->{if(i==1)show("home");if(i==2)show("api");if(i==3)finish();}));
        live=create(selected);content.addView(live,new LinearLayout.LayoutParams(-1,0,1));WmControls.localizeTree(live,language);
    }
    private View create(String id){
        int accent=shade(0xff149ca7),face=shade(0xffc9e7e9);LinearLayout rows=column();
        switch(id){
        case "month":{WmMonthView month=new WmMonthView(this,regular,bold,locale());month.setMonth(now().year,now().month);month.setOnDayClickListener((year,m,day)->notice(year+"-"+m+"-"+day));return month;}
        case "month_calendar":{WmMonthCalendar month=new WmMonthCalendar(this,regular,bold,locale());month.setMonth(now().year,now().month);month.setOnDateListener((year,m,day)->notice(year+"-"+m+"-"+day));return month;}
        case "datefield":{WmDateField field=new WmDateField(this,regular,locale(),now(),accent);field.setOnOpenListener(source->{WmDateTimeField.Value v=source.value();popup("Date",new WmCalendarPicker(this,regular,bold,locale(),v.year,v.month,v.day,accent,(year,m,day)->{source.setValue(new WmDateTimeField.Value(year,m,day,0,0));dismissPopup();}));});row(rows,field,44);break;}
        case "favorites":{WmBrowserFavoritesView favorites=new WmBrowserFavoritesView(this,regular,accent,entry->notice(entry.name));favorites.setEntries(new WmBrowserFavoritesView.Entry[]{new WmBrowserFavoritesView.Entry("folder","Examples",true),new WmBrowserFavoritesView.Entry("site","WMDroid",false)});return favorites;}
        case "folders":{WmDocumentFolders folders=new WmDocumentFolders(this,regular,bold,accent,new WmDocumentFolders.Listener(){public void command(int command,String folder){notice("Command "+command+": "+folder);}public void rename(String folder,String name){notice(folder+": "+name);}});folders.setFolders(new WmNotesView.Folder[]{new WmNotesView.Folder("examples","Examples"),new WmNotesView.Folder("work","Work")},"examples");return folders;}
        case "pointer":{FrameLayout target=new FrameLayout(this);TextView click=button(t("클릭 수: 0","Clicks: 0"),()->{});final int[] count={0};click.setOnClickListener(v->click.setText("Clicks: "+(++count[0])));target.addView(click,new FrameLayout.LayoutParams(200,100));WmPointer pointer=new WmPointer(this,target,()->show("home"));target.addView(pointer,new FrameLayout.LayoutParams(-1,-1));return target;}
        case "home":for(String[] d:CATALOG){TextView entry=button(t(d[1],d[2]),()->show(d[0]));entry.setTag(d);row(rows,entry,44);}break;
        case "controls":{
            row(rows,label(t("일반 텍스트","Regular text")),44);TextView heavy=WmControls.text(this,bold,t("굵은 텍스트","Bold text"));row(rows,heavy,44);
            row(rows,button(t("버튼","Button"),()->notice(t("눌림","Clicked"))),44);TextView disabled=button(t("사용 안 함","Disabled"),()->{});disabled.setEnabled(false);row(rows,disabled,44);
            EditText edit=new EditText(this);edit.setHint(t("텍스트 입력","Enter text"));WmControls.input(edit,regular);row(rows,edit,48);
            CheckBox check=new CheckBox(this);WmControls.check(check,regular);check.setText(t("확인란","Check box"));row(rows,check,44);
            RadioGroup radio=new RadioGroup(this);for(int i=0;i<2;i++){RadioButton r=new RadioButton(this);WmControls.radio(r,regular);r.setText("Radio "+(i+1));radio.addView(r);}row(rows,radio,-2);
            WmThreeStateCheck tri=new WmThreeStateCheck(this,regular);tri.setText(t("세 상태 확인란","Three-state check"));tri.setCheckState(WmThreeStateCheck.INDETERMINATE);row(rows,tri,44);break;}
        case "choice":{
            WmChoice choice=new WmChoice(this,regular,new String[]{"Alpha","Beta","Gamma"},0,accent);choice.setOnOpenListener(source->{WmRadioMenu menu=new WmRadioMenu(this,regular,source.choices(),source.selectedIndex(),"",accent,index->{source.choose(index);dismissPopup();});popup("WmChoice",menu);});row(rows,choice,44);
            WmEditableChoice edit=new WmEditableChoice(this,regular,new String[]{"15","30","60"},"20",accent);edit.setOnOpenListener(source->popup("WmEditableChoice",new WmRadioMenu(this,regular,new String[]{"15","30","60"},-1,"",accent,index->{source.setValue(new String[]{"15","30","60"}[index]);dismissPopup();})));row(rows,edit,44);break;}
        case "dates":{
            WmDateTimeField field=new WmDateTimeField(this,regular,locale(),now(),accent);field.setOnDateOpen(source->{WmDateTimeField.Value v=source.dateValue();popup("WmCalendarPicker",new WmCalendarPicker(this,regular,bold,locale(),v.year,v.month,v.day,accent,(year,month,day)->{source.setValue(new WmDateTimeField.Value(year,month,day,v.hour,v.minute));dismissPopup();}));});field.setOnTimeOpen(source->popup("Time",new WmRadioMenu(this,regular,new String[]{"09:00","12:00","18:00"},-1,"",accent,index->{WmDateTimeField.Value v=source.dateValue();source.setValue(new WmDateTimeField.Value(v.year,v.month,v.day,new int[]{9,12,18}[index],0));dismissPopup();})));row(rows,field,44);break;}
        case "categories":row(rows,new WmCategoryPicker(this,regular,locale(),new String[]{"Work","Personal","Travel"},"Work"),300);row(rows,new WmCategoryNameForm(this,regular,bold,accent),200);break;
        case "boxes":row(rows,new WmBoxSelect(this,regular,new String[]{"Monday","Tuesday","Wednesday","Thursday"},5,true,accent),200);break;
        case "tabs":{WmPropertySheet sheet=new WmPropertySheet(this,regular,face);for(int i=0;i<6;i++)sheet.addPage("Tab "+(i+1),label("Page "+(i+1)));return sheet;}
        case "wizard":{WmWizard wizard=new WmWizard(this,regular,locale(),accent);for(int i=0;i<3;i++){LinearLayout p=column();row(p,label(t("단계 ","Step ")+(i+1)),44);EditText e=new EditText(this);WmControls.input(e,regular);row(p,e,44);wizard.addPage(p);}wizard.setOnFinish(()->notice(t("완료","Finished")));wizard.setOnCancel(()->show("home"));return wizard;}
        case "input":row(rows,new WmInputDialog(this,regular,bold,t("이름","Name"),accent,64,value->notice(value)),240);break;
        case "message":row(rows,new WmMessageBox(this,regular,locale(),"WMDroid",t("예제 메시지","Example message"),accent,()->notice("OK")),260);break;
        case "menus":row(rows,button(t("팝업 메뉴 열기","Open popup menu"),()->popup("WmRadioMenu",new WmRadioMenu(this,regular,new WmMenuItem[]{WmMenuItem.command(10,t("명령","Command")),WmMenuItem.checked(11,t("선택됨","Checked"),true),WmMenuItem.command(12,t("사용 안 함","Disabled")).withEnabled(false),WmMenuItem.separator(),WmMenuItem.submenu(t("하위 메뉴","Submenu"),WmMenuItem.command(13,"Child"))},"",accent,index->notice("ID "+index)))),44);break;
        case "scroll":for(int i=0;i<100;i++)row(rows,button("Row "+i,()->notice("Selected")),44);break;
        case "today":{
            WmToday.Style style=new WmToday.Style(Color.BLACK,Color.WHITE,accent,0xff90b0b0);
            WmToday.Layout list=new WmToday.Layout(this,style);for(int i=0;i<4;i++){WmToday.Row r=new WmToday.Row(this,new ImageView(this),regular,style,"Row "+i+(i%2==0?"\nDetail":""));r.setOnClickListener(v->notice("Today"));list.register("row"+i,r,64,WmToday.Divider.BETWEEN);}list.arrange(Collections.emptyList(),Collections.emptySet());return new WmScrollView(this,list);}
        case "clock":return new WmClockPanel(this,regular,language);
        case "task":return new WmTaskEditor(this,regular,locale(),new WmTaskEditor.Value("SDK task",0,false,now(),now(),"Work",0,"Editable sample"),accent,face);
        case "reminder":return new WmAppointmentReminder(this,regular,locale(),new WmReminder(true,15,WmReminder.MINUTES),accent);
        case "calendar_general":return new WmCalendarGeneralOptions(this,regular,bold,locale(),new WmCalendarGeneralOptions.Options(0,Calendar.MONDAY,5,true,true),accent,value->notice("Changed"));
        case "calendar_appointments":{WmCalendarAppointmentsOptions options=new WmCalendarAppointmentsOptions(this,regular,bold,locale(),new WmCalendarAppointmentsOptions.Options(true,15,0,63),accent,value->notice("Changed"));options.setAccounts(new String[]{"local"},new String[]{"Local sample"},"local",value->notice(value));return options;}
        case "touch":return new WmTouchSoundOptions(this,regular,language,false,1,(enabled,level)->notice("enabled="+enabled+"; level="+level));
        case "speeddial":return new WmSpeedDialView(this,regular,bold,accent,new WmSpeedDialView.Entry[]{new WmSpeedDialView.Entry(2,"Example","010-0000-0000"),new WmSpeedDialView.Entry(3,"Work","02-000-0000")});
        case "subcard":return new WmContactSubcardView(this,regular,0,new String[]{"Prefix","First","Middle","Last","Suffix","Nickname"},new String[]{"","Alex","","Example","","SDK"},accent);
        case "documents":return new WmDocumentProperties(this,regular,locale(),"Example.txt",new String[]{"Documents","Work"},0,new String[]{"Device"},0,"128 bytes","2026-10-13","Text",accent);
        case "browser":browser=new WmBrowserView(this,regular);browser.webView().loadData("<h1>WMDroid</h1><p>Local HTML example</p><input placeholder='Type here'>","text/html","UTF-8");return browser;
        case "favorite":return new WmBrowserFavoriteForm(this,regular,"WMDroid","https://github.com/ancalina/WMDroid",new String[]{"Favorites","Work"},0,accent);
        case "folder":return new WmBrowserFolderForm(this,regular,"Example");
        case "media":media=new WmMediaView(this,regular,bold);media.setPlaylist(new WmMediaQueue.Entry[]{new WmMediaQueue.Entry(sampleAudio(),"SDK tone")},0,false);return media;
        case "playlist":{WmMediaPlaylistView playlist=new WmMediaPlaylistView(this,regular,id2->notice("Command "+id2));playlist.setEntries(new WmMediaQueue.Entry[]{new WmMediaQueue.Entry(sampleAudio(),"Example track")},0);return playlist;}
        case "library":return new WmMediaLibraryView(this,regular,id2->notice("Command "+id2));
        case "media_properties":{WmMediaPropertiesView properties=new WmMediaPropertiesView(this,regular,bold);properties.setValues(new String[]{"Example artist","Example album","Example artist","Example track","Instrumental","0:05","PCM","Mono 16-bit",null,null,"app sample","1","128 kbps"});return properties;}
        case "icons":{row(rows,label("Regular / 012345 / 가나다"),44);TextView b=WmControls.text(this,bold,"Bold / 012345 / 가나다");row(rows,b,44);logo=new ImageView(this);logo.setImageDrawable(new WmLogo(this));row(rows,logo,64);break;}
        case "utilities":row(rows,label("Reminder: "+WmReminder.fromMinutes(true,120).quantity+" hours\nToday width: "+WmMetrics.WIDTH+"\nProvider rows: "+TodayContract.MAX_ROWS+"\nColor: "+Integer.toHexString(WmColor.gray(0xff149ca7))),200);break;
        case "api":return new ApiCatalogView(this,regular,language,accent,face,this::show);
        }
        return new WmScrollView(this,rows,LinearLayout.LayoutParams.WRAP_CONTENT,face);
    }
}
