package com.helpapp.helpapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.client.Firebase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LinearLayout llalertButtons;
    FloatingActionButton whistel, controlRoom, seaForce, call;
    LocationManager locationManager;
    ImageView closeButton, crossDialogBox;
    TextView captainName, location, contactedcontrolRoom, contactedSeaForce, contactedCall;
    EditText shipName, messege;
    Button submitButton;
    ImageButton normal, satelite;
    private PopupWindow pwindo;
    LatLng locationPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (InternetConnection.checkConnection(getApplicationContext())) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(getApplicationContext(), "Check your internet connection and try again", Toast.LENGTH_SHORT).show();
        }

        Firebase.setAndroidContext(this);

        llalertButtons = (LinearLayout) findViewById(R.id.ll_alerts);
        whistel = (FloatingActionButton) findViewById(R.id.whistel);
        controlRoom = (FloatingActionButton) findViewById(R.id.btn_control_room);
        seaForce = (FloatingActionButton) findViewById(R.id.btn_sea_force);
        call = (FloatingActionButton) findViewById(R.id.btn_emergency_call);
        closeButton = (ImageView) findViewById(R.id.closeButton);
        normal = (ImageButton) findViewById(R.id.normal_view);
        satelite = (ImageButton) findViewById(R.id.satelite_view);
        contactedcontrolRoom = (TextView) findViewById(R.id.contacted_control_room);
        contactedSeaForce = (TextView) findViewById(R.id.contacted_seaForce);
        contactedCall = (TextView) findViewById(R.id.contacted_call);

        contactedcontrolRoom.setVisibility(View.INVISIBLE);
        contactedSeaForce.setVisibility(View.INVISIBLE);
        contactedCall.setVisibility(View.INVISIBLE);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                llalertButtons.setVisibility(View.INVISIBLE);
                whistel.setVisibility(View.VISIBLE);
            }
        });

        llalertButtons.setVisibility(View.INVISIBLE);

        whistel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                whistel.setVisibility(View.INVISIBLE);
                llalertButtons.setVisibility(View.VISIBLE);
            }
        });

        controlRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initiatePopUpControlRoom();
            }
        });

        seaForce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initiatePopUpSeaForce();
            }
        });

        satelite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            }
        });

        normal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        });

        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:9989809647"));

                if (ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivity(callIntent);
                call.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                call.setClickable(false);
                contactedCall.setVisibility(View.VISIBLE);
            }
        });

    }

    private void initiatePopUpControlRoom() {
        try {
            LayoutInflater inflater = (LayoutInflater) MapsActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View layout = inflater.inflate(R.layout.popup_control_room,
                    (ViewGroup) findViewById(R.id.popup_element));
            pwindo = new PopupWindow(layout, 600, 850, true);
            pwindo.showAtLocation(layout, Gravity.CENTER, 0, 0);

            captainName = (TextView) layout.findViewById(R.id.tv_name_of_captain);
            location = (TextView) layout.findViewById(R.id.tv_location);
            crossDialogBox = (ImageView) layout.findViewById(R.id.cross_btn_dialog_box);
            submitButton = (Button) layout.findViewById(R.id.btn_submit_messege);
            shipName = (EditText) layout.findViewById(R.id.ed_name_of_ship);
            messege = (EditText) layout.findViewById(R.id.messege);


            crossDialogBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pwindo.dismiss();
                }
            });

            captainName.setText(UserDetails.username);
            location.setText(String.valueOf(locationPoint));

            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String nameOfship = shipName.getText().toString();
                    final String messegeBody = messege.getText().toString();

                    String toMail = "careers@mainbrain.tech";
                    String subject = nameOfship + " need help of Control Room at the location: " + locationPoint;
                    final String body = "Name of Ship: " + nameOfship + "\n" +  "Name of Captain: " + captainName.getText().toString() + "\n" + "Location: " + locationPoint + "\n" +
                            "Messege: " + messegeBody;

                    Intent email = new Intent(Intent.ACTION_SEND);
                    email.putExtra(Intent.EXTRA_EMAIL, new String[]{ toMail});
                    email.putExtra(Intent.EXTRA_SUBJECT, subject);
                    email.putExtra(Intent.EXTRA_TEXT, body);
                    //need this to prompts email client only
                    email.setType("message/rfc822");
                    startActivity(Intent.createChooser(email, "Choose an Email client :"));
                    pwindo.dismiss();
                    //Toast.makeText(getApplicationContext(), "Your messege has been sent to control team. They will contact you Shortly", Toast.LENGTH_LONG).show();
                    controlRoom.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    controlRoom.setClickable(false);
                    contactedcontrolRoom.setVisibility(View.VISIBLE);

                }

            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initiatePopUpSeaForce() {
        try {
            LayoutInflater inflater = (LayoutInflater) MapsActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.popup_control_room,
                    (ViewGroup) findViewById(R.id.popup_element));
            pwindo = new PopupWindow(layout, 600, 850, true);
            pwindo.showAtLocation(layout, Gravity.CENTER, 0, 0);

            captainName = (TextView) layout.findViewById(R.id.tv_name_of_captain);
            location = (TextView) layout.findViewById(R.id.tv_location);
            crossDialogBox = (ImageView) layout.findViewById(R.id.cross_btn_dialog_box);
            submitButton = (Button) layout.findViewById(R.id.btn_submit_messege);
            shipName = (EditText) layout.findViewById(R.id.ed_name_of_ship);
            messege = (EditText) layout.findViewById(R.id.messege);


            crossDialogBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pwindo.dismiss();
                }
            });

            captainName.setText(UserDetails.username);
            location.setText(String.valueOf(locationPoint));

            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String nameOfship = shipName.getText().toString();
                    final String messegeBody = messege.getText().toString();

                    String toMail = "ramprasad8583@gmail.com";
                    String subject = nameOfship + " need help regarding Sea Force and Direction at the location: " + locationPoint;
                    String body = "Name of Ship: " + nameOfship + "\n" +  "Name of Captain: " + captainName.getText().toString() + "\n" + "Location: " + locationPoint + "\n" +
                            "Messege: " + messegeBody;

                    Intent email = new Intent(Intent.ACTION_SEND);
                    email.putExtra(Intent.EXTRA_EMAIL, new String[]{ toMail});
                    email.putExtra(Intent.EXTRA_SUBJECT, subject);
                    email.putExtra(Intent.EXTRA_TEXT, body);
                    //need this to prompts email client only
                    email.setType("message/rfc822");
                    startActivity(Intent.createChooser(email, "Choose an Email client :"));
                    pwindo.dismiss();
                    //Toast.makeText(getApplicationContext(), "Your messege has been sent to control team. They will contact you Shortly", Toast.LENGTH_LONG).show();
                    seaForce.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    seaForce.setClickable(false);
                    contactedSeaForce.setVisibility(View.VISIBLE);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
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
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //if(locationManager == null)
        checkForGPS();
        Location location = getLastKnownLocation();
        if (location != null) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            locationPoint = new LatLng(lat, lng);
            System.out.println(locationPoint);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationPoint, 15));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
        }

    }

    private void checkForGPS() {
        boolean isGPSEnabled = false;
        if(locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)){
            isGPSEnabled = true;
        }
        if(isGPSEnabled == false){
            AlertDialog.Builder dialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.myDialog));
            dialog.setMessage("Please turn on your GPS");
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub

                }
            });
            dialog.show();
        }
    }

    private Location getLastKnownLocation() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return bestLocation;
            }
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }
}
