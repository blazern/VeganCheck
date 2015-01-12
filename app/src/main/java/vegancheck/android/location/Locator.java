package vegancheck.android.location;

import android.location.Location;

public interface Locator {
    interface LocatorListener {
        void onLocatorDeterminedLocation(Location location, String locationProviderName);
    }

    /**
     * listener's onLocatorDeterminedLocation(..) will be called once a location is
     * determined.<br>
     * <b>NOTE</b> that the onLocatorDeterminedLocation(..) method might be called more than once.
     */
    void requestLocation(LocatorListener listener);

    void cancelLocationRequestFor(LocatorListener listener);
}
