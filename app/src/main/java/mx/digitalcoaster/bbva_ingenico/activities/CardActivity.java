package mx.digitalcoaster.bbva_ingenico.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.digitalcoaster.rzertuche.requestsflap.CommonActivity;
import com.digitalcoaster.rzertuche.requestsflap.FlapRequests;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import mx.digitalcoaster.bbva_ingenico.R;
import mx.digitalcoaster.bbva_ingenico.dialogs.AutoResizeTextView;
import mx.digitalcoaster.bbva_ingenico.dialogs.CustomDialog;
import mx.digitalcoaster.bbva_ingenico.dialogs.CustomDialogPoints;
import mx.digitalcoaster.bbva_ingenico.dialogs.LoadingDialog;

public class CardActivity extends FlapRequests implements FlapRequests.FlapResponses, LocationListener {

    private static String TAG = CardActivity.class.getSimpleName();



    ImageButton goBackButton, regresarButton, diviceButton;
    Button pagarButton;
    Activity context = this;
    private AutoResizeTextView amountTextView;
    private TextView statusEditText;
    private ImageView ivStatus;
    private LinearLayout llStatus;
    private Dialog dialog;
    private Toolbar mToolbar;

    private Boolean finishPayment = false;
    private Boolean pin_entered = false;
    Boolean isEMVAnswers = false;


    //Dispositivos Bluetooth
    protected static ArrayAdapter<String> arrayAdapter;
    protected static List<BluetoothDevice> foundDevicesList;


    //GPS
    private LocationManager locationManager;
    private String provider;
    private String cancel_id = "";


    String sMoneda = "";

    LoadingDialog loading;
    CustomDialog error;
    String sToken;
    String sMonto;
    String sConcepto;
    String sEmail;
    String sOrden;
    String sAmbiente;
    Boolean bEligio_meses;
    Intent fromIntent;

    Boolean isReaderConnected = false;
    Boolean isBTConnected = false;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        fromIntent = getIntent();

        SharedPreferences prefs = this.getSharedPreferences("ingenico", Context.MODE_PRIVATE);
        //Argumentos recibidos del PayFragment
        sToken = prefs.getString("token", null);
        sMonto = fromIntent.getStringExtra("monto");
        sConcepto = fromIntent.getStringExtra("concepto");
        sEmail = fromIntent.getStringExtra("email");
        sOrden = fromIntent.getStringExtra("noTrans");
        sConcepto = sConcepto.length() < 1 ? "-" : sConcepto;
        sAmbiente = prefs.getString("ambiente", null);
        setEnv(sAmbiente);

        //Servicio unico, en este caso
        String servicios = prefs.getString("servicios", null);
        setServicio(servicios); //Guardar solo un numero de servivcio.
        moneda = prefs.getString("moneda", null);
        setMoneda(moneda);

        mToolbar = findViewById(R.id.toolbar2);
        diviceButton = mToolbar.findViewById(R.id.deviceButton);

        amountTextView = findViewById(R.id.importeEntero);
        statusEditText = findViewById(R.id.statusEditText);
        pagarButton = findViewById(R.id.ibAceptar);

        MyOnClickListener myOnClickListener = new MyOnClickListener();
        pagarButton.setOnClickListener(myOnClickListener);

        bEligio_meses = false;
        pinEntered = false;
        loading = new LoadingDialog(context,"","","");

        setMontoTotal ();
        verifyConexion ();
        registerCallback(this); //not sure

        //location
                locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setCostAllowed(false);
        criteria.setAltitudeRequired(false);
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        provider = locationManager.getBestProvider(criteria, true);
        Location location = null;
        if ( context.getPackageManager().checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, context.getPackageName()) == PackageManager.PERMISSION_GRANTED){
            location = locationManager.getLastKnownLocation(provider);
        } else {
            Log.d("GPS", "NO LOCATION");
        }
        if (location != null) {
            Log.d("GPS","Provider " + provider + " has been selected.");
            onLocationChanged(location);
        } else {
            Log.d("GPS", "Ubicación no disponible");
        }

    }


    private void setMontoTotal(){

        if(moneda.contains("0")){
            sMoneda = "MXN";
        } else {
            sMoneda = "USD";
        }
        //Pones ela cifra en la pantalla
        amountTextView.setText("$" + sMonto + " "+ sMoneda);
    }

    private void verifyConexion(){

        //to retrieve the list of paired companions
        //Set<PclUtilities.BluetoothCompanion> getPairedCompanions = mPclUtil.GetPairedCompanions();


        /*//Verifica bluetooth
        if (isBTConnected || isReaderConnected){
            ivStatus.setImageDrawable(ContextCompat.getDrawable(getContext(), R.mipmap.img_procesando));
        }else {
            ivStatus.setImageDrawable(ContextCompat.getDrawable(getContext(), R.mipmap.img_procesando)); }*/


        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }

        //Pop up para activar bluetooth en caso de estar apagado
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
        Log.d("HEEEY", "GRANTED");
        //startWalker();
        if (isBTConnected){
            diviceButton.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.deviceon));
            statusEditText.setText("DISPOSITIVO CONECTADO. \n CONTINUAR");
        }else {
            //FALTA cambiar imagen y agregar un boton para activar bluetooth
            diviceButton.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.deviceoff));
            statusEditText.setText("VINCULA EL LECTOR DE TARJETAS \n AL DISPOSITIVO MÓVIL");
        }

    }

    @Override
    public void isDeviceConnected(Boolean bConnected) {
        isReaderConnected = bConnected;
        if (bConnected){
            diviceButton.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.deviceon));
            statusEditText.setText("DISPOSITIVO CONECTADO. \n CONTINUAR");
        }else {
            //FALTA cambiar imagen y agregar un boton para activar bluetooth
            diviceButton.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.deviceoff));
            statusEditText.setText("VINCULA EL LECTOR DE TARJETAS \n AL DISPOSITIVO MÓVIL");
        }
    }


    @Override
    public void isBluetoothDeviceConnected(Boolean bConnected){
        Log.d("BluetoothConnected", bConnected.toString());
        if(loading.showing)
            loading.dismiss();

        loading.showing=false;

        if (bConnected){
            isBTConnected = true;
            diviceButton.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.deviceon));
            statusEditText.setText("DISPOSITIVO CONECTADO. \n CONTINUAR");
            CustomDialog cl = new CustomDialog(context,"Ingenico conectado", "");
            cl.show();
        }else {
            //No esta conectado
            if (!finishPayment){ //Y no se ha hecho el pago
                //FALTA cambiar imagen y agregar un boton para activar bluetooth
                diviceButton.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.deviceoff));
                statusEditText.setText("VINCULA EL LECTOR DE TARJETAS \n AL DISPOSITIVO MÓVIL");
                error  = new CustomDialog(context,"Lector bluetooth desconectado");
                error.show();
            }
        }
    }

    @Override
    public void didFindCard() {
        loading.setLoader_text("Leyendo Tarjeta...");
        loading.allowBackButton(false, null);
    }


    @Override
    public void didHavePointsOrMonths(Boolean bPoints, ArrayList<String> monthsList, Boolean isEMV) {
        if(loading.showing){
            loading.dismiss();
        }
        loading.showing=false;
        Log.d("didHavePointsOrMonths", bPoints.toString());
        Log.d("didHavePointsOrMonths", monthsList.toString());
        showPointAndMeses(bPoints, monthsList, isEMV);

    }

    public void showPointAndMeses(Boolean bPoints, ArrayList<String> monthsList, Boolean isEMV){
        CustomDialogPoints mCustomDialogPoints = new CustomDialogPoints(context, bPoints, monthsList);
        mCustomDialogPoints.setRunnable(context, ()-> {
            Boolean usarPuntitos = mCustomDialogPoints.mSwitch.isChecked();
            String  mesesSelected = mCustomDialogPoints.mSpinner.getSelectedItem().toString();
            setPoints(usarPuntitos);
            setMsi(mesesSelected);
            if (!mesesSelected.equals("0")){
                if (monthsList.size()>1){
                    //setServicio("773"); ordenes de nacho
                    //Log.d("test Pago","EMV CON MESES 773 ");
                }
            }
            if (isEMV){
                //Log.d("test Pago","EMV CON MESES 1488 o 773 respectivamente");
                emvPayment(getApplicationContext());
            }else {
                //Log.d("test Pago","MSR CON MESES 1488 o 773 respectivamente");
                msrPayment(getApplicationContext());
            }
            loading = new LoadingDialog(context,"Por favor espere...","","");
            loading.allowBackButton(false, null);
            loading.show();
            loading.showing=true;
        });
        mCustomDialogPoints.allowBackButton(true, ()->{
            if (bbDeviceController != null){
                statusEditText.setText("Lectura cancelada");
            }

        });
        mCustomDialogPoints.show();
    }


    @Override
    public void askForCVV() {
        Log.d("askForCVV", "Dame el cvv");
        finishPayment = true;

        if(loading.showing){
            loading.dismiss();}
        loading.showing=false;
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.swipe_amount_dialog);
        dialog.setTitle("Ingresa el CVV");

        dialog.findViewById(R.id.setButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amountTextView.setText("$" + sMonto +" "+ sMoneda);
                dialog.dismiss();
                setCvv(((EditText)(dialog.findViewById(R.id.cvvEditText))).getText().toString());
            }

        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                resetAll();
            }
        });

        dialog.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.dismiss();
                    if (bbDeviceController != null){
                        statusEditText.setText("Lectura cancelada");
                    }

                    resetAll();
                    return true;
                } else {
                    return false;
                }
            }
        });

        dialog.show();
    }

    @Override
    public void loginResponse(Boolean aBoolean, String s, String s1, String s2) {

    }

    public void userNodeResponse(JSONObject jsonObject, String s) {

    }

    @Override
    public void getTransactionsResponse(Boolean aBoolean, JSONArray jsonArray, String s) {

    }

    @Override
    public void getSearchResponse(Boolean aBoolean, JSONArray jsonArray) {

    }

    @Override
    public void didCancelOrder(Boolean aBoolean, String s) {

    }


    @Override
    public void didFinishPayment(Boolean aBoolean, String s, String s1, String s2, String s3) {

    }

    @Override
    public void didSendSignature(Boolean aBoolean) {

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
    public void pinRequest() {

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

    //LacationListener
    @Override
    public void onLocationChanged(Location location) {

    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //Buttons
    class MyOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Log.d("SERVICIO","servicio"+servicio);
            if (isBTConnected || isReaderConnected){
                setPaymentArguments(sMonto, sConcepto, sEmail, sOrden, sToken, sAmbiente, latitud, longitud, servicio);
                setVersion_app("whitelabel_a_1.0.2");
                statusEditText.setText("Inserta o desliza la tarjeta...");
                loading = new LoadingDialog(context,"Inserta o desliza la tarjeta...","","");
                loading.show();
                loading.showing=true;
                if (bbDeviceController != null && isReaderConnected){
                    checkCard();
                    loading.allowBackButton(true, ()->{
                        statusEditText.setText("Lectura cancelada");
                    });
                }
                if (bbDeviceController != null && isBTConnected){
                    checkCard();
                    loading.allowBackButton(true, ()->{
                        statusEditText.setText("Lectura cancelada");
                    });
                }
            } else {
                statusEditText.setText("Conecta el dispositivo");
                error  = new CustomDialog(context,"Conecta el dispositivo");
                error.show();
            }
        }
    }

}
