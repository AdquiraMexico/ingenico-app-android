package mx.digitalcoaster.bbva_ingenico.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import mx.digitalcoaster.bbva_ingenico.R;
import mx.digitalcoaster.bbva_ingenico.activities.InicioActivity;
import mx.digitalcoaster.bbva_ingenico.graphics.ResizeAnimation;


/**
 * Created by zertuche on 9/11/15.
 */
public class MenuDialog {

    LinearLayout menu;
    ImageButton dismiss_button;
    Dialog dialog;
    Activity context;

    public MenuDialog(Activity context){
        this.context=context;
        dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_menu);
        dialog.setCanceledOnTouchOutside(false);

        menu =  dialog.findViewById(R.id.drop);

    }

    public void show(){
        //Dismiss button en el loader no existe entonces esta invisible
        dismiss_button =  dialog.findViewById(R.id.imageButton4);
        dismiss_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss(99);
            }
        });

        ImageButton i_hisotrial = dialog.findViewById(R.id.imageButton5);
        i_hisotrial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss(0);
            }
        });
        TextView historial = dialog.findViewById(R.id.textView16);
        historial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss(0);
            }
        });

        ImageButton i_soporte = dialog.findViewById(R.id.imageButton6);
        i_soporte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss(2);
            }
        });
        TextView soporte = dialog.findViewById(R.id.textView17);
        soporte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss(2);
            }
        });

        /*ImageButton i_perfil = dialog.findViewById(R.id.imageButton7);
        i_perfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss(1);
            }
        });
        TextView perfil = dialog.findViewById(R.id.textView18);
        perfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss(1);
            }
        });*/

        ImageButton i_cerrar =  dialog.findViewById(R.id.imageButton8);
        i_cerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss(3);
            }
        });
        TextView cerrar =  dialog.findViewById(R.id.textView19);
        cerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss(3);
            }
        });

        dialog.show();

        ResizeAnimation resizeAnimation = new ResizeAnimation(menu, 850);
        resizeAnimation.setDuration(400);
        menu.startAnimation(resizeAnimation);

    }

    public void dismiss(int option){

        ResizeAnimation resizeAnimation = new ResizeAnimation(menu, 0);
        resizeAnimation.setDuration(400);
        resizeAnimation.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation arg0) {
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            @Override
            public void onAnimationEnd(Animation arg0) {
                dialog.dismiss();
                if (option != 99) ((InicioActivity) context).displayView(option);
            }
        });
        menu.startAnimation(resizeAnimation);

    }


    public void allowBackButton(Boolean allow, Runnable runnable){
        if (allow){
            dialog.setOnKeyListener(new Dialog.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        context.runOnUiThread(runnable);
                        dismiss(99);
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


