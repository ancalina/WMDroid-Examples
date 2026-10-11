package me.ancal.wmdroid.sample;
import android.database.MatrixCursor;
import me.ancal.wmdroid.sdk.TodayProvider;
/** Public demo data, no personal records and no permissions. */
public final class SampleProvider extends TodayProvider {
    protected String getAuthority(){return "me.ancal.wmdroid.sample.today";}
    protected void fillRows(MatrixCursor rows,String language){int count=getContext().getSharedPreferences("sample",0).getInt("counter",0);rows.addRow(new Object[]{"counter",me.ancal.wmdroid.sdk.WmStrings.text(getContext(),language,"SDK 샘플"),String.format(me.ancal.wmdroid.sdk.WmStrings.locale(language),me.ancal.wmdroid.sdk.WmStrings.text(getContext(),language,"카운터: %d"),count),0,"me.ancal.wmdroid.sample.SampleActivity"});}
}
