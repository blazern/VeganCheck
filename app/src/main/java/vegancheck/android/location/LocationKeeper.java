package vegancheck.android.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import vegancheck.android.App;

public class LocationKeeper {
    public static final String SHARED_PREFERENCES_NAME =
            LocationKeeper.class.getCanonicalName() + ".SHARED_PREFERENCES";
    public static final long DEFAULT_RENEWAL_PERIOD = 1000 * 60 * 5; // 5 minutes
    public static final long DEFAULT_EXPIRATION_TIME = 1000 * 60 * 60; // an hour

    private static final String KEY_SAVED_LOCATION_RENEWAL_TIME =
            LocationKeeper.class.getCanonicalName() + ".SAVED_LOCATION_RENEWAL_TIME";

    private final Context context;
    private volatile Location lastLocation;

    private final Runnable requestLocationRunnable;

    private final long renewalPeriod;
    private final long expirationTime;
    private volatile long lastRenewalTime;

    private final Locator.LocatorListener locatorListener = new Locator.LocatorListener() {
        @Override
        public void onLocatorDeterminedLocation(final Location location, final String locationProviderName) {
            synchronized (this) {
                App.assertCondition(location != null);
                if (location == null) {
                    return;
                }

                if (lastLocation == null
                        || location.getAccuracy() < lastLocation.getAccuracy()
                        || isItTimeForRenewal()
                        || isLastLocationExpired()) {
                    lastLocation = location;
                    lastRenewalTime = System.currentTimeMillis();

                    final SharedPreferences preferences =
                            context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
                    PreferencesLocationParser.encode(preferences, lastLocation);
                    preferences.edit().putLong(KEY_SAVED_LOCATION_RENEWAL_TIME, lastRenewalTime).apply();
                }
            }
        }
    };

    private boolean isLastLocationExpired() {
        return expirationTime < System.currentTimeMillis() - lastRenewalTime;
    }

    private boolean isItTimeForRenewal() {
        return renewalPeriod < System.currentTimeMillis() - lastRenewalTime;
    }

    /**
     * @param context must be not null
     * @param locator must be not null
     * @param renewalPeriod must be >= 0
     * @param expirationTime must be >= 0. A location is considered as expired if it
     * was received more than expirationTime time ago.
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public LocationKeeper(
            final Context context,
            final Locator locator,
            final long renewalPeriod,
            final long expirationTime) {
        if (context == null) {
            throw new IllegalArgumentException("context must be not null");
        } else if (locator == null) {
            throw new IllegalArgumentException("locator must be not null");
        } else if (renewalPeriod < 0) {
            throw new IllegalArgumentException("renewalPeriod must be >= 0");
        } else if (expirationTime < 0) {
            throw new IllegalArgumentException("expirationTime must be >= 0");
        }

        this.context = context;
        final SharedPreferences preferences =
                context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        this.lastLocation = PreferencesLocationParser.parse(preferences);
        this.lastRenewalTime = preferences.getLong(KEY_SAVED_LOCATION_RENEWAL_TIME, 0);
        this.renewalPeriod = renewalPeriod;
        this.expirationTime = expirationTime;

        this.requestLocationRunnable = new Runnable() {
            @Override
            public void run() {
                locator.requestLocation(locatorListener);
            }
        };
        final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutor.scheduleAtFixedRate(requestLocationRunnable, renewalPeriod, renewalPeriod, TimeUnit.MILLISECONDS);

        requestLocationRunnable.run();
    }

    /**
     * @param context must be not null
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public LocationKeeper(final Context context) {
        this(
                context,
                new DefaultLocator((LocationManager)context.getSystemService(Context.LOCATION_SERVICE)),
                DEFAULT_RENEWAL_PERIOD,
                DEFAULT_EXPIRATION_TIME);
    }

    public Location getLastLocation() {
        if (!isLastLocationExpired()) {
            return lastLocation;
        } else {
            return null;
        }
    }

    public void requestLocationRenewal() {
        requestLocationRunnable.run();
    }
}
