package me.ancal.wmdroid.sample;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import java.lang.reflect.*;
import java.util.*;
import org.json.*;
import me.ancal.wmdroid.sdk.*;

/** Descriptions come from matching SDK sources; signatures come from the loaded AAR. */
final class ApiCatalogView extends LinearLayout implements WmControls.BackHandler,WmControls.RestyleListener,WmControls.LanguageListener {
    interface Open {void show(String demo);}
    private Typeface font;
    private int selection,face;
    private String language;
    private final Open open;
    private final JSONArray types;
    private boolean detail;
    private String query="";
    private final Map<String,String> demos=new HashMap<>();
    ApiCatalogView(Context context,Typeface font,String language,int selection,int face,Open open){
        super(context);setOrientation(VERTICAL);this.font=font;this.language=language;this.selection=selection;this.face=face;this.open=open;
        try(java.io.InputStream in=context.getAssets().open("sdk-api-catalog.json")){
            java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();byte[] buffer=new byte[4096];int count;while((count=in.read(buffer))!=-1)bytes.write(buffer,0,count);
            types=new JSONArray(bytes.toString("UTF-8"));
        }catch(java.io.IOException|JSONException error){throw new IllegalStateException("Invalid SDK API catalog",error);}
        map("controls","WmControls","WmThreeStateCheck","WmTouchFeedback");map("choice","WmChoice","WmEditableChoice","WmComboGlyph","WmRadioMenu");
        map("dates","WmDateTimeField","WmCalendarPicker","WmMonthYearEditor","WmYearChooser");map("datefield","WmDateField");map("categories","WmCategoryPicker","WmCategoryNameForm");map("boxes","WmBoxSelect");
        map("tabs","WmPropertySheet","WmTabStrip","WmTabDrawable");map("wizard","WmWizard");map("input","WmInputDialog");map("message","WmMessageBox");map("menus","WmMenuItem","WmCommandMenu","WmPopup","WmBubble");
        map("scroll","WmScrollView","WmScrollBar");map("today","WmToday");map("clock","WmClockPanel","WmTimeFormat");map("task","WmTaskEditor");map("reminder","WmReminder","WmAppointmentReminder");
        map("calendar_general","WmCalendarGeneralOptions","WmCalendarOptionsLayout");map("calendar_appointments","WmCalendarAppointmentsOptions");map("touch","WmTouchSoundOptions","WmVolumeFeedback");map("speeddial","WmSpeedDialView");map("subcard","WmContactSubcardView");
        map("documents","WmDocumentProperties");map("folders","WmDocumentFolders");map("browser","WmBrowserView");map("favorite","WmBrowserFavoriteForm");map("favorites","WmBrowserFavoritesView");map("folder","WmBrowserFolderForm");
        map("media","WmMediaView","WmMediaQueue");map("playlist","WmMediaPlaylistView");map("library","WmMediaLibraryView");map("media_properties","WmMediaPropertiesView");map("icons","WmLogo","WmIcons","WmIconCaption","WmFonts");
        map("month","WmMonthView");map("month_calendar","WmMonthCalendar","WmMonthCalendarHeader");map("pointer","WmPointer");
        map("old_week","WmCalendarView","WmCalendarHeader","WmCalendarDuration","WmCalendarSymbols");map("old_agenda","WmAgendaView");map("old_year","WmYearView");map("old_appointment","WmAppointmentEditor","WmAppointmentText");map("old_recurrence","WmRecurrenceEditor","WmRecurrenceChoice","WmRecurrenceRule");map("old_phone","WmPhoneView","WmPhoneKeypad");map("old_contacts","WmContactsView","WmContactDetailsView","WmContactSummaryView","WmCharacterSearch");map("old_notes","WmNotesView");map("old_settings","WmSettingsView","WmListSettingsPane");map("old_pwi","WmPwiView","WmPwiDocument","WmPwiLayout","WmPwiTextLayout");
        list();
    }
    private void map(String demo,String... names){for(String name:names)demos.put(name,demo);}
    private TextView label(String text){TextView view=WmControls.text(getContext(),font,text);view.setPadding(8,6,8,6);return view;}
    private String t(String ko,String en){return "ko".equals(language)?ko:en;}
    private void list(){
        detail=false;removeAllViews();EditText search=new EditText(getContext());WmControls.input(search,font);search.setSingleLine(true);search.setHint(t("API 검색","Search APIs"));search.setText(query);addView(search,new LayoutParams(-1,44));
        LinearLayout rows=new LinearLayout(getContext());rows.setOrientation(VERTICAL);addView(new WmScrollView(getContext(),rows,LayoutParams.WRAP_CONTENT,face),new LayoutParams(-1,0,1));
        Runnable filter=()->{rows.removeAllViews();String key=query.toLowerCase(Locale.US);for(int i=0;i<types.length();i++){JSONObject type=types.optJSONObject(i);String name=type.optString("name");if(!name.toLowerCase(Locale.US).contains(key)&&!type.optString("description").toLowerCase(Locale.US).contains(key))continue;TextView button=label(name);WmControls.entry(button,selection);button.setOnClickListener(v->details(type));rows.addView(button,new LayoutParams(-1,44));}};
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int count,int after){}public void onTextChanged(CharSequence s,int start,int before,int count){query=s.toString();filter.run();}public void afterTextChanged(Editable text){}});filter.run();
    }
    private void details(JSONObject type){
        detail=true;removeAllViews();LinearLayout rows=new LinearLayout(getContext());rows.setOrientation(VERTICAL);
        String name=type.optString("name");rows.addView(label(name));rows.addView(label(type.optString("description")));
        if(demos.containsKey(name)){TextView button=label(t("예제 열기","Open example"));WmControls.button(button,selection);button.setOnClickListener(v->open.show(demos.get(name)));rows.addView(button,new LayoutParams(-1,44));}
        ArrayList<String> declarations=new ArrayList<>();
        try{collect(Class.forName("me.ancal.wmdroid.sdk."+name,false,getContext().getClassLoader()),declarations);}
        catch(ClassNotFoundException|LinkageError error){declarations.add(error.toString());}
        Collections.sort(declarations);for(String declaration:declarations){TextView view=label(declaration);view.setTextIsSelectable(true);rows.addView(view);}
        addView(new WmScrollView(getContext(),rows,LayoutParams.WRAP_CONTENT,face),new LayoutParams(-1,0,1));
    }
    private void collect(Class<?> type,List<String> output){
        output.add(Modifier.toString(type.getModifiers())+" "+type.getName());
        for(Field field:type.getDeclaredFields())if(Modifier.isPublic(field.getModifiers())&&!field.isSynthetic())output.add(field.toGenericString());
        for(Constructor<?> ctor:type.getDeclaredConstructors())if(Modifier.isPublic(ctor.getModifiers())&&!ctor.isSynthetic())output.add(ctor.toGenericString());
        for(Method method:type.getDeclaredMethods())if(Modifier.isPublic(method.getModifiers())&&!method.isSynthetic())output.add(method.toGenericString());
        for(Class<?> child:type.getDeclaredClasses())if(Modifier.isPublic(child.getModifiers()))collect(child,output);
    }
    @Override public void onWmRestyle(Typeface regular,Typeface bold,int selection,int tabColor){font=regular;this.selection=selection;face=tabColor;}
    @Override public void onWmLanguageChanged(String language){this.language=language;}
    @Override public boolean back(){if(!detail)return false;list();return true;}
}
