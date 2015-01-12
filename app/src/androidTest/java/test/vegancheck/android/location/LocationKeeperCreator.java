package test.vegancheck.android.location;

import android.content.Context;

import vegancheck.android.location.LocationKeeper;
import vegancheck.android.location.Locator;

final class LocationKeeperCreator {
    private final Context context;
    private final Locator locator;
    private long renewalPeriod = LocationKeeper.DEFAULT_RENEWAL_PERIOD;
    private long expirationTime = LocationKeeper.DEFAULT_EXPIRATION_TIME;

    private LocationKeeperCreator(final Context context, final Locator locator) {
        this.context = context;
        this.locator = locator;
    }

    public static LocationKeeperCreator start(final Context context, final Locator locator) {
        return new LocationKeeperCreator(context, locator);
    }

    public LocationKeeperCreator setRenewalPeriod(final long renewalPeriod) {
        this.renewalPeriod = renewalPeriod;
        return this;
    }

    public LocationKeeperCreator setExpirationTime(final long expirationTime) {
        this.expirationTime = expirationTime;
        return this;
    }

    public LocationKeeper create() {
        return new LocationKeeper(context, locator, renewalPeriod, expirationTime);
    }
}
