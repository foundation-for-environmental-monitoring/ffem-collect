package io.ffem.collect.android.common;


/**
 * Global Configuration settings for the app.
 */
public final class AppConfig {

    /**
     * Date on which the app version will expire.
     * This is to ensure that installs from apk meant for testing only are not used for too long.
     */
    public static final boolean APP_EXPIRY = false;
    public static final int APP_EXPIRY_DAY = 15;
    public static final int APP_EXPIRY_MONTH = 9;
    public static final int APP_EXPIRY_YEAR = 2019;

    private AppConfig() {
    }

}
