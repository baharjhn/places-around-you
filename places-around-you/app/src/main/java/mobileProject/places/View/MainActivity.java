package mobileProject.places.View;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.AndroidNetworking;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import java.util.ArrayList;

import mobileProject.places.Controller.DataProvider;
import mobileProject.places.Controller.RecyclerViewAdapter.MarginDecoration;
import mobileProject.places.Controller.RecyclerViewAdapter.VenueAdapter;
import mobileProject.places.Model.Preferences.MyPreferenceManager;
import mobileProject.places.Model.Venue;
import mobileProject.places.R;

public class MainActivity extends Activity implements VenueAdapter.ItemClickListener {
    DataProvider dataProvider;
    ArrayList<Venue> venuesList;
    RecyclerView recyclerView;
    VenueAdapter mAdapter;
    MyPreferenceManager preferenceManager;
    private LocationCallback mLocationCallback;
    LocationRequest locationRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AndroidNetworking.initialize(getApplicationContext());
        preferenceManager = new MyPreferenceManager(getApplicationContext());
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new MarginDecoration(this));
        venuesList = new ArrayList<>();
        getPermission();
        double[] ll = new double[2];
        if (preferenceManager.loadLat() != null) {
            ll = new double[]{Double.parseDouble(preferenceManager.loadLat()), Double.parseDouble(preferenceManager.loadLong())};
        }

        dataProvider = new DataProvider(this, new DataProvider.DataReadyListener() {
            @Override
            public void onDataReady(ArrayList<Venue> venues) {
                mAdapter.update(venues);
            }
        });
        dataProvider.fetchData(ll);

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                    if (dataProvider.getCurrentLocation()!=null&&location.distanceTo(dataProvider.getCurrentLocation()) > 100) {
                        dataProvider.fetchData(new double[]{location.getLatitude(), location.getLongitude()});
                        System.out.println("---------------------------- update");
                        System.out.println("============= location is getting updated :|");
                    }
                }
            }

            ;
        };


        mAdapter = new VenueAdapter(getApplicationContext(), venuesList);
        mAdapter.setClickListener(this);
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    // Recycle view scrolling down...
                    if (!recyclerView.canScrollVertically(RecyclerView.FOCUS_DOWN)) {
                        if (mAdapter.getItemCount()>0){
                            dataProvider.getMoreVenues();
                            Toast.makeText(getApplicationContext() ,"درحال بروز رسانی"  , Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });

    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(getApplicationContext(), DetailedVenueActivity.class);
        intent.putExtra("id", dataProvider.getVenuesList().get(position).getId());
        intent.putExtra("name", dataProvider.getVenuesList().get(position).getName());
        startActivity(intent);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dataProvider.getCurrentLocation() != null) {
            preferenceManager.saveLat(dataProvider.getCurrentLocation().getLatitude());
            preferenceManager.saveLong(dataProvider.getCurrentLocation().getLongitude());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (dataProvider.getCurrentLocation() != null) {
            preferenceManager.saveLat(dataProvider.getCurrentLocation().getLatitude());
            preferenceManager.saveLong(dataProvider.getCurrentLocation().getLongitude());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        getPermission();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            getPermission();
            return;
        } else {

            dataProvider.getClient().requestLocationUpdates(locationRequest,
                    mLocationCallback,
                    null /* Looper */);
        }
    }


    void getPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?

            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    1);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        } else {
            // Permission has already been granted
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    double[] pll = new double[2];
                    dataProvider.fetchData(pll);
                    System.out.println("---------------------------- permission");
                } else {
//                    getPermission();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }
}
