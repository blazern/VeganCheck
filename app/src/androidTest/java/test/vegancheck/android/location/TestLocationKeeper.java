package test.vegancheck.android.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.test.AndroidTestCase;
import android.test.IsolatedContext;
import android.test.mock.MockContentResolver;

import junit.framework.Assert;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import vegancheck.android.location.LocationKeeper;
import vegancheck.android.location.Locator;

public class TestLocationKeeper extends AndroidTestCase {
    private Context context;
    private static final Locator BAD_LOCATOR = new Locator() {
        @Override public void requestLocation(LocatorListener listener) {
            // ima bad, bad Locator! Won't do nothing yo!
        }
        @Override public void cancelLocationRequestFor(LocatorListener listener) {}
    };

    @Override
    protected void setUp () {
        context = new IsolatedContext(new MockContentResolver(), getContext());
        clearRelatedPreferencesIn(context);
    }

    @SuppressLint("CommitPrefEdits")
    private void clearRelatedPreferencesIn(final Context context) {
        final SharedPreferences sharedPreferences =
                context.getSharedPreferences(
                        LocationKeeper.SHARED_PREFERENCES_NAME,
                        Context.MODE_PRIVATE);

        sharedPreferences.edit().clear().commit();
    }

    @Override
    protected void tearDown () {
        clearRelatedPreferencesIn(context);
        context = null;
    }

    public void testEmptyAtFirstStart() {
        final LocationKeeper locationKeeper = LocationKeeperCreator.start(context, BAD_LOCATOR).create();

        Assert.assertNull(locationKeeper.getLastLocation());
    }

    public void testRequestsLocationAtStart() {
        final AtomicBoolean isLocationRequested = new AtomicBoolean(false);
        final Locator locator = new Locator() {
            @Override public void requestLocation(LocatorListener listener) {
                isLocationRequested.set(true);
            }
            @Override public void cancelLocationRequestFor(LocatorListener listener) {}
        };

        final LocationKeeper locationKeeper =
                LocationKeeperCreator.start(context, locator).create();

        Assert.assertTrue(isLocationRequested.get());
    }

    public void testReturnsGatheredLocation() {
        final Location mockLocation = new Location("mock");
        final Locator locator = new Locator() {
            @Override public void requestLocation(LocatorListener listener) {
                listener.onLocatorDeterminedLocation(mockLocation, "mock");
            }
            @Override public void cancelLocationRequestFor(LocatorListener listener) {}
        };

        final LocationKeeper locationKeeper =
                LocationKeeperCreator.start(context, locator).create();

        Assert.assertEquals(mockLocation, locationKeeper.getLastLocation());
    }

    public void testReturnsBestLocation() {
        final Location bestLocation = createGoodLocation();
        final Location worstLocation = createBadLocation();
        final Locator locator = new Locator() {
            @Override public void requestLocation(LocatorListener listener) {
                listener.onLocatorDeterminedLocation(bestLocation, "mock1");
                listener.onLocatorDeterminedLocation(worstLocation, "mock2");
            }
            @Override public void cancelLocationRequestFor(LocatorListener listener) {}
        };

        final LocationKeeper locationKeeper =
                LocationKeeperCreator.start(context, locator).create();

        Assert.assertEquals(bestLocation, locationKeeper.getLastLocation());
    }

    private Location createGoodLocation() {
        final Location goodLocation = new Location("goodMock");
        goodLocation.setAccuracy(10);
        return goodLocation;
    }

    private Location createBadLocation() {
        final Location goodLocation = new Location("badMock");
        goodLocation.setAccuracy(100);
        return goodLocation;
    }

    public synchronized void testReturnsNewestLocationAfterTimeout() throws InterruptedException {
        final int renewalPeriod = 50;
        final Location bestLocation = createGoodLocation();
        final Location worstLocation = createBadLocation();
        final long locationKeeperCreationTime = System.currentTimeMillis();
        final Locator locator = new Locator() {
            @Override public void requestLocation(LocatorListener listener) {
                if (System.currentTimeMillis() - locationKeeperCreationTime < renewalPeriod) {
                    listener.onLocatorDeterminedLocation(bestLocation, "mock");
                } else {
                    listener.onLocatorDeterminedLocation(worstLocation, "mock");
                }
            }
            @Override public void cancelLocationRequestFor(LocatorListener listener) {}
        };

        final LocationKeeper locationKeeper =
                LocationKeeperCreator
                        .start(context, locator)
                        .setRenewalPeriod(renewalPeriod)
                        .create();

        Assert.assertEquals(locationKeeper.getLastLocation(), bestLocation);

        wait(renewalPeriod * 2);

        Assert.assertEquals(locationKeeper.getLastLocation(), worstLocation);
    }

    public synchronized void testRequestsCorrectNumberOfTimes() throws InterruptedException {
        final int renewalPeriod = 10;
        final AtomicInteger requestsCount = new AtomicInteger(0);
        final Locator locator = new Locator() {
            @Override public void requestLocation(LocatorListener listener) {
                requestsCount.incrementAndGet();
            }
            @Override public void cancelLocationRequestFor(LocatorListener listener) {}
        };

        final LocationKeeper locationKeeper =
                LocationKeeperCreator
                        .start(context, locator)
                        .setRenewalPeriod(renewalPeriod)
                        .create();

        final int waitedRenewalsCount = 5;
        wait(renewalPeriod * waitedRenewalsCount);

        final int requestsCountCopy = requestsCount.get();

        final String assertionString = "requestsCount: " + requestsCountCopy;
        // requestsCount should be at least 6 - the first request started at LocationKeeper creation,
        // the rest during waiting.
        Assert.assertTrue(assertionString, waitedRenewalsCount < requestsCountCopy);
        // requestsCount is probably 6, but we want to protect ourselfs from false fails so we check it for 8
        Assert.assertTrue(assertionString, requestsCountCopy <= waitedRenewalsCount + 3);
    }

    public void testRestoresLastSavedLocationAtSecondStart() {
        final Locator goodLocator = new Locator() {
            @Override public void requestLocation(LocatorListener listener) {
                listener.onLocatorDeterminedLocation(createGoodLocation(), "mock");
            }
            @Override public void cancelLocationRequestFor(LocatorListener listener) {}
        };

        {
            final LocationKeeper locationKeeper =
                    LocationKeeperCreator.start(context, goodLocator).create();
            // First start with good Locator will gather a Location.
            Assert.assertNotNull(locationKeeper.getLastLocation());
        }

        {
            final LocationKeeper locationKeeper =
                    LocationKeeperCreator.start(context, BAD_LOCATOR).create();
            // Second start with bad Locator,
            // must still be able to get a location found by another LocationKeeper.
            Assert.assertNotNull(locationKeeper.getLastLocation());
        }
    }

    public void testRestoredLocationIsCorrect() {
        final long time = System.currentTimeMillis();
        final String provider = "provider";
        final double longitude = 123;
        final double latitude = 321;
        final float accuracy = 5;

        final Locator goodLocator = new Locator() {
            @Override public void requestLocation(LocatorListener listener) {
                final Location location = new Location(provider);
                location.setTime(time);
                location.setLongitude(longitude);
                location.setLatitude(latitude);
                location.setAccuracy(accuracy);
                listener.onLocatorDeterminedLocation(location, provider);
            }
            @Override public void cancelLocationRequestFor(LocatorListener listener) {}
        };

        {
            final LocationKeeper locationKeeper =
                    LocationKeeperCreator.start(context, goodLocator).create();
            // First start with good Locator will gather a Location.
            Assert.assertNotNull(locationKeeper.getLastLocation());
        }

        {
            final LocationKeeper locationKeeper =
                    LocationKeeperCreator.start(context, BAD_LOCATOR).create();
            // Second start with bad Locator,
            // must still be able to get a location found by another LocationKeeper.
            final Location location = locationKeeper.getLastLocation();
            Assert.assertNotNull(location);
            Assert.assertEquals(location.getProvider(), provider);
            Assert.assertEquals(location.getTime(), time);
            Assert.assertEquals(location.getAccuracy(), accuracy);
            Assert.assertEquals(location.getLongitude(), longitude);
            Assert.assertEquals(location.getLatitude(), latitude);
        }
    }

    public synchronized void testDoesNotRestoreExpiredLocation() throws InterruptedException {
        final Locator goodLocator = new Locator() {
            @Override public void requestLocation(LocatorListener listener) {
                listener.onLocatorDeterminedLocation(createGoodLocation(), "mock");
            }
            @Override public void cancelLocationRequestFor(LocatorListener listener) {}
        };

        {
            final LocationKeeper locationKeeper =
                    LocationKeeperCreator.start(context, goodLocator).create();
            // First start with good Locator will gather a Location.
            Assert.assertNotNull(locationKeeper.getLastLocation());
        }

        {
            final int expirationTime = 10;
            wait(expirationTime * 2);

            final LocationKeeper locationKeeper =
                    LocationKeeperCreator
                            .start(context, BAD_LOCATOR)
                            .setExpirationTime(expirationTime)
                            .create();
            // Second start with bad Locator,
            // must NOT restore the Location gathered by the other LocationKeeper because it is outdated.
            Assert.assertNull(locationKeeper.getLastLocation());
        }
    }

    public synchronized void testDoesNotGiveExpiredLocation() throws InterruptedException {
        final Locator goodLocator = new Locator() {
            @Override public void requestLocation(LocatorListener listener) {
                listener.onLocatorDeterminedLocation(createGoodLocation(), "mock");
            }
            @Override public void cancelLocationRequestFor(LocatorListener listener) {}
        };

        final int expirationTime = 50;
        final LocationKeeper locationKeeper =
                LocationKeeperCreator
                        .start(context, goodLocator)
                        .setRenewalPeriod(expirationTime * 100)
                        .setExpirationTime(expirationTime)
                        .create();

        Assert.assertNotNull(locationKeeper.getLastLocation());
        wait(expirationTime * 2);
        Assert.assertNull(locationKeeper.getLastLocation());
    }

    public void testLocationRenewalRequestWorks() {
        final AtomicInteger requestsCount = new AtomicInteger(0);
        final Locator locator = new Locator() {
            @Override public void requestLocation(LocatorListener listener) {
                requestsCount.incrementAndGet();
            }
            @Override public void cancelLocationRequestFor(LocatorListener listener) {}
        };

        Assert.assertEquals(0, requestsCount.get());

        final LocationKeeper locationKeeper =
                LocationKeeperCreator
                        .start(context, locator)
                        .setRenewalPeriod(999999999)
                        .create();

        Assert.assertEquals(1, requestsCount.get());

        locationKeeper.requestLocationRenewal();

        Assert.assertEquals(2, requestsCount.get());
    }

    public synchronized void testNewLocationReplacesExpiredLocation() throws InterruptedException {
        final Locator goodLocator = new Locator() {
            @Override public void requestLocation(LocatorListener listener) {
                listener.onLocatorDeterminedLocation(createGoodLocation(), "mock");
            }
            @Override public void cancelLocationRequestFor(LocatorListener listener) {}
        };

        final int expirationTime = 50;
        final LocationKeeper locationKeeper =
                LocationKeeperCreator
                        .start(context, goodLocator)
                        .setRenewalPeriod(999999999)
                        .setExpirationTime(expirationTime)
                        .create();

        Assert.assertNotNull(locationKeeper.getLastLocation());
        wait(expirationTime * 2);
        Assert.assertNull(locationKeeper.getLastLocation());

        locationKeeper.requestLocationRenewal();
        Assert.assertNotNull(locationKeeper.getLastLocation());
    }
}
