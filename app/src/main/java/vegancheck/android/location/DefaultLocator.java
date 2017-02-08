package vegancheck.android.location;

import android.location.LocationManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import vegancheck.android.App;

final class DefaultLocator implements Locator {
    private final LocationManager locationManager;
    private final List<OSLocationsSenderToLocatorListener> locationsSenders =
            new ArrayList<OSLocationsSenderToLocatorListener>();

    /**
     * @param locationManager must be not null
     * @throws java.lang.IllegalArgumentException if any parameter is invalid
     */
    public DefaultLocator(final LocationManager locationManager) {
        if (locationManager == null) {
            throw new IllegalArgumentException("locationManager must be not null");
        }
        this.locationManager = locationManager;
    }

    @Override
    public void requestLocation(final LocatorListener listener) {
        if (listener == null) {
            App.error(this, "listener is null?");
            return;
        }

        cancelLocationRequestFor(listener);
        // TODO: commented out so that the app wouldn't crash because of lack of the permission.
//        locationsSenders.add(new OSLocationsSenderToLocatorListener(listener, locationManager));
    }

    @Override
    public void cancelLocationRequestFor(final LocatorListener listener) {
        if (listener == null) {
            App.error(this, "listener is null?");
            return;
        }

        final Iterator<OSLocationsSenderToLocatorListener> iterator = locationsSenders.iterator();
        while (iterator.hasNext()) {
            final OSLocationsSenderToLocatorListener locationSender = iterator.next();
            if (locationSender.isMadeFor(listener)) {
                locationSender.cancel();
                iterator.remove();
            }
        }
    }
}
