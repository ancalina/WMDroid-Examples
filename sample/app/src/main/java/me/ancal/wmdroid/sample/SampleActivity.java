package me.ancal.wmdroid.sample;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.*;
import me.ancal.wmdroid.sdk.*;
public final class SampleActivity extends WmApplet {
    private TextView counter,increment,info,popupExample,calendarExample,agendaExample,yearExample,appointmentExample,recurrenceExample,phoneExample,contactsExample,notesExample,settingsExample;private WmPropertySheet sheet;private String language="ko";private int selected;
    private PhoneDemo phoneDemo;
    private AppointmentDemo appointmentDemo;
    private ContactsDemo contactsDemo;
    private PwiDemo pwiDemo;
    private NotesDemo notesDemo;
    private SettingsDemo settingsDemo;
    private LinearLayout[] page;
    private WmPopup samplePopup;
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);language=WmStrings.language(this,getIntent().getStringExtra(WmApplet.LANGUAGE));caption(text("WMDroid SDK 샘플"));
        page=new LinearLayout[2];
        for(int i=0;i<page.length;i++){page[i]=new LinearLayout(this);page[i].setOrientation(LinearLayout.VERTICAL);}
        counter=label("");counter.setPadding(WmMetrics.INSET,0,WmMetrics.INSET,0);page[0].addView(counter,new LinearLayout.LayoutParams(-1,WmMetrics.todayHeight(1)));
        increment=entry(text("카운터 증가"),this::increment);page[0].addView(increment,new LinearLayout.LayoutParams(-1,44));
        popupExample=entry(text("팝업 예제"),()->{
            TextView body=label(text("호스트와 같은 팝업 컨트롤입니다."));body.setPadding(12,12,12,20);
            examplePopup(text("SDK 팝업"),body,444,240);
        });page[0].addView(popupExample,new LinearLayout.LayoutParams(-1,44));
        calendarExample=entry(text("주 보기 예제"),this::calendarExample);page[0].addView(calendarExample,new LinearLayout.LayoutParams(-1,44));
        agendaExample=entry(text("일정 보기 예제"),this::agendaExample);page[0].addView(agendaExample,new LinearLayout.LayoutParams(-1,44));
        yearExample=entry(text("연도 보기 예제"),this::yearExample);page[0].addView(yearExample,new LinearLayout.LayoutParams(-1,44));
        appointmentExample=entry(text("약속 편집 예제"),this::appointmentExample);page[0].addView(appointmentExample,new LinearLayout.LayoutParams(-1,44));
        recurrenceExample=entry(text("되풀이 편집 예제"),this::recurrenceExample);page[0].addView(recurrenceExample,new LinearLayout.LayoutParams(-1,44));
        phoneExample=entry(text("전화 예제"),this::phoneExample);page[0].addView(phoneExample,new LinearLayout.LayoutParams(-1,44));
        contactsExample=entry(text("연락처 예제"),this::contactsExample);page[0].addView(contactsExample,new LinearLayout.LayoutParams(-1,44));
        notesExample=entry(text("메모 예제"),this::notesExample);page[0].addView(notesExample,new LinearLayout.LayoutParams(-1,44));
        settingsExample=entry(text("설정 그리드 예제"),this::settingsExample);page[0].addView(settingsExample,new LinearLayout.LayoutParams(-1,44));
        WmScrollView examples=new WmScrollView(this,page[0],LinearLayout.LayoutParams.WRAP_CONTENT,shade(0xffc9e7e9));
        page[0]=new LinearLayout(this);page[0].setOrientation(LinearLayout.VERTICAL);page[0].addView(examples,new LinearLayout.LayoutParams(-1,0,1));
        info=label(infoText());
        info.setGravity(Gravity.TOP);info.setPadding(WmMetrics.INSET,WmMetrics.INSET,WmMetrics.INSET,WmMetrics.INSET);page[1].addView(info,new LinearLayout.LayoutParams(-1,-2));
        sheet=new WmPropertySheet(this,regular,shade(0xffc9e7e9));sheet.addPage(text("카운터"),page[0]);sheet.addPage(text("정보","sample.info"),page[1]);
        sheet.setOnPageChangedListener(index->selected=index);content.addView(sheet,new LinearLayout.LayoutParams(-1,0,1));
        commands();closeButton(false,null);
        select(saved==null?0:saved.getInt("tab",0));update();
        if(saved!=null&&saved.getBoolean("contacts",false)){contactsExample();contactsDemo.list.setQuery(saved.getString("contacts_query",""));if(saved.getBoolean("contact_summary",false)){contactsDemo.openSummary();contactsDemo.summary.setTab(saved.getInt("contact_tab",0));}contactsDemo.restoreDetails(saved);}
        if(saved!=null&&saved.getString("pwi")!=null){openPwi(saved.getString("pwi"));if(pwiDemo!=null)pwiDemo.scroll.scrollToPosition(saved.getInt("pwi_scroll",0));}
        if(saved!=null&&saved.getBoolean("phone",false)){phoneExample();phoneDemo.phone.setNumber(saved.getString("phone_number",""));phoneDemo.phone.setKeypadVisible(saved.getBoolean("phone_keypad",true));phoneCommands();}
        if(saved!=null&&saved.getBoolean("notes",false)){notesExample();notesDemo.restoreState(saved);}
        if(saved!=null&&saved.getBoolean("settings",false)){settingsExample();if(settingsDemo!=null)settingsDemo.restoreState(saved);}
        if(saved!=null&&saved.getBundle("appointment_draft")!=null)appointmentExample(saved.getBundle("appointment_draft"),saved.getBundle("appointment_categories"));
    }
    @Override protected void onResume(){super.onResume();update();}
    @Override protected void onShellStateChanged(WmShell.State state){
        if(counter==null)return;boolean languageChanged=!language.equals(state.language);language=state.language;
        caption(appointmentDemo!=null?appointmentCaption():settingsDemo!=null?text("설정 그리드 예제"):notesDemo!=null?text("메모 예제"):pwiDemo!=null?text("PWI 읽기 예제"):phoneDemo!=null?text("전화 예제"):contactsDemo!=null?text("연락처 예제"):text("WMDroid SDK 샘플"));increment.setText(text("카운터 증가"));info.setText(infoText());popupExample.setText(text("팝업 예제"));calendarExample.setText(text("주 보기 예제"));agendaExample.setText(text("일정 보기 예제"));yearExample.setText(text("연도 보기 예제"));appointmentExample.setText(text("약속 편집 예제"));phoneExample.setText(text("전화 예제"));
        notesExample.setText(text("메모 예제"));settingsExample.setText(text("설정 그리드 예제"));
        contactsExample.setText(text("연락처 예제"));if(contactsDemo!=null)contactsDemo.localize();
        recurrenceExample.setText(text("되풀이 편집 예제"));sheet.setPageTitle(0,text("카운터"));sheet.setPageTitle(1,text("정보","sample.info"));
        if(phoneDemo!=null)phoneDemo.phone.setStatus(text("발신하지 않는 예제"));
        if(languageChanged){if(samplePopup!=null&&samplePopup.getWindowToken()!=null)dismissPopup();samplePopup=null;if(appointmentDemo!=null)appointmentCommands();else if(settingsDemo!=null)settingsCommands();else if(notesDemo!=null)notesCommands();else if(phoneDemo!=null)phoneCommands();else if(contactsDemo!=null)contactsCommands();else if(pwiDemo!=null)pwiCommands();else commands();}update();
    }
    @Override protected void onSaveInstanceState(Bundle out){out.putInt("tab",selected);if(appointmentDemo!=null){out.putBundle("appointment_draft",appointmentDemo.editor.saveInteractionState());out.putBundle("appointment_categories",appointmentDemo.saveCategories());}if(settingsDemo!=null)settingsDemo.saveState(out);if(notesDemo!=null)notesDemo.saveState(out);if(pwiDemo!=null){out.putString("pwi",pwiDemo.template);out.putInt("pwi_scroll",pwiDemo.scroll.getRequestedScrollPosition());}out.putBoolean("phone",phoneDemo!=null);if(phoneDemo!=null){out.putString("phone_number",phoneDemo.phone.number());out.putBoolean("phone_keypad",phoneDemo.phone.isKeypadVisible());}out.putBoolean("contacts",contactsDemo!=null);if(contactsDemo!=null){out.putString("contacts_query",contactsDemo.list.query());out.putBoolean("contact_summary",contactsDemo.summary!=null);if(contactsDemo.summary!=null)out.putInt("contact_tab",contactsDemo.summary.selectedTab());contactsDemo.saveDetails(out);}super.onSaveInstanceState(out);}
    private String text(String authored){return WmStrings.text(this,language,authored);}
    private String text(String authored,String key){return WmStrings.text(this,language,authored,key);}
    private WmPopup examplePopup(String caption,android.view.View body,int width,int anchor){samplePopup=showPopup(caption,body,width,anchor);return samplePopup;}
    private void commands(){
        softKeys("+1",this::increment,text("메뉴"),null);
        softKeyMenu(true,text("메뉴"),()->showCommandMenu(true,new WmMenuItem[]{
            WmMenuItem.submenu(text("보기"),WmMenuItem.checked(10,text("카운터"),selected==0),WmMenuItem.checked(11,text("정보","sample.info"),selected==1)),
            WmMenuItem.command(12,text("전화 예제")),WmMenuItem.command(13,text("연락처 예제")),WmMenuItem.command(14,text("PWI 읽기 예제")),WmMenuItem.command(15,text("메모 예제")),WmMenuItem.command(16,text("설정 그리드 예제")),
            WmMenuItem.separator(),WmMenuItem.command(20,text("닫기"))
        },id->{if(id==10||id==11)select(id-10);else if(id==12)phoneExample();else if(id==13)contactsExample();else if(id==14)pwiExample();else if(id==15)notesExample();else if(id==16)settingsExample();else if(id==20)finish();}));
    }
    private void settingsExample(){
        if(settingsDemo!=null)return;SettingsDemo next=new SettingsDemo(this,regular,shade(0xff149ca7),shade(0xffc9e7e9),language,this::closeSettings);
        if(appointmentDemo!=null)closeAppointment();
        if(notesDemo!=null)closeNotes();if(pwiDemo!=null)closePwi();if(phoneDemo!=null)closePhone();if(contactsDemo!=null)closeContacts();
        dismissPopup();samplePopup=null;content.removeView(sheet);settingsDemo=next;content.addView(settingsDemo,new LinearLayout.LayoutParams(-1,0,1));
        caption(text("설정 그리드 예제"));closeButton(false,this::closeSettings);settingsCommands();
    }
    private void closeSettings(){
        if(settingsDemo==null)return;content.removeView(settingsDemo);settingsDemo=null;content.addView(sheet,new LinearLayout.LayoutParams(-1,0,1));
        WmControls.restyleTree(sheet,regular,bold,shade(0xff149ca7),shade(0xffc9e7e9));caption(text("WMDroid SDK 샘플"));closeButton(false,null);commands();
    }
    private void settingsCommands(){
        softKeys(text("뒤로"),this::closeSettings,text("메뉴"),null);
        softKeyMenu(true,text("메뉴"),()->showCommandMenu(true,new WmMenuItem[]{WmMenuItem.command(61,text("샘플로 돌아가기"))},id->{if(id==61)closeSettings();}));
    }
    private void notesExample(){
        if(appointmentDemo!=null)closeAppointment();
        if(notesDemo!=null)return;if(settingsDemo!=null)closeSettings();if(pwiDemo!=null)closePwi();if(phoneDemo!=null)closePhone();if(contactsDemo!=null)closeContacts();
        dismissPopup();samplePopup=null;content.removeView(sheet);
        notesDemo=new NotesDemo(this,regular,bold,shade(0xff149ca7),language,this::notesCommands,this::closeNotes);
        content.addView(notesDemo,new LinearLayout.LayoutParams(-1,0,1));caption(text("메모 예제"));notesCommands();
    }
    private void closeNotes(){
        if(notesDemo==null)return;content.removeView(notesDemo);notesDemo=null;content.addView(sheet,new LinearLayout.LayoutParams(-1,0,1));
        WmControls.restyleTree(sheet,regular,bold,shade(0xff149ca7),shade(0xffc9e7e9));caption(text("WMDroid SDK 샘플"));closeButton(false,null);commands();
    }
    private void notesCommands(){
        if(notesDemo==null)return;boolean editing=notesDemo.notes.isEditing();closeButton(editing,()->{if(notesDemo.notes.isEditing())notesDemo.finish(true);else notesDemo.back();});
        softKeys(text(editing?"저장":"메모 열기"),()->{if(notesDemo.notes.isEditing())notesDemo.finish(true);else{WmNotesView.Entry entry=notesDemo.notes.selected();notesDemo.open(entry!=null?entry.key:"examples".equals(notesDemo.notes.folderId())?"checklist":"welcome");}},text("메뉴"),null);
        softKeyMenu(true,text("메뉴"),()->showCommandMenu(true,new WmMenuItem[]{WmMenuItem.command(60,text(editing?"취소":"샘플로 돌아가기"))},id->{if(notesDemo.notes.isEditing())notesDemo.finish(false);else closeNotes();}));
    }
    private void pwiExample(){openPwi("memo");}
    private void openPwi(String template){
        PwiDemo next;
        try{next=new PwiDemo(this,template,shade(0xffc9e7e9),this::closePwi);}
        catch(java.io.IOException error){Toast.makeText(this,text("PWI 표시 실패: ")+error.getMessage(),Toast.LENGTH_LONG).show();return;}
        if(appointmentDemo!=null)closeAppointment();
        if(settingsDemo!=null)closeSettings();if(notesDemo!=null)closeNotes();if(phoneDemo!=null)closePhone();if(contactsDemo!=null)closeContacts();
        dismissPopup();samplePopup=null;if(pwiDemo!=null)content.removeView(pwiDemo);else content.removeView(sheet);
        pwiDemo=next;content.addView(pwiDemo,new LinearLayout.LayoutParams(-1,0,1));
        caption(text("PWI 읽기 예제"));closeButton(false,this::closePwi);pwiCommands();
    }
    private void closePwi(){
        if(pwiDemo==null)return;content.removeView(pwiDemo);pwiDemo=null;content.addView(sheet,new LinearLayout.LayoutParams(-1,0,1));
        WmControls.restyleTree(sheet,regular,bold,shade(0xff149ca7),shade(0xffc9e7e9));caption(text("WMDroid SDK 샘플"));closeButton(false,null);commands();
    }
    private void pwiCommands(){
        softKeys(text("뒤로"),this::closePwi,text("메뉴"),null);
        softKeyMenu(true,text("메뉴"),()->showCommandMenu(true,new WmMenuItem[]{
            WmMenuItem.command(50,text("메모","sample.memo")),WmMenuItem.command(51,text("회의")),
            WmMenuItem.command(52,text("전화")),WmMenuItem.command(53,text("할 일"))
        },id->{if(id>=50&&id<=53)openPwi(new String[]{"memo","meeting","phone","todo"}[id-50]);}));
    }
    private void phoneExample(){
        if(appointmentDemo!=null)closeAppointment();
        if(phoneDemo!=null)return;if(settingsDemo!=null)closeSettings();if(notesDemo!=null)closeNotes();if(pwiDemo!=null)closePwi();if(contactsDemo!=null)closeContacts();
        dismissPopup();samplePopup=null;content.removeView(sheet);phoneDemo=new PhoneDemo();content.addView(phoneDemo,new LinearLayout.LayoutParams(-1,0,1));
        caption(text("전화 예제"));closeButton(false,this::closePhone);phoneCommands();
    }
    private void closePhone(){
        if(phoneDemo==null)return;content.removeView(phoneDemo);phoneDemo=null;content.addView(sheet,new LinearLayout.LayoutParams(-1,0,1));
        WmControls.restyleTree(sheet,regular,bold,shade(0xff149ca7),shade(0xffc9e7e9));caption(text("WMDroid SDK 샘플"));closeButton(false,null);commands();
    }
    private void phoneCommands(){
        softKeys(text(phoneDemo.phone.isKeypadVisible()?"키패드 숨기기":"키패드 표시",phoneDemo.phone.isKeypadVisible()?"Hide Keypad":"Show Keypad"),()->{phoneDemo.phone.setKeypadVisible(!phoneDemo.phone.isKeypadVisible());phoneCommands();},text("메뉴"),null);
        softKeyMenu(true,text("메뉴"),()->showCommandMenu(true,new WmMenuItem[]{WmMenuItem.command(30,text("번호 지우기")),WmMenuItem.command(31,text("샘플로 돌아가기"))},id->{if(id==30)phoneDemo.phone.setNumber("");else if(id==31)closePhone();}));
    }
    private final class PhoneDemo extends LinearLayout implements WmControls.BackHandler {
        final WmPhoneView phone;
        PhoneDemo(){
            super(SampleActivity.this);setOrientation(VERTICAL);
            phone=new WmPhoneView(SampleActivity.this,regular,bold,shade(0xff149ca7),new WmPhoneView.Listener(){
                public void dial(String number){notice(text("통화 예제: ")+number);}
                public void voicemail(){notice(text("음성 사서함 예제"));}
                public void speedDial(){notice(text("단축 다이얼 예제"));}
                public void history(){notice(text("통화 기록 예제"));}
            });phone.setStatus(text("발신하지 않는 예제"));phone.setVoicemailAvailable(true);phone.onWmLanguageChanged(language);addView(phone,new LinearLayout.LayoutParams(-1,-1));
        }
        private void notice(String message){Toast.makeText(SampleActivity.this,message,Toast.LENGTH_SHORT).show();}
        @Override public boolean back(){closePhone();return true;}
    }
    private void contactsExample(){
        if(appointmentDemo!=null)closeAppointment();
        if(contactsDemo!=null)return;if(settingsDemo!=null)closeSettings();if(notesDemo!=null)closeNotes();if(pwiDemo!=null)closePwi();if(phoneDemo!=null)closePhone();dismissPopup();samplePopup=null;content.removeView(sheet);
        contactsDemo=new ContactsDemo();content.addView(contactsDemo,new LinearLayout.LayoutParams(-1,0,1));caption(text("연락처 예제"));closeButton(false,()->contactsDemo.back());contactsCommands();
    }
    private void closeContacts(){
        if(contactsDemo==null)return;content.removeView(contactsDemo);contactsDemo=null;content.addView(sheet,new LinearLayout.LayoutParams(-1,0,1));
        WmControls.restyleTree(sheet,regular,bold,shade(0xff149ca7),shade(0xffc9e7e9));caption(text("WMDroid SDK 샘플"));closeButton(false,null);commands();
    }
    private void contactsCommands(){
        if(contactsDemo.detailsEditor!=null){closeButton(true,()->contactsDemo.finishDetails(true));softKeys("",null,text("메뉴"),null);softKeyMenu(true,text("메뉴"),()->showCommandMenu(true,new WmMenuItem[]{WmMenuItem.command(41,text("취소"))},id->contactsDemo.finishDetails(false)));return;}
        boolean summary=contactsDemo.summary!=null,notes=summary&&contactsDemo.summary.selectedTab()==1;
        WmContactSummaryView.Phone chosen=summary?contactsDemo.summary.selected():null;WmContactSummaryView.Link link=summary?contactsDemo.summary.selectedLink():null;
        boolean address=summary&&contactsDemo.summary.selectedAddress()!=null;boolean sms=chosen!=null&&chosen.action==WmContactSummaryView.SMS;closeButton(summary,()->contactsDemo.back());
        String ko=notes||address?"편집":!summary?"새로 만들기":link!=null?link.kind==WmContactSummaryView.EMAIL?"전자 메일":"찾아보기":sms?"문자 보내기":"통화";
        String en=notes||address?"Edit":!summary?"New":link!=null?link.kind==WmContactSummaryView.EMAIL?"E-mail":"Browse":sms?"Send text message":"Call";
        softKeys(text(ko,en),()->{if(address)contactsDemo.addressNotice(contactsDemo.summary.selectedAddress().key);else if(notes||!summary)Toast.makeText(this,text("합성 데이터 예제입니다."),Toast.LENGTH_SHORT).show();else if(link!=null)contactsDemo.linkNotice(link);else if(chosen!=null){if(sms)contactsDemo.messageNotice(chosen.number);else contactsDemo.notice(chosen.number);}},text("메뉴"),null);
        softKeyMenu(true,text("메뉴"),()->showCommandMenu(true,new WmMenuItem[]{WmMenuItem.command(40,text("샘플로 돌아가기"))},id->closeContacts()));
    }
    private final class ContactsDemo extends LinearLayout implements WmControls.BackHandler {
        final WmContactsView list;WmContactSummaryView summary;int dialCount,messageCount,linkCount,addressCount,detailOpenCount,detailSavedCount;WmContactDetailsView detailsEditor;WmContactDetailsView.Field[] draftFields;String draftNotes="Synthetic SDK note";
        final String[] componentKeys={"f8106","f8109","f8107","f8108"};
        String[][] components={{"","Synthetic","","SDK sample","",""},{"123 Example Street\nSecond Floor","Example City","CA","94000","Canada"},{"9 Other Lane","","","",""},{"","","","",""}},editingComponents;String subcardKey;
        String[][] copyComponents(String[][] source){String[][] copy=new String[source.length][];for(int i=0;i<source.length;i++)copy[i]=source[i].clone();return copy;}
        int componentIndex(String key){for(int i=0;i<componentKeys.length;i++)if(componentKeys[i].equals(key))return i;return -1;}
        String[] componentLabels(boolean name){return name?new String[]{text("직함")+":",text("이름","contacts.name.first")+":",text("중간 이름")+":",text("성")+":",text("애칭")+":",text("호칭(한글)")+":"}:new String[]{text("읍/면/동")+":",text("구/군/시")+":",text("시/도")+":",text("우편번호")+":",text("국가/지역","sample.contacts.country_caption")+":"};}
        // Sample-only assembly; native parsing and regional address formatting remain unproved.
        String componentText(int index,String[] values){if(index==0){StringBuilder out=new StringBuilder();for(int i:new int[]{0,1,2,3,5})if(!values[i].isEmpty()){if(out.length()>0)out.append(' ');out.append(values[i]);}return out.length()==0?values[4]:out.toString();}StringBuilder city=new StringBuilder(values[1]);if(!values[2].isEmpty()){if(city.length()>0)city.append(", ");city.append(values[2]);}if(!values[3].isEmpty()){if(city.length()>0)city.append(' ');city.append(values[3]);}StringBuilder out=new StringBuilder();for(String line:new String[]{values[0],city.toString(),values[4]})if(!line.isEmpty()){if(out.length()>0)out.append('\n');out.append(line);}return out.toString();}
        void openComponent(WmContactDetailsView.Field field){
            int index=componentIndex(field.key);if(index<0){detailOpenCount++;Toast.makeText(SampleActivity.this,text("하위 창 요청 예제: ")+field.label,Toast.LENGTH_SHORT).show();return;}detailOpenCount++;
            String[] values=editingComponents[index].clone();if(!field.value.equals(componentText(index,values))){values=new String[index==0?6:5];java.util.Arrays.fill(values,"");values[index==0?1:0]=field.value;}
            WmContactSubcardView card=new WmContactSubcardView(SampleActivity.this,regular,index==0?WmContactSubcardView.NAME:WmContactSubcardView.ADDRESS,componentLabels(index==0),values,shade(0xff149ca7));
            detailsEditor.showSubcard(card,()->{editingComponents[index]=card.values();detailsEditor.setFieldValue(field.key,componentText(index,editingComponents[index]));subcardKey=null;},()->subcardKey=null);subcardKey=field.key;
        }
        void storeComponents(Bundle out,String prefix,String[][] values){for(int i=0;i<values.length;i++)out.putStringArray(prefix+i,values[i].clone());}
        String[][] loadComponents(Bundle saved,String prefix,String[][] fallback){String[][] result=copyComponents(fallback);for(int i=0;i<result.length;i++){String[] value=saved.getStringArray(prefix+i);if(value!=null&&value.length==result[i].length){boolean valid=true;for(String item:value)if(item==null)valid=false;if(valid)result[i]=value.clone();}}return result;}

        ContactsDemo(){
            super(SampleActivity.this);setOrientation(VERTICAL);
            list=new WmContactsView(SampleActivity.this,regular,bold,shade(0xff149ca7),new WmContactsView.Listener(){public void selected(WmContactsView.Entry entry){}public void open(WmContactsView.Entry entry){openSummary();}});
            list.setEntries(new WmContactsView.Entry[]{new WmContactsView.Entry("sample","Synthetic SDK sample","(202) 555-0100")});list.onWmLanguageChanged(language);addView(list,new LinearLayout.LayoutParams(-1,-1));
        }
        void openSummary(){
            if(summary!=null)return;removeView(list);summary=new WmContactSummaryView(SampleActivity.this,regular,bold,shade(0xff149ca7),new WmContactSummaryView.Listener(){public void selected(WmContactSummaryView.Phone phone){contactsCommands();}public void dial(WmContactSummaryView.Phone phone){notice(phone.number);}public void message(WmContactSummaryView.Phone phone){messageNotice(phone.number);}public void selectedLink(WmContactSummaryView.Link link){contactsCommands();}public void openLink(WmContactSummaryView.Link link){linkNotice(link);}public void selectedAddress(WmContactSummaryView.Address address){contactsCommands();}public void editAddress(WmContactSummaryView.Address address){addressNotice(address.key);}});
            summary.setOnTabChangedListener(tab->contactsCommands());addView(summary,new LinearLayout.LayoutParams(-1,-1));localize();contactsCommands();
        }
        void localize(){
            if(detailsEditor!=null){WmContactDetailsView.Field[] current=detailsEditor.fields(),captions=detailFields();for(int i=0;i<current.length;i++){WmContactDetailsView.Field old=current[i];current[i]=new WmContactDetailsView.Field(old.key,captions[i].label,old.value,old.kind,old.inputType);}detailsEditor.setFields(current);WmControls.localizeTree(detailsEditor,language);if(detailsEditor.subcard()!=null)detailsEditor.subcard().setLabels(componentLabels("f8106".equals(subcardKey)));}
            WmControls.localizeTree(list,language);if(summary!=null){WmControls.localizeTree(summary,language);summary.setContact(detailValue("f8106","Synthetic SDK sample"),detailValue("f8072","Example Corp"),detailValue("f8081","Engineer"),new WmContactSummaryView.Phone[]{
                new WmContactSummaryView.Phone("mobile",text("휴대폰으로 전화 걸기"),detailValue("f8063","(202) 555-0102"),WmContactSummaryView.CALL,WmContactSummaryView.ICON_MOBILE),
                new WmContactSummaryView.Phone("work",text("회사로 전화 걸기"),detailValue("f8069","(202) 555-0100"),WmContactSummaryView.CALL,WmContactSummaryView.ICON_WORK),
                new WmContactSummaryView.Phone("home",text("집으로 전화 걸기"),detailValue("f8060","(202) 555-0101"),WmContactSummaryView.CALL,WmContactSummaryView.ICON_HOME),
                new WmContactSummaryView.Phone("sms",text("문자 보내기"),detailValue("f8063","(202) 555-0102"),WmContactSummaryView.SMS,WmContactSummaryView.ICON_SMS)},new WmContactSummaryView.Link[]{
                new WmContactSummaryView.Link("email",text("전자 메일 보내기"),detailValue("f8056","sdk@example.test"),WmContactSummaryView.EMAIL),
                new WmContactSummaryView.Link("email2",text("전자 메일 보내기 2"),detailValue("f8057","other@example.test"),WmContactSummaryView.EMAIL),
                new WmContactSummaryView.Link("web",text("웹 페이지 찾아보기"),detailValue("f8067","https://example.test"),WmContactSummaryView.WEB)},new WmContactSummaryView.Address[]{new WmContactSummaryView.Address("work-address",text("회사 주소"),detailValue("f8109","123 Example Street\nSecond Floor\nExample City, CA 94000\nCanada")),new WmContactSummaryView.Address("home-address",text("집 주소"),detailValue("f8107","9 Other Lane"))},draftNotes);}
        }
        boolean phoneField(String key){return key.matches("f(?:8069|8068|8063|8060|8064|8054|8059|8122|8070|8061|8065|8052)");}
        String detailValue(String key,String fallback){if(draftFields!=null)for(WmContactDetailsView.Field field:draftFields)if(field.key.equals(key))return field.value;return fallback;}
        WmContactDetailsView.Field[] detailFields(){
            WmContactDetailsView.Field[] captions=new WmContactDetailsView.Field[]{
                new WmContactDetailsView.Field("f8106",text("이름")+":","Synthetic SDK sample",WmContactDetailsView.SUBCARD),
                new WmContactDetailsView.Field("f8114",text("그림")+":","",WmContactDetailsView.ACTION),
                new WmContactDetailsView.Field("f8072",text("회사")+":","Example Corp",WmContactDetailsView.TEXT),
                new WmContactDetailsView.Field("f8073",text("부서")+":","",WmContactDetailsView.TEXT),
                new WmContactDetailsView.Field("f8081",text("직함","contacts.details.job_title")+":","Engineer",WmContactDetailsView.TEXT),
                new WmContactDetailsView.Field("f8071",text("표시 방법")+":","SDK sample, Synthetic",WmContactDetailsView.TEXT),
                new WmContactDetailsView.Field("f8069",text("회사 전화")+":","(202) 555-0100",WmContactDetailsView.TEXT),
                new WmContactDetailsView.Field("f8068",text("회사 팩스")+":","",WmContactDetailsView.TEXT),
                new WmContactDetailsView.Field("f8109",text("회사 주소","sample.contacts.work_address_short")+":","123 Example Street\nSecond Floor\nExample City, CA 94000\nCanada",WmContactDetailsView.SUBCARD),
                new WmContactDetailsView.Field("f8116",text("IM")+":","",WmContactDetailsView.TEXT),
                new WmContactDetailsView.Field("f8056",text("전자 메일")+":","sdk@example.test",WmContactDetailsView.TEXT),
                new WmContactDetailsView.Field("f8063",text("휴대폰")+":","(202) 555-0102",WmContactDetailsView.TEXT),
                new WmContactDetailsView.Field("f8113",text("벨 소리")+":","",WmContactDetailsView.ACTION),
                new WmContactDetailsView.Field("f8067",text("웹 페이지")+":","https://example.test",WmContactDetailsView.TEXT),
                new WmContactDetailsView.Field("f8079",text("사무실")+":","",WmContactDetailsView.TEXT),
                new WmContactDetailsView.Field("f8060",text("집 전화")+":","(202) 555-0101",WmContactDetailsView.TEXT),
                new WmContactDetailsView.Field("f8107",text("집 주소","sample.contacts.home_address_short")+":","9 Other Lane",WmContactDetailsView.SUBCARD),
                new WmContactDetailsView.Field("f8062",text("범주")+":","",WmContactDetailsView.ACTION),
                new WmContactDetailsView.Field("f8108",text("기타 주소","sample.contacts.other_address_short")+":","",WmContactDetailsView.SUBCARD),
                new WmContactDetailsView.Field("f8064",text("호출기")+":","",WmContactDetailsView.TEXT),
                new WmContactDetailsView.Field("f8054",text("카폰")+":","",WmContactDetailsView.TEXT),
                new WmContactDetailsView.Field("f8059",text("집 팩스")+":","",WmContactDetailsView.TEXT),
                new WmContactDetailsView.Field("f8122",text("회사 전화","contacts.details.company_tel")+":","",WmContactDetailsView.TEXT),
                new WmContactDetailsView.Field("f8070",text("회사 전화 2")+":","",WmContactDetailsView.TEXT),
                new WmContactDetailsView.Field("f8061",text("집 전화 2")+":","",WmContactDetailsView.TEXT),
                new WmContactDetailsView.Field("f8065",text("무선 전화")+":","",WmContactDetailsView.TEXT),
                new WmContactDetailsView.Field("f8117",text("IM2")+":","",WmContactDetailsView.TEXT),
                new WmContactDetailsView.Field("f8118",text("IM3")+":","",WmContactDetailsView.TEXT),
                new WmContactDetailsView.Field("f8057",text("전자 메일 2")+":","other@example.test",WmContactDetailsView.TEXT),
                new WmContactDetailsView.Field("f8058",text("전자 메일 3")+":","",WmContactDetailsView.TEXT),
                new WmContactDetailsView.Field("f8051",text("비서")+":","",WmContactDetailsView.TEXT),
                new WmContactDetailsView.Field("f8052",text("비서 전화")+":","",WmContactDetailsView.TEXT),
                new WmContactDetailsView.Field("f8121",text("관리자")+":","",WmContactDetailsView.TEXT),
                new WmContactDetailsView.Field("f8120",text("주민등록번호")+":","",WmContactDetailsView.TEXT),
                new WmContactDetailsView.Field("f8123",text("계정")+":","",WmContactDetailsView.TEXT),
                new WmContactDetailsView.Field("f8119",text("고객 ID")+":","",WmContactDetailsView.TEXT),
                new WmContactDetailsView.Field("f8053",text("생일")+":","",WmContactDetailsView.ACTION),
                new WmContactDetailsView.Field("f8050",text("기념일")+":","",WmContactDetailsView.ACTION),
                new WmContactDetailsView.Field("f8066",text("배우자")+":","",WmContactDetailsView.TEXT),
                new WmContactDetailsView.Field("f8055",text("자녀")+":","",WmContactDetailsView.TEXT)};
            for(int i=0;i<captions.length;i++){WmContactDetailsView.Field field=captions[i];int input=phoneField(field.key)?android.text.InputType.TYPE_CLASS_PHONE:field.key.matches("f805[6-8]")?android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:field.key.equals("f8067")?android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_URI:android.text.InputType.TYPE_CLASS_TEXT;captions[i]=new WmContactDetailsView.Field(field.key,field.label,field.value,field.kind,input);}
            if(draftFields==null)return captions;
            for(int i=0;i<captions.length;i++){WmContactDetailsView.Field old=draftFields[i],label=captions[i];captions[i]=new WmContactDetailsView.Field(old.key,label.label,old.value,old.kind,label.inputType);}
            return captions;
        }
        void openDetails(){
            if(detailsEditor!=null)return;editingComponents=copyComponents(components);detailsEditor=new WmContactDetailsView(SampleActivity.this,regular,detailFields(),draftNotes,shade(0xff149ca7),new WmContactDetailsView.Listener(){public void committed(WmContactDetailsView.Field field){if(phoneField(field.key)){String formatted=android.telephony.PhoneNumberUtils.formatNumber(field.value);if(formatted!=null)detailsEditor.setFieldValue(field.key,formatted);}}public void open(WmContactDetailsView.Field field){openComponent(field);}});
            removeView(summary);addView(detailsEditor,new LinearLayout.LayoutParams(-1,-1));WmControls.localizeTree(detailsEditor,language);contactsCommands();
        }
        void saveDetails(Bundle out){
            if(detailsEditor==null&&draftFields==null)return;storeComponents(out,"contact_components_",components);WmContactDetailsView.Field[] snapshot=detailFields();String[] values=new String[snapshot.length];for(int i=0;i<values.length;i++)values[i]=snapshot[i].value;out.putStringArray("contact_detail_values",values.clone());out.putString("contact_detail_notes",draftNotes);out.putBoolean("contact_editor",detailsEditor!=null);
            if(detailsEditor!=null){storeComponents(out,"contact_edit_components_",editingComponents);if(detailsEditor.subcard()!=null){out.putString("contact_subcard",subcardKey);out.putStringArray("contact_subcard_values",detailsEditor.subcard().values());}snapshot=detailsEditor.fields();for(int i=0;i<values.length;i++)values[i]=snapshot[i].value;out.putStringArray("contact_edit_values",values.clone());out.putString("contact_edit_notes",detailsEditor.notes());out.putInt("contact_editor_tab",detailsEditor.selectedTab());}
        }
        void restoreDetails(Bundle saved){
            components=loadComponents(saved,"contact_components_",components);String[] values=saved.getStringArray("contact_detail_values");WmContactDetailsView.Field[] initial=detailFields();if(values==null||values.length!=initial.length)return;for(int i=0;i<initial.length;i++){WmContactDetailsView.Field field=initial[i];initial[i]=new WmContactDetailsView.Field(field.key,field.label,values[i]==null?"":values[i],field.kind,field.inputType);}draftFields=initial;draftNotes=saved.getString("contact_detail_notes","Synthetic SDK note");localize();
            if(saved.getBoolean("contact_editor",false)){if(summary==null)openSummary();openDetails();values=saved.getStringArray("contact_edit_values");initial=detailsEditor.fields();if(values!=null&&values.length==initial.length){for(int i=0;i<initial.length;i++){WmContactDetailsView.Field field=initial[i];initial[i]=new WmContactDetailsView.Field(field.key,field.label,values[i]==null?"":values[i],field.kind,field.inputType);}detailsEditor.setFields(initial);}detailsEditor.setNotes(saved.getString("contact_edit_notes",draftNotes));detailsEditor.setTab(saved.getInt("contact_editor_tab",0));editingComponents=loadComponents(saved,"contact_edit_components_",components);String key=saved.getString("contact_subcard");if(componentIndex(key)>=0){openComponent(detailsEditor.field(key));String[] cardValues=saved.getStringArray("contact_subcard_values");if(cardValues!=null&&cardValues.length==detailsEditor.subcard().values().length){boolean valid=true;for(String value:cardValues)if(value==null)valid=false;if(valid)detailsEditor.subcard().setValues(cardValues);}}}
        }
        void finishDetails(boolean save){
            if(detailsEditor==null)return;detailsEditor.closeSubcard(save);if(save){components=copyComponents(editingComponents);detailsEditor.clearFocus();draftFields=detailsEditor.fields();draftNotes=detailsEditor.notes();detailSavedCount++;}removeView(detailsEditor);detailsEditor=null;addView(summary,new LinearLayout.LayoutParams(-1,-1));localize();contactsCommands();
        }
        void notice(String number){dialCount++;Toast.makeText(SampleActivity.this,text("발신하지 않는 예제: ")+number,Toast.LENGTH_SHORT).show();}
        void messageNotice(String number){messageCount++;Toast.makeText(SampleActivity.this,text("문자 발신 없는 예제: ")+number,Toast.LENGTH_SHORT).show();}
        void addressNotice(String key){addressCount++;openDetails();detailsEditor.editField("work-address".equals(key)?"f8109":"f8107");}
        void linkNotice(WmContactSummaryView.Link link){linkCount++;Toast.makeText(SampleActivity.this,text("외부 앱을 열지 않는 예제: ")+link.value,Toast.LENGTH_SHORT).show();}
        @Override public boolean back(){if(detailsEditor!=null){if(!detailsEditor.back())finishDetails(false);return true;}if(summary!=null){removeView(summary);summary=null;addView(list,new LinearLayout.LayoutParams(-1,-1));contactsCommands();}else closeContacts();return true;}
    }
    private String infoText(){return text("SDK 버전 1\n이 샘플의 공개 카운터만 표시합니다.\n한글·영문, 탭, 하단 키를 사용합니다.");}
    private void calendarExample(){
        WmCalendarView calendar=new WmCalendarView(this,regular,WmStrings.locale(language));
        java.util.Calendar now=java.util.Calendar.getInstance();calendar.setRange(now.get(java.util.Calendar.YEAR),now.get(java.util.Calendar.MONTH)+1,now.get(java.util.Calendar.DAY_OF_MONTH),WmCalendarView.WEEK);
        calendar.setEvents(java.util.Arrays.asList(new WmCalendarView.Event(0,540,630,false,text("예제 약속"),"9:00",null),new WmCalendarView.Event(1,0,1440,true,text("종일 예제"),"",null)));
        calendar.setColors(shade(0xff149ca7),WmPalette.face(shade(0xff149ca7)),android.graphics.Color.WHITE);
        calendar.setOnEventListener(new WmCalendarView.OnEventListener(){
            public void onSelected(WmCalendarView.Event event){}
            public void onOpen(WmCalendarView.Event event){TextView card=label(event.title+"\n"+event.detail);card.setPadding(12,12,12,20);examplePopup(text("예제 약속"),card,444,240);}
        });
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setMinimumHeight(544);WmCalendarHeader header=new WmCalendarHeader(this,regular,WmStrings.locale(language));header.setMode(WmCalendarHeader.WEEK);header.setDate(now.get(java.util.Calendar.YEAR),now.get(java.util.Calendar.MONTH)+1,now.get(java.util.Calendar.DAY_OF_MONTH));header.setColors(shade(0xff149ca7));header.setOnCommandListener(command->{if(command==WmCalendarHeader.TODAY)now.setTimeInMillis(System.currentTimeMillis());else if(command==WmCalendarHeader.PREVIOUS||command==WmCalendarHeader.NEXT)now.add(java.util.Calendar.DAY_OF_MONTH,command==WmCalendarHeader.PREVIOUS?-7:7);else return;header.setDate(now.get(java.util.Calendar.YEAR),now.get(java.util.Calendar.MONTH)+1,now.get(java.util.Calendar.DAY_OF_MONTH));calendar.setRange(now.get(java.util.Calendar.YEAR),now.get(java.util.Calendar.MONTH)+1,now.get(java.util.Calendar.DAY_OF_MONTH),WmCalendarView.WEEK);});body.addView(header,new LinearLayout.LayoutParams(-1,44));body.addView(calendar,new LinearLayout.LayoutParams(-1,500));
        examplePopup(text("주 보기 예제"),body,444,240);calendar.post(()->calendar.scrollToMinute(8*60));
    }
    private void select(int index){sheet.selectPage(index==1?1:0);selected=sheet.selectedPage();}
    private void agendaExample(){showAgenda(java.util.Calendar.getInstance());}
    private void yearExample(){showYear(java.util.Calendar.getInstance(),false);}
    private void appointmentExample(){appointmentExample(null);}
    private void appointmentExample(Bundle state){appointmentExample(state,null);}
    private void appointmentExample(Bundle state,Bundle categoriesState){
        if(appointmentDemo!=null)return;AppointmentDemo next=new AppointmentDemo();
        try{if(state!=null)next.editor.validateInteractionState(state);}catch(IllegalArgumentException invalid){Toast.makeText(this,text("약속 초안을 복원할 수 없습니다."),Toast.LENGTH_LONG).show();return;}
        if(settingsDemo!=null)closeSettings();if(notesDemo!=null)closeNotes();if(pwiDemo!=null)closePwi();if(phoneDemo!=null)closePhone();if(contactsDemo!=null)closeContacts();
        dismissPopup();samplePopup=null;content.removeView(sheet);appointmentDemo=next;content.addView(next,new LinearLayout.LayoutParams(-1,0,1));
        if(state!=null)next.editor.restoreInteractionState(state);
        if(categoriesState!=null)try{next.restoreCategories(categoriesState);}catch(IllegalArgumentException invalid){Toast.makeText(this,text("범주 초안을 복원할 수 없습니다."),Toast.LENGTH_LONG).show();}
        appointmentCommands();
    }
    private void closeAppointment(){
        if(appointmentDemo==null)return;dismissPopup();samplePopup=null;content.removeView(appointmentDemo);appointmentDemo=null;content.addView(sheet,new LinearLayout.LayoutParams(-1,0,1));
        WmControls.restyleTree(sheet,regular,bold,shade(0xff149ca7),shade(0xffc9e7e9));caption(text("WMDroid SDK 샘플"));closeButton(false,null);commands();
    }
    private String appointmentCaption(){return text("약속 편집 예제");}
    private void appointmentCommands(){
        if(appointmentDemo==null)return;
        if(appointmentDemo.categoryPicker!=null){
            boolean adding=appointmentDemo.newName!=null;caption(appointmentCaption());closeButton(true,()->appointmentDemo.finishCategories(true));
            if(adding)softKeys(text("완료","category.new.done"),()->appointmentDemo.finishCategories(true),text("취소"),()->appointmentDemo.finishCategories(false));
            else softKeys(text("새로 만들기"),()->appointmentDemo.openNewCategory(),"",null);
            appointmentDemo.localizeCategories();return;
        }
        caption(text("약속 편집 예제"));closeButton(false,this::closeAppointment);
        softKeys(text("현재 값 확인"),this::inspectAppointment,text("메뉴"),null);
        softKeyMenu(true,text("메뉴"),()->showCommandMenu(true,new WmMenuItem[]{WmMenuItem.command(62,text("샘플로 돌아가기"))},id->{if(id==62)closeAppointment();}));
    }
    private void inspectAppointment(){
        if(appointmentDemo==null)return;WmAppointmentEditor editor=appointmentDemo.editor;
        try{WmAppointmentEditor.Value value=editor.value();editor.setError("");TextView result=label(value.subject+"\n"+text("범주: ")+value.categories+"\n"+text("상태: ")+editor.busyChoice().getText()+" / "+text("보안 수준: ")+editor.sensitivityChoice().getText()+"\n"+text("미리 알림: ")+editor.reminderField().enabledChoice().getText()+(value.reminder.enabled?" "+value.reminder.quantity+" "+editor.reminderField().unitChoice().getText():"")+"\n"+text("유효한 값입니다. 저장하지 않습니다."));result.setPadding(12,8,12,8);examplePopup(text("현재 값 확인"),result,444,240);}
        catch(IllegalArgumentException invalid){editor.setError("올바른 시각을 입력하세요.");}
    }
    private final class AppointmentDemo extends FrameLayout implements WmControls.BackHandler {
        final WmAppointmentEditor editor;
        WmCategoryPicker categoryPicker;
        WmCategoryNameForm newCategory;
        EditText newName;

        String categoryError="";
        AppointmentDemo(){
            super(SampleActivity.this);java.util.Locale locale=WmStrings.locale(language);
            WmAppointmentEditor.Value initial=new WmAppointmentEditor.Value(text("예제 약속"),text("회의실"),text("저장하지 않는 합성 예제입니다."),new WmDateTimeField.Value(2026,1,20,9,30),new WmDateTimeField.Value(2026,1,20,10,30),false);
            editor=new WmAppointmentEditor(SampleActivity.this,regular,locale,initial,shade(0xff149ca7),shade(0xffc9e7e9));WmControls.restyleTree(editor,regular,bold,shade(0xff149ca7),shade(0xffc9e7e9));addView(editor,new LayoutParams(-1,-1));editor.setOnCategoriesOpen(this::openCategories);
        }
        void openCategories(){
            if(categoryPicker!=null)return;
            categoryPicker=new WmCategoryPicker(SampleActivity.this,regular,WmStrings.locale(language),new String[]{"Work","Personal"},editor.categories());
            editor.setVisibility(GONE);addView(categoryPicker,new LayoutParams(-1,-1));WmControls.restyleTree(categoryPicker,regular,bold,shade(0xff149ca7),shade(0xffc9e7e9));appointmentCommands();
        }
        void openNewCategory(){
            if(categoryPicker==null||newName!=null)return;
            newCategory=new WmCategoryNameForm(SampleActivity.this,regular,bold,shade(0xff149ca7));newName=newCategory.nameField();newCategory.onWmLanguageChanged(language);
            categoryError="";
            categoryPicker.setVisibility(GONE);addView(newCategory,new LayoutParams(-1,-1));appointmentCommands();
        }
        void localizeCategories(){if(newName!=null){newCategory.onWmLanguageChanged(language);if(categoryError.equals("invalid"))newCategory.showNameWarning(()->categoryError="");}}

        void finishCategories(boolean accept){
            if(categoryPicker==null||newCategory!=null&&WmControls.blocksOwnerCommands(newCategory))return;
            if(newName!=null){
                if(accept)try{String raw=newName.getText().toString();if(!raw.replace(" ","").isEmpty())categoryPicker.addCategory(raw);}catch(IllegalArgumentException invalid){categoryError="invalid";localizeCategories();return;}
                android.view.inputmethod.InputMethodManager keyboard=(android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);if(keyboard!=null)keyboard.hideSoftInputFromWindow(newName.getWindowToken(),0);
                removeView(newCategory);newCategory=null;newName=null;categoryError="";categoryPicker.setVisibility(VISIBLE);appointmentCommands();return;
            }
            if(accept&&categoryPicker.result()!=null)editor.setCategories(categoryPicker.result());removeView(categoryPicker);categoryPicker=null;editor.setVisibility(VISIBLE);appointmentCommands();
        }
        Bundle saveCategories(){
            if(categoryPicker==null)return null;
            Bundle state=new Bundle();state.putBundle("picker",categoryPicker.saveInteractionState());state.putString("new_name",newName==null?null:newName.getText().toString());state.putIntArray("carets",newName==null?null:new int[]{newName.getSelectionStart(),newName.getSelectionEnd()});state.putString("error",categoryError);return state;
        }
        void restoreCategories(Bundle state){
            if(state==null||state.size()!=4||!(state.get("picker") instanceof Bundle)||!state.containsKey("new_name")||(state.get("new_name")!=null&&!(state.get("new_name") instanceof String))||!state.containsKey("carets")||!(state.get("error") instanceof String))throw new IllegalArgumentException("Category owner state required");
            String input=state.getString("new_name"),error=state.getString("error");if(!error.equals("")&&!error.equals("duplicate")&&!error.equals("invalid"))throw new IllegalArgumentException("Category error required");
            int[] carets=null;if(input!=null){if(input.length()>255||!(state.get("carets") instanceof int[])||(carets=state.getIntArray("carets")).length!=2||carets[0]<0||carets[1]<0||carets[0]>input.length()||carets[1]>input.length())throw new IllegalArgumentException("Category caret range required");}else if(state.get("carets")!=null||!error.equals(""))throw new IllegalArgumentException("Category list state required");
            WmCategoryPicker restored=WmCategoryPicker.fromInteractionState(SampleActivity.this,regular,WmStrings.locale(language),state.getBundle("picker"));
            if(!editor.categories().equals(state.getBundle("picker").getString("original")))throw new IllegalArgumentException("Category owner selection mismatch");
            categoryPicker=restored;editor.setVisibility(GONE);addView(categoryPicker,new LayoutParams(-1,-1));WmControls.restyleTree(categoryPicker,regular,bold,shade(0xff149ca7),shade(0xffc9e7e9));
            if(input!=null){openNewCategory();newName.setText(input);newName.setSelection(carets[0],carets[1]);categoryError=error;localizeCategories();}
        }
        @Override public boolean back(){if(newCategory!=null&&newCategory.back())return true;if(categoryPicker!=null)finishCategories(false);else if(!editor.back())closeAppointment();return true;}
    }
    private void recurrenceExample(){
        java.util.Locale locale=WmStrings.locale(language);
        WmRecurrenceEditor.Value initial=new WmRecurrenceEditor.Value(WmRecurrenceRule.none(),new WmDateTimeField.Value(2026,1,20,9,30),new WmDateTimeField.Value(2026,2,20,0,0),60,false);
        WmRecurrenceEditor editor=new WmRecurrenceEditor(this,regular,locale,initial,shade(0xff149ca7),shade(0xffc9e7e9));
        editor.setMinimumHeight(500);
        editor.setOnCancel(()->{dismissPopup();samplePopup=null;});
        editor.setOnFinish(()->{
            WmRecurrenceEditor.Value value=editor.value();
            TextView result=label(text("유효한 값입니다. 저장하지 않습니다.")+"\n"+value.durationMinutes+" "+text("분"));result.setPadding(12,12,12,20);
            examplePopup(text("되풀이 편집 예제"),result,444,240);
        });
        examplePopup(text("되풀이 편집 예제"),editor,444,240);
    }
    private void showYear(java.util.Calendar selected,boolean yearStart){
        java.util.Locale locale=WmStrings.locale(language);
        WmYearView grid=new WmYearView(this,regular,locale);grid.setColors(shade(0xff149ca7));grid.setDate(selected.get(java.util.Calendar.YEAR),selected.get(java.util.Calendar.MONTH)+1,selected.get(java.util.Calendar.DAY_OF_MONTH));
        WmCalendarHeader header=new WmCalendarHeader(this,regular,locale);header.setMode(WmCalendarHeader.YEAR);header.setColors(shade(0xff149ca7));
        Runnable updateHeader=()->{int[] date=grid.date();header.setDate(date[0],date[1],date[2]);int offset=grid.getMonthOffset(),count=grid.getVisibleMonthCount();header.setYearRange(offset<0&&offset+count>=0?date[0]-1:date[0],offset>0&&offset+count>12?date[0]+1:date[0]);};
        grid.setOnRangeChangedListener((year,month,day)->updateHeader.run());updateHeader.run();if(yearStart)grid.showYearStart();
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setMinimumHeight(544);body.addView(header,new LinearLayout.LayoutParams(-1,WmCalendarHeader.HEIGHT));body.addView(grid,new LinearLayout.LayoutParams(-1,500));
        grid.setOnDayClickListener((year,month,day)->{java.util.Calendar chosen=new java.util.GregorianCalendar(year,month-1,day);showAgenda(chosen);});
        header.setOnCommandListener(command->{
            if(command==WmCalendarHeader.PREVIOUS||command==WmCalendarHeader.NEXT){grid.movePage(command==WmCalendarHeader.PREVIOUS?-1:1);return;}
            if(command==WmCalendarHeader.TODAY){java.util.Calendar now=java.util.Calendar.getInstance();grid.setDate(now.get(java.util.Calendar.YEAR),now.get(java.util.Calendar.MONTH)+1,now.get(java.util.Calendar.DAY_OF_MONTH));return;}
            if(command==WmCalendarHeader.YEAR_PICKER)yearPicker(grid,body,locale);
        });
        examplePopup(text("연도 보기 예제"),body,444,240);
    }
    private void yearPicker(WmYearView grid,LinearLayout scene,java.util.Locale locale){
        int[] date=grid.date(),nearby=WmYearChooser.nearbyYears(date[0]);String[] labels=new String[nearby.length+1];
        for(int i=0;i<nearby.length;i++)labels[i]=String.valueOf(nearby[i]);labels[nearby.length]=WmYearChooser.otherLabel(this,locale);
        WmRadioMenu menu=new WmRadioMenu(this,regular,labels,date[0]-nearby[0],text(" 선택됨"),shade(0xff149ca7),index->{
            if(index<nearby.length){showYear(yearDate(date,nearby[index]),true);return;}
            WmYearChooser editor=new WmYearChooser(this,regular,locale,date[0],shade(0xff149ca7),new WmYearChooser.Listener(){
                public void chosen(int year){showYear(yearDate(date,year),true);}
                public void cancel(){android.view.ViewParent parent=scene.getParent();if(parent instanceof android.view.ViewGroup)((android.view.ViewGroup)parent).removeView(scene);WmControls.restyleTree(scene,regular,bold,shade(0xff149ca7),shade(0xffc9e7e9));examplePopup(text("연도 보기 예제"),scene,444,240);}
            });examplePopup(text("연도 선택"),editor,364,240);
        });examplePopup(text("연도 선택"),menu,364,240);
    }
    private java.util.Calendar yearDate(int[] date,int year){java.util.Calendar chosen=new java.util.GregorianCalendar(year,date[1]-1,1);chosen.set(java.util.Calendar.DAY_OF_MONTH,Math.min(date[2],chosen.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)));return chosen;}
    private void showAgenda(java.util.Calendar day){
        java.util.Locale locale=WmStrings.locale(language);
        WmAgendaView agenda=new WmAgendaView(this,regular,locale);agenda.setDate(day.get(java.util.Calendar.YEAR),day.get(java.util.Calendar.MONTH)+1,day.get(java.util.Calendar.DAY_OF_MONTH));agenda.setColors(shade(0xff149ca7));
        int sampleYear=day.get(java.util.Calendar.YEAR),sampleMonth=day.get(java.util.Calendar.MONTH)+1,sampleDay=day.get(java.util.Calendar.DAY_OF_MONTH);
        WmTimeFormat.Agenda clock=new WmTimeFormat.Agenda(locale,android.text.format.DateFormat.is24HourFormat(this),text("여러 날"));
        String range=clock.format(new WmDateTimeField.Value(sampleYear,sampleMonth,sampleDay,0,0),new WmDateTimeField.Value(sampleYear,sampleMonth,sampleDay,9,0),new WmDateTimeField.Value(sampleYear,sampleMonth,sampleDay,10,30));
        java.util.Calendar finish=(java.util.Calendar)day.clone();finish.set(java.util.Calendar.HOUR_OF_DAY,10);finish.set(java.util.Calendar.MINUTE,30);finish.set(java.util.Calendar.SECOND,0);finish.set(java.util.Calendar.MILLISECOND,0);
        java.util.Calendar begin=(java.util.Calendar)finish.clone();begin.set(java.util.Calendar.HOUR_OF_DAY,9);begin.set(java.util.Calendar.MINUTE,0);
        agenda.setEvents(java.util.Arrays.asList(new WmAgendaView.Event(0,1440,true,text("종일 예제"),"","",0,null),new WmAgendaView.Event(540,630,false,text("예제 약속"),range,text("회의실"),0,null,WmCalendarSymbols.REMINDER|WmCalendarSymbols.RECURRENCE|WmCalendarSymbols.LOCATION,false,begin.getTimeInMillis(),finish.getTimeInMillis(),WmAgendaView.Event.BUSY)));
        agenda.setOnEventListener(new WmAgendaView.OnEventListener(){public void onSelected(WmAgendaView.Event event){}public void onOpen(WmAgendaView.Event event){TextView card=label(event.title+"\n"+event.time+(event.location.isEmpty()?"":" ("+event.location+")"));card.setPadding(12,12,12,20);examplePopup(text("예제 약속"),card,444,240);}});
        WmCalendarHeader header=new WmCalendarHeader(this,regular,locale);header.setMode(WmCalendarHeader.AGENDA);header.setDate(day.get(java.util.Calendar.YEAR),day.get(java.util.Calendar.MONTH)+1,day.get(java.util.Calendar.DAY_OF_MONTH));header.setColors(shade(0xff149ca7));
        header.setOnCommandListener(command->{
            if(command==WmCalendarHeader.DATE_PICKER){WmCalendarPicker picker=new WmCalendarPicker(this,regular,locale,day.get(java.util.Calendar.YEAR),day.get(java.util.Calendar.MONTH)+1,day.get(java.util.Calendar.DAY_OF_MONTH),shade(0xff149ca7),(year,month,date)->{day.set(year,month-1,date);showAgenda(day);});picker.setOnDateChangeListener((year,month,date)->day.set(year,month-1,date));examplePopup(text("날짜 선택"),picker,Math.min(WmMetrics.WIDTH,picker.naturalSize().x+4),240);return;}
            if(command==WmCalendarHeader.TODAY)day.setTimeInMillis(System.currentTimeMillis());
            else if(command==WmCalendarHeader.PREVIOUS||command==WmCalendarHeader.NEXT)day.add(java.util.Calendar.DAY_OF_MONTH,command==WmCalendarHeader.PREVIOUS?-1:1);
            else if(command>=WmCalendarHeader.FIRST_WEEKDAY&&command<WmCalendarHeader.FIRST_WEEKDAY+7){int first=java.util.Calendar.getInstance(locale).getFirstDayOfWeek(),offset=(day.get(java.util.Calendar.DAY_OF_WEEK)-first+7)%7;day.add(java.util.Calendar.DAY_OF_MONTH,command-WmCalendarHeader.FIRST_WEEKDAY-offset);}
            else return;showAgenda(day);
        });
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setMinimumHeight(544);body.addView(header,new LinearLayout.LayoutParams(-1,44));body.addView(agenda,new LinearLayout.LayoutParams(-1,500));examplePopup(text("일정 보기 예제"),body,WmMetrics.WIDTH,240);
    }
    private void increment(){getSharedPreferences("sample",0).edit().putInt("counter",getSharedPreferences("sample",0).getInt("counter",0)+1).apply();update();getContentResolver().notifyChange(TodayContract.rows("me.ancal.wmdroid.sample.today"),null);}
    private void update(){counter.setText(text("카운터: ")+getSharedPreferences("sample",0).getInt("counter",0));}
}
