package mobileProject.places.Controller;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

import mobileProject.places.Model.Preferences.MyPreferenceManager;
import mobileProject.places.Model.Venue;

public class DataProvider {
    private ArrayList<Venue> venuesList;
    private Activity activity;
    private Location currentLocation;
    private double[] ll;
    private FusedLocationProviderClient client;
    private DataReadyListener readyListener;
    private DetailsReadyListener detailsReadyListener;
    // Foursquare credentials are read from local.properties at build time (see README).
    private static final String CLIENT_ID = mobileProject.places.BuildConfig.FOURSQUARE_CLIENT_ID;
    private static final String CLIENT_SECRET = mobileProject.places.BuildConfig.FOURSQUARE_CLIENT_SECRET;
    private double radiusExpansion;


    public DataProvider(Activity activity, DataReadyListener readyListener) {
        this.activity = activity;
        venuesList = new ArrayList<>();
        client = LocationServices.getFusedLocationProviderClient(activity);
        this.readyListener = readyListener;
        currentLocation = new Location("");
        ll = new double[2];
        radiusExpansion = 1;
    }

    public DataProvider(Activity activity, DataReadyListener readyListener, DetailsReadyListener detailsReadyListener) {
        this.activity = activity;
        this.readyListener = readyListener;
        currentLocation = new Location("");
        this.detailsReadyListener = detailsReadyListener;
        MyPreferenceManager myPreferenceManager = new MyPreferenceManager(activity.getApplicationContext());
        ll = new double[]{Double.valueOf(myPreferenceManager.loadLat()), Double.valueOf(myPreferenceManager.loadLong())};

    }

    public void fetchData(final double[] pll) {
        if (ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        client.getLastLocation()
                .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            System.out.println("======= GPS WORKS FINE " + location.getLatitude() + "  " + location.getLongitude());
                            ll[0] = location.getLatitude();
                            ll[1] = location.getLongitude();
                            currentLocation = location;
                            float[] temp = new float[3];
                            if (pll != null)
                                Location.distanceBetween(pll[0], pll[1], ll[0], ll[1], temp);

                            System.out.println(pll[0] + " " + pll[1] + "     --------------------");
                            if (temp[0] > 100 && isNetworkConnected())
                                getPlaces(ll, true);
                            else
                                getPlaces(ll, false);
                        } else {
                            System.out.println("==== COULD NOT GET LOCATION :| ");
                            Toast.makeText(activity.getApplicationContext(), "مشکل دریافت اطلاعات مکانی ", Toast.LENGTH_SHORT).show();
                            ll = pll;
                            getPlaces(ll, false);
                        }
                    }

                });

    }

    private void getPlaces(double[] ll, boolean getFromNetwork) {
        if (getFromNetwork) {
            AndroidNetworking.get("https://api.foursquare.com/v2/venues/search?")
                    .addQueryParameter("ll", ll[0] + "," + ll[1])
                    .addQueryParameter("client_id", CLIENT_ID)
                    .addQueryParameter("client_secret", CLIENT_SECRET)
                    .addQueryParameter("v", "20180406")
                    .build()
                    .getAsJSONObject((new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {
                                System.out.println("================= response: " + response.toString());
                                onReqEnd(response.getJSONObject("response"), 0);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(ANError error) {
                            // handle error
                            System.out.println("================= error: " + error.getErrorBody() + "    " + error.getErrorDetail());
                            Toast.makeText(activity.getApplicationContext(), "مشکل در برقراری ارتباط", Toast.LENGTH_SHORT).show();

                        }
                    }));
        } else {
            System.out.println("fetch from DB ============  ");
            this.venuesList = (AppConf.getDatabaseOperation().getVenues());
            if (this.venuesList.size() < 4)
                getPlaces(ll, true);
            readyListener.onDataReady(this.venuesList);
        }

    }

    private void onReqEnd(JSONObject response, int MODE) throws JSONException {
        // Mode 0 for getting new data
        // Mode 1 for adding data
        // Mode 2 for recommendation


        if (MODE == 0) {
            JSONArray venuesJson = response.getJSONArray("venues");
            this.venuesList.clear();
            for (int i = 0; i < venuesJson.length(); i++) {
                String id = venuesJson.getJSONObject(i).getString("id"), name = venuesJson.getJSONObject(i).getString("name");
                this.venuesList.add(new Venue(id, name));
            }
            System.out.println("data processed ========");
            AppConf.getDatabaseOperation().updateDB(this.venuesList);
            readyListener.onDataReady(this.venuesList);
        } else if (MODE == 1) {
            JSONArray venuesJson = response.getJSONArray("venues");
            for (int i = 0; i < venuesJson.length(); i++) {
                String id = venuesJson.getJSONObject(i).getString("id"), name = venuesJson.getJSONObject(i).getString("name");
                Venue v = new Venue(id, name);
                if (!this.venuesList.contains(v)) {
                    this.venuesList.add(v);
                    AppConf.getDatabaseOperation().addVenue(v);
                }
            }

            System.out.println("data processed ========");
            readyListener.onDataReady(this.venuesList);
        } else if (MODE == 2) {
            JSONArray venuesJson = response.getJSONArray("items");
            ArrayList<Venue> venues = new ArrayList<>();
            for (int i = 0; i < venuesJson.length(); i++) {
                String id = venuesJson.getJSONObject(i).getString("id"), name = venuesJson.getJSONObject(i).getString("name");
                Venue v = new Venue(id, name);
                venues.add(v);
            }
            if (venues.size() < 1)
                Toast.makeText(activity.getApplicationContext(), "مکان مشابهی یافت نشد", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(activity.getApplicationContext(), "مکان های پیشنهادی ما", Toast.LENGTH_SHORT).show();

            readyListener.onDataReady(venues);
        }
    }


    private static Location getLocationInLatLngRad(double radiusInMeters, Location currentLocation, double i) {
        double x0 = currentLocation.getLongitude();
        double y0 = currentLocation.getLatitude();

        Random random = new Random();

        // Convert radius from meters to degrees.
        double radiusInDegrees = radiusInMeters / 111320f;

        // Get a random distance and a random angle.
        double u = random.nextDouble();
        double v = random.nextDouble();
        double w = radiusInDegrees * Math.sqrt(u) + (i * radiusInDegrees);
        double t = 2 * Math.PI * v;
        // Get the x and y delta values.
        double x = w * Math.cos(t);
        double y = w * Math.sin(t);

        // Compensate the x value.
        double new_x = x / Math.cos(Math.toRadians(y0));

        double foundLatitude;
        double foundLongitude;

        foundLatitude = y0 + y;
        foundLongitude = x0 + new_x;

        Location copy = new Location(currentLocation);
        copy.setLatitude(foundLatitude);
        copy.setLongitude(foundLongitude);
        return copy;
    }

    public void getMoreVenues() {

        Location add = getLocationInLatLngRad(100, currentLocation, radiusExpansion);

        radiusExpansion++;
        AndroidNetworking.get("https://api.foursquare.com/v2/venues/search?")
                .addQueryParameter("ll", add.getLatitude() + "," + add.getLongitude())
                .addQueryParameter("client_id", CLIENT_ID)
                .addQueryParameter("client_secret", CLIENT_SECRET)
                .addQueryParameter("v", "20180406")
                .build()
                .getAsJSONObject((new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            System.out.println("================= response: " + response.toString());
                            onReqEnd(response.getJSONObject("response"), 1);
                            Toast.makeText(activity.getApplicationContext(), "مکان های جدید افزوده شدند", Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        System.out.println("================= error: " + error.getErrorBody() + "    " + error.getErrorDetail());
                        Toast.makeText(activity.getApplicationContext(), "مشکل در برقراری ارتباط", Toast.LENGTH_SHORT).show();

                    }
                }));
    }

    public void getRecommendations(String id) {
        AndroidNetworking.get("https://api.foursquare.com/v2/venues/{id}/similar?")
                .addPathParameter("id", id)
                .addQueryParameter("client_id", CLIENT_ID)
                .addQueryParameter("client_secret", CLIENT_SECRET)
                .addQueryParameter("v", "20180406")
                .build()
                .getAsJSONObject((new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            System.out.println("================= response: " + response.toString());
                            onReqEnd(response.getJSONObject("response").getJSONObject("similarVenues"), 2);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        // handle error
                        System.out.println("================= error: " + error.getErrorBody() + "    " + error.getErrorDetail());
                        Toast.makeText(activity.getApplicationContext(), "مشکل در برقراری ارتباط", Toast.LENGTH_SHORT).show();

                    }
                }));
    }


    public interface DataReadyListener {
        void onDataReady(ArrayList<Venue> venues);
    }

    public interface DetailsReadyListener {
        void onDetailsReady(Venue venueJson);
    }


    public Location getCurrentLocation() {
        return currentLocation;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    public ArrayList<Venue> getVenuesList() {
        return venuesList;
    }

    public FusedLocationProviderClient getClient() {
        return client;
    }


    public void getDetails(String id) {
        Venue venue = AppConf.getDatabaseOperation().getVenue(id);
        if (venue == null || venue.getHasInfo() == 0) {
            AndroidNetworking.get("https://api.foursquare.com/v2/venues/{id}?")
                    .addPathParameter("id", id)
                    .addQueryParameter("client_id", CLIENT_ID)
                    .addQueryParameter("client_secret", CLIENT_SECRET)
                    .addQueryParameter("v", "20180406")
                    .build()
                    .getAsJSONObject((new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {

                            try {
                                System.out.println("================= response: " + response.toString());
                                onDetailsReqEnd(response.getJSONObject("response"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(ANError error) {
                            // handle error
                            System.out.println("================= error: " + error.getErrorBody() + error.getErrorDetail());

                        }
                    }));
        } else detailsReadyListener.onDetailsReady(venue);

    }

    private void onDetailsReqEnd(JSONObject response) throws JSONException {
        System.out.println(response.toString());
        JSONObject venueJson = response.getJSONObject("venue");
        JSONObject contact = venueJson.getJSONObject("contact");
        JSONObject location = venueJson.getJSONObject("location");
        JSONArray category = venueJson.getJSONArray("categories");
        Venue venue = new Venue(venueJson.getString("id"), venueJson.getString("name"));
        if (contact == null || contact.isNull("phone")) {
            venue.setPhone(" ندارد ");
        } else
            venue.setPhone(contact.getString("phone"));
        if (location == null || location.isNull("address")) {
            venue.setAddress(" ندارد ");
        } else
            venue.setAddress(location.getString("address"));
        StringBuilder cat = new StringBuilder();
        for (int i = 0; i < category.length(); i++) {
            if (i == category.length() - 1)
                cat.append(category.getJSONObject(i).getString("name"));
            else
                cat.append(category.getJSONObject(i).getString("name")).append(",");
        }
        if (cat.length() > 0)
            venue.setCategory(cat.toString());
        else
            venue.setCategory("ندارد");

        System.out.println("data processed ========");
        detailsReadyListener.onDetailsReady(venue);
        AppConf.getDatabaseOperation().updateVenue(venue);

    }
}