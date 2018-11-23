package mx.digitalcoaster.bbva_ingenico.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.view.KeyEvent;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import mx.digitalcoaster.bbva_ingenico.R;
import mx.digitalcoaster.bbva_ingenico.graphics.Circle;
import mx.digitalcoaster.bbva_ingenico.graphics.Circle2;
import mx.digitalcoaster.bbva_ingenico.graphics.CircleAngleAnimation;


/**
 * Created by zertuche on 9/11/15.
 */
public class LoadingDialog {
    ImageView dot1;
    ImageView dot2;
    ImageView dot3;
    ImageView dot4;
    ImageView dot5;
    TextView loader_text;
    Button dismiss_button;
    Dialog dialog;
    Activity context;
    FrameLayout fl;
    public  Boolean showing = false;

    Circle circle;
    Circle2 circle2;

    ArrayList<ImageView> dots ;

    public LoadingDialog(Activity context, String text1, String blueText, String text2){
        this.context=context;
        dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_loading);
        dialog.setCanceledOnTouchOutside(false);
        loader_text = (TextView) dialog.findViewById(R.id.tvLoadingMesg);
        loader_text.setText(Html.fromHtml(text1 + "<font color='#13BCEE'>" + blueText + "</font>" + text2));
    }

    public void setLoader_text(String text) {
       this.loader_text.setText(text);
    }

    public void show(){
        //Dismiss button en el loader no existe entonces esta invisible
        dismiss_button = (Button) dialog.findViewById(R.id.button);

        fl = (FrameLayout) dialog.findViewById(R.id.fl);
        circle = (Circle) dialog.findViewById(R.id.circle);
        circle.setSize(fl.getLayoutParams().width);

        circle2 = (Circle2) dialog.findViewById(R.id.circle2);
        circle2.setSize(fl.getLayoutParams().width);

        CircleAngleAnimation animation = new CircleAngleAnimation(circle, 360);
        animation.setDuration(5000);
        animation.setRepeatCount(Animation.INFINITE);
        circle.startAnimation(animation);

        dialog.show();

        dot1 = (ImageView) dialog.findViewById(R.id.imageView5);
        dot2 = (ImageView) dialog.findViewById(R.id.imageView7);
        dot3 = (ImageView) dialog.findViewById(R.id.imageView8);
        dot4 = (ImageView) dialog.findViewById(R.id.imageView9);
        dot5 = (ImageView) dialog.findViewById(R.id.imageView10);

        dots = new ArrayList<ImageView>() {{
            add(dot1);
            add(dot2);
            add(dot3);
            add(dot4);
            add(dot5);
        }};
        playDots(0);
    }

    public void dismiss(){
        dot1.clearAnimation();
        dot2.clearAnimation();
        dot3.clearAnimation();
        dot4.clearAnimation();
        dot5.clearAnimation();
        circle.clearAnimation();
        dialog.dismiss();
    }

    public void playDots(final int index){

        ScaleAnimation scaleanimation = new ScaleAnimation(1, (float)1.5, 1, (float)1.5, Animation.RELATIVE_TO_SELF, (float)0.5, Animation.RELATIVE_TO_SELF, (float)0.5);
        scaleanimation.setDuration(500);
        dots.get(index).startAnimation(scaleanimation);

        scaleanimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                int indexplus = index;
                indexplus++;
                indexplus = indexplus == 5 ? 0 : indexplus;
                playDots(indexplus);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void allowBackButton(Boolean allow, final Runnable runnable){
        if (allow){
            dialog.setOnKeyListener(new Dialog.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        context.runOnUiThread(runnable);
                        dismiss();
                    }
                    return true;
                }
            });
        } else {
            dialog.setOnKeyListener(new Dialog.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface arg0, int keyCode,
                                     KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                    }
                    return true;
                }
            });
        }
    }
}