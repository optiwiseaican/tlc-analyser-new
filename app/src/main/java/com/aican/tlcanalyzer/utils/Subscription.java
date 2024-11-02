package com.aican.tlcanalyzer.utils;

public class Subscription {

    /*
         Don't change the key's, unless want to see the conflicts
     */

    public static int numberOfUsers = 10;

    public static String adminID = "na";
    public static String adminIDKey  = "naKey" + UserRoles.UID;
    public static String numberOfUsersKey = "numberOfUsersKey" + UserRoles.UID;
    public static String adminPass = "na021";
    public static String adminPassKey = "na021Key" + UserRoles.UID;
    public static String userName = "N/A";
    public static String userNameKey = "userNameee" + UserRoles.UID;

    public static String email = "N/A";
    public static String emailKey = "emailIDDDD" + UserRoles.UID;

    public static boolean isActive = false;

    public static String SUBSCRIPTION_ACTIVE_DATE;
    public static String SUBSCRIPTION_END_DATE;
    public static String SUBSCRIPTION_END_DATE_KEY = "subenndkey" + UserRoles.UID;

    public static String TODAY_DATE_FROM_INTERNET;

    public static String LAST_ACCESS_DATE;
    public static String LAST_ACCESS_TIME;

    public static int NO_OF_PROJECTS_MADE;
    public static int PROJECT_LIMIT;

    public static final String PREFS_NAME = "MyPrefs";
    public static final String LAST_LAUNCH_DATE_KEY = "lastLaunchDate" + UserRoles.UID;
    public static final long MAX_ALLOWED_DIFFERENCE_MILLIS = 24 * 60 * 60 * 1000; // Maximum allowed difference: 24 hours


}
