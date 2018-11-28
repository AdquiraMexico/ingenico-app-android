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
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


import mx.digitalcoaster.bbva_ingenico.activities.CommonActivity;
import mx.digitalcoaster.bbva_ingenico.activities.FlapRequests;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import mx.digitalcoaster.bbva_ingenico.R;
import mx.digitalcoaster.bbva_ingenico.dialogs.AutoResizeTextView;
import mx.digitalcoaster.bbva_ingenico.dialogs.CustomDialog;
import mx.digitalcoaster.bbva_ingenico.dialogs.CustomDialogPoints;
import mx.digitalcoaster.bbva_ingenico.dialogs.CustomDialogReader;
import mx.digitalcoaster.bbva_ingenico.dialogs.LoadingDialog;

public class CardActivity extends FlapRequests implements FlapRequests.FlapResponses, LocationListener {

    private static String TAG = CardActivity.class.getSimpleName();



    ImageButton goBackButton, regresarButton, diviceButton;
    Button pagarButton;
    Activity context = this;
    private AutoResizeTextView amountTextView;
    private TextView statusEditText, monedaTV;
    private ImageView ivStatus;
    private LinearLayout llStatus;
    private Dialog dialog;
    Toolbar mToolbar;

    private Boolean finishPayment = false;
    private Boolean pin_entered = false;
    Boolean isEMVAnswers = false;
    private int cheat = 0;


    //Dispositivos Bluetooth
    protected static ArrayAdapter<String> arrayAdapter;
    protected static List<BluetoothDevice> foundDevicesList;

    private static final String ACTION_USB_PERMISSION = "mx.digitalcoaster.bbva_ingenico.activities.requestsflap.USB_PERMISSION";

    //GPS
    private LocationManager locationManager;
    private String provider;
    private String cancel_id = "";


    String sMoneda = "";

    LoadingDialog loading;
    CustomDialog error;
    CustomDialogReader customReader;
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
        monedaTV = findViewById(R.id.monedaTV);
        monedaTV.setText(moneda);

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


        diviceButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                makeConexion();
            }
        });



    }

    private void makeConexion(){
        //setDevice("Ingenico");
        //ivStatus.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.img_procesando));
        //startIngenico ();
            Log.d("DIGITALCOASTER", "STARTINGENICO");
        startIngenico ();
        Log.d("DIGITALCOASTER", "SCANDEVICES");
        scanDevices();
        Log.d("DIGITALCOASTER", "PROMPTFORCONEXION");
        promptForConnection();


    }


    public void promptForConnection() {

        Object[] pairedObjects = BluetoothAdapter.getDefaultAdapter().getBondedDevices().toArray();
        final BluetoothDevice[] pairedDevices = new BluetoothDevice[pairedObjects.length];

        for (int i = 0; i < pairedObjects.length; ++i) {
            pairedDevices[i] = (BluetoothDevice) pairedObjects[i];
        }


        final ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View view =super.getView(position, convertView, parent);
                TextView textView= view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.rgb(255, 255, 255));
                return view;
            }
        };

        for (int i = 0; i < pairedDevices.length; ++i) {
            mArrayAdapter.add(pairedDevices[i].getName());
        }
        dismissDialog();

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.bluetooth_2_device_list_dialog);
        dialog.setTitle("Dispositivos");

        ListView listView1 = dialog.findViewById(R.id.pairedDeviceList);
        listView1.setAdapter(mArrayAdapter);
        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            //Dispositivo seleccionado
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                loading = new LoadingDialog(context,"Conectando dispositivo...","","");
                loading.show();
                loading.showing=true;

                String nameDevice = pairedDevices[position].getName ();
                CharSequence csDevice = pairedDevices[position].getAddress ();
                String sDevice = pairedDevices[position].getAddress () + pairedDevices[position].getName ();
                Log.d("Divice", nameDevice);
                connectTo (sDevice);

                //setPaymentArguments("2.00","-","-","-","token","P","-","-","-");

                //connectNomad(pairedDevices[position]);
                dismissDialog();
            }

        });

       /* arrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1);
        ListView listView2 = (ListView) dialog.findViewById(R.id.discoveredDeviceList);
        listView2.setAdapter(arrayAdapter);
        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                loading = new LoadingDialog(context,"Conectando dispositivo...","","");
                loading.show();
                loading.showing=true;

                String nameDevice = pairedDevices[position].getName ();
                connectTo (nameDevice);
                //connectNomad(foundDevicesList.get(position));
                dismissDialog();
            }

        });*/

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //stopScanBluetooth();
                dismissDialog();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
        //scanBluetooth();
    }


    private void setMontoTotal(){

        if(moneda.contains("0")){
            sMoneda = "MXN";
            monedaTV.setText(sMoneda);

        } else {
            sMoneda = "USD";
            monedaTV.setText(sMoneda);
        }
        //Pones ela cifra en la pantalla
        amountTextView.setText("$" + sMonto);
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
        Log.d("HEEEY", "GRANTºED");
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
            Log.d("DIGITALCOASTER","ISDICECONNECTEDCHANGE");
            loading.dismiss();
            diviceButton.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.deviceon));
            statusEditText.setText("DISPOSITIVO CONECTADO \n CONTINUAR");
            pagarButton.setVisibility(View.VISIBLE);
            loading.showing = false;
            CustomDialog cl = new CustomDialog(context,"Dispositivo móvil y lector \n de tarjetas vinculados",runnable, 0);
            cl.show();
        }else {
            //FALTA cambiar imagen y agregar un boton para activar bluetooth
            diviceButton.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.deviceoff));
           // loading.dismiss();
            statusEditText.setText("VINCULA EL LECTOR DE TARJETAS \n AL DISPOSITIVO MÓVIL");
            loading.showing = false;

        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            cheat = 0;
            // handler.postDelayed(this, 100);
        }
    };


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
            //CustomDialog cl = new CustomDialog(context,"Ingenico conectado");
            //cl.show();
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
                    statusEditText.setText("Lectura cancelada");
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
    public void startedOnlineTransaction(Boolean isEMV) {
        if(isEMV){
            //Pago por chip, MVS, TLV
            loading.setLoader_text("Realizando pago...");

        } else {
            //Pago por banda, TRACKS, MSR
            loading.setLoader_text("Realizando pago...");
            loading.show ();
            loading.showing = true;
        }
    }




    @Override
    public void didFinishPayment(Boolean bSuccess, String sTransactionId, String sAuthCode, String sCriptograma, String sMessage) {
        CardActivity.this.runOnUiThread(new Runnable() {
            public void run() {

                Log.d("didFinishPayment", bSuccess.toString());
                Log.d("didFinishPayment", sTransactionId);
                Log.d("didFinishPayment", sAuthCode);
                Log.d("didFinishPayment", sCriptograma);
                Log.d("didFinishPayment", sMessage);

                if (loading.showing) {
                    loading.dismiss();
                }
                loading.showing = false;
                pinEntered = false;

                //En caso de pago correcto
                if (bSuccess) {
                    Log.d("DIGITALCOASTER", "ya estamos con el finishpayment");
                    customReader.closeDialog();

                    //finishPayment = true;
                    String amountString = getAmount();
                    Log.d("mpos", "Amount. " + amountString + sMoneda);

                    SharedPreferences prefs = context.getSharedPreferences("flap", Context.MODE_PRIVATE);
                    prefs.edit().putString("monto", amountString).apply();
                    prefs.edit().putString("auth", sAuthCode).apply();
                    prefs.edit().putString("transaction", sTransactionId).apply();
                    prefs.edit().putString("ambiente", getEnv()).apply();
                    prefs.edit().putString("token", token).apply();

                    //finishPayment = false;

                    //CrashAnalitycs
            /*Answers.getInstance().logPurchase(new PurchaseEvent()
                    .putCustomAttribute("Environment", env)
                    .putItemPrice(BigDecimal.valueOf(Double.valueOf(amountString)))
                    .putCurrency(Currency.getInstance(money))
                    .putSuccess(true));*/

                    //Se pide algun tipo de firma electronica
                    if (pin_entered) {
                        //Answers.getInstance().logCustom(new CustomEvent("CHIP & PIN Payment Succesfull"));



                        Intent myIntent = new Intent(context, InicioActivity.class);
                        myIntent.putExtra("ambiente", getEnv());
                        myIntent.putExtra("amount", amountString);
                        myIntent.putExtra("ambiente", getEnv());
                        myIntent.putExtra("ambiente", getEnv());
                        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(myIntent);

                        CustomDialog mCustomDialog = new CustomDialog(context, amountString,
                                "El recibo se enviará al ",
                                "correo electrónico del usuario",
                                "¡Gracias por utilizar ",
                                "MPOS!",
                                sAuthCode,
                                sMoneda,
                                () -> {
                                    Intent myIntent = new Intent(context, InicioActivity.class);
                                    myIntent.putExtra("ambiente", getEnv());
                                    myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(myIntent);
                                    //Log.d("DataRequest","SUCCESS: " + response);
                                }
                        );
                        mCustomDialog.show();

                    } else { //Pide signature

               /* if (isEMVAnswers){
                    Answers.getInstance().logCustom(new CustomEvent("EMV Payment Succesfull"));
                } else {
                    Answers.getInstance().logCustom(new CustomEvent("MSR Payment Succesfull"));
                }*/
                        Intent intent;
                        intent = new Intent(CardActivity.this, FingerDrawActivity.class);
                        intent.putExtra("amount", amountString);
                        intent.putExtra("ambiente", getEnv());
                        intent.putExtra("transaccion", sTransactionId);
                        intent.putExtra("token", getToken());
                        startActivity(intent);


                        /*CustomDialog customDialog = new CustomDialog(context, "La transacción se realizó ",
                                "¡CORRECTAMENTE!", () -> {

                            Intent intent;
                            intent = new Intent(CardActivity.this, FingerDrawActivity.class);
                            intent.putExtra("amount", amountString);
                            intent.putExtra("ambiente", getEnv());
                            intent.putExtra("transaccion", sTransactionId);
                            intent.putExtra("token", getToken());
                            startActivity(intent);

                        });
                        customDialog.show();*/
                    }


                    // En caso de pago incorrecto
                } else {
                    statusEditText.setText("Error en Pago (" + sMessage + " )");
                    error = new CustomDialog(context, "Error en Pago (" + sMessage + " )", () -> {
                        finishPayment = false;
                    });
                    error.show();
                    pagarButton.setEnabled(true);
                    regresarButton.setEnabled(true);
            /*if (isEMVAnswers){
                Answers.getInstance().logCustom(new CustomEvent("EMV Payment Error"));
            } else {
                Answers.getInstance().logCustom(new CustomEvent("MSR Payment Error"));
            }*/
                }
            }
        });
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
    public void didSendSignature(Boolean aBoolean) {

    }

    @Override
    public void didGetTicketWithTotal(String s, String s1, String s2, String s3, String s4, String s5, String s6, String s7, String s8, String s9, String s10, String s11, String s12, String s13, String s14, String s15, String s16, String s17, String s18, String s19, String s20, String s21, String s22, String s23, String s24, String s25, String s26) {

    }



    @Override
    public void scanResult(Boolean bSucces, List<String> divicesList, String sMessage) {
        Log.d("ZERTUCHE","devicesFound");
        if (divicesList != null) {
                for (int i = 0; i < divicesList.size (); i++) {
                    Log.d ("ZERTUCHE", "devicesFound: " + divicesList.get (i));
                }
                connectTo (divicesList.get (0)); //Al dispositivo conectado

        }
        else {
            Log.d("SCANRESULT", bSucces.toString());
            Log.d("SCANRESULT", sMessage);
            if (loading != null){
                if(loading.showing){
                    loading.dismiss();}
                loading.showing=false;
            }
            if (error == null){
                error  = new CustomDialog(context, sMessage);
                error.show();
            }
        }
    }

    @Override
    public void pinRequest() {

    }

    @Override
    public void confirmedAmount() {

        if(loading.showing){
            loading.dismiss();}
        loading.showing=false;
        CustomDialogReader reader = new CustomDialogReader(context,
                ()->{
                    statusEditText.setText("Deslizar tarjeta...");
                    loading = new LoadingDialog(context,"Deslizar tarjeta...","","");
                    loading.show();
                    loading.showing=true;
                    //swipeNomad();
                },
                ()->{
                    statusEditText.setText("Insertar tarjeta...");
                    loading = new LoadingDialog(context,"Insertar tarjeta...","","");
                    loading.show();
                    loading.showing=true;
                    emvPayment (this);
                    //emvNomad();
                },
                ()->{
                    statusEditText.setText("Lectura cancelada");
                }, getFallback());
        reader.allowBackButton(true, ()->{
            statusEditText.setText("Lectura cancelada");
        });
        reader.show();

    }

    @Override
    public void selectApplicationList(String[] strings) {

    }

    @Override
    public void selectApplication(int i) {

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


    //Dismiss Dialog
    public void dismissDialog() {
        if(dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    //Buttons
    class MyOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Log.d("SERVICIO","servicio"+servicio);
            if (isBTConnected || isReaderConnected){
                setPaymentArguments(sMonto, "-","-","-","token","P","-","-","-");
                //            setPaymentArguments("2.00","-","-","-","token","P","-","-","-");
                setVersion_app("whitelabel_a_1.0.2");

                if(loading.showing){
                    loading.dismiss();}
                loading.showing=false;
                customReader = new CustomDialogReader(context,
                        ()->{
                            statusEditText.setText("Deslizar tarjeta...");
                            loading = new LoadingDialog(context,"Deslizar tarjeta...","","");
                            loading.show();
                            loading.showing=true;
                            //swipeNomad();
                        },
                        ()->{
                            statusEditText.setText("Insertar tarjeta...");
                            loading = new LoadingDialog(context,"Insertar tarjeta...","","");
                            loading.show();
                            loading.showing=true;
                            emvPayment (context);
                            //emvNomad();
                        },
                        ()->{
                            statusEditText.setText("Lectura cancelada");
                        }, getFallback());
                customReader.allowBackButton(true, ()->{
                    statusEditText.setText("Lectura cancelada");
                });
                customReader.show();


                /*statusEditText.setText("Inserta o desliza la tarjeta...");
                loading = new LoadingDialog(context,"Inserta o desliza la tarjeta...","","");
                loading.show();
                loading.showing=true;*/

                if (isReaderConnected){
                    checkCard();
                    loading.allowBackButton(true, ()->{
                        statusEditText.setText("Lectura cancelada");
                    });
                }
                if (isBTConnected){
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
