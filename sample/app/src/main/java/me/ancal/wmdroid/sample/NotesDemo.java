package me.ancal.wmdroid.sample;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.nio.charset.Charset;
import me.ancal.wmdroid.sdk.*;

/** Two synthetic plain-text notes. This is not a RichInk/PWI editor or a document provider. */
final class NotesDemo extends LinearLayout implements WmControls.BackHandler,WmControls.LanguageListener {
    final WmNotesView notes;
    private final SharedPreferences storage;
    private final TextView notice;
    private final Runnable changed,close;
    private String language="ko";
    private String editingKey;
    private static final String[] KEYS={"welcome","checklist"};
    private static final String[] DEFAULTS={"Synthetic SDK note.\nEdit this text, then Save or Cancel.","Synthetic checklist\nTry folder and sort menus."};
    NotesDemo(Context context,Typeface regular,Typeface bold,int selection,String language,Runnable changed,Runnable close){
        super(context);this.changed=changed;this.close=close;this.language=WmStrings.language(context,language);setOrientation(VERTICAL);
        storage=context.getSharedPreferences("notes-demo",Context.MODE_PRIVATE);
        notice=WmControls.text(context,regular,"");notice.setPadding(8,4,8,4);addView(notice,new LinearLayout.LayoutParams(-1,-2));
        notes=new WmNotesView(context,regular,bold,selection,entry->open(entry.key));
        notes.editor().setFilters(new InputFilter[]{new InputFilter.LengthFilter(8192)});
        addView(notes,new LinearLayout.LayoutParams(-1,0,1));onWmLanguageChanged(language);
    }
    private String text(String authored){return WmStrings.text(getContext(),language,authored);}
    private String text(String authored,String key){return WmStrings.text(getContext(),language,authored,key);}
    private int index(String key){for(int i=0;i<KEYS.length;i++)if(KEYS[i].equals(key))return i;return -1;}
    private String savedText(int index){return storage.getString(KEYS[index],DEFAULTS[index]);}
    private void refresh(){
        notes.setFolders(new WmNotesView.Folder[]{new WmNotesView.Folder("examples",text("예제"))});
        WmNotesView.Entry[] entries=new WmNotesView.Entry[KEYS.length];
        for(int i=0;i<entries.length;i++)entries[i]=new WmNotesView.Entry(KEYS[i],i==0?text("SDK 메모"):text("확인 목록"),storage.getLong(KEYS[i]+".modified",1768867200000L+i),savedText(i).getBytes(Charset.forName("UTF-8")).length,"txt",i==0?"":"examples");
        notes.setEntries(entries);
    }
    void open(String key){int index=index(key);if(index<0)return;editingKey=key;notes.edit(savedText(index));notes.editor().requestFocus();changed.run();}
    void finish(boolean save){
        if(editingKey==null)return;
        if(save&&!storage.edit().putString(editingKey,notes.text()).putLong(editingKey+".modified",System.currentTimeMillis()).commit()){
            Toast.makeText(getContext(),text("저장 실패. 입력을 유지합니다."),Toast.LENGTH_LONG).show();return;
        }
        editingKey=null;notes.showList();refresh();changed.run();
    }
    void saveState(Bundle out){
        out.putBoolean("notes",true);out.putString("notes_key",editingKey);
        if(editingKey!=null)out.putString("notes_draft",notes.text());
        out.putBundle("notes_interaction",notes.saveInteractionState());
    }
    void restoreState(Bundle state){
        String key=state.getString("notes_key");if(index(key)>=0){open(key);notes.editor().setText(state.getString("notes_draft",notes.text()));}
        notes.restoreInteractionState(state.getBundle("notes_interaction"));changed.run();
    }
    @Override public void onWmLanguageChanged(String language){
        this.language=WmStrings.language(getContext(),language);notice.setText(text("합성 일반 텍스트 예제 · 최대 8192자"));
        notes.onWmLanguageChanged(language);refresh();
    }
    @Override public boolean back(){if(notes.back())return true;if(notes.isEditing())finish(false);else close.run();return true;}
}
