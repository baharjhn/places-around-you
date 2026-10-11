package mobileProject.places.View;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import mobileProject.places.Controller.DataProvider;
import mobileProject.places.Controller.RecyclerViewAdapter.MarginDecoration;
import mobileProject.places.Controller.RecyclerViewAdapter.VenueAdapter;
import mobileProject.places.Model.Venue;
import mobileProject.places.R;

public class DetailedVenueActivity extends Activity implements VenueAdapter.ItemClickListener {
    TextView venueName, venueAddress, venuePhone, venueCategory;
    AppCompatButton button;
    DataProvider dataProvider;
    RecyclerView recyclerView;
    VenueAdapter mAdapter;
    Venue mVenue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_venue);

        dataProvider = new DataProvider(this, new DataProvider.DataReadyListener() {
            @Override
            public void onDataReady(ArrayList<Venue> venues) {

                if (mAdapter != null)
                    mAdapter.update(venues);
                else mAdapter = new VenueAdapter(getApplicationContext(), venues);

                if (venues.size()>0)
                    button.setText("مکان های مشابه: ");
                else
                    button.setText("مکان مشابهی یافت نشد!");
            }
        }, new DataProvider.DetailsReadyListener() {
            @Override
            public void onDetailsReady(Venue venue) {

                mVenue = venue;
                venuePhone.setText(venue.getPhone());

                venueAddress.setText(venue.getAddress());

                venueCategory.setText(venue.getCategory());
            }
        });
        venueName = findViewById(R.id.venue_name);
        venueAddress = findViewById(R.id.venue_address);
        venuePhone = findViewById(R.id.venue_phone);
        venueCategory = findViewById(R.id.venue_category);
        button = findViewById(R.id.edit_info_button);

        venueName.setText(getIntent().getStringExtra("name"));

        String id = getIntent().getStringExtra("id");
        dataProvider.getDetails(id);
        mAdapter = new VenueAdapter(getApplicationContext(), new ArrayList<Venue>());
        mAdapter.setClickListener(this);
        dataProvider.getRecommendations(id);
        recyclerView = findViewById(R.id.recommendation_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new MarginDecoration(this));
        recyclerView.setAdapter(mAdapter);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(getApplicationContext(), DetailedVenueActivity.class);
        intent.putExtra("id", mAdapter.getItem(position).getId());
        intent.putExtra("name", mAdapter.getItem(position).getName());
        startActivity(intent);
    }
}
