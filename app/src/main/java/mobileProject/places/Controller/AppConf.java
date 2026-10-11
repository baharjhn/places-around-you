package mobileProject.places.Controller;

import android.app.Application;

import mobileProject.places.Model.Database.DatabaseOperation;

public class AppConf extends Application {

    private static DatabaseOperation databaseOperation;

    @Override
    public void onCreate() {
        super.onCreate();
        databaseOperation = new DatabaseOperation(this);
    }

    public static DatabaseOperation getDatabaseOperation() {
        return databaseOperation;
    }
}