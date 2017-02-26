package com.example.moon.vehicleapp;

import android.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.w3c.dom.Document;

import java.io.InputStream;
import java.util.ArrayList;
import android.os.Handler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Target t;
    protected FirebaseAuth mAuth= FirebaseAuth.getInstance();
    final FirebaseDatabase database1 = FirebaseDatabase.getInstance();
    DatabaseReference ref = database1.getReference("Users/EmergencyVehiclePersonnels/"+mAuth.getCurrentUser().getUid()+"/Target");
    private Location locationGPS;

    Location locationNet;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        googleMap.setMyLocationEnabled(true);
            LocationManager mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);


            locationNet = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            locationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);


        }

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                 Target     t1 = dataSnapshot.getValue(Target.class);
                    setT(t1);
                routePLan();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

public void routePLan(){

    LatLng dest = new LatLng(Double.parseDouble(t.lng), Double.parseDouble(t.lat));
        mMap.addMarker(new
                MarkerOptions().position(dest).title("Victim"));
    mMap.getUiSettings().setMapToolbarEnabled(true);
    LatLng src;
    if(locationGPS!=null)

   src = new LatLng(locationGPS.getLatitude(),locationGPS.getLongitude()
    );
    else

        src = new LatLng(locationNet.getLatitude(),locationNet.getLongitude()
        );
    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(src, 10);
    mMap.animateCamera(cameraUpdate);

    route(src,dest,"driving");


}



    private void setT(Target t) {
       this.t=t;
    }


    protected void route(LatLng sourcePosition, LatLng destPosition, String mode) {
        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                try {
                    Document doc = (Document) msg.obj;
                    GMapV2Direction md = new GMapV2Direction();
                    ArrayList<LatLng> directionPoint = md.getDirection(doc);
                    PolylineOptions rectLine = new PolylineOptions().width(15).color(Color.RED);

                    for (int i = 0; i < directionPoint.size(); i++) {
                        rectLine.add(directionPoint.get(i));
                    }
                    Polyline polylin = mMap.addPolyline(rectLine);
                    md.getDurationText(doc);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        };

        new GMapV2DirectionAsyncTask(handler, sourcePosition, destPosition, GMapV2Direction.MODE_DRIVING).execute();
    }

    public class GMapV2DirectionAsyncTask extends AsyncTask<String, Void, Document> {

        private final  String TAG = GMapV2DirectionAsyncTask.class.getSimpleName();
        private Handler handler;
        private LatLng  start, end;
        private String mode;

        public GMapV2DirectionAsyncTask(Handler handler, LatLng start, LatLng end, String mode) {
            this.start = start;
            this.end = end;
            this.mode = mode;
            this.handler = handler;
        }

        @Override
        protected Document doInBackground(String... params) {

            String url = "http://maps.googleapis.com/maps/api/directions/xml?"
                    + "origin=" + start.latitude + "," + start.longitude
                    + "&destination=" + end.latitude + "," + end.longitude
                    + "&sensor=false&units=metric&mode=" + mode;
            Log.d("url", url);
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpContext localContext = new BasicHttpContext();
                HttpPost httpPost = new HttpPost(url);
                HttpResponse response = httpClient.execute(httpPost, localContext);
                InputStream in = response.getEntity().getContent();
                DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder();
                Document doc = builder.parse(in);
                return doc;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Document result) {
            if (result != null) {
                Log.d(TAG, "---- GMapV2DirectionAsyncTask OK ----");
                Message message = new Message();
                message.obj = result;
                handler.dispatchMessage(message);
            } else {
                Log.d(TAG, "---- GMapV2DirectionAsyncTask ERROR ----");
            }
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }



}
}
