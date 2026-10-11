package mobileProject.places.Model.Database;

public final class DatabaseConstants {

    public static final String DATABASE_NAME = "bazaarProject.db";
    public static final int DATABASE_VERSION = 1;

    public static class PlaceTable {

        public static final String TABLE_NAME = "venues";
        static final String VENUE_UID = "_id";
        public static final String NAME = "name";
        public static final String ADDRESS = "address";
        public static final String PHONE = "phone";
        public static final String LAT = "lat";
        public static final String LONG = "long";
        public static final String HASINFO = "has_info";

        public static final String CATEGORIES = "categories";
        public static final String PLACE_ID = "venue_id";

        public static final String CREATE_PLACE_TABLE_SYNTAX =
                "create table " + TABLE_NAME + " ( " +
                        VENUE_UID + " integer primary key autoincrement ," +
                        PLACE_ID + " text ," +
                        NAME + " text ," +
                        ADDRESS + " text ," +
                        PHONE + " integer ," +
                        CATEGORIES + " text ," +
                        HASINFO + " integer" +
                        ");";

        public static final String DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

}
