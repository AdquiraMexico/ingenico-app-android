package mx.digitalcoaster.bbva_ingenico.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import mx.digitalcoaster.bbva_ingenico.activities.FlapRequests;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import mx.digitalcoaster.bbva_ingenico.dialogs.CustomDialog;
import mx.digitalcoaster.bbva_ingenico.dialogs.LoadingDialog;
import mx.digitalcoaster.bbva_ingenico.R;

public class LoginActivity extends FlapRequests implements FlapRequests.FlapResponses {

    private int cheat = 0;
    private Boolean renovando = false;
    // UI references.
    private AutoCompleteTextView userNameTV;
    private EditText passwordTV;
    Button accessButton;
    LinearLayout llLogin;
    LoadingDialog loadingDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setEnv("P");
        userNameTV = findViewById(R.id.email);
        passwordTV = findViewById(R.id.password);
        accessButton = findViewById(R.id.accessButton);
        llLogin = findViewById(R.id.llLogin);

        registerCallback(this);
        setServicio("1488;773");
        renuevaSesion();
        modoDeveloper();


        accessButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                attemptLogin();
            }
        });

    }

    @SuppressLint("ClickableViewAccessibility")
    //Para activar el modo developer
    public void modoDeveloper(){
        llLogin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_POINTER_DOWN:
                        // multitouch!! - touch down
                        int count = event.getPointerCount(); // Number of 'fingers' in this time
                        if (count == 2) {
                            cheat++;
                            if (cheat == 3) {
                                modoDev();
                            }
                            Handler handler = new Handler();
                            handler.postDelayed(runnable, 5000);
                        }
                        break;
                }
                return false;
            }
        });
    }

    public void modoDev(){
        //Toast.makeText(getApplicationContext(), "Desarrollo =)", Toast.LENGTH_LONG).show();
        setEnv("D");
        CustomDialog dialog2 = new CustomDialog(this, "Modo Developer", "", runnable);
        userNameTV.setText("mpm");
        passwordTV.setText("1234");
        dialog2.show();
    }


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            cheat = 0;
            // handler.postDelayed(this, 100);
        }
    };



    private void renuevaSesion(){
        SharedPreferences prefs = this.getSharedPreferences("ingenico", Context.MODE_PRIVATE);
        String user_sp = prefs.getString("usuario",null);
        String password_sp = prefs.getString("password",null);

        if (user_sp!=null){
            renovando = true;
            loadingDialog = new LoadingDialog(this, "Renovando sesión", "", "");
            loadingDialog.show();
            System.out.println(user_sp + " " + password_sp);
            loginUser(user_sp, password_sp, "P", this);
        }
    }

    //Does the login
    private void attemptLogin() {
        // Reset errors.
        userNameTV.setError(null);
        passwordTV.setError(null);

        String sUsername = userNameTV.getText().toString().trim();
        String sPassword = passwordTV.getText().toString();

        boolean cancelLogin = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(sPassword)) {
            passwordTV.setError(getString(R.string.error_field_required));
            focusView = passwordTV;
            cancelLogin = true;
        }

        // Check not empty username.
        if (TextUtils.isEmpty(sUsername)) {
            userNameTV.setError(getString(R.string.error_field_required));
            //focusView = userNameTV;
            cancelLogin = true;
        }

        if (cancelLogin) {
            // There was an error; don't attempt login and focus the first form field with an error.
            focusView.requestFocus();

        } else {
//            if(username.equals("v1") && password.equals("v1")) {
//                Intent myIntent = new Intent(LoginActivity.this, InicioActivity.class);
//                startActivity(myIntent);
//                finish();
//            } else {
            loadingDialog = new LoadingDialog(this, "Iniciando sesión", "", "");
            loadingDialog.show();
            loginUser(sUsername, sPassword, env, this);
            //}

        }
    }


    @Override                                                                       //Servicios disponibles,solo 1 en este caso
    public void loginResponse(Boolean success, String message, String token, String servicios){
        if (success){
            Log.d("Login",token+" EXITO");
            this.token = token;
            SharedPreferences prefs = this.getSharedPreferences("ingenico", Context.MODE_PRIVATE);
            prefs.edit().putString("servicios", servicios).apply();

            if (renovando){
                getUserNode(prefs.getString("usuario", null), token, env, this);
            } else{
                getUserNode(userNameTV.getText().toString().trim(), token, env, this);
            }
        } else {
            Log.d("Login",token +" con error: " + message);
            loadingDialog.dismiss();
            CustomDialog dialog = new CustomDialog(this, "Usuario y/o contraseña incorrectos.");
            dialog.show();
        }
    }

    @Override
    public void userNodeResponse(JSONObject jsonObject, String status) {
        Log.d("USER","user node: "+ status);
        String username = "";

        if (status.toLowerCase().trim().equals("N_ACT".toLowerCase().trim())){

            //Ingresa Correctamente
            Intent Intent = new Intent(LoginActivity.this, InicioActivity.class);
            Intent.putExtra("token", token);
            Intent.putExtra("ambiente", env);
            Intent.putExtra("user", username);
            this.startActivity(Intent);

            //Guarda los valores
            SharedPreferences prefs = this.getSharedPreferences("ingenico", Context.MODE_PRIVATE);
            if (!renovando && env.equals("P")) {
                prefs.edit().putString("usuario", userNameTV.getText().toString().trim()).apply();
                prefs.edit().putString("password", passwordTV.getText().toString()).apply();
            }
            prefs.edit().putString("token", token).apply();
            prefs.edit().putString("ambiente", env).apply();
            prefs.edit().putString("name", jsonObject.optString("name")).apply();

            //Answers.getInstance().logLogin(new LoginEvent().putMethod(env).putSuccess(true));

            loadingDialog.dismiss();
            this.finish();
            //txtMessage.setText("USUARIO/PWD CORRECTOS");

        } else {
            loadingDialog.dismiss();
            CustomDialog dialog = new CustomDialog(this, "Problemas en Conexión, Intente nuevamente");
            dialog.show();
        }

    }

    @Override
    public void getTransactionsResponse(Boolean bSuccess, JSONArray jsonArray, String sMessage) {
        if(bSuccess){
            Log.d("TRANSACTIONS",jsonArray.toString());
        } else {
            Log.d("TRANSACTIONS",sMessage);
        }
    }
    @Override
    public void getSearchResponse(Boolean aBoolean, JSONArray jsonArray) {
        Log.d("SEARCH",jsonArray.toString());

    }
    @Override
    public void didCancelOrder(Boolean success, String message) {
        Log.d("didCancelOrder",success.toString());
        Log.d("didCancelOrder",message);
    }

    @Override
    public void didHavePointsOrMonths(Boolean bPoints, ArrayList<String> monthsList, Boolean aBoolean1) {
        Log.d("didHavePointsOrMonths", bPoints.toString());
        Log.d("didHavePointsOrMonths", monthsList.toString());
    }
    @Override
    public void didFinishPayment(Boolean success, String transactionId, String authCode, String criptograma, String message) {
        Log.d("didFinishPayment", success.toString());
        Log.d("didFinishPayment", transactionId);
        Log.d("didFinishPayment", authCode);
        Log.d("didFinishPayment", criptograma);
        Log.d("didFinishPayment", message);
    }

    @Override
    public void scanResult(Boolean success, List<String> devices, String message) {
        Log.d("ZERTUCHE","devicesFound");
        if (!success){
            Log.d("SCANRESULT", success.toString());
            Log.d("SCANRESULT", message);
        } else {
            Log.d("SCANRESULT", devices.toString());
        }

    }

    @Override
    public void getTicket() {
    }
    @Override
    public void didFindCard() {
    }
    @Override
    public void askForCVV() {
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
