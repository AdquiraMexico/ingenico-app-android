package mx.digitalcoaster.bbva_ingenico.activities;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
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
import mx.digitalcoaster.bbva_ingenico.dialogs.LoadingDialog;

public class CardActivity extends FlapRequests implements FlapRequests.FlapResponses {

    private static String TAG = CardActivity.class.getSimpleName();

    Intent fromIntent;

    //mPOS App
    private ImageButton pagarButton;
    private ImageButton goBackButton;
    private ImageButton regresarButton;
    private AutoResizeTextView amountTextView;
    private TextView statusEditText;
    private TextView statusEditText2;
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

    Boolean isReaderConnected = false;
    Boolean isBTConnected = false;
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);
    }

    @Override
    public void loginResponse(Boolean aBoolean, String s, String s1, String s2) {

    }

    @Override
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
    public void didFindCard() {

    }

    @Override
    public void askForCVV() {

    }

    @Override
    public void didHavePointsOrMonths(Boolean aBoolean, ArrayList<String> arrayList, Boolean aBoolean1) {

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
    public void isBluetoothDeviceConnected(Boolean aBoolean) {

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
}
