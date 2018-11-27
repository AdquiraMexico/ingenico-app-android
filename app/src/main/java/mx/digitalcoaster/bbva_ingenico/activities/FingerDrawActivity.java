package mx.digitalcoaster.bbva_ingenico.activities;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import mx.digitalcoaster.bbva_ingenico.activities.FlapRequests;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import mx.digitalcoaster.bbva_ingenico.R;
import mx.digitalcoaster.bbva_ingenico.dialogs.CustomDialog;
import mx.digitalcoaster.bbva_ingenico.dialogs.LoadingDialog;
import mx.digitalcoaster.bbva_ingenico.graphics.FingerDrawingPad;

public class FingerDrawActivity extends FlapRequests implements FlapRequests.FlapResponses {

    Activity context = this;
    Intent fromIntent;

    private Button clearSignBtn;
    private Button printSignBtn;
    private FingerDrawingPad signView;
    private Bitmap sign;

    String action;
    String ambiente;
    String transaccion;
    String monto;
    String encodedImage;
    String auth;
    String typeMoneda;
    LoadingDialog loadingDialog;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_finger_draw);

        registerCallback(this);

        SharedPreferences prefs = context.getSharedPreferences("ingenico", Context.MODE_PRIVATE);
        fromIntent = getIntent();
        action = fromIntent.getStringExtra("action");
        ambiente = prefs.getString("ambiente", null);
        monto = prefs.getString("monto", null);
        transaccion = prefs.getString("transaction", null);
        token = prefs.getString("token", null);
        auth = prefs.getString("auth", null);
        typeMoneda = prefs.getString("moneda", null);

        Log.d("mpos", "ambiente: " + ambiente);
        Log.d("mpos","monto: " + monto);
        Log.d("mpos","token: " + token);
        Log.d("mpos","moneda: " + typeMoneda);

        setEnv(ambiente);

        String servicios = prefs.getString("servicios", null);
        setServicio(servicios);

        clearSignBtn = findViewById(R.id.clear_signature_btn);
        printSignBtn = findViewById(R.id.print_signature_btn);
        signView = findViewById(R.id.sign_view);

        initViews();
    }

    private void initViews(){
        loadingDialog = new LoadingDialog(context, "Por favor espere...","", "");


        clearSignBtn.setOnClickListener(v -> signView.clearCanvas() );

        printSignBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                printSignBtn.setEnabled(false);
                loadingDialog.show();
                if(signView.isDrawingCacheEnabled()){
                    signView.setDrawingCacheEnabled(false);
                }
                signView.setDrawingCacheEnabled(true);
                signView.setBackgroundColor(Color.WHITE);
                sign = signView.getDrawingCache();
                ByteBuffer buffer = ByteBuffer.allocate(sign.getHeight()*sign.getWidth()*4);
                sign.copyPixelsToBuffer(buffer);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                sign.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                sendSignature(context, encodedImage, transaccion);
            }

        });
    }


    @Override
    public void didSendSignature(Boolean success) {
        loadingDialog.dismiss();
        if(typeMoneda.contains("0")){
            typeMoneda = "MXN";
        } else {
            typeMoneda = "USD";
        }
        Log.e("MONEDA FIRMA",moneda);
        if (success){

            //Answers.getInstance().logCustom(new CustomEvent("Signature Success"));


            CustomDialog cl = new CustomDialog(context,
                    monto,
                    "El recibo se enviará al ",
                    "correo electrónico del tarjetahabiente",
                    "¡Gracias por utilizar ",
                    "MPOS!",
                    auth,
                    typeMoneda,
                    ()-> {
                        Intent myIntent = new Intent(context, InicioActivity.class);
                        myIntent.putExtra("action", action);
                        myIntent.putExtra("ambiente", ambiente);
                        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(myIntent);
                        //Log.d("DataRequest","SUCCESS: " + response);
                    }
            );
            cl.show();
        } else {

            //Answers.getInstance().logCustom(new CustomEvent("Signature Error"));

            CustomDialog error  = new CustomDialog(context,
                    "Error al enviar firma, el pago fue exitoso.",
                    ()-> {
                        Intent myIntent = new Intent(context, InicioActivity.class);
                        myIntent.putExtra("action", action);
                        myIntent.putExtra("ambiente", ambiente);
                        myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(myIntent);
                        //Log.d("DataRequest","SUCCESS: " + response);
                    });
            error.show();
        }
    }

    @Override
    public void isDeviceConnected(Boolean connected) {
    }

    @Override
    public void scanResult(Boolean success, List<String> devices, String message) {
        if (!success){
            Log.d("SCANRESULT", success.toString());
            Log.d("SCANRESULT", message);
        } else {
            Log.d("SCANRESULT", devices.toString());
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
    public void getTransactionsResponse(Boolean success, JSONArray transactions, String message) {
        if(success){
            Log.d("TRANSACTIONS",transactions.toString());
        } else {
            Log.d("TRANSACTIONS",message);
        }
    }
    @Override
    public void getSearchResponse(Boolean success, JSONArray transactions){
        Log.d("SEARCH",transactions.toString());
    }
    @Override
    public void didCancelOrder(Boolean success, String message) {
        Log.d("didCancelOrder",success.toString());
        Log.d("didCancelOrder",message);
    }
    @Override
    public void didFindCard(){
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
    public void didGetTicketWithTotal(String s, String s1, String s2, String s3, String s4, String s5, String s6, String s7, String s8, String s9, String s10, String s11, String s12, String s13, String s14, String s15, String s16, String s17, String s18, String s19, String s20, String s21, String s22, String s23, String s24, String s25, String s26) {

        Log.d("didGetTicket", s);
        Log.d("didGetTicket", s1);
        Log.d("didGetTicket", s2);
        Log.d("didGetTicket", s3);
        Log.d("didGetTicket", s4);
        Log.d("didGetTicket", s5);
        Log.d("didGetTicket", s6);
        Log.d("didGetTicket", s7);
        Log.d("didGetTicket", s8);
        Log.d("didGetTicket", s9);
        Log.d("didGetTicket", s10);
        Log.d("didGetTicket", s11);
        Log.d("didGetTicket", s12);
        Log.d("didGetTicket", s13);
        Log.d("didGetTicket", s14);
        Log.d("didGetTicket", s15);
        Log.d("didGetTicket", s16);
        Log.d("didGetTicket", s17);
        Log.d("didGetTicket", s18);
        Log.d("didGetTicket", s19);
        Log.d("didGetTicket", s20);
        Log.d("didGetTicket", s21);
        Log.d("didGetTicket", s22);
        Log.d("didGetTicket", s23);
        Log.d("didGetTicket", s24);
        Log.d("didGetTicket", s25);
        Log.d("didGetTicket", s26);
    }


    @Override
    public void isBluetoothDeviceConnected(Boolean connected){
        Log.d("BluetoothConnected", connected.toString());
    }
    @Override
    public void pinRequest(){
        Log.d("pinRequest", "pinRequest");
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
    public void startedOnlineTransaction(Boolean isEMV){
    }


    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onPause() {
        super.onPause();
        //bbDeviceController = null;
        //bbDeviceController = null;
        currentActivity = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //bbDeviceController = null;
        //bbDeviceController = null;
    }
}
