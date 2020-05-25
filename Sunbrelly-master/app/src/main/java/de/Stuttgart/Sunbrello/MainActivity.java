package de.Stuttgart.Sunbrello;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor light;
    private float referenceBrightness;
    private boolean isReferenceInitialized = false;
    private int signal = 0;
    private final int maxSignal = 50;
    private final int minSignal = -50;
    private final int sensitivity = 100; //how many lux the light needs to increase to start the motor
    TextView brightnessTextView;
    TextView signalTextView;
    TextView stopStart;
    private float currentLux;
    private boolean hasLightSensor = false;
    private TextView serverSignal;
    private TextView antwortESP;
    OkHttpClient postClient = new OkHttpClient();
    Boolean isSearchingShadow = false;
    Boolean isStopped = false;
    private String ipAddress = "192.168.4.255";
    private TextInputLayout textInputIp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Sunbrello App");
        brightnessTextView = (TextView) findViewById(R.id.lightSignal);
        signalTextView = (TextView) findViewById(R.id.electricitySignal);
        serverSignal = (TextView) findViewById(R.id.serverSignal);
        stopStart = (TextView) findViewById(R.id.stop);
        antwortESP = (TextView) findViewById(R.id.antwortESP);
        textInputIp = findViewById(R.id.ipAddress);

        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        checkIfLightSensorIsAvailable();

        sendMotorSignal(signal);
    }

    public void startUdpClient(View view) {
        //UDP Client erstellen
        EchoClient client = new EchoClient(ipAddress);
        Toast.makeText(MainActivity.this, "Nachricht an ESP: " + signal + "wird versucht zu senden.", Toast.LENGTH_SHORT).show();
        System.out.println("send message: " + signal);
        client.sendEcho("" + signal);
        client.close();
    }


    private void checkIfLightSensorIsAvailable() {
        //get list of supported Sensors
        List<Sensor> list = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor sensor : list) {
            if (sensor.getName().contains("Light") || sensor.getName().contains("light")) {
                // maxLux = light.getMaximumRange();
                hasLightSensor = true;
                Log.i("light sensor", "exists!");

                //Log.i("light sensor version", Integer.toString(sensor.getVersion()));
            }
            Log.i("sensor", sensor.getName() + sensor.toString());
        }
        //error message, if no light sensor was found
        if (!hasLightSensor) {
            Log.i("info", "Mensch Kerle, es konnte kein Lichtsensor gefunden werden...");
            for (Sensor sensor : list) {
                Log.i("sensor", sensor.getName() + sensor.toString());
            }
        }
        Log.i("light sensor", "" + hasLightSensor);

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        sendMsgToServer("User closed App");
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL);
        sendMsgToServer("User opened App");
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        if (!isStopped) {
            if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                currentLux = event.values[0];
                brightnessTextView.setText(Float.toString(currentLux));
                if (!isReferenceInitialized) {
                    referenceBrightness = currentLux;
                    isReferenceInitialized = true;

                }
                if ((currentLux - referenceBrightness) > sensitivity && signal < maxSignal) {
                    signal += 1;
                    signalTextView.setText(Integer.toString(signal));
                    sendMotorSignal(signal);
                    isSearchingShadow = true;

                }
                if (((currentLux - referenceBrightness) <= sensitivity) && isSearchingShadow) {
                    signal = 0;
                    signalTextView.setText(Integer.toString(signal));
                    sendMotorSignal(signal);
                    isSearchingShadow = false;

                }

            }
        }

    }

    public void clickLeft(View view) {
        if (signal >= (minSignal + 10)) {
            Log.i("info", "Button links wurde geklickt!");
            signal -= 10;
            signalTextView.setText(Integer.toString(signal));
        }
        sendMotorSignal(signal);
    }

    public void clickRight(View view) {
        if (signal <= (maxSignal - 10)) {
            Log.i("info", "Button rechts wurde geklickt!");
            signal += 10;
            signalTextView.setText(Integer.toString(signal));
        }
        sendMotorSignal(signal);
    }

    public void sendMotorSignal(int signal) {
        Log.d("OKHTTP", "Post signal function called");
        String url = "https://shielded-everglades-18448.herokuapp.com/postSignal"; // connection to Java server
        MediaType JSON = MediaType.parse("application/json;charset=utf-8");
        JSONObject actualdata = new JSONObject();
        try {
            actualdata.put("signal", Integer.toString(signal));
        } catch (JSONException e) {
            Log.d("OKHHTP", "JSON Exception");
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(JSON, actualdata.toString());
        Log.d("OKHTTP", "RequestBody created");
        Request newReq = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Log.d("OKHTTP", "Request build");

        postClient.newCall(newReq).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("OKHTTP", "FAILED");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String newRes = response.body().string();
                    Log.d("OKHTTP", "onResponse() called");
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            serverSignal.setText(newRes);
                            Log.d("OKHTTP", "Request done, got the response");
                            Log.d("OKHTTP", newRes);
                        }
                    });
                }
            }

        });
    }

    public void sendMsgwithUDPdirectlyToESP8266(String message) {
//        //Set message
//        client.setMessage(message);
//        //Send message
//        client.NachrichtSenden();
    }

    public void sendMsgToServer(String msg) {

        Log.d("OKHTTP", "Post message function called");
        String url = "https://shielded-everglades-18448.herokuapp.com/postMsg";
        MediaType JSON = MediaType.parse("application/json;charset=utf-8");
        JSONObject actualdata = new JSONObject();
        try {
            actualdata.put("msg", msg);
        } catch (JSONException e) {
            Log.d("OKHHTP", "JSON Exception");
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(JSON, actualdata.toString());
        Log.d("OKHTTP", "RequestBody created");
        Request newReq = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Log.d("OKHTTP", "Request build");

        postClient.newCall(newReq).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("OKHTTP", "FAILED");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String newRes = response.body().string();
                    Log.d("OKHTTP", "onResponse() called");
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("OKHTTP", "Request done, got the response");
                            Log.d("OKHTTP", newRes);
                        }
                    });
                }
            }

        });
    }

    public void setClicked(View view) {
        referenceBrightness = currentLux;
        isReferenceInitialized = true;
        Toast.makeText(MainActivity.this, "Die derzeitige Helligkeit wurde als Refernzwert gesetzt.", Toast.LENGTH_SHORT).show();
    }

    public void stopClicked(View view) {
        if (!isStopped) {
            isStopped = true;
            stopStart.setText("Start");
            signal = 0;
            signalTextView.setText(Integer.toString(signal));
            sendMotorSignal(signal);
            Toast.makeText(MainActivity.this, "Lichtsteuerung ausgeschaltet.", Toast.LENGTH_SHORT).show();
        } else {
            isStopped = false;
            stopStart.setText("Stop");
            signal = 0;
            signalTextView.setText(Integer.toString(signal));
            sendMotorSignal(signal);
            Toast.makeText(MainActivity.this, "Lichtsteuerung eingeschaltet.", Toast.LENGTH_SHORT).show();
        }

    }

    public void confirmInput(View v) {
        if (!validateInput()) {
            return;
        }
        textInputIp.setHint(ipAddress);
    }

    public boolean validateInput() {
        ipAddress = textInputIp.getEditText().getText().toString().trim();
        if (ipAddress.isEmpty()) {
            textInputIp.setError("Ip Adresse darf nicht leer sein...");
            return false;
        } else if (ipAddress.length() > 15) {
            textInputIp.setError("Ip Adresse zu lang...");
            return false;
        } else if (ipAddress.length() < 7) {
            textInputIp.setError("Ip Adresse zu kurz...");
            return false;
        } else {
            textInputIp.setError(null);
            textInputIp.setErrorEnabled(false);
            return true;
        }
    }


    //Http get request
//    public void getRequest() throws MalformedURLException {
//        URL url = new URL("https://jsonplaceholder.typicode.com/posts/1");
//        HttpURLConnection client = null;
//        try {
//            client = (HttpURLConnection) url.openConnection();
//            client.setRequestMethod("POST");
//            client.setRequestProperty("Key", "Value");
//            client.setDoOutput(true);
//            OutputStream outputPost = new BufferedOutputStream(client.getOutputStream());
//            // writeStream(outputPost);
//            outputPost.flush();
//            outputPost.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.i("fail", "Get request failed");
//        }  finally {
//            if(client != null) // Stelle sicher, dass die Verbindung nicht null ist.
//                client.disconnect();
//        }
//    }

//    //Image download
//    public void downloadImage(View view) {
//        ImageDownloader task = new ImageDownloader();
//        Bitmap myImage;
//        try {
//            myImage = task.execute("https://upload.wikimedia.org/wikipedia/en/0/02/Homer_Simpson_2006.png").get();
//            Log.i("info", "Bild geladen");
//            imageView.setImageBitmap(myImage);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
//
//        @Override
//        protected Bitmap doInBackground(String... urls) {
//
//            try {
//                URL url = new URL(urls[0]);
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.connect();
//                InputStream inputStream = connection.getInputStream();
//                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);
//
//                return myBitmap;
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//    }
}
