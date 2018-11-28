package mx.digitalcoaster.bbva_ingenico.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import mx.digitalcoaster.bbva_ingenico.R;
import mx.digitalcoaster.bbva_ingenico.dialogs.AutoResizeTextView;

public class FinishActivity extends AppCompatActivity {

    Intent fromIntent = getIntent();
    String monto, typeMoney, aprobacion;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_finish);

        AutoResizeTextView tvMonto = findViewById(R.id.importeEntero);

        tvMonto.setText("$ "+monto+" "+ typeMoney);

        TextView tvAprobacion = dialog.findViewById(R.id.tvAprob);
        tvAprobacion.setText("No. de Aprobación: "+aprobacion);
    }
}
