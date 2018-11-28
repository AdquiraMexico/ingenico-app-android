package mx.digitalcoaster.bbva_ingenico.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import mx.digitalcoaster.bbva_ingenico.R;

/**
 * Created by zertuche on 9/10/15.
 */
public class CustomDialog {
    Dialog dialog;

    public CustomDialog(Activity context, String mensaje){
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_login_error);
        TextView tvMensError = dialog.findViewById(R.id.tvMensError);
        tvMensError.setText(mensaje);
        Button tvAceptar = dialog.findViewById(R.id.tvAceptar);
        tvAceptar.setOnClickListener(v -> {
            dialog.dismiss();
        } );
    }

    //Mensaje de Error
    public CustomDialog(Activity context, String mensaje, Runnable aceptar){
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_login_error);
        TextView tvMensError = dialog.findViewById(R.id.tvMensError);
        tvMensError.setText(mensaje);
        Button tvAceptar = dialog.findViewById(R.id.tvAceptar);
        tvAceptar.setOnClickListener(v -> {
            context.runOnUiThread(aceptar);
            dialog.dismiss();
        } );
    }

    //Mensaje de conexion correcta
    public CustomDialog(Activity context, String mensaje, Runnable aceptar, int i){
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_conexion_exitosa);
        TextView tvMensError = dialog.findViewById(R.id.tvMensError);
        tvMensError.setText(mensaje);
        Button tvAceptar = dialog.findViewById(R.id.tvAceptar);
        tvAceptar.setOnClickListener(v -> {
            context.runOnUiThread(aceptar);
            dialog.dismiss();
        } );
    }

    //Mensaje de Pago realizado
    public CustomDialog(Activity context, String monto, String mensaje1, String mensaje2, String mensaje3, String mensaje4, String aprobacion, String typeMoney, Runnable aceptar){
        dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialog_pago_realizado);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        AutoResizeTextView tvMonto =  dialog.findViewById(R.id.importeEntero);

        tvMonto.setText("$ "+monto+" "+ typeMoney);

        TextView tvAprobacion = dialog.findViewById(R.id.tvAprob);
        tvAprobacion.setText("No. de Aprobación: "+aprobacion);

        TextView tvMensaje = dialog.findViewById(R.id.tvMensaje);

        tvMensaje.setText(Html.fromHtml(mensaje1 + "<br><font color='#0abaee'>" + mensaje2 + "</font><br>" + mensaje3 + "<font color='#0abaee'>" + mensaje4 + "</font>"));
        ImageButton tvAceptar = dialog.findViewById(R.id.tvAceptar2);
        tvAceptar.setOnClickListener(v -> {
            context.runOnUiThread(aceptar);
            dialog.dismiss();
        });
    }


    //Mensaje de transaccion realizada
    public CustomDialog(Activity context, String mensaje, String parteVerde){
        dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialog_sucees);
        TextView tvMensError = dialog.findViewById(R.id.tvMensSucces);
        tvMensError.setText(Html.fromHtml(mensaje + "<font color='#072146'>" + parteVerde + "</font>"));
        Button tvAceptar = dialog.findViewById(R.id.tvAceptar);
        tvAceptar.setOnClickListener(v -> {
            dialog.dismiss();
        });
    }

    public void closeDialog(View v){
        dialog.dismiss();
    }

    //Mensaje de transaccion realizada2
    public CustomDialog(Activity context, String mensaje, String parteVerde, Runnable aceptar){
        dialog = new Dialog(context);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialog_sucees);
        TextView tvMensError = dialog.findViewById(R.id.tvMensSucces);
        tvMensError.setText(Html.fromHtml(mensaje + "<font color='#072146'>" + parteVerde + "</font>"));
        Button tvAceptar = dialog.findViewById(R.id.tvAceptar);
        tvAceptar.setOnClickListener(v -> {
            context.runOnUiThread(aceptar);
            dialog.dismiss();
        } );
    }

    public void show(){
        dialog.show();
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
