package mobileProject.places.Model.Preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class MyPreferenceManager {
    private SharedPreferences preferences;

    public MyPreferenceManager(Context context) {
        preferences = context.getSharedPreferences("user_credential", Context.MODE_PRIVATE);
    }

    public void saveLat(double lat) {
        preferences.edit().putString("lat", String.valueOf(lat)).apply();
    }

    public void saveLong(double longitude) {
        preferences.edit().putString("long", String.valueOf(longitude)).apply();
    }

    public String loadLat() {
        return preferences.getString("lat", null);
    }

    public String loadLong() {
        return preferences.getString("long", null);
    }

}
