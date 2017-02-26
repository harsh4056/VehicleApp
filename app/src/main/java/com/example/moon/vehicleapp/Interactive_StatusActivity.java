package com.example.moon.vehicleapp;

import android.app.Dialog;
import android.content.Context;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.Visibility;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Interactive_StatusActivity extends AppCompatActivity {

    private static final String TAG = "DialogActivity";
    private static final int DLG_EXAMPLE1 = 0;
    private static final int TEXT_ID = 0;


    protected FirebaseAuth mAuth;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Users");
    final public String EmergencyVehiclePersonnels="EmergencyVehiclePersonnels";
    final FirebaseDatabase database1 = FirebaseDatabase.getInstance();


    // [END declare_auth]

    // [START declare_auth_listener]
    private FirebaseAuth.AuthStateListener mAuthListener;
    Toolbar tb;
    FloatingActionButton btnMap,btnAvail;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interactive__status);

        User evp= new User();
        mAuth = FirebaseAuth.getInstance();
        DatabaseReference ref = database1.getReference("Users/EmergencyVehiclePersonnels/"+mAuth.getCurrentUser().getUid()+"/Target");
       writeNewUser(mAuth.getCurrentUser().getUid(),"Available","0.1","0.1","Harsh","J 1223","9479548395","Amb");
        writeNewTarget(mAuth.getCurrentUser().getUid(),"Ram prasad","26.24847","78.170577","C-137","1");
        tb= (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(tb);


        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

// Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {


                makeUseOfNewLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }


            btnMap=(FloatingActionButton) findViewById(R.id.map);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionOnBusy();
            }
        });
        btnMap.setVisibility(View.INVISIBLE);
          btnAvail = (FloatingActionButton) findViewById(R.id.avail);
        btnAvail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               actionOnAvail();
                mAuth.signOut();
                Interactive_StatusActivity.this.finish();


            }
        });


        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
        Target t= dataSnapshot.getValue(Target.class);
                if(t.toggle.equals("1")){
               emergtencyDiaolougeLaunch(t);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void emergtencyDiaolougeLaunch(Target t) {


        String message=
                "\nName : "+t.name+
                        "\nCentre : "+t.address;


       new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText(message)
                .setCustomImage(R.drawable.pic)

                .setCancelText("REJECT!!")
                .setConfirmText("ACCEPT!")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                    showDialog(DLG_EXAMPLE1);
                        sDialog.cancel();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {

                        btnMap.setVisibility(View.VISIBLE);
                        actionOnBusy();
                        sDialog.cancel();
                    }
                })
                .show();
    }

    private void makeUseOfNewLocation(Location location) {
        Toast.makeText(Interactive_StatusActivity.this,location.getLatitude()+","+location.getLongitude(),Toast.LENGTH_SHORT).show();


        myRef.child(EmergencyVehiclePersonnels).child(mAuth.getCurrentUser().getUid()).child("lng").setValue(location.getLongitude());
        myRef.child(EmergencyVehiclePersonnels).child(mAuth.getCurrentUser().getUid()).child("lat").setValue(location.getLatitude());


    }


    public void actionOnAvail(){



        myRef.child(EmergencyVehiclePersonnels).child(mAuth.getCurrentUser().getUid()).child("status").setValue("Available");


        tb.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

    }
    public void actionOnBusy(){



        myRef.child(EmergencyVehiclePersonnels).child(mAuth.getCurrentUser().getUid()).child("status").setValue("Busy");




        tb.setBackgroundColor(getResources().getColor(R.color.colorAccent));


        Intent i = new Intent(Interactive_StatusActivity.this,MapsActivity.class);
        startActivity(i);
    }




    private void writeNewUser(String userId, String status, String lng,String lat,String name,String phone,String unit,String icon) {
        User eVp = new User(status, lng,lat,name,phone,unit,icon);


      myRef.child(EmergencyVehiclePersonnels).child(mAuth.getCurrentUser().getUid()).setValue(eVp);


    }

    private void writeNewTarget(String userId, String name, String lng,String lat,String address,String toggle) {
        Target target = new Target(name, lng,lat,address,toggle);


        myRef.child(EmergencyVehiclePersonnels).child(mAuth.getCurrentUser().getUid()).child("Target").setValue(target);


    }


    private Dialog createExampleDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rejected!");
        builder.setMessage("Specify the reason:");

        // Use an EditText view to get user input.
        final EditText input = new EditText(this);
        input.setId(TEXT_ID);
        builder.setView(input);
        builder.setCancelable(false);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                myRef.child(EmergencyVehiclePersonnels).child(mAuth.getCurrentUser().getUid()).child("status").setValue(value);
                myRef.child(EmergencyVehiclePersonnels).child(mAuth.getCurrentUser().getUid()).child("Target").child("toggle").setValue("0");
                Log.d(TAG, "User name: " + value);
                return;
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });

        return builder.create();
    }


    @Override
    protected Dialog onCreateDialog(int id) {

        switch (id) {
            case DLG_EXAMPLE1:
                return createExampleDialog();
            default:
                return null;
        }
    }

    /**
     * If a dialog has already been created,
     * this is called to reset the dialog
     * before showing it a 2nd time. Optional.
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {

        switch (id) {
            case DLG_EXAMPLE1:
                // Clear the input box.
                EditText text = (EditText) dialog.findViewById(TEXT_ID);
                text.setText("");
                break;
        }
    }

    @Override
    public void onBackPressed() {
       // super.onBackPressed();
    }
}
