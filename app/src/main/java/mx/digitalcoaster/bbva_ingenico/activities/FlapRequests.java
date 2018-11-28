package mx.digitalcoaster.bbva_ingenico.activities;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RadioGroup;
import mx.digitalcoaster.bbva_ingenico.activities.CommonActivity;
import com.ingenico.pclservice.PclService;
import com.ingenico.pclutilities.PclUtilities;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.conn.ConnectTimeoutException;
import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * Created by rzertuche on 1/20/16.
 */
public abstract class FlapRequests extends CommonActivity {

    /* VARIABLES USED DURING TESTS */
    private static final String TAG = "PCLTESTAPP";
    private static final int JUSTIFIED_CENTER = 0;
    private static final int JUSTIFIED_RIGHT = 1;
    private static final int JUSTIFIED_LEFT = 2;
    private static final String EXTRA_SIGNATURE_BMP = "signature";
    private String[] slistOfBmp;
    AssetManager assetManager;
    static int id = 1;
    private boolean mBcrRead;
    private Context mContext;
    private boolean mCallbackRegistered = false;
    private static Bitmap mLastSignature = null;

    private NetworkTask mNetworkTask;
    private int miScanState = 0;
    private static final int NB_ISO_FONTS = 7;
    static class PclObject {
        CommonActivity.PclServiceConnection serviceConnection;
        PclService service;
        NetworkTask nwTask;
        int iScanState;
    }
    private RadioGroup mRadioGroup;
    private PclUtilities mPclUtil;
    private boolean mServiceStarted;

    CharSequence mCurrentDevice;

    UsbManager mUsbManager = null;
    PendingIntent mPermissionIntent = null;
    private boolean mPermissionRequested = false;
    private boolean mPermissionGranted = false;
    private static final String ACTION_USB_PERMISSION = "mx.digitalcoaster.bbva_ingenico.USB_PERMISSION";

    private long mTestStartTime;
    private long mTestEndTime;
    private String strCardNumber="12345";
    private boolean bFound = false;


    protected static mx.digitalcoaster.bbva_ingenico.activities.FlapRequests currentActivity;
    public FlapRequests() {}

    public String amount        = "";
    public String cashbackAmount= "0.00";
    public String concept       = "";
    public String email         = "";
    public String cvv           = "";
    public String orden         = "";
    public String servicio      = "1488";
    public String token         = "";
    public String msi           = "0";
    public String env           = "P";
    public String version_app   = "test";
    public boolean points       = false;
    public String latitud       = "";
    public String longitud      = "";
    public String global_bin    = "";
    public String global_tlv    = "";
    public String moneda        = "0";

    public boolean pinEntered   = false;

    public String device        = "Walker";

    private boolean fallback    = false;
    private int fallbackCounter = 0;
    private String tracks       = "";

    private String transactionId = "";
    private String authCode = "";
    private String criptograma = "";
    private String payment_message = "";
    private String servicioString = "";

    //WisePad
    protected static final String[] DEVICE_NAMES = new String[] { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
    protected Boolean isEMVWisepad = true;
    protected Boolean pin_error = false;
    public String total,direccion,monedaticket,horaTransaccion,tarjeta,referencia,tipoPago,afiliacion,comercio,tipoOperacion,autorizacion,titular,emisor,firma,fechaTransaccion, labelName, criptgrama, concepto, saldoActualPesos, saldoRedimidoPesos, saldoAnteriorPesos, saldoActualPuntos, saldoRedimidoPuntos, saldoAnteriorPuntos, banco, medioPago, arqc, aid;

    String version = Build.VERSION.RELEASE;


    // -- -- -- INTERFACE -- -- -- //
    public interface FlapResponses{
        //GENERAL
        void loginResponse(Boolean success, String message, String token, String servicios);
        void userNodeResponse(JSONObject user, String status);
        void getTransactionsResponse(Boolean success, JSONArray transactions, String message);
        void getSearchResponse(Boolean success, JSONArray transactions);
        void didCancelOrder(Boolean success, String message);
        void getTicket();
        void didFindCard();
        void askForCVV();
        void didHavePointsOrMonths(Boolean points, ArrayList<String> months, Boolean isEMV);
        void didFinishPayment(Boolean success, String transactionId, String authCode, String criptograma, String message);
        void didSendSignature(Boolean success);
        void didGetTicketWithTotal(String total, String direccion, String moneda, String horatransaccion, String tarjeta, String referencia, String tipoPago, String afiliacion, String comercio, String tipoOperacion, String autorizacion, String titular, String emisor, String firma, String fechaTransaccion, String label, String criptgrama, String concepto, String saldoActualPesos, String saldoRedimidoPesos, String saldoAnteriorPesos, String saldoActualPuntos, String saldoRedimidoPuntos, String saldoAnteriorPuntos, String banco, String medioPago, String aid);
        //EMVSWIPE
        void isDeviceConnected(Boolean connected);
        //WISEPAD
        void scanResult(Boolean success, List<String> devices, String message);
        void isBluetoothDeviceConnected(Boolean connected);
        void pinRequest();
        void confirmedAmount();
        void selectApplicationList(String[] appNameList);
        public void selectApplication(int index);
        //
        void startedOnlineTransaction(Boolean isEMV);

    }
    public void registerCallback(FlapResponses callbackClass){
        flap_responses = callbackClass;
    }
    FlapResponses flap_responses;
    // -- -- -- INTERFACE -- -- -- //

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //100% Our Methods
    public void startIngenico(){

        Log.d("FlapRequests onCreate","ingenico");
        device = "Ingenico";

        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), 0);
            ZipFile zf = new ZipFile(ai.sourceDir);
            ZipEntry ze = zf.getEntry("classes.dex");
            long time = ze.getTime();
            zf.close();

            String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(time);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!mServiceStarted)
        {
            Log.d("FlapRequests onCreate","no service ingenico");
            SharedPreferences settings = getSharedPreferences("PCLSERVICE", MODE_PRIVATE);
            boolean enableLog = settings.getBoolean("ENABLE_LOG", false);
            Intent i = new Intent(this, PclService.class);
            i.putExtra("PACKAGE_NAME", "mx.digitalcoaster.bbva_ingenico");
            i.putExtra("FILE_NAME", "pairing_addr.txt");
            i.putExtra("ENABLE_LOG", enableLog);
            if (getApplicationContext().startService(i) != null)
                mServiceStarted = true;
            else
                Log.d("FlapRequests onCreate","no file created");
        }
        initService();
        mPclUtil = new PclUtilities(this, "mx.digitalcoaster.bbva_ingenico", "pairing_addr.txt");

    }

    public void loginUser(String username, String password, String env, Context context){

        /*KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
        sf.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);*/

        String url = "https://www.adquiramexico.com.mx/";
        if (env.equals("D")){ url = "https://prepro.adquiracloud.mx/"; }

        if ((android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) && !env.equals("D")){
            // Do something for lollipop and above versions
            url = "https://www.adquiramexico.com.mx/";
        } else{
            // do something for phones running an SDK before lollipop
            //url="https://int.adquiramexico.com.mx/";
            url = "https://prepro.adquiracloud.mx/";
        }




        //call callback method
        final AsyncHttpClient client = new AsyncHttpClient();
        HttpEntity entity;
        entity = new StringEntity("" +
                "{\"username\":\""+username+"\"," +
                "\"password\":\""+password+"\"," +
                "\"returnServices\":"+ true +
                "}", "UTF-8");

        Log.d("Login", username.toString());
        Log.d("Login", password.toString());
        Log.d("Login", url+"PaymentTCMService/rest/login");


        client.setMaxRetriesAndTimeout(3,30000);
        client.setTimeout(30000);
        client.post(context, url+"PaymentTCMService/rest/login", entity, "application/json", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d("Login", response.toString());
                JSONArray statusArray = response.optJSONArray("status");
                JSONObject status = null;
                //String services = null;
                JSONObject services = null;

                try {
                    status = statusArray.getJSONObject(0);
                    String code = status.optString("code");
                    String message = status.optString("message");
                    String token = response.optString("token");

                    services = response.getJSONObject("services");
//                    String servicesString = services.toString();

                    services = response.getJSONObject("services");
                    String servicesString = "";
                    Iterator<String> iter = services.keys();
                    while (iter.hasNext()) {
                        String key = iter.next();
                        servicesString += key+";";
                    }
                    servicesString = servicesString.substring(0, servicesString.length()-1);
                    Log.d("services", servicesString);


                    if (code.equals("0")){
                        flap_responses.loginResponse(true, message, token, services.toString());
                    } else {
                        flap_responses.loginResponse(false, message, "", "");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    flap_responses.loginResponse(false, "Ocurrio un error", "", "");
                }


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                //System.out.println(responseString.toString());
                String message = null;
                if(message!=null){
                    message=responseString;
                }else{
                    message="";
                }
                flap_responses.loginResponse(false, message, "", "");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                String message = null;
                if(message!=null){
                    message=errorResponse.toString();
                }else{
                    message="";
                }
                flap_responses.loginResponse(false, message, "", "");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                String message = null;
                if(message!=null){
                    message=errorResponse.toString();
                }else{
                    message="";
                }
                flap_responses.loginResponse(false, message, "", "");
            }
        });
    }
    public void getUserNode(String username, String token, String env, Context context){

        String url = "https://www.adquiramexico.com.mx/";
        if (env.equals("D")){ url = "https://prepro.adquiracloud.mx/"; }

        if ((android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) && !env.equals("D")){
            // Do something for lollipop and above versions
            url = "https://www.adquiramexico.com.mx/";
        } else{
            // do something for phones running an SDK before lollipop
            //url="https://int.adquiramexico.com.mx/";
            url = "https://prepro.adquiracloud.mx/";
        }


        //call callback method
        final AsyncHttpClient client = new AsyncHttpClient();
        client.setMaxRetriesAndTimeout(3,30000);
        client.addHeader("user-token", token);
        Log.d("Token", token);
        Log.d("Nodo", url + "PaymentTCMService/mpos/rest/v1/user/" + username + "/node");
        client.get(context, url + "PaymentTCMService/mpos/rest/v1/user/" + username + "/node", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                System.out.println(response.toString());
                String status = response.optString("status");
                flap_responses.userNodeResponse(response, status);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                //System.out.println(responseString.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                System.out.println(errorResponse.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                //System.out.println(errorResponse.toString());
                flap_responses.userNodeResponse(null, "Error al traer nodo");
            }
        });
    }

    public void checkCard(){
        AsyncTask.Status mStatus = AsyncTask.Status.PENDING;
        mPclService.addDynamicBridge(1087, 0);
        mNetworkTask = new NetworkTask(1087,1,1, false);
        mNetworkTask.execute();
    }

    public void authorizePayment(){
        AsyncTask.Status mStatus = AsyncTask.Status.PENDING;
        mPclService.addDynamicBridge(1087, 0);
        mNetworkTask = new NetworkTask(1087,2,1, false);
        mNetworkTask.execute();
    }


    public void didAuthorize(){
        //runOnUiThread(new Runnable() {

        flap_responses.didFinishPayment(true,"2341234","adq35445","-","-");

    }


    private void searchBinForPointsAndMonths(String bin, final Boolean isEMV, String tlv){
        Log.d("BinForPointsAndMonths", "yeap");
        String firstURL = "https://www.adquiramexico.com.mx/";
        if (env.equals("D")){ firstURL = "https://prepro.adquiracloud.mx/"; }

        if ((android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) && !env.equals("D")){
            // Do something for lollipop and above versions
            firstURL = "https://www.adquiramexico.com.mx/";
        } else{
            // do something for phones running an SDK before lollipop
            //url="https://int.adquiramexico.com.mx/";
            firstURL = "https://prepro.adquiracloud.mx/";
        }

        final AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Content-Type", "application/json");
        client.addHeader("user-token", token);

        String url = "https://prepro.adquiracloud.mx/PaymentTCMService/rest/v1/paymentMethod/periods?bin="+bin+"&service="+servicio+"&currency=MXP";
        if (env.equals("P")){
            url = firstURL+"PaymentTCMService/rest/v1/paymentMethod/periods?bin="+bin+"&service="+servicio+"&currency=MXP";
        }
        Log.d("PointsAndMonths",url);
        client.setMaxRetriesAndTimeout(3,30000);
        client.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("PointsAndMonths",response.toString());
                Boolean points = response.optBoolean("points");
                JSONArray months = response.optJSONArray("periods");
                ArrayList<String> months_string = new ArrayList<String>();
                for (int i=0; i<months.length();i++){
                    try {
                        months_string.add(months.get(i)+"");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (!points && months.length()<2){
                    if (isEMV){
                        Log.d("test Pago","EMV NO PUNTOS, NO MESES ");
                        emvPayment(getBaseContext());
                    } else {
                        Log.d("test Pago","MSR NO PUNTOS, NO MESES ");
                        msrPayment(getBaseContext());
                    }
                } else{
                    flap_responses.didHavePointsOrMonths(points, months_string, isEMV);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);

                String message = null;
                message=errorResponse.toString();
                if(message!=null){
                    message=errorResponse.toString();
                }else{
                    message="Error de conexión";
                }
                flap_responses.didFinishPayment(false, "", "", "", message);
            }
        });
    }
    public void emvPayment(Context context) {
        flap_responses.startedOnlineTransaction(true);

        String url = "https://www.adquiramexico.com.mx/";
        if (env.equals("D")){ url = "https://prepro.adquiracloud.mx/"; }

        if ((android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) && !env.equals("D")){
            // Do something for lollipop and above versions
            url = "https://www.adquiramexico.com.mx/";
        } else{
            // do something for phones running an SDK before lollipop
            //url="https://int.adquiramexico.com.mx/";
            url = "https://prepro.adquiracloud.mx/";
        }

        //call callback method
        final AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(30 * 1000);
        HttpEntity entity;

        String pinEnteredString = "01";
        if (pinEntered) pinEnteredString = "00";

        Log.d("servicioG",servicio);

        JSONObject obj = null;
        try {
            obj = new JSONObject(servicio);
            Log.d("My App", obj.toString());
            Iterator keysToCopyIterator = obj.keys();
            List<String> keysList = new ArrayList<String>();
            servicio = "";
            while(keysToCopyIterator.hasNext()) {
                String key = (String) keysToCopyIterator.next();
                servicio = servicio + key + ";";
            }
        } catch (Throwable t) {
        }

        Log.d("servicioJ",servicio);

        if (servicio != null){
            String[] servicioArray = Arrays.copyOf(servicio.split("\\;"), servicio.split("\\;").length, String[].class);
            if (servicioArray.length > 1) {
                for (int i = 0; i < servicioArray.length; i++) {
                    if (i != 0) servicioString += ",";
                    servicioString += "\"" + servicioArray[i] + "\"";
                }
            } else {
                servicioString = "\""+ servicio + "\"";
            }
        }


        String json = "" +
                "{\"TAGSEMV\":\"" + global_tlv + "\"," +
                "\"amount\":\"" + amount + "\"," +
                "\"concept\":\"" + concept + "\"," +
                "\"ticket\":" + true + "," +
                "\"currency\":" + moneda + "," +
                "\"email\":\"" + email + "\"," +
                "\"token\":\"" + token + "\"," +
                "\"device\":\"" + device + "\"," +
                "\"service\":["+servicioString+"]," +
                "\"orderid\":\"" + orden + "\"," +
                "\"period\":\"" + msi + "\"," +
                "\"points\":" + points + "," +
                "\"sFlag\":\"" + pinEnteredString + "\"," +
                "\"extraData\": { " +
                "\"lat\":\"" + latitud + "\"," +
                "\"lon\":\"" + longitud + "\"," +
                "\"source\":\"" + version_app + "\"," +
                "\"sign\":\"1\"" +
                "}}";

        JSONObject jObject = null;
        try {
            jObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        entity = new StringEntity(jObject.toString(), "UTF-8");

        int indexLabel = global_tlv.indexOf("9F12");
        Log.e("INDEX PAGO", String.valueOf(indexLabel+1));

        String lengthLabel=global_tlv.substring(indexLabel+4,indexLabel+6);
        Log.e("PosicionesDespuesIndex",lengthLabel.toString());

        int temp1 = Integer.parseInt(lengthLabel.trim(), 16 );
        Log.e("PosicionesIndexDecimal", String.valueOf(temp1));


        String label=global_tlv.substring(indexLabel+6,indexLabel+6+(temp1*2));
        try{
            labelName = new String(new BigInteger(label, 16).toByteArray());
            Log.e("Conversion", String.valueOf(labelName));

        }catch(NumberFormatException ex){ // handle your exception
            Log.e("ConversionException", ex.toString());

        }





        Log.d("JSON EMV", json);
        Log.d("PAYMENT TCM SERVICE",url + "PaymentTCMService/rest/emv/payment");

        client.setMaxRetriesAndTimeout(3,30000);
        client.post(context, url + "PaymentTCMService/rest/emv/payment", entity, "application/json", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("EMVPayment", response.toString());
                JSONArray status = response.optJSONArray("status");
                JSONObject statusJson = status.optJSONObject(0);
                String code = statusJson.optString("code");

                payment_message = statusJson.has("message") ? statusJson.optString("message") : null;
                String messageError = statusJson.has("message") ? statusJson.optString("message") : null;
                Log.d("MESSAGE ERROR ", messageError);

                if (code.equals("0")) {
                    transactionId = response.optString("transactionId");
                    authCode = response.optString("authCode");

                    JSONObject ticket = response.optJSONObject("ticket");
                    if(ticket!= null){
                        total = ticket.optString("total");
                        direccion = ticket.optString("direccion");
                        monedaticket = ticket.optString("moneda");
                        horaTransaccion = ticket.optString("horaTransaccion");
                        tarjeta = ticket.optString("tarjeta");
                        referencia = ticket.optString("referencia");
                        tipoPago = ticket.optString("tipoPago");
                        afiliacion = ticket.optString("afiliacion");
                        comercio = ticket.optString("comercio");
                        tipoOperacion = ticket.optString("tipoOperacion");
                        autorizacion = ticket.optString("autorizacion");
                        titular = ticket.optString("titular");
                        emisor = ticket.optString("emisor");
                        firma = ticket.optString("firma");
                        fechaTransaccion = ticket.optString("fechaTransaccion");
                        concepto = ticket.optString("referencia");
                        banco = ticket.optString("bankName");
                        medioPago = ticket.optString("paymentMethod");
                        arqc = ticket.optString("criptograma");
                        aid = ticket.optString("aid");
                        saldoActualPesos = ticket.has("saldoActualPesos") ? ticket.optString("saldoActualPesos") : "";
                        saldoRedimidoPesos = ticket.has("saldoRedimidoPesos") ? ticket.optString("saldoRedimidoPesos") : "";
                        saldoAnteriorPesos = ticket.has("saldoAnteriorPesos") ? ticket.optString("saldoAnteriorPesos") : "";
                        saldoActualPuntos = ticket.has("saldoActualPuntos") ? ticket.optString("saldoActualPuntos") : "";
                        saldoRedimidoPuntos = ticket.has("saldoRedimidoPuntos") ? ticket.optString("saldoRedimidoPuntos") : "";
                        saldoAnteriorPuntos = ticket.has("saldoAnteriorPuntos") ? ticket.optString("saldoAnteriorPuntos") : "";


                        flap_responses.didGetTicketWithTotal(total,direccion,monedaticket,horaTransaccion,tarjeta,referencia,tipoPago,afiliacion,comercio,tipoOperacion,autorizacion,titular,emisor,firma,fechaTransaccion,labelName, arqc,concepto,saldoActualPesos,saldoRedimidoPesos,saldoAnteriorPesos,saldoActualPuntos,saldoRedimidoPuntos,saldoAnteriorPuntos,banco,medioPago,aid);


                    }

                    criptograma = ticket.optString("criptograma");

                    String scriptTlv = response.has("scriptTlv") ? response.optString("scriptTlv") : null;
                    String issuerAuthenticationDataTlv = response.has("issuerAuthenticationDataTlv") ? response.optString("issuerAuthenticationDataTlv") : null;

                    String isr = "";
                    if (issuerAuthenticationDataTlv != null) {
                        isr = "8A023030" + issuerAuthenticationDataTlv;
                    } else {
                        isr = "8A023030";
                    }

                    if (scriptTlv != null) {
                        isr = scriptTlv + isr;
                    }

                    try {
                        int i = 0;
                        Thread.sleep(5);
                        while (isr == null && i < 50) {
                            try {
                                i++;
                                Log.d("IAP", "Waiting ISR value...");
                                Thread.sleep(20);
                            } catch (InterruptedException e) {

                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Log.d("IAP", isr);
                    // APPROVED ONLINE


                } else {
                    // NOT APPROVED ONLINE
                }

            }

            public void onFailure(Throwable error, String content) {
                if (error.getCause() instanceof ConnectTimeoutException) {
                    // NOT APPROVED ONLINE
                    String message = null;
                    message=error.getMessage();
                    if(message!=null){
                        message=error.getMessage();
                    }else{
                        message="Error de conexión";
                    }
                    flap_responses.didFinishPayment(false, "", "", "", message);
                    resetAll();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                String message = null;
                message=responseString;
                if(message!=null){
                    message=responseString;
                }else{
                    message="Error de conexión";
                }
                flap_responses.didFinishPayment(false, "", "", "", message);
                resetAll();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                System.out.println(errorResponse.toString());
                String message = null;
                message=errorResponse.toString();
                if(message!=null){
                    message=errorResponse.toString();
                }else{
                    message="Error de conexión";
                }
                flap_responses.didFinishPayment(false, "", "", "", message);
                resetAll();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d("TEST", errorResponse.toString());
                String msg = null;
                msg=errorResponse.toString();
                if(msg!=null){
                    msg=errorResponse.toString();
                }else{
                    msg="Error de conexión";
                }
                flap_responses.didFinishPayment(false, "", "", "", msg);
                resetAll();
            }
        });
    }
    public void getTicket(){
        flap_responses.didGetTicketWithTotal(total,direccion,monedaticket,horaTransaccion,tarjeta,referencia,tipoPago,afiliacion,comercio,tipoOperacion,autorizacion,titular,emisor,firma,fechaTransaccion,labelName, arqc,concepto,saldoActualPesos,saldoRedimidoPesos,saldoAnteriorPesos,saldoActualPuntos,saldoRedimidoPuntos,saldoAnteriorPuntos,banco,medioPago,aid);

    }
    public void msrPayment(Context context){
        flap_responses.startedOnlineTransaction(false);

        String url = "https://www.adquiramexico.com.mx/";
        if (env.equals("D")){ url = "https://prepro.adquiracloud.mx/"; }

        if ((android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) && !env.equals("D")){
            // Do something for lollipop and above versions
            url = "https://www.adquiramexico.com.mx/";
        } else{
            // do something for phones running an SDK before lollipop
            //url="https://int.adquiramexico.com.mx/";
            url = "https://prepro.adquiracloud.mx/";
        }

        //call callback method
        final AsyncHttpClient client = new AsyncHttpClient();
        HttpEntity entity;
        client.setMaxRetriesAndTimeout(3,30000);

        String[] servicioArray = Arrays.copyOf(servicio.split("\\;"), servicio.split("\\;").length, String[].class);
        if (servicioArray.length > 1) {
            for (int i = 0; i < servicioArray.length; i++) {
                if (i != 0) servicioString += ",";
                servicioString += "\"" + servicioArray[i] + "\"";
            }
        } else {
            servicioString = "\""+ servicio + "\"";
        }

        String json = "" +
                "{\"tracks\":\""+tracks+"\"," +
                "\"amount\":\""+amount+"\"," +
                "\"currency\":" + moneda + "," +
                "\"concept\":\""+concept+"\"," +
                "\"ticket\":"+true+"," +
                "\"email\":\""+email+"\"," +
                "\"token\":\""+token+"\"," +
                "\"device\":\""+device+"\"," +
                "\"cvv\":\""+cvv+"\"," +
                "\"service\":["+servicioString+"]," +
                "\"orderid\":\""+orden+"\"," +
                "\"period\":\""+msi+"\"," +
                "\"points\":"+points+"," +
                "\"fallback\":"+fallback+"," +
                "\"sFlag\":\"01\","+
                "\"extraData\": { " +
                "\"lat\":\""+latitud+"\"," +
                "\"lon\":\""+longitud+"\"," +
                "\"source\":\""+version_app+"\"," +
                "\"sign\":\"1\"" +
                "}}";
        entity = new StringEntity(json, "UTF-8");
        Log.d("JSON EMV", json);
        client.post(context, url + "PaymentTCMService/rest/paymentSwipe", entity, "application/json", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                JSONArray status = response.optJSONArray("status");
                JSONObject statusJson = status.optJSONObject(0);
                String code = statusJson.optString("code");
                String msg = statusJson.optString("message");

                if (code.equals("0") ){
                    String transactionId = response.optString("transactionId");
                    String authCode = response.optString("authCode");

                    JSONObject ticket = response.optJSONObject("ticket");
                    String criptograma = ticket.optString("criptograma");

                    flap_responses.didFinishPayment(true, transactionId, authCode, criptograma, msg);
                } else {
                    flap_responses.didFinishPayment(false, "", "", "", msg);
                    resetAll();
                }

            }

            public void onFailure(Throwable error, String content) {
                if ( error.getCause() instanceof ConnectTimeoutException) {
                    System.out.println("JSON Error" + content.toString());
                    String msg = null;
                    msg=error.getMessage().toString();
                    if(msg!=null){
                        msg=error.getMessage().toString();
                    }else{
                        msg="Error de conexión";
                    }
                    flap_responses.didFinishPayment(false, "", "", "", msg);
                    resetAll();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                System.out.println(responseString.toString());
                String msg = null;
                msg=responseString.toString();
                if(msg!=null){
                    msg=responseString.toString();
                }else{
                    msg="Error de conexión";
                }
                flap_responses.didFinishPayment(false, "", "", "", msg);
                resetAll();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                System.out.println(errorResponse.toString());
                String msg = null;
                msg=errorResponse.toString();
                if(msg!=null){
                    msg=errorResponse.toString();
                }else{
                    msg="Error de conexión";
                }
                flap_responses.didFinishPayment(false, "", "", "", msg);
                resetAll();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                System.out.println(errorResponse.toString());
                String msg = null;
                msg=errorResponse.toString();
                if(msg!=null){
                    msg=errorResponse.toString();
                }else{
                    msg="Error de conexión";
                }
                flap_responses.didFinishPayment(false, "", "", "", msg);
                resetAll();
            }
        });
    }
    public void cancelPayment(Context context){
        //flap_responses.startedOnlineTransaction(true);

        String url = "https://www.adquiramexico.com.mx/";
        if (env.equals("D")){ url = "https://prepro.adquiracloud.mx/"; }

        if ((android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) && !env.equals("D")){
            // Do something for lollipop and above versions
            url = "https://www.adquiramexico.com.mx/";
        } else{
            // do something for phones running an SDK before lollipop
            //url="https://int.adquiramexico.com.mx/";
            url = "https://prepro.adquiracloud.mx/";
        }

        //call callback method
        final AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(30 * 1000);
        HttpEntity entity;
        String json = "" +
                "{\"token\":\""+token+"\"," +
                "\"transactionId\":\""+transactionId+"\"," +
                "\"reverse\":"+true+
                "}";
        entity = new StringEntity(json, "UTF-8");
        Log.d("JSON EMV", json);
        client.setMaxRetriesAndTimeout(3,30000);
        client.post(context, url + "PaymentTCMService/rest/cancellation/typed", entity, "application/json", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("CancelationPayment", response.toString());
                JSONArray status = response.optJSONArray("status");
                JSONObject statusJson = status.optJSONObject(0);
                String code = statusJson.optString("code");



                if (code.equals("0") ){
                    flap_responses.didFinishPayment(false, "", "", "", "Transacción Declinada");
                } else {
                    if (authCode.length() > 0 && criptograma.length() > 0 && transactionId.length() > 0)
                        flap_responses.didFinishPayment(true, transactionId, authCode, criptograma, payment_message);
                    else
                        flap_responses.didFinishPayment(false, "", "", "", payment_message);
                }
                resetAll();

            }

            public void onFailure(Throwable error, String content) {
                if ( error.getCause() instanceof ConnectTimeoutException) {
                    System.out.println("JSON Error" + error.getMessage());
                    String msg = null;
                    msg=error.getMessage().toString();
                    if(msg!=null){
                        msg=error.getMessage().toString();
                    }else{
                        msg="Error de conexión";
                    }
                    flap_responses.didFinishPayment(false, "", "", "", msg);
                    resetAll();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                System.out.println(responseString.toString());
                String msg = null;
                msg=responseString.toString();
                if(msg!=null){
                    msg=responseString.toString();
                }else{
                    msg="Error de conexión";
                }
                if (authCode.length() > 0 && criptograma.length() > 0 && transactionId.length() > 0){}
                //flap_responses.didFinishPayment(true, transactionId, authCode, criptograma, payment_message);
                else
                    flap_responses.didFinishPayment(false, "", "", "", msg);
                resetAll();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                System.out.println(errorResponse.toString());
                String msg = null;
                msg=errorResponse.toString();
                if(msg!=null){
                    msg=errorResponse.toString();
                }else{
                    msg="Error de conexión";
                }

                if (authCode.length() > 0 && criptograma.length() > 0 && transactionId.length() > 0){}
                //flap_responses.didFinishPayment(true, transactionId, authCode, criptograma, payment_message);
                else
                    flap_responses.didFinishPayment(false, "", "", "", msg);
                resetAll();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d("TEST", errorResponse.toString());

                String msg = null;
                msg=errorResponse.toString();
                if(msg!=null){
                    msg=errorResponse.toString();
                }else{
                    msg="Error de conexión";
                }

                if (authCode.length() > 0 && criptograma.length() > 0 && transactionId.length() > 0){}
                //flap_responses.didFinishPayment(true, transactionId, authCode, criptograma, payment_message);
                else
                    flap_responses.didFinishPayment(false, "", "", "", msg);
                resetAll();
            }
        });
    }
    public void sendSignature(Context context, String image_base64, String transactionId){
        flap_responses.didSendSignature(true);

/*
        String url = "https://www.adquiramexico.com.mx/";
        if (env.equals("D")){ url = "https://prepro.adquiracloud.mx/"; }
        Log.d("FIRMA URL", url);
        //call callback method
        final AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("user-token", token);
        client.addHeader("app-token", "mpos20140729");
        HttpEntity entity;
        String json = "" +
                "{" +
                "\"type\":\"sign\"," +
                "\"mime\":\"image/png\"," +
                "\"content\":\""+image_base64+"\"" +
                "}";
        json = json.replace("\n", "").replace("\r", "");
        json = json.replace(" ", "");
        entity = new StringEntity(json, "");

        Log.d("JSON Token", token);
        Log.d("JSON Transaction", transactionId);
        Log.d("JSON Firma", image_base64);
        client.post(context, url + "PaymentTCMService/rest/"+transactionId+"/data", entity, "application/json", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                Log.d("Signature", response.toString());
                JSONObject statusJson = response.optJSONObject("status");
                String code = statusJson.optString("code");

                if (code.equals("0") ){
                    flap_responses.didSendSignature(true);
                } else {
                    flap_responses.didSendSignature(false);
                }
                resetAll();

            }

            public void onFailure(Throwable error, String content) {
                if ( error.getCause() instanceof ConnectTimeoutException) {
                   // Log.d("Signature", content);
                    String msg = "Error de conexión";
                    flap_responses.didSendSignature(false);
                    resetAll();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.d("Signature", responseString);
                String msg = "Error de conexión";
                flap_responses.didSendSignature(false);
                resetAll();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d("Signature", errorResponse.toString());
                //System.out.println(errorResponse.toString());
                String msg = "Error de conexión";
                flap_responses.didSendSignature(false);
                resetAll();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Log.d("Signature", errorResponse.toString());
                flap_responses.didSendSignature(false);
                resetAll();
            }
        });*/
    }
    public void getTransactions(Context context, String token, String fromDate, String toDate){
        String url = "https://www.adquiramexico.com.mx/";

        if (env.equals("D")){ url = "https://prepro.adquiracloud.mx/"; }

        //call callback method
        final AsyncHttpClient client = new AsyncHttpClient();
        HttpEntity entity;
        entity = new StringEntity("" +
                "{\"token\":\""+token+"\"," +
                "\"approved\":"+true+"," +
                "{\"fromDate\":\""+fromDate+"\"," +
                "{\"toDate\":\""+toDate+"\"}", "UTF-8");
        client.post(context, url + "PaymentTCMService/rest/report/transaction", entity, "application/json", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
               // JSONObject jsonObject = (new JSONObject(response)).getJSONObject("");

                System.out.println(response.toString());
                JSONArray transactions =  response.optJSONArray("transactions");
                System.out.println(transactions);

                flap_responses.getTransactionsResponse(true, transactions, "transacciones");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                //super.onFailure(statusCode, headers, responseString, throwable);
                //System.out.println(responseString.toString());
                flap_responses.getTransactionsResponse(false, null, "Error de conexión");

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
               // System.out.println(errorResponse.toString());
                flap_responses.getTransactionsResponse(false, null, "Error de conexión");

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                //System.out.println(errorResponse.toString());
                flap_responses.getTransactionsResponse(false, null, "Error de conexión");
            }
        });
    }
    public void searchByAuthCodeOrOrder(Context context, String token, String auth_code, String order){
        String url = "https://www.adquiramexico.com.mx/";
        if (env.equals("D")){ url = "https://prepro.adquiracloud.mx/"; }
        //call callback method
        String term = "";
        String seached_term = "";
        if (auth_code.length()>0){
            term = "authCode";  seached_term = auth_code;
        } else {
            term = "order";     seached_term = order;
        }
        final AsyncHttpClient client = new AsyncHttpClient();
        HttpEntity entity;
        String json = "" +
                "{\"token\":\""+token+"\"," +
                "\""+term+"\":\""+seached_term+"\"," +
                "\"approved\":"+true+
                "}";
        entity = new StringEntity(json, "UTF-8");
        client.post(context, url + "PaymentTCMService/rest/report/transaction", entity, "application/json", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                System.out.print(response.toString());
                JSONArray transactions = response.optJSONArray("transactions");
                flap_responses.getSearchResponse(true, transactions);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                //System.out.println(responseString.toString());
                flap_responses.getSearchResponse(false, null);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                //System.out.println(errorResponse.toString());
                flap_responses.getSearchResponse(false, null);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                //System.out.println(errorResponse.toString());
                flap_responses.getSearchResponse(false, null);
            }
        });
    }
    public void cancelByTransactionId(Context context, String token, String transactionId){
        String url = "https://www.adquiramexico.com.mx/";
        if (env.equals("D")){ url = "https://prepro.adquiracloud.mx/"; }


        if ((android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) && !env.equals("D")){
            // Do something for lollipop and above versions
            url = "https://www.adquiramexico.com.mx/";
        } else{
            // do something for phones running an SDK before lollipop
            url="https://int.adquiramexico.com.mx/";
        }

        //call callback method
        final AsyncHttpClient client = new AsyncHttpClient();
        HttpEntity entity;
        String json = "" +
                "{\"token\":\""+token+"\"," +
                "\"transactionId\":\""+transactionId+"\"" +
                "}";
        entity = new StringEntity(json, "UTF-8");
        client.setMaxRetriesAndTimeout(3,30000);
        client.post(context, url + "PaymentTCMService/rest/cancellation/typed", entity, "application/json", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                JSONArray status = response.optJSONArray("status");
                JSONObject statusJson = status.optJSONObject(0);
                String code = statusJson.optString("code");
                String msg = statusJson.optString("message");

                if (code.equals("0") ){
                    flap_responses.didCancelOrder(true, msg);
                } else {
                    flap_responses.didCancelOrder(false, msg);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                //System.out.println(responseString.toString());
                flap_responses.didCancelOrder(false, "Error al cancelar, intente nuevamente.");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                //System.out.println(errorResponse.toString());
                flap_responses.didCancelOrder(false, "Error al cancelar, intente nuevamente.");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                //System.out.println(errorResponse.toString());
                flap_responses.didCancelOrder(false, "Error al cancelar, intente nuevamente.");
            }
        });
    }

    public void stopScan(){
        stopPclService();
    }

    public void scanDevices(){
        if (!mServiceStarted)
        {
            SharedPreferences settings = getSharedPreferences("PCLSERVICE", MODE_PRIVATE);
            boolean enableLog = settings.getBoolean("ENABLE_LOG", true);
            Intent i = new Intent(this, PclService.class);
            i.putExtra("PACKAGE_NAME", "mx.digitalcoaster.bbva_ingenico.requestsflap");
            i.putExtra("FILE_NAME", "pairing_addr.txt");
            i.putExtra("ENABLE_LOG", enableLog);
            if (getApplicationContext().startService(i) != null)
                mServiceStarted = true;
        }
        refreshTerminalsList("scan");
        if (isCompanionConnected()) {
            new GetTermInfoTask().execute();
        }
    }

    public void connectTo(String deviceName){
        Log.d(TAG, String.format("ConnectTo text=%s", deviceName));
        if (!deviceName.equals(mCurrentDevice))
        {
            releaseService();
            stopPclService();

            Log.d(TAG, String.format("current:%s saved:%s", deviceName, mCurrentDevice));
            mCurrentDevice = deviceName;
            if (mCurrentDevice.charAt(2) == ':') {
                Log.d("ZERTUCHE","DEVICE CONECTADOOOOOO "+mCurrentDevice);
                mPclUtil.ActivateCompanion(((String) mCurrentDevice).substring(0, 17));
            }
            else {
                Log.d("ZERTUCHE","DEVICE CONECTADOOOOOO "+mCurrentDevice);

                mPclUtil.activateUsbCompanion((String) mCurrentDevice);
            }

            startPclService();
            Log.d(TAG, "onCheckedChanged => INIT");
            initService();
        }
    }

    public void setPaymentArguments(String v_amount, String v_concept, String v_email, String v_orden, String v_token, String v_env, String v_lat, String v_long, String v_servicio){
        if (v_env.equals("D")) env = "D";
        else env = "P";

        if (env.equals("D")) servicio = "773";
        else servicio = v_servicio;

        amount  = v_amount;
        concept = v_concept;
        email   = v_email;
        orden   = v_orden;
        token   = v_token;
        latitud = v_lat;
        longitud= v_long;


    }
    public void resetAll(){
        amount        = "";
        cashbackAmount= "";
        concept       = "";
        email         = "";
        cvv           = "";
        orden         = "";
        servicio      = "";
        //token         = "";
        msi           = "0";
        //env           = "";
        //version_app   = "";
        points       = false;
        longitud      = "";
        latitud       = "";
        global_bin    = "";
        global_tlv    = "";

        fallback        = false;
        fallbackCounter = 0;
        tracks          = "";

        isEMVWisepad    = true;

        transactionId   = "";
        authCode        = "";

    }

    protected void onRequestOnlineProcess(String tlv) {
        Log.d("onRequestOnlineProcess", "yeap");
        global_tlv = tlv;
        Pattern mPattern = Pattern.compile("((C408)[0-9]{6}[F]{6})");
        Matcher matcher = mPattern.matcher(tlv.toString());

        Pattern mPattern2 = Pattern.compile("((c408)[0-9]{6})");
        Matcher matcher2 = mPattern2.matcher(tlv.toString());


        if (matcher.find()) {
            global_bin = matcher.group(1).substring(4, 10);
            searchBinForPointsAndMonths(global_bin, true, global_tlv);
        } else if (matcher2.find()) {
            global_bin = matcher2.group(1).substring(4, 10);
            searchBinForPointsAndMonths(global_bin, true, global_tlv);
        } else {
            emvPayment(getBaseContext());
        }

//        String content = getString(R.string.request_data_to_server) + "\n";
//        Hashtable<String, String> decodeData = BBDeviceController.decodeTlv(tlv);
//        Object[] keys = decodeData.keySet().toArray();
//        Arrays.sort(keys);
//        for (Object key : keys) {
//            String value = decodeData.get(key);
//            content += key + ": " + value + "\n";
//        }
    }



    //Setting Variables
    public String getAmount() {
        return amount;
    }
    public void setAmount(String amount) {
        this.amount = amount;
    }
    public String getConcept() {
        return concept;
    }
    public void setConcept(String concept) {
        this.concept = concept;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getCvv() {
        return cvv;
    }
    public void setCvv(String cvv) {
        this.cvv = cvv;
        if (cvv.length()==3 || cvv.length()==4){
            if(TextUtils.isDigitsOnly(cvv)){
                searchBinForPointsAndMonths(global_bin, false, "");
            } else {
                flap_responses.didFinishPayment(false, "", "", "", "El CVV debe ser númerico de 3 o 4 caracteres.");
            }
        } else {
            flap_responses.didFinishPayment(false, "", "", "", "El CVV debe ser númerico de 3 o 4 caracteres.");
        }
    }
    public String getOrden() {
        return orden;
    }
    public void setOrden(String orden) {
        this.orden = orden;
    }
    public String getServicio() {
        return servicio;
    }
    public void setServicio(String servicio) {
        this.servicio = servicio;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public String getMsi() {
        return msi;
    }
    public void setMsi(String msi) {
        this.msi = msi;
    }
    public String getEnv() {
        return env;
    }
    public void setEnv(String ambiente) {
        this.env = ambiente;
    }
    public String getLatitud() {
        return latitud;
    }
    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }
    public String getLongitud() {
        return longitud;
    }
    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }
    public String getVersion_app() {
        return version_app;
    }
    public void setVersion_app(String version_app) {
        this.version_app = version_app;
    }
    public boolean isPoints() {
        return points;
    }
    public void setPoints(boolean points) {
        this.points = points;
    }
    public String getDevice() {
        return device;
    }
    public void setDevice(String device) {
        this.device = device;
    }
    public String getMoneda() {
        return moneda;
    }
    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }
    public boolean getFallback(){
        return fallback;
    }


    //LISTENERS
    @Override
    void onPclServiceConnected() {
        Log.d(TAG, "onPclServiceConnected");
        mPclService.addDynamicBridgeLocal(6000, 0);

        if (isCompanionConnected()){
            flap_responses.isDeviceConnected(true);
        } else {
            flap_responses.isDeviceConnected(false);
        }
    }


    @Override
    public void onBarCodeReceived(String barCodeValue, int symbology) {

    }

    @Override
    public void onBarCodeClosed() {

    }


    public boolean isCompanionConnected()
    {
        boolean bRet = false;
        if (mPclService != null)
        {
            byte result[] = new byte[1];
            {
                if (mPclService.serverStatus(result) == true)
                {
                    if (result[0] == 0x10)
                        bRet = true;
                }
            }
        }
        return bRet;
    }

    class GetTermInfoTask extends AsyncTask<Void, Void, Boolean> {
        protected Boolean doInBackground(Void... tmp) {
            Boolean bRet = getTermInfo();
            return bRet;
        }

        protected void onPostExecute(Boolean result) {
            if (result == true)
            {
            }
        }
    }

    @Override
    public void onStateChanged(String state) {
        if (state.equals("CONNECTED"))
        {
            new GetTermInfoTask().execute();
            flap_responses.isDeviceConnected(true);
        }
        else
        {
            flap_responses.isDeviceConnected(false);
        }
    }


    private void startPclService() {
        if (!mServiceStarted)
        {
            Log.d("ZERTUCHE","STARTPCLSERVICE");
            SharedPreferences settings = getSharedPreferences("PCLSERVICE", MODE_PRIVATE);
            boolean enableLog = settings.getBoolean("ENABLE_LOG", false);
            Intent i = new Intent(this, PclService.class);
            i.putExtra("PACKAGE_NAME", "mx.digitalcoaster.bbva_ingenico");
            i.putExtra("FILE_NAME", "pairing_addr.txt");
            i.putExtra("ENABLE_LOG", enableLog);
            if (getApplicationContext().startService(i) != null)
                mServiceStarted = true;
        }
    }

    private void stopPclService() {
        if (mServiceStarted)
        {
            Intent i = new Intent(this, PclService.class);
            if (getApplicationContext().stopService(i))
                mServiceStarted = false;
        }
    }

    void refreshTerminalsList(String string) {

        Log.d(TAG, "refreshTerminalsList: " + string);
        boolean bFound = false;
        Set<PclUtilities.BluetoothCompanion> btComps = mPclUtil.GetPairedCompanions();
        List<String> devices = new ArrayList<>();
        int i = 0;
        if (btComps != null && (btComps.size() > 0)) {
            Log.d(TAG, "refreshTerminalsList:  if true");
            // Loop through paired devices
            for (PclUtilities.BluetoothCompanion comp : btComps) {
                Log.d(TAG, "refreshTerminalsList:  if true for comp:"+btComps.size());
                Log.d(TAG, comp.getBluetoothDevice().getAddress());
                String deviceName = comp.getBluetoothDevice().getAddress() + " - " + comp.getBluetoothDevice().getName();

                if (comp.isActivated()) {
                    Log.d(TAG, "refreshTerminalsList:  if true comp");
                    bFound = true;
                    mCurrentDevice = comp.getBluetoothDevice().getAddress() + " - " + comp.getBluetoothDevice().getName();
                }
                else {
                }
                devices.add(deviceName);
                i++;
            }
        }

        if (devices.size()>0)  flap_responses.scanResult(true, devices,"");
        else flap_responses.scanResult(false, null,"No devices found");

        Log.d(TAG, "END refreshTerminalsList");
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device != null) {
                        if (PclUtilities.isIngenicoUsbDevice(device)) {
                            refreshTerminalsList("deviceAttached");
                        }
                    }
                }
            }
        }
    };


    protected void initService()
    {
        if (!mBound)
        {
            Log.d(TAG, "initService" );
            SharedPreferences settings = getSharedPreferences("PCLSERVICE", MODE_PRIVATE);
            boolean enableLog = settings.getBoolean("ENABLE_LOG", false);
            mServiceConnection = new CommonActivity.PclServiceConnection();
            Intent intent = new Intent(this, PclService.class);
            intent.putExtra("PACKAGE_NAME", "com.ingenico.pcltestappwithlib");
            intent.putExtra("FILE_NAME", "pairing_addr.txt");
            intent.putExtra("ENABLE_LOG", enableLog);
            mBound = bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }


    class NetworkTask extends AsyncTask<Void, Integer, Boolean> {
        private InputStream mmTcpInStream;
        private OutputStream mmTcpOutStream;
        private Socket mmTcpSocket;
        public ServerSocket mmTcpServerSocket;
        public boolean mmRunning;
        private int mPort;
        private int mTailleBuf;
        private int mLoopsNb;
        boolean ismAndroidServer;
        private long time_elapsed = 0;
        public String strResult;
        private int mCount;
        byte[] buffer2 = new byte[1024 * 2];
        int total;
        int bytes;

        NetworkTask(int port, int tailleBuf, int loopsNb, boolean isAndroidServer) {
            mmTcpSocket = null;
            mmTcpServerSocket = null;
            mmTcpInStream = null;
            mmTcpOutStream = null;
            mPort = port;
            mTailleBuf = tailleBuf;
            mLoopsNb = loopsNb;
            ismAndroidServer = isAndroidServer;
            mCount = 0;
        }

        public String bytesToHexString(byte[] bytes) {
            StringBuilder sb = new StringBuilder();
            String temp = new String(bytes);
            for (byte b : bytes) {
                sb.append(String.format("%02x ", b & 0xff));
            }
            return sb.toString();
        }

        protected Boolean doInBackground(Void... dummy) {
            if (android.os.Debug.isDebuggerConnected())
                android.os.Debug.waitForDebugger();
            Log.d(TAG, "TcpThread: Started");

            int iIteraciones = 0;
            int index = 0;
            //Ingenico
//		byte[] buffer=new byte[]{0x44,0x53,0x50,0x00,0x26,0x4D,0x44,0x53,0x30,0x33,0x32,0x4E,0x4F,0x4D,0x42,0x45,0x20,0x44,0x45,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x43,0x4F,0x4D,0x45,0x52,0x43,0x49,0x4F,0x20,0x20,0x20,0x20,0x20,0x20,0x20,0x20};
//		byte[] GetCardbuffer=new byte[]{0x42,0x49,0x4E,0x00,0x0B,0x4D,0x49,0x44,0x30,0x30,0x35,0x31,0x31,0x30,0x30,0x30};

            //Banamex
            //	byte[] buffer = new byte[mTailleBuf];  // buffer store for the stream
            //	byte[] GetCardbuffer=new byte[]{0x43,0x43,0x30,0x30,0x39,0x30};

            //Bancomer
            String mount;
            int mont_int;
            byte[] buffer;
            byte[] GetCardbuffer;
            buffer = new byte[]{
                    0x5A, 0x32, 0x49, 0x6e, 0x67, 0x65, 0x6e,
                    0x69, 0x63, 0x6f
            };
            GetCardbuffer = new byte[]{0x43, 0x35, 0x31, 0x00, 0x4C,
                    (byte) 0xC1, 0x01, 0x30,
                    (byte) 0xC1, 0x03, 0x18, 0x04, 0x20,
                    (byte) 0xC1, 0x03, 0x14, 0x25, 0x57,
                    (byte) 0xC1, 0x01, 0x07,
                    (byte) 0xC1, 0x04};
            mount = amount;
            mount = mount.replace("$", "");
            float mont_f = Float.parseFloat(mount);

            //mount=Integer.toHexString(mont_int);
            mont_f = mont_f * 100;
            mont_int = Math.round(mont_f);
            byte[] mont_byte = ByteBuffer.allocate(4).putInt(mont_int).array();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//		byte[] GetCardbuffer=new byte[]{0x42,0x49,0x4E,0x00,0x0B,0x4D,0x49,0x44,0x30,0x30,0x35,0x31,0x31,0x30,0x30,0x30};

            byte[] c = new byte[]{(byte) 0xC1, 0x04, 0x00, 0x00, 0x00, 0x00, (byte) 0xC1, 0x02, 0x04, (byte) 0x84
                    , (byte) 0xC1, 0x01, 0x00, (byte) 0xE1, 0x27, 0x5F, 0x2A, (byte) 0x82, (byte) 0x84, (byte) 0x95, (byte) 0x9A, (byte) 0x9C
                    , (byte) 0x9F, 0x02, (byte) 0x9F, 0x03, (byte) 0x9F, 0x09, (byte) 0x9F, 0x10, (byte) 0x9F, 0x1A, (byte) 0x9F, 0x1E
                    , (byte) 0x9F, 0x26, (byte) 0x9F, 0x27, (byte) 0x9F, 0x33, (byte) 0x9F, 0x34, (byte) 0x9F, 0x35, (byte) 0x9F, 0x36
                    , (byte) 0x9F, 0x37, (byte) 0x9F, 0x41, (byte) 0x9F, 0x53, (byte) 0x9F, 0x6E};
            try {
                outputStream.write(GetCardbuffer);
                outputStream.write(mont_byte);
                outputStream.write(c);

            } catch (IOException e) {
                e.printStackTrace();
            }
            GetCardbuffer = outputStream.toByteArray();
//            }else {
//                GetCardbuffer = new byte[]{0x43, 0x35, 0x31, 0x00, 0x4F,
//                        (byte) 0xC1, 0x01, 0x45,
//                        (byte) 0xC1, 0x03, 0x18, 0x04, 0x20,
//                        (byte) 0xC1, 0x03, 0x14, 0x25, 0x57,
//                        (byte) 0xC1, 0x01, 0x07,
//                        (byte) 0xC1, 0x04, 0x00,0x00,0x00,0x01,
//                        (byte)0xC1, 0x04, 0x00, 0x00, 0x00, 0x00, (byte)0xC1, 0x02, 0x04, (byte)0x84,(byte) 0xC1, 0x01, 0x00,(byte) 0xE1, 0x2A,(byte) 0x9F,
//				0x39, 0x5F, 0x34,(byte) 0x8A, 0x5F, 0x2A, (byte)0x82, (byte)0x84, (byte)0x95, (byte)0x9A, (byte)0x9C, (byte)0x9F, 0x02, (byte)0x9F, 0x03, (byte)0x9F,
//				0x09, (byte)0x9F, 0x10, (byte)0x9F, 0x1A, (byte)0x9F, 0x1E, (byte)0x9F, 0x26, (byte)0x9F, 0x27, (byte)0x9F, 0x33, (byte)0x9F, 0x34, (byte)0x9F,
//				0x35, (byte)0x9F, 0x36, (byte)0x9F, 0x37, (byte)0x9F, 0x41, (byte)0x9F, 0x53
//
//                };


//				(byte)0xC1, 0x04, 0x00, 0x00, 0x00, 0x00, (byte)0xC1, 0x02, 0x04, (byte)0x84,(byte) 0xC1, 0x01, 0x00,(byte) 0xE1, 0x2A,(byte) 0x9F,
//				0x39, 0x5F, 0x34,(byte) 0x8A, 0x5F, 0x2A, (byte)0x82, (byte)0x84, (byte)0x95, (byte)0x9A, (byte)0x9C, (byte)0x9F, 0x02, (byte)0x9F, 0x03, (byte)0x9F,
//				0x09, (byte)0x9F, 0x10, (byte)0x9F, 0x1A, (byte)0x9F, 0x1E, (byte)0x9F, 0x26, (byte)0x9F, 0x27, (byte)0x9F, 0x33, (byte)0x9F, 0x34, (byte)0x9F,
//				0x35, (byte)0x9F, 0x36, (byte)0x9F, 0x37, (byte)0x9F, 0x41, (byte)0x9F, 0x53};


            //MIT
            //C50 DISPLAY
//		byte[] buffer=new byte[]{0x30, 0x33, 0x35, 0x43, 0x35, 0x30, 0x41, 0x4D, 0x45, 0x4E, 0x53,
//				0x41, 0x4A, 0x45, 0x20, 0x68 ,0x6f ,0x6c ,0x61,0x21 ,0x21, 0x20, 0x20,
//				0x4C, 0x49, 0x4E, 0x45, 0x41, 0x32, 0x20, 0x64, 0x65, 0x20, 0x74, 0x65,
//				0x78, 0x74, 0x6F};
            //C93 INICIA TRANSACCION
//		byte[] GetCardbuffer=new byte[]{
//				0x30, 0x37, 0x37, 0x43, 0x39, 0x33, 0x41, 0x49, 0x4E, 0x53, 0x45,
//				0x52, 0x54, 0x45, 0x20, 0x43, 0x48, 0x49, 0x50, 0x20, 0x4F, 0x20, 0x20,
//				0x44, 0x45, 0x53, 0x4C, 0x49, 0x43, 0x45, 0x20, 0x54, 0x41, 0x52, 0x4A,
//				0x45, 0x54, 0x41, 0x1C, 0x42, 0x32, 0x37, 0x30, 0x34, 0x31, 0x35, 0x1C,
//				0x43, 0x31, 0x30, 0x32, 0x37, 0x1C, 0x44, 0x31, 0x2E, 0x30, 0x30, 0x1C,
//				0x45, 0x30, 0x2E, 0x30, 0x30, 0x1C, 0x46, 0x30, 0x34, 0x38, 0x34, 0x1C,
//				0x47, 0x33, 0x30, 0x1C, 0x48, 0x54, 0x41, 0x47, 0x53, 0x03, 0x45
//		};
            //C93 INICIA TRANSACCION CLESS
//		byte[] GetCardbuffer=new byte[]{
//			0x30, 0x38, 0x33, 0x43, 0x39, 0x33, 0x41, 0x41, 0x50, 0x52, 0x4F, 0x58,
//			0x49, 0x4D, 0x45, 0x20, 0x49, 0x4E, 0x53, 0x45, 0x52, 0x54, 0x45, 0x20,
//			0x4F, 0x20, 0x44, 0x45, 0x53, 0x4C, 0x49, 0x43, 0x45, 0x20, 0x54, 0x41,
//			0x52, 0x4A, 0x45, 0x54, 0x41, 0x1C, 0x42, 0x31, 0x30, 0x30, 0x39, 0x31,
//			0x37, 0x1C, 0x43, 0x31, 0x30, 0x32, 0x37, 0x1C, 0x44, 0x31, 0x2E, 0x30,
//			0x30, 0x1C, 0x45, 0x30, 0x2E, 0x30, 0x30, 0x1C, 0x46, 0x30, 0x34, 0x38,
//			0x34, 0x1C, 0x47, 0x33, 0x30, 0x1C, 0x48, 0x54, 0x41, 0x47, 0x53, 0x1C,
//			0x4B, 0x31
//		};
//        byte[] GetCardbuffer=new byte[]{
//				0x30, 0x38, 0x33, 0x43, 0x39, 0x33, 0x41, 0x41, 0x50, 0x52, 0x4F, 0x58,
//				0x49, 0x4D, 0x45, 0x20, 0x49, 0x4E, 0x53, 0x45, 0x52, 0x54, 0x45, 0x20,
//				0x4F, 0x20, 0x44, 0x45, 0x53, 0x4C, 0x49, 0x43, 0x45, 0x20, 0x54, 0x41,
//				0x52, 0x4A, 0x45, 0x54, 0x41, 0x1C, 0x42, 0x31, 0x30, 0x30, 0x39, 0x31,
//				0x37, 0x1C, 0x43, 0x31, 0x30, 0x32, 0x37, 0x1C, 0x44, 0x31, 0x2E, 0x30,
//				0x30, 0x1C, 0x45, 0x30, 0x2E, 0x30, 0x30, 0x1C, 0x46, 0x30, 0x34, 0x38,
//				0x34, 0x1C, 0x47, 0x33, 0x30, 0x1C, 0x48, 0x54, 0x41, 0x47, 0x53, 0x1C,
//				0x4B, 0x31
//            };



		/* Sin título2 (04/12/2017 01:14:06 p.m.)
		   Posición Inicial: 00000000, Posición Final: 0000000E, Longitud: 0000000F */

//		byte[] Approvalbuffer=new byte[]{
//			0x30, 0x31, 0x32, 0x43, 0x39, 0x33, 0x41, 0x30, 0x30, 0x1C, 0x42, 0x1C,
//			0x43, 0x1C, 0x44
//		};
            byte[] Approvalbuffer = new byte[]{
                    0x43, 0x35, 0x34, 0x00, 0x2A, (byte) 0xC1, 0x01, 0x00, (byte) 0xC1, 0x06, 0x31, 0x31, 0x31, 0x32, 0x32, 0x32, (byte) 0xC1,
                    0x02, 0x30, 0x30, (byte) 0x91, 0x00, (byte) 0xC1, 0x03, 0x18, 0x04, 0x30, (byte) 0xC1, 0x03, 0x12, 0x40, 0x21, (byte) 0xE2, 0x0D, (byte) 0x9F,
                    0x26, (byte) 0x9F, 0x27, (byte) 0x9F, 0x36, (byte) 0x95, (byte) 0x9F, 0x10, (byte) 0x9F, 0x37, (byte) 0x9B, (byte) 0x8A
            };

            //			byte[] buffer3=new byte[1024];
            //mtvResult = (EditText)findViewById(R.id.tvResult);


            if (mTailleBuf == 1) {
                buffer = Arrays.copyOf(GetCardbuffer, GetCardbuffer.length);

            } else if (mTailleBuf == 2) {
                buffer = Arrays.copyOf(Approvalbuffer, Approvalbuffer.length);
            }
            //Log.d("DIGITALCOASTER",bytesToHexString(GetCardbuffer));

//			for (int i=0; i<mTailleBuf; i++)
//			{
//				buffer[i] = (byte) i;
//			}

            mmRunning = true;

            try {
                if (ismAndroidServer) {
                    Log.d(TAG, "TcpThread: Waiting for connection...");
                    mmTcpServerSocket = new ServerSocket(mPort);
                } else {
                    Log.d(TAG, "TcpThread: Connecting...");
                    mmTcpSocket = new Socket("127.0.0.1", mPort);
                    mmTcpSocket.setTcpNoDelay(true);
                    mmTcpSocket.setSoTimeout(3000);
                }


            } catch (UnknownHostException e1) {
                e1.printStackTrace();
                Log.e(TAG, "TcpThread: UnknownHostException");
                return Boolean.FALSE;
            } catch (IOException e1) {
                e1.printStackTrace();
                Log.e(TAG, "TcpThread: IOException");
                return Boolean.FALSE;
            }

            if (ismAndroidServer) {
                try {
                    mmTcpSocket = mmTcpServerSocket.accept();
                } catch (IOException e2) {
                    e2.printStackTrace();
                    try {
                        mmTcpServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (mmTcpSocket != null) {

                Log.d(TAG, "TcpThread: Connected");
                try {
                    mmTcpInStream = mmTcpSocket.getInputStream();
                    mmTcpOutStream = mmTcpSocket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "TcpThread: IOException");
                    try {
                        mmTcpSocket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    return Boolean.FALSE;
                }


                if (mLoopsNb > 0) {
                    long start_time = System.currentTimeMillis();
                    Log.d(TAG, String.format("TcpThread: Start sending %d", buffer.length));
                    mCount = 0;
                    while (mmRunning) {
                        if (isCancelled()) {
                            Log.d(TAG, String.format("TcpThread: Cancelled"));
                            closeStreams();
                            break;
                        }
                        if (mmTcpOutStream != null) {
                            try {
                                //Log.d(TAG, "TcpThread: Write");
                                mmTcpOutStream.write(buffer, 0, buffer.length);
                            } catch (IOException e1) {
                                e1.printStackTrace();
                                Log.e(TAG, "TcpThread: Write IOException");
                                closeStreams();
                                break;
                            }
                        }

                        if (mmTcpInStream != null) {
                            total = 0;
                            bytes = 0;
                            strCardNumber = "";
//							total = 1;
//								while (total>0)
//							iIteraciones=0;
//							Handler handler = new Handler();
//							while(iIteraciones<6 && bytes>=0) {
//							total = 0;
                            while (total < 1024) {
                                try {

                                    bytes += mmTcpInStream.read(buffer2, bytes, buffer2.length - bytes);
//									bytes= mmTcpInStream.read(buffer2);


                                    //buffer2.toString());
                                    if (bytes == -1) {    //end of the stream
                                        Log.e(TAG, "TcpThread: End of stream");
//										Thread.sleep (10000);
                                        closeStreams();
                                        break;

                                    } else {
                                        total = bytes;
                                        if (total > 0) {
//									try {
                                            //Log.d(TAG, "TcpThread: Write");
                                            //ARCN
//										mmTcpOutStream.write(Approvalbuffer , 0, Approvalbuffer .length);

//									} catch (IOException e1) {
//										e1.printStackTrace();
//										Log.e(TAG, "TcpThread: Write IOException");
                                            closeStreams();
                                            break;

                                        }
                                    }
                                } catch (IOException e) {
                                    Log.e(TAG, "TcpThread: IOException(read)");
                                    e.printStackTrace();
                                    if (iIteraciones < 5) {
                                        iIteraciones++;
                                        continue;

                                    }
                                    break;
                                }
                            }

//							strCardNumber.concat(buffer2.toString());
//
//concat(buffer2, buffer3);

//							for(byte bt:buffer3) {
//								buffer2[index+(iIteraciones*1024)]=bt;
//								index++;
//
//							}
//							index=0;
//
//
//							iIteraciones++;
//							}

                            mmRunning = false;
                            strCardNumber = new String(buffer2);

//							strCardNumber=strCardNumber.subSequence(20,26).toString() + "******" +strCardNumber.subSequence(32,36).toString();
//							strCardNumber=strCardNumber.subSequence(20,26).toString() + "******" +strCardNumber.subSequence(32,36).toString();
                            //Banamex
                            //strCardNumber=strCardNumber.subSequence(05,11).toString() + "******" +strCardNumber.subSequence(17,21).toString();
                            strResult = strCardNumber;
                            this.publishProgress(0);
                            //substring(20,18);
                            if (total == mTailleBuf)
                                mCount++;
                        } else {
                            break;
                        }

                        if (mCount % 10 == 0)
                            publishProgress(mCount);
                        if (mCount >= mLoopsNb)
                            break;
                    }
                    Log.d(TAG, String.format("TcpThread: Stop sending (count=%d)", mCount));
                    long stop_time = System.currentTimeMillis();
                    time_elapsed = stop_time - start_time;
                    Log.d(TAG, String.format("START = %d | STOP = %d | TIME = %d", start_time, stop_time, time_elapsed));
                    //mtvStatus.setText("OK\nEND OF TEST\n\n" );
                    Log.d(TAG, String.format("Bandwith = %d bits/second", (8 * ((1000 * mTailleBuf * mCount * 2) / time_elapsed))));
                }
            }

            if (ismAndroidServer) {
                // wait for other side to close connection
                if (mmTcpInStream != null) {

                    while (true) {
                        try {
                            bytes = mmTcpInStream.read(buffer);
                            if (bytes == -1) {    //end of the stream
                                Log.e(TAG, "TcpThread: End of stream");
                                closeStreams();
                                break;
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "TcpThread: IOException(read)");
                            e.printStackTrace();
                            closeStreams();
                            break;
                        }
                    }
                }

                try {
                    mmTcpServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                closeStreams();
            }

            Log.d(TAG, "TcpThread: Ended Tailed Buff:"+mTailleBuf);
            Log.d(TAG, "TcpThread: Ended");

            // IF 1, es la lectura
            if (mTailleBuf == 1) {
                Log.d("ZERTUCHE", "TLV:" + bytesToHexString(buffer2));

                //RootDecoder.decode( bytesToHexString(buffer2));
                Log.d("ZERTUCHE", "CARD:" + bytesToHexString(buffer2).replace(" ", "").substring(18, 24));
                Log.d("ZERTUCHE", "METHOD:" + bytesToHexString(buffer2).replace(" ", "").substring(206,212));
                String method = bytesToHexString(buffer2).replace(" ", "").substring(206,212) ;
                Log.d("pinRequest", "pinRequest: "+ method);

                if(method.equals("410302")) {
                    Log.d("pinRequest", "pinRequest");
                    flap_responses.pinRequest();
                }
                //Webservice a meses sin intereses
                //searchBinForPointsAndMonths(bytesToHexString(buffer2).replace(" ", "").substring(18, 24),true, bytesToHexString(buffer2));
                //HAPPY PATH
                authorizePayment();


                // IF 2, approved
            } else if (mTailleBuf == 2) {
                Log.d("ZERTUCHE", "Approved");
                //authorizePayment();
                didAuthorize();
                //
            } else {
                Log.d("ZERTUCHE", "Error");
            }


            return Boolean.TRUE;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //mtvStatus.setText(String.format("TEST IN PROGRESS...\nPackets: %d", values[0]));
            String hex = bytesToHexString(buffer2);
//            bytes=strResult.length();
////			super.onProgressUpdate(values);
            bytes = 3 * total;
            hex = new String(hex.substring(0, bytes));
            bytes = 0;
            //mtvResult.setText(hex.toUpperCase());
        }


        protected void onPostExecute(Boolean result) {
//			if (result == true && mCount == mLoopsNb)
//			{
//				strResult = "OK";
//				if (time_elapsed != 0)
//					mtvResult.setText(String.format("Transfer rate = %d kbits/second", (8*mTailleBuf*mCount*2)/time_elapsed));
//			}
//			else
//			{
//				strResult = "KO";
//			}


//			mtvStatus.setText(strResult + "\nEND OF TEST");
        }

        private void closeStreams() {
            if (mmTcpInStream != null) {
                try {
                    mmTcpInStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                mmTcpInStream = null;
            }
            if (mmTcpOutStream != null) {
                synchronized (mmTcpOutStream) {
                    try {
                        mmTcpOutStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mmTcpOutStream = null;
                }
            }
            if (mmTcpSocket != null) {
                try {
                    mmTcpSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mmTcpSocket = null;
            }
        }

    }


}
