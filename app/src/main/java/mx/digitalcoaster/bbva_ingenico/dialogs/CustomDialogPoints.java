package mx.digitalcoaster.bbva_ingenico.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

import mx.digitalcoaster.bbva_ingenico.R;


/**
 * Created by zertuche on 9/10/15.
 */
public class CustomDialogPoints {
    Dialog dialog;
    public Switch mSwitch;
    public Spinner mSpinner;
    LinearLayout puntosLayout;
    LinearLayout mesesLayout;
    Activity context;

    public void closeDialog(View v){
        dialog.dismiss();
    }

    public CustomDialogPoints(Activity context, Boolean usePuntos, ArrayList<String> months){
        this.context = context;
        dialog = new Dialog (context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialog_points_meses);

        puntosLayout = dialog.findViewById(R.id.puntosLayout);
        mesesLayout = dialog.findViewById(R.id.mesesLayout);

        if (!usePuntos){
            puntosLayout.setVisibility(View.GONE);
        }

        if (months.size()==1){
            mesesLayout.setVisibility(View.GONE);
        }


        mSwitch = (Switch)dialog.findViewById(R.id.switch1);
        mSwitch.setEnabled(usePuntos);

        mSpinner = (Spinner)dialog.findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String> (context, android.R.layout.simple_spinner_item, months);
        mSpinner.setAdapter(adapter);

        //SIEMPRE ESCONDER
        puntosLayout.setVisibility(View.GONE);

    }

    public void setRunnable(Activity context, Runnable aceptar){
        TextView tvAceptar = (TextView) dialog.findViewById(R.id.tvAceptar);
        tvAceptar.setOnClickListener(v -> {
            context.runOnUiThread(aceptar);
            dialog.dismiss();
        } );
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
