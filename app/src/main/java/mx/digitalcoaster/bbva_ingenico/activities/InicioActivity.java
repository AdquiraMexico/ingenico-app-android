package mx.digitalcoaster.bbva_ingenico.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import mx.digitalcoaster.bbva_ingenico.R;
import mx.digitalcoaster.bbva_ingenico.dialogs.AutoResizeEditText;
import mx.digitalcoaster.bbva_ingenico.dialogs.CustomDialog;
import mx.digitalcoaster.bbva_ingenico.dialogs.MenuDialog;

public class InicioActivity extends AppCompatActivity {

    private static String TAG = InicioActivity.class.getSimpleName();

    private TextView tvConcepto, tvNoTrans;
    private EditText etConcepto, etNoTrans, etMailTarj, propinaET;
    private AutoResizeEditText importeTxt;
    private Button ibAceptar;
    Toolbar mToolbar;
    ImageButton menu_open;
    Intent fromIntent;
    Context context = this;

    SharedPreferences prefs;
    public MenuItem menu_1;
    public MenuItem menu_2;
    public MenuItem menu_3;

    public String sTransaccion;
    public  String sConcepto;
    public  String sPropina;

    private Spinner spPropina;
    private Spinner spMoneda;
    private Spinner spComercio;

    private float fOriginalSize;
    private float fOriginalSize2;

    public boolean bPropina = false;
    public int iPropina_selected = 0;

    public LinearLayout propinaLayout;
    public  LinearLayout comercioLayout;
    public boolean mIsRestoredFromBackstack = false;
    Boolean bpermiso;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        fromIntent = getIntent();
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setTitle("");
        ab.setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(R.layout.action_bar_title);
        ab.setDisplayShowHomeEnabled(true);
        // display the first navigation drawer view on app launch

        prefs = this.getSharedPreferences("ingenico", Context.MODE_PRIVATE);
        sTransaccion = prefs.getString("trans", null);
        sConcepto = prefs.getString("concepto", null);

        if (sTransaccion == null)
            sTransaccion = "0";

        if (sConcepto == null)
            sConcepto = "0";

        if (sPropina == null)
            sPropina = "0";

        propinaLayout = (LinearLayout) findViewById(R.id.layoutPropina);

        tvConcepto = findViewById(R.id.textView26);
        tvNoTrans = findViewById(R.id.textView27);

        etConcepto = findViewById(R.id.etConcepto);
        etNoTrans = findViewById(R.id.etNoTrans);
        etMailTarj = findViewById(R.id.etMailTarj);
        propinaET = findViewById(R.id.propinatext);

        importeTxt = findViewById(R.id.importeEntero);
        ibAceptar =  findViewById(R.id.ibAceptar);
        fOriginalSize= importeTxt.getTextSize();

        mToolbar = this.findViewById(R.id.toolbar);
        menu_open =  mToolbar.findViewById(R.id.imageButton3);

        spPropina = findViewById(R.id.spinner2);
        spMoneda = findViewById(R.id.spinner3);


        importeTxt.setSelection(importeTxt.getText().length());
        importeTxt.setEnabled(true);
        importeTxt.setFocusableInTouchMode(true);
        importeTxt.setMovementMethod(null);
        // can be added after layout inflation; it doesn't have to be fixed
        // value
        importeTxt.setMaxHeight(70);
        textListener();
        bpermiso = permisoLocalizacion();
        muestraMenuSuperior();
        monedaTipo();
        propinaTipo();

        ibAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bpermiso)
                    procedePago();
                else
                    bpermiso = permisoLocalizacion();
            }
        });
        
    }

    //Menu navigation drawer superior
    public void displayView(int position) {
        if (position == 0){
            Intent mainIntent = new Intent().setClass( InicioActivity.this, HistorialActivity.class);
            mainIntent.putExtra("token", fromIntent.getStringExtra("token"));
            mainIntent.putExtra("ambiente", fromIntent.getStringExtra("ambiente"));
            startActivity(mainIntent);

        }else if(position == 1){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://mpos.flap.com.mx/perfil"));
            startActivity(browserIntent);

        }else if(position == 2){
            Intent i2 = new Intent().setClass( InicioActivity.this, SoporteActivity.class);
            startActivity(i2);

        } else if (position == 3) {
            //Cerrar Sesión
            SharedPreferences prefs = this.getSharedPreferences("ingenico", Context.MODE_PRIVATE);
            prefs.edit().clear().commit();
            Intent mainIntent = new Intent().setClass(  InicioActivity.this, LoginActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainIntent);
        }
    }


    //Oculta el teclado
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }


    //Funcion que controla los textos de las cantidades monetarias
    @SuppressLint("ClickableViewAccessibility")
    private void textListener(){
        TextWatcher txtWatcher = new TextWatcher() { // Monto total
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                importeTxt.setSelection(importeTxt.getText().length());

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                float width = importeTxt.getWidth();
                float size = importeTxt.getTextSize();
                if (!s.toString().matches("^\\$(\\d{1,3}(\\,\\d{3})*|(\\d+))(\\.\\d{2})?$")) {
                    String userInput = "" + s.toString().replaceAll("[^\\d]", "");
                    StringBuilder cashAmountBuilder = new StringBuilder(userInput);

                    while (cashAmountBuilder.length() > 3 && cashAmountBuilder.charAt(0) == '0') {
                        cashAmountBuilder.deleteCharAt(0);
                    }
                    while (cashAmountBuilder.length() < 3) {
                        cashAmountBuilder.insert(0, '0');
                    }
                    cashAmountBuilder.insert(cashAmountBuilder.length() - 2, '.');

                    importeTxt.removeTextChangedListener(this);
                    importeTxt.setText(cashAmountBuilder.toString());

                    importeTxt.setTextKeepState("$" + cashAmountBuilder.toString());
                    importeTxt.setSelection(importeTxt.getText().length());

                    importeTxt.addTextChangedListener(this);
                }
                if (importeTxt.getText().length() > 14) {
                    hideSoftKeyboard((Activity) context);
                    importeTxt.setTextSize(TypedValue.COMPLEX_UNIT_PX, fOriginalSize);
                    CustomDialog dialog = new CustomDialog(getParent(), "Monto excedido");
                    dialog.show();
                    importeTxt.removeTextChangedListener(this);
                    importeTxt.setText("");
                    importeTxt.addTextChangedListener(this);
                }

            }
        };

        importeTxt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (importeTxt.getText().length() > 0) {
                        importeTxt.removeTextChangedListener(txtWatcher);
                        importeTxt.setText("");
                        importeTxt.addTextChangedListener(txtWatcher);
                    }
                }
                return false;
            }
        });
        importeTxt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    importeTxt.setSelection(importeTxt.getText().length());
                }
            }
        });
        importeTxt.addTextChangedListener(txtWatcher);



        TextWatcher textWatcher2 =  new TextWatcher() {  //Propina
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                importeTxt.setSelection(importeTxt.getText().length());
            }

            @Override

            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                float width = propinaET.getWidth();
                float size = propinaET.getTextSize();
                if (!s.toString().matches("^\\$(\\d{1,3}(\\,\\d{3})*|(\\d+))(\\.\\d{2})?$")) {
                    String userInput = "" + s.toString().replaceAll("[^\\d]", "");
                    StringBuilder cashAmountBuilder = new StringBuilder(userInput);

                    while (cashAmountBuilder.length() > 3 && cashAmountBuilder.charAt(0) == '0') {
                        cashAmountBuilder.deleteCharAt(0);
                    }
                    while (cashAmountBuilder.length() < 3) {
                        cashAmountBuilder.insert(0, '0');
                    }
                    cashAmountBuilder.insert(cashAmountBuilder.length() - 2, '.');

                    propinaET.removeTextChangedListener(this);
                    propinaET.setText(cashAmountBuilder.toString());

                    propinaET.setTextKeepState("$" + cashAmountBuilder.toString());
                    propinaET.setSelection(propinaET.getText().length());

                    propinaET.addTextChangedListener(this);
                }
                if (propinaET.getText().length() > 14) {
                    hideSoftKeyboard((Activity) context);
                    propinaET.setTextSize(TypedValue.COMPLEX_UNIT_PX, fOriginalSize2);
                    CustomDialog dialog = new CustomDialog((Activity) context, "Monto excedido");
                    dialog.show();
                    propinaET.removeTextChangedListener(this);
                    propinaET.setText("");
                    propinaET.addTextChangedListener(this);
                }
            }

        };
        propinaET.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (propinaET.getText().length() > 0) {
                        propinaET.removeTextChangedListener(textWatcher2);
                        propinaET.setText("");
                        propinaET.addTextChangedListener(textWatcher2);
                    }
                }
                return false;
            }
        });
        propinaET.addTextChangedListener(textWatcher2);

        /*etMailTarj.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    ibAceptar.performClick();
                    return true;
                }
                return false;
            }
        });*/


    }

    //Funcion que maneja el monto total a pagar
    private void procedePago(){
        //Quita los signos
        String sAmount = importeTxt.getText().toString().replace("$", "");
        String sAmount2 = sAmount.replace("MX", "");
        sAmount2 = sAmount2.replace(" ", "");

        if (sAmount2.length() > 0) {

            Float fAmount = Float.parseFloat(sAmount2);

            if (fAmount > 0) {
                if (bPropina) {
                    //Quita los signos
                    String propina_amount = propinaET.getText().toString().replace("$", "");
                    propina_amount = propina_amount.replace("MX", "");
                    propina_amount = propina_amount.replace(" ", "");
                    if (iPropina_selected == 3) {
                        if (propina_amount.length() > 0) {
                            Float fPropinaAmount = Float.parseFloat(propina_amount);

                            if (fPropinaAmount > 0) {
                                fAmount = fAmount + fPropinaAmount;
                                sAmount2 = String.format("%.2f", fAmount);

                                hideSoftKeyboard((Activity) context);
                                
                                Intent intent = new Intent(InicioActivity.this, CardActivity.class);
                                intent.putExtra("concepto", etConcepto.getText().toString());
                                intent.putExtra("monto", sAmount2);
                                intent.putExtra("email", etMailTarj.getText().toString());
                                intent.putExtra("noTrans", etNoTrans.getText().toString());
                                intent.putExtra("token",  fromIntent.getStringExtra("token"));
                                intent.putExtra("ambiente",  fromIntent.getStringExtra("ambiente"));
                                //intent.putExtra("token", prefs.getString("token", null));
                               // intent.putExtra("ambiente", prefs.getString("ambiente", null));

                                startActivity(intent);


                            } else {
                                CustomDialog dialog = new CustomDialog(this, "El importe de propina no es válido");
                                dialog.show();
                            }
                        } else {
                            CustomDialog dialog = new CustomDialog(this, "El importe de propina no es válido");
                            dialog.show();
                        }
                    } else {

                        if (iPropina_selected == 0)
                            fAmount = fAmount * Float.parseFloat("1.1");

                        else if (iPropina_selected == 1)
                            fAmount = fAmount * Float.parseFloat("1.15");

                        else
                            fAmount = fAmount * Float.parseFloat("1.2");


                        sAmount2 = String.format("%.2f", fAmount);

                        hideSoftKeyboard((Activity) context);
                        Intent intent = new Intent(InicioActivity.this, CardActivity.class);
                        intent.putExtra("concepto", etConcepto.getText().toString());
                        intent.putExtra("monto", sAmount2);
                        intent.putExtra("email", etMailTarj.getText().toString());
                        intent.putExtra("noTrans", etNoTrans.getText().toString());
                        intent.putExtra("token",  fromIntent.getStringExtra("token"));
                        intent.putExtra("ambiente",  fromIntent.getStringExtra("ambiente"));
                        startActivity(intent);

                    }
                }else{
                    hideSoftKeyboard((Activity) context);
                    Intent intent = new Intent(InicioActivity.this, CardActivity.class);
                    intent.putExtra("concepto", etConcepto.getText().toString());
                    intent.putExtra("monto", sAmount2);
                    intent.putExtra("email", etMailTarj.getText().toString());
                    intent.putExtra("noTrans", etNoTrans.getText().toString());
                    intent.putExtra("token",  fromIntent.getStringExtra("token"));
                    intent.putExtra("ambiente",  fromIntent.getStringExtra("ambiente"));
                    startActivity(intent);
                }

            }
            else {
                CustomDialog dialog = new CustomDialog(this, "El importe no es válido");
                dialog.show();
            }

        }
        else {
            CustomDialog dialog = new CustomDialog(this, "El importe no es válido");
            dialog.show();
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private boolean permisoLocalizacion(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 60);
            Log.d("Location", "PIDIENDO");

        }else
            return true;

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d("location", ""+requestCode);
        switch (requestCode) {
            case 60: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    CustomDialog dialog  = new CustomDialog(this,"Se necesita acceder a tu localizaicón para utilizar esta aplicación");
                    dialog.show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void muestraMenuSuperior(){
        menu_open.setVisibility(View.VISIBLE);
        menu_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MenuDialog menu = new MenuDialog((Activity) context);
                menu.show();
            }
        });
    }

    private void propinaTipo() {
        ArrayList<String> propinaList = new ArrayList<>();
        propinaList.add("10%");
        propinaList.add("15%");
        propinaList.add("20%");
        propinaList.add("Otro");

        //Arma la lista de opciones
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, R.id.rowTV, propinaList) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                //((TextView) v).setTextSize(16);
                return v;

            }
            /*//Muestra lista de opciones
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                String propina = propinaList.get(position);
                TextView textTV = v.findViewById(R.id.rowTV);
                textTV.setText(propina);
                return v;
            }*/

        };
        spPropina.setAdapter(adapter);

        //Listener spinner propina
        spPropina.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                LinearLayout vista_centrar = (LinearLayout) adapterView.getChildAt(0);
                if(vista_centrar != null){
                    ((LinearLayout) adapterView.getChildAt(0)).setGravity(Gravity.CENTER);
                }
                if (i == 3){
                    propinaET.setVisibility(View.VISIBLE);
                } else {
                    propinaET.setVisibility(View.GONE);
                }
                iPropina_selected = i;
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void monedaTipo(){
        prefs.edit().putString("moneda", "0").apply();

        ArrayList<String> monedasList = new ArrayList<>();
        monedasList.add("MXN");
        monedasList.add("USD");
        ArrayAdapter<String> adapterMoneda = new ArrayAdapter<String>(this, R.layout.spinner_item, R.id.rowTV, monedasList){
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                //((TextView) v).setTextSize(16);
                /*String propina = monedasList.get(position);
                TextView textTV = v.findViewById(R.id.rowTV);
                textTV.setText(propina);*/
                return v;
            }
        };
        spMoneda.setAdapter(adapterMoneda);

        spMoneda.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                prefs.edit().putString("moneda", i + "").apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu ) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_inicio, menu);
        menu_1 = menu.findItem(R.id.cbTransaccion);
        menu_2 = menu.findItem(R.id.cbConcepto);
        menu_3 = menu.findItem(R.id.cbPropina);


        if (sTransaccion.equals("1")){
            etNoTrans.setVisibility(View.VISIBLE);
            tvNoTrans.setVisibility(View.VISIBLE);
            menu_1.setChecked(true);
        }
        if (sConcepto.equals("1")){
            etConcepto.setVisibility(View.VISIBLE);
            tvConcepto.setVisibility(View.VISIBLE);
            menu_2.setChecked(true);
        }
        if (sPropina.equals("1")){
            bPropina = true;
            propinaLayout.setVisibility(View.VISIBLE);
            menu_3.setChecked(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cbTransaccion:
                if(item.isChecked()) {
                    item.setChecked(false);
                    etNoTrans.setVisibility(View.GONE);
                    tvNoTrans.setVisibility(View.GONE);
                    prefs.edit().putString("trans","0").apply();
                } else {
                    etNoTrans.setVisibility(View.VISIBLE);
                    tvNoTrans.setVisibility(View.VISIBLE);
                    item.setChecked(true);
                    prefs.edit().putString("trans","1").apply();
                }
                return true;
            case R.id.cbConcepto:
                if(item.isChecked()) {
                    item.setChecked(false);
                    etConcepto.setVisibility(View.GONE);
                    tvConcepto.setVisibility(View.GONE);
                    prefs.edit().putString("concepto","0").apply();
                } else {
                    etConcepto.setVisibility(View.VISIBLE);
                    tvConcepto.setVisibility(View.VISIBLE);
                    item.setChecked(true);
                    prefs.edit().putString("concepto","1").apply();
                }
                return true;
            case R.id.cbPropina:
                if(item.isChecked()) {
                    bPropina = false;
                    item.setChecked(false);
                    propinaLayout.setVisibility(View.GONE);
                    prefs.edit().putString("propina","0").apply();
                } else {
                    bPropina = true;
                    propinaLayout.setVisibility(View.VISIBLE);
                    item.setChecked(true);
                    prefs.edit().putString("propina","1").apply();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if(mIsRestoredFromBackstack)
        {
            // The fragment restored from backstack, do some work here!
            prefs = this.getSharedPreferences("ingenico", Context.MODE_PRIVATE);
            sTransaccion = prefs.getString("trans", "0");
            sConcepto = prefs.getString("concepto", "0");
            sPropina = prefs.getString("propina", "0");

            if (sTransaccion.equals("1")){
                etNoTrans.setVisibility(View.VISIBLE);
                menu_1.setChecked(true);
            } else {
                etNoTrans.setVisibility(View.GONE);
                menu_1.setChecked(false);
            }
            if (sConcepto.equals("1")){
                etConcepto.setVisibility(View.VISIBLE);
                menu_2.setChecked(true);
            } else {
                etConcepto.setVisibility(View.GONE);
                menu_2.setChecked(false);
            }
            if (sPropina.equals("1")){
                bPropina = true;
                propinaLayout.setVisibility(View.VISIBLE);
                menu_3.setChecked(true);
            } else {
                bPropina = false;
                propinaLayout.setVisibility(View.INVISIBLE);
                menu_3.setChecked(false);
            }
        }
    }



}
