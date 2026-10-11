# Places Around You

An Android app (Java) that finds **venues near the user's current location** with the **Foursquare Places API**, shows them in a scrolling list, and caches results in **SQLite** so they still load offline. Built as a mobile development course project in 2019. The app's interface is in Persian.

## Features

- **Location-aware search:** gets the device location with Google Play Services (`FusedLocationProviderClient`) and requests nearby venues from Foursquare.
- **Live location updates:** if the user moves more than 100 m, the list refreshes for the new position.
- **Infinite scroll:** when the user reaches the end of the list, the app samples a random point in a gradually widening ring around them and loads more venues, skipping ones already shown.
- **Venue details:** tapping a venue shows its address, phone number and categories.
- **Similar places:** the details screen recommends similar venues using Foursquare's `venues/{id}/similar` endpoint.
- **Offline caching:**
  - venues and their details are stored in SQLite;
  - the last known location is saved in `SharedPreferences`;
  - if the user hasn't moved or there is no network, the list loads from the local database instead of the API.

## Architecture

The code is organized into Model, View and Controller packages:

```
app/src/main/java/mobileProject/places/
├── Controller/
│   ├── DataProvider.java          # Location, Foursquare API calls (search, details, similar), cache logic
│   ├── AppConf.java               # Application class: database setup
│   └── RecyclerViewAdapter/       # VenueAdapter, MarginDecoration
├── Model/
│   ├── Venue.java
│   ├── Database/                  # SQLiteOpenHelper, DAO and queries for the venues table
│   └── Preferences/               # Last known location (SharedPreferences)
└── View/
    ├── MainActivity.java          # Nearby venues list, location updates, infinite scroll
    └── DetailedVenueActivity.java # Venue details and similar places
```

## Tech stack

Java · Android SDK 28 (min SDK 15) · Google Play Services Location · Foursquare API v2 · Fast Android Networking · RecyclerView/CardView · SQLite · SharedPreferences

## Running it

1. Open the project folder in Android Studio and let it sync with Gradle (Android Studio uses its own Gradle installation).
2. Add your Foursquare credentials to `local.properties` (this file is not committed). See `local.properties.example`:
   ```
   foursquare.clientId=YOUR_FOURSQUARE_CLIENT_ID
   foursquare.clientSecret=YOUR_FOURSQUARE_CLIENT_SECRET
   ```
3. Run the `app` configuration on a device or emulator with location enabled, and allow location access.

Note: the app uses Foursquare's v2 API (client ID and secret), which was current when it was built. New Foursquare accounts use the newer Places API with API keys, so the request code would need small updates to run today.

## Team

Group project by **Bahar Jahani** and **Amirreza Kazemi**, mobile development course, 2019.

## Changes before publishing

- Moved the Foursquare client ID and secret out of the source code into `local.properties`, which is read at build time through `BuildConfig`.
- Removed build outputs, IDE files and the machine-specific `local.properties`.
- Kept only the files needed to understand and build the app: source code, layouts, resources and build scripts.
