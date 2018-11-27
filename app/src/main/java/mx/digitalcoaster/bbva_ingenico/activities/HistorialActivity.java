package mx.digitalcoaster.bbva_ingenico.activities;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import mx.digitalcoaster.bbva_ingenico.activities.FlapRequests;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import mx.digitalcoaster.bbva_ingenico.R;
import mx.digitalcoaster.bbva_ingenico.adapters.HistorialArrayAdapter;
import mx.digitalcoaster.bbva_ingenico.dialogs.CustomDialog;
import mx.digitalcoaster.bbva_ingenico.dialogs.LoadingDialog;
import mx.digitalcoaster.bbva_ingenico.models.Historial;

public class HistorialActivity extends FlapRequests implements FlapRequests.FlapResponses {

    Activity context = this;
    private SwipeMenuListView lvHistorial;
    private List historiales;
    ArrayAdapter adaptador;
    private MenuItem mSearchAction;
    private EditText mSearchEt;
    private boolean mSearchOpened;
    private String typeOfSearch; //O = Orden A = Aprobacion
    private String mSearchQuery;
    private List<Historial> mHistorialsFiltered;
    private Button ibAprob, ibOrden;
    private LinearLayout llFiltros;
    private TextView tvUltTrans;
    LoadingDialog ld;
    Toolbar mToolbar;
    ImageButton lupaIB, backIB;
    ImageView logoIV;
    CharSequence csDevice;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);


        Intent fromIntent = getIntent ();
        env=fromIntent.getStringExtra("ambiente");
        SharedPreferences prefs = context.getSharedPreferences("ingenico", Context.MODE_PRIVATE);
        token= prefs.getString("token", null);


        ibAprob =  findViewById(R.id.ibAprob);
        ibOrden =  findViewById(R.id.ibOrden);
        llFiltros = findViewById(R.id.llFiltros);
        tvUltTrans= findViewById(R.id.tvUltiTrans);
        lvHistorial = findViewById(R.id.lvHistorial);

        mToolbar = findViewById (R.id.toolbar3);
        mSearchEt = mToolbar.findViewById (R.id.etSearch);
        lupaIB = mToolbar.findViewById (R.id.lupaButton);
        backIB = mToolbar.findViewById (R.id.backBttn);
        logoIV = mToolbar.findViewById (R.id.logomposbbva);



        ibAprob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSearchByAprobacion();
            }
        });
        ibOrden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSearchByOrden();
            }
        });

        lupaIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSearchOpened) {
                    closeSearchBar();
                } else {
                    openSearchBar(mSearchQuery);
                }
            }
        });

        backIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.onBackPressed();
            }
        });


        ld = new LoadingDialog(context, "Cargando historial", "", "");
        ld.show();

        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 1);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = df.format(c.getTime());

        Calendar c2 =Calendar.getInstance();
        c2.add(Calendar.DAY_OF_MONTH, -7);
        String formattedDate2 = df.format(c2.getTime());

        getTransactions(context, token, formattedDate2, formattedDate);

        historiales = new ArrayList<Historial>();
        adaptador = new HistorialArrayAdapter (context, historiales);
        lvHistorial.setAdapter(adaptador);

        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                switch (menu.getViewType()) {
                    case 1:
                        SwipeMenuItem item1 = new SwipeMenuItem(context);
                        item1.setBackground(R.color.white);
                        item1.setTitle("CANCELAR TRANSACCIÓN");
                        item1.setTitleSize(18);
                        item1.setTitleColor(R.color.blue_celeste_light);
                        item1.setWidth(600);
                        menu.addMenuItem(item1);
                        break;
                }

            }
        };
        lvHistorial.setMenuCreator(creator);

        lvHistorial.setOnMenuItemClickListener((int position, SwipeMenu menu, int index) -> {
            Historial item = (Historial) adaptador.getItem(position);
            String value = item.getConcepto();
            //Cancelar transaccion
            if (!item.getMostrarCancelar()){
                CustomDialog dialog = new CustomDialog (context, "Esta transacción ya fue cancelada.");
                dialog.show();
            } else {
                ld = new LoadingDialog(context, "Cancelando transacción...", "", "");
                ld.show();
                cancelByTransactionId(context,token, item.getTransactionId());
            }
            return false;

        });

    }

    private void openSearchBar(String queryText) {
        llFiltros.setVisibility(View.VISIBLE);
        tvUltTrans.setVisibility(View.INVISIBLE);
        mSearchEt.setVisibility (View.VISIBLE);
        logoIV.setVisibility (View.INVISIBLE);

        // Search edit text field setup.
        //mSearchEt = (EditText) ab.getCustomView().findViewById(R.id.etSearch);
        mSearchEt.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            mSearchQuery = mSearchEt.getText().toString();
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchDefualt();
                return true;
            }
            return false;
        });
        mSearchEt.setText(queryText);
        mSearchEt.requestFocus();
        mSearchOpened = true;

    }

    public void searchDefualt(){
        if(mSearchEt.getText().toString().length()>0){
            typeOfSearch="A";
            ibAprob.setBackground (ContextCompat.getDrawable(context, R.mipmap.btn_off));
            ibOrden.setBackground (ContextCompat.getDrawable(context, R.mipmap.btn_on));
            //mSearchEt = (EditText) ab.getCustomView().findViewById(R.id.etSearch);
            ld = new LoadingDialog(context,  "Buscando ", "No. de aprobación: ", mSearchEt.getText().toString());
            ld.show();
            searchByAuthCodeOrOrder(context, token, mSearchEt.getText().toString(), "");
//            sv.searchTransaction(token, mSearchEt.getText().toString(),
//                    () -> {
//                        ld.dismiss();
//                        CustomDialog dialog = new CustomDialog(HistorialActivity.this, "Error al buscar número de aprobación.");
//                        dialog.show();
//                    },
//                    (JsonObject jo) -> {
//                        llenarListView(jo);
//                        ld.dismiss();
//                    }
//            );
        } else {
            CustomDialog cd = new CustomDialog(context, "Campo vacío, ingresa No. de Aprobación o No. de Orden");
            cd.show();
        }
    }


    private void closeSearchBar() {
        llFiltros.setVisibility(View.INVISIBLE);
        tvUltTrans.setVisibility(View.VISIBLE);
        mSearchEt.setVisibility (View.INVISIBLE);
        logoIV.setVisibility (View.VISIBLE);
        mSearchOpened = false;
    }


    public void setSearchByAprobacion(){
        Log.d("HistorialActivity",mSearchEt.getText().toString().length()+"");
        if(mSearchEt.getText().toString().length()>0){
            typeOfSearch="A";
            ibAprob.setBackground (ContextCompat.getDrawable(context, R.mipmap.btn_on));
            ibOrden.setBackground(ContextCompat.getDrawable(context, R.mipmap.btn_off));
            ld = new LoadingDialog(context, "Buscando ", "No. de aprobación: ", mSearchEt.getText().toString());
            ld.show();
            searchByAuthCodeOrOrder(context, token, mSearchEt.getText().toString(), "");
        } else {
            CustomDialog cd = new CustomDialog(context, "Campo vacío, ingresa No. de Aprobación o No. de Orden");
            cd.show();
        }
        //mSearchEt.setHint("Buscar Aprobación");
    }

    public void setSearchByOrden() {
        Log.d("HistorialActivity",mSearchEt.getText().toString().length()+"");
        if(mSearchEt.getText().toString().length()>0){
            typeOfSearch = "O";
            ibAprob.setBackground (ContextCompat.getDrawable(context, R.mipmap.btn_off));
            ibOrden.setBackground(ContextCompat.getDrawable(context, R.mipmap.btn_on));
            //mSearchEt.setHint("Buscar Orden");
            ld = new LoadingDialog(context, "Buscando ", "No. de orden: ", mSearchEt.getText().toString());
            ld.show();
            searchByAuthCodeOrOrder(context, token, "", mSearchEt.getText().toString());
        } else {
            CustomDialog cd = new CustomDialog(context, "Campo vacío, ingresa No. de Aprobación o No. de Orden");
            cd.show();
        }
    }

    private void llenarListView(JSONArray ar) {
        if (ar.length()>0){
            historiales.clear();
            for (int i=0; i<ar.length();i++){
                JSONObject nobj = null;
                try {
                    nobj = (JSONObject) ar.get(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String fullDate= nobj.optString("fecha").toString().replace("\"", "").replace(",", "");
                String[] dateParts = fullDate.split(" ");
                String date = dateParts[1] + "." + dateParts[0].toUpperCase() + "." + dateParts[2];
                String hora = dateParts[3] + " " + dateParts[4];
                String importe= nobj.optString("importe").toString().replace("\"", "");
                if (importe.contains(".")){
                    String[] importeParts = importe.split("\\.");
                    String decimalPart = importeParts[1];
                    if (decimalPart.length()==1) decimalPart+="0";
                    else decimalPart=decimalPart.substring(0,2);
                    importe =importeParts[0] + "." + decimalPart;
                } else {
                    importe += ".00";
                }

                historiales.add(new Historial(
                        nobj.optString("transaccionId").toString().replace("\"", ""),
                        date,
                        hora,
                        importe,
                        obtenerValorObj(nobj,"concepto"),
                        obtenerValorObj(nobj,"orden"),
                        obtenerValorObj(nobj, "aprobacion"),
                        obtenerValorObj(nobj, "banco"),
                        obtenerValorObj(nobj, "ultimosDigitos"),
                        obtenerValorObj(nobj, "moneda"),
                        obtenerValorObj(nobj, "tipo"),
                        obtenerValorObj(nobj, "subStatus")
                ));
                adaptador.notifyDataSetChanged();
            }

            adaptador.notifyDataSetChanged();
        } else {
            CustomDialog cd = new CustomDialog(context, "No existen transacciones con ese criterio.");
            cd.show();
        }

    }


    @Override
    public void getTransactionsResponse(Boolean success, JSONArray transactions, String message) {
        ld.dismiss();
        if(success){
            Log.d("TRANSACTIONS",transactions.toString());
            adaptador = new HistorialArrayAdapter(context, historiales);
            lvHistorial.setAdapter(adaptador);
            llenarListView(transactions);
        } else {
            Log.d("TRANSACTIONS",message);
            CustomDialog dialog = new CustomDialog(context, message);
            dialog.show();
        }
    }
    @Override
    public void getSearchResponse(Boolean success, JSONArray transactions){
        ld.dismiss();
        if(success){
            Log.d("SEARCHTRANSACTIONS",transactions.toString());
            adaptador = new HistorialArrayAdapter(context, historiales);
            lvHistorial.setAdapter(adaptador);
            llenarListView(transactions);
        } else {
            Log.d("SEARCHTRANSACTIONS","Error");
            CustomDialog dialog = new CustomDialog(context, "Error al buscar.");
            dialog.show();
        }
    }
    @Override
    public void didCancelOrder(Boolean success, String message) {
        Log.d("didCancelOrder",success.toString());
        Log.d("didCancelOrder",message);
        ld.dismiss();
        if (success){
            CustomDialog dialog = new CustomDialog(context, "Transacción cancelada ", "Exitosamente.", ()->{
                ld = new LoadingDialog(context, "Cargando historial", "", "");
                ld.show();

                Calendar c = Calendar.getInstance();
                c.add(Calendar.DATE, 1);

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                String formattedDate = df.format(c.getTime());

                Calendar c2 =Calendar.getInstance();
                c2.add(Calendar.DAY_OF_MONTH, -7);
                String formattedDate2 = df.format(c2.getTime());

                getTransactions(context, token, formattedDate2, formattedDate);
            });
            dialog.show();
            adaptador.notifyDataSetChanged();
        } else {
            CustomDialog dialog = new CustomDialog(context, "Error en la transacción al cancelar.");
            dialog.show();
        }
    }

    @Override
    public void loginResponse(Boolean success, String message, String token, String servicios){
        if (success){
            Log.d("Login",token+" EXITO");
        } else {
            Log.d("Login",token+" con error: "+message);
        }
    }
    @Override
    public void userNodeResponse(JSONObject user, String status){
        Log.d("USER","user node: "+ status);
    }

    @Override
    public void askForCVV() {
        Log.d("askForCVV", "Dame el cvv");
    }
    @Override
    public void didHavePointsOrMonths(Boolean points, ArrayList<String> months, Boolean isEMV) {
        Log.d("didHavePointsOrMonths", points.toString());
        Log.d("didHavePointsOrMonths", months.toString());
    }
    @Override
    public void didFinishPayment(Boolean success, String transactionId, String authCode, String criptograma, String message){
        Log.d("didFinishPayment", success.toString());
        Log.d("didFinishPayment", transactionId);
        Log.d("didFinishPayment", authCode);
        Log.d("didFinishPayment", criptograma);
        Log.d("didFinishPayment", message);
    }
    @Override
    public void didSendSignature(Boolean success) {
        Log.d("didSendSignature", success.toString());
    }

    @Override
    public void isBluetoothDeviceConnected(Boolean connected){
        Log.d("BluetoothConnected", connected.toString());
    }
    @Override
    public void pinRequest(){
        Log.d("pinRequest", "pinRequest");
    }


    public String obtenerValorObj(JSONObject nobj, String valor){
        return (nobj.optString(valor)!=null ? nobj.optString(valor): "").toString().replace("\"", "");
    }


    @Override
    public void didFindCard() {

    }

    @Override
    public void didGetTicketWithTotal(String s, String s1, String s2, String s3, String s4, String s5, String s6, String s7, String s8, String s9, String s10, String s11, String s12, String s13, String s14, String s15, String s16, String s17, String s18, String s19, String s20, String s21, String s22, String s23, String s24, String s25, String s26) {

    }

    @Override
    public void isDeviceConnected(Boolean aBoolean) {

    }

    @Override
    public void scanResult(Boolean aBoolean, List<String> list, String s) {

    }

    @Override
    public void confirmedAmount() {

    }

    @Override
    public void selectApplicationList(String[] strings) {

    }

    @Override
    public void selectApplication(int i) {

    }

    @Override
    public void startedOnlineTransaction(Boolean aBoolean) {

    }
}
