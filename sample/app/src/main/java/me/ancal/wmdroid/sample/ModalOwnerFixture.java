package me.ancal.wmdroid.sample;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import me.ancal.wmdroid.sdk.WmControls;

/** Synthetic modal graph for independent AAR regression checks; no sample UI wiring. */
public final class ModalOwnerFixture extends FrameLayout implements WmControls.ModalOwner {
    private boolean blocking=true;
    private View nominated;
    public ModalOwnerFixture(Context context){super(context);}
    public void setBlocking(boolean value){blocking=value;}
    public void nominate(View view){nominated=view;}
    @Override public boolean blocksOwnerCommands(){return blocking;}
    @Override public View inputMethodOwner(){return nominated;}
}
