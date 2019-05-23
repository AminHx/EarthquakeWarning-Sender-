package com.aminhx.earthquakewarning;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    //public static final String BASE_URL = "http://www.erqkermanshah.ir:80/MySite/";
    private GoogleMap mMap;
    private static final int PERMISSION_REQUEST_CODE = 1234;
    private TextView lbl_coordinate;
    private EditText txt_sWave, txt_magnitude;
    private Button btn_submit;

    public String txtCoordinate;
    public String txtSWave;
    public String txtMagnitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        checkPermissions();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        lbl_coordinate = (TextView) findViewById(R.id.lbl_coordinate);
        txt_sWave = (EditText) findViewById(R.id.txt_sWave);
        txt_magnitude = (EditText) findViewById(R.id.txt_magnitude);
        btn_submit = (Button) findViewById(R.id.btn_submit);


    }



    private void checkPermissions() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if(requestCode == PERMISSION_REQUEST_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied to access location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;


        final GpsTracker gpstracker = new GpsTracker(this);
        final double lat = gpstracker.getLatitude();
        final double lon = gpstracker.getLongitude();

        // Add a marker in Sydney and move the camera
        final LatLng myLocation = new LatLng(lat, lon);
        if(gpstracker.canGetLocation()){
            Toast.makeText(this,
                    "Your Location \n lat : " + lat + "\n lon : " + lon,
                    Toast.LENGTH_SHORT).show();
        } else {
            gpstracker.showGpsAlertDialog();
        }
        mMap.addMarker(new MarkerOptions().position(myLocation).title("Your Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        mMap.setMinZoomPreference(6.0f);
        mMap.setMaxZoomPreference(14.0f);



        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                MarkerOptions myMarker = new MarkerOptions();
                myMarker.position(latLng);
                myMarker.title(latLng.latitude + " : " + latLng.longitude);
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(myLocation).title("Your Location"));
                mMap.addMarker(myMarker).setIcon(BitmapDescriptorFactory.fromResource(R.raw.earthquake));

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(new MarkerOptions().position(myLocation).getPosition());
                builder.include(new MarkerOptions().position(latLng).getPosition());
                LatLngBounds bounds = builder.build();
                int width = getResources().getDisplayMetrics().widthPixels;
                int height = getResources().getDisplayMetrics().heightPixels;
                int padding = (int) (width * 0.20); // offset from edges of the map 20% of screen
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
                mMap.animateCamera(cu);

                Polygon line = mMap.addPolygon(new PolygonOptions()
                        .add(myLocation, latLng)
                        .strokeColor(0x80800000)
                        .fillColor(Color.BLUE)
                );

                lbl_coordinate.setText(String.format("%.1f", latLng.latitude)+ "-" +String.format("%.1f", latLng.longitude));

            }
        });

        btn_submit.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String co= lbl_coordinate.getText().toString();
                final String wa= txt_sWave.getText().toString();
                final String ma= txt_magnitude.getText().toString();



                Thread background = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HttpClient client = new DefaultHttpClient();

                        String url="http://erqkermanshah.ir/push.php?cord="+co+"&speed="+wa+"&ma="+ma;
                        try {
                            String SetServerString = "";
                            HttpGet httpget = new HttpGet(url);
                            ResponseHandler<String> responsehandler = new BasicResponseHandler();
                            SetServerString = client.execute(httpget, responsehandler);
                            threadMsg(SetServerString);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    private void threadMsg(String msg) {
                        if (!msg.equals(null) && !msg.equals("")) {
                            Bundle b = new Bundle();
                            Message msgObj = handler.obtainMessage();

                            b.putString("message", msg);
                            msgObj.setData(b);
                            handler.sendMessage(msgObj);
                        }
                    }

                    private final Handler handler = new Handler() {

                        @Override
                        public void handleMessage(Message msg) {
                            String aResponse = msg.getData().getString("message");

                            if ((null != aResponse)) {
                                lbl_coordinate.setText(aResponse);
                            } else {
                                lbl_coordinate.setText("Not Got Response From Server.");
                            }
                        }
                    };

                });
                background.start();
            }
        });
    }


    private class myTask extends AsyncTask<myHttpManager.RequestPackage,Void , String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(myHttpManager.RequestPackage... params) {
            myHttpManager.RequestPackage p = params[0];
            String content = myHttpManager.getDataHttpURLConnection(p);
            return content;
        }

        @Override
        protected void onPostExecute(String result) {
            result = result.replace("<br>", "\n");
            lbl_coordinate.setText(result);
            super.onPostExecute(result);
        }
    }

    private void CreatNotfication() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MapsActivity.this);
        mBuilder.setSmallIcon(R.drawable.nn);
        mBuilder.setContentTitle("NoobNoob");
        mBuilder.setContentText("GodDaaamn!");

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, mBuilder.build());
    }

}
