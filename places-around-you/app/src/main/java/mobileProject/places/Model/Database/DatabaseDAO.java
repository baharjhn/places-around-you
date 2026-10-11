package mobileProject.places.Model.Database;

import java.util.ArrayList;

import mobileProject.places.Model.Venue;

public interface DatabaseDAO {
    void addVenue(Venue venue);

    void updateVenue(Venue venue);

    Venue getVenue(String id);

    void updateDB(ArrayList<Venue> venues);

    ArrayList<Venue> getVenues();
}
