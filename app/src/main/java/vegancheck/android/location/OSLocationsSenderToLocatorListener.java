package vegancheck.android.location;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.List;

final class OSLocationsSenderToLocatorListener {
    private final Locator.LocatorListener notificationsListener;
    private boolean isCanceled;

    private final class LocationManagerListener implements LocationListener {
        final String provider;
        LocationManagerListener(final String provider) {
            this.provider = provider;
        }
        @Override
        public void onLocationChanged(final Location location) {
            synchronized (OSLocationsSenderToLocatorListener.this) {
                if (!isCanceled) {
                    notificationsListener.onLocatorDeterminedLocation(location, provider);
                }
            }
        }
        @Override public void onProviderDisabled(final String s) {}
        @Override public void onStatusChanged(String s, int i, Bundle bundle) {}
        @Override public void onProviderEnabled(String s) {}
    }

    /**
     * @param notificationsListener must be not null
     * @param locationManager must be not null
     * @throws java.lang.IllegalArgumentException if any argument is invalid
     */
    public OSLocationsSenderToLocatorListener(
            final Locator.LocatorListener notificationsListener,
            final LocationManager locationManager) {
        if (notificationsListener == null) {
            throw new IllegalArgumentException("notificationsListener must be not null");
        } else if (locationManager == null) {
            throw new IllegalArgumentException("locationManager must be not null");
        }
        this.notificationsListener = notificationsListener;

        final List<String> locationProviders = locationManager.getProviders(true);
        for (final String provider : locationProviders) {
            locationManager.requestSingleUpdate(
                    provider,
                    new LocationManagerListener(provider),
                    null);
        }
    }

    synchronized public void cancel() {
        isCanceled = true;
    }

    public boolean isMadeFor(final Locator.LocatorListener notificationsListener) {
        return this.notificationsListener == notificationsListener;
    }
}
