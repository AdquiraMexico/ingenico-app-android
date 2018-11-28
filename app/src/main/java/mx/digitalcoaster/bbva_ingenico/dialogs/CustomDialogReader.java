package mx.digitalcoaster.bbva_ingenico.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import mx.digitalcoaster.bbva_ingenico.R;

/**
 * Created by rsonckjr on 9/10/15.
 */
public class CustomDialogReader {
    Dialog dialog;
    Activity context;

    public void closeDialog(){
        dialog.dismiss();
    }

    public CustomDialogReader(Activity context, Runnable swipe, Runnable insert, Runnable cancelar, Boolean showOnlyOne){
        this.context = context;
        dialog = new Dialog (context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialog_reader);
        //ImageButton deslizar = (ImageButton) dialog.findViewById(R.id.imageButton3);
        /*deslizar.setOnClickListener(v -> {
            context.runOnUiThread(swipe);
            dialog.dismiss();
        } );*/

       /* ImageButton insertar = (ImageButton) dialog.findViewById(R.id.imageButton4);
        insertar.setOnClickListener(v -> {
            context.runOnUiThread(insert);
            dialog.dismiss();
        } );*/

        TextView tvcancelar = (TextView) dialog.findViewById(R.id.tvAceptar);
        tvcancelar.setOnClickListener(v -> {
            context.runOnUiThread(cancelar);
            dialog.dismiss();
        } );

        /*if (showOnlyOne){
            insertar.setVisibility(View.GONE);
        }*/
    }

    public void allowBackButton(Boolean allow, Runnable runnable){
        if (allow){
            dialog.setOnKeyListener(new Dialog.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        context.runOnUiThread(runnable);
                        dialog.dismiss();
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

    public void show(){
        dialog.show();
    }
}
