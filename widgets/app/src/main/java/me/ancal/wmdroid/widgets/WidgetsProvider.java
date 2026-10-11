package me.ancal.wmdroid.widgets;

import android.database.MatrixCursor;
import android.os.Bundle;
import me.ancal.wmdroid.sdk.TodayContract;
import me.ancal.wmdroid.sdk.TodayProvider;

/** Four independently discoverable modules in one APK. Queries only read cached data. */
public abstract class WidgetsProvider extends TodayProvider {
    private final String kind;
    protected WidgetsProvider(String kind){this.kind=kind;}
    @Override protected final String getAuthority(){return "me.ancal.wmdroid.widgets."+kind;}
    @Override protected final void fillRows(MatrixCursor rows,String language){
        WidgetData.Snapshot snapshot=WidgetData.snapshot(getContext(),kind,language);
        WidgetData.refresh(getContext(),kind,false);
        rows.addRow(new Object[]{kind,snapshot.title,snapshot.detail,0,"me.ancal.wmdroid.widgets.WidgetsActivity"});
    }
    @Override protected final TodayContract.Widget createWidget(String id,Bundle options){
        if(!kind.equals(id))return null;
        String language=me.ancal.wmdroid.sdk.WmStrings.language(getContext(),options.getString("language"));
        WidgetData.Snapshot snapshot=WidgetData.snapshot(getContext(),kind,language);
        WidgetData.refresh(getContext(),kind,false);
        return WidgetRenderer.render(getContext(),kind,options,snapshot);
    }
    public static final class Weather extends WidgetsProvider { public Weather(){super("weather");} }
    public static final class Transit extends WidgetsProvider { public Transit(){super("transit");} }
    public static final class Splatnet extends WidgetsProvider { public Splatnet(){super("splatnet");} }
    public static final class Battery extends WidgetsProvider { public Battery(){super("battery");} }
}
