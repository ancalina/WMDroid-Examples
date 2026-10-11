package me.ancal.wmdroid.sample;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.LinearLayout;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import me.ancal.wmdroid.sdk.*;

/** Original template reader, in the existing applet shell. No file writes. */
final class PwiDemo extends LinearLayout implements WmControls.BackHandler {
    final WmPwiView reader;
    final WmScrollView scroll;
    final String template;
    private final Runnable close;
    PwiDemo(Context context,String template,int face,Runnable close) throws IOException {
        super(context);this.template=template;this.close=close;setOrientation(VERTICAL);
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(InputStream in=WmResources.openAsset(context,"pwi/"+template+".pwi")){
            byte[] buffer=new byte[4096];int count;while((count=in.read(buffer))!=-1)bytes.write(buffer,0,count);
        }
        WmPwiDocument document=WmPwiDocument.read(bytes.toByteArray(),java.nio.charset.Charset.forName("UTF-8"));
        // Saved mode/scale are document defaults; DPI and language flags remain explicit sample assumptions.
        WmPwiDocument.SavedView saved=document.getSavedView();
        reader=new WmPwiView(context,document,new WmPwiView.Profile(saved.mode,192,192,saved.documentScale,true,false),(font,format)->{
            int weight=format.getFontWeight();
            if(format.isItalic()||(weight!=400&&weight!=700))throw new IOException("Sample font style unavailable");
            if("Tahoma".equals(font.faceName))return WmFonts.load(context,weight==700?"fonts/tahoma/bold.ttf":"fonts/tahoma/regular.ttf",false,weight==700);
            // Original gulim.ac3 is compressed. This explicit sample substitution is not GDI/Gulim fidelity.
            if("굴림".equals(font.faceName)||"Gulim".equals(font.faceName))return WmFonts.load(context,"fonts/samsung/ssgd.ttf",true,weight==700);
            throw new IOException("Sample font unavailable: "+font.faceName);
        });
        scroll=new WmScrollView(context,reader,-2,face);addView(scroll,new LinearLayout.LayoutParams(-1,-1));
    }
    @Override public boolean back(){close.run();return true;}
}
