package test.vegancheck.android.test.vegancheck.android.network;

import android.test.AndroidTestCase;

import junit.framework.Assert;

import java.text.ParseException;

import vegancheck.android.Product;
import vegancheck.android.network.ServersProductsParser;

public class TestServersProductsParser extends AndroidTestCase {
    public void testParsesProductWithTooManySeparators() {
        boolean exceptionThrown = false;
        try {
            ServersProductsParser.parse(
                    "Greenfield BARBERRY GARDEN§0§0§0§0§ORIMI LLC§§§§§§§§§§§§§§§§§§§§§§§§§§",
                    "123456");
        } catch (final ParseException e) {
            exceptionThrown = true;
        }

        Assert.assertFalse(exceptionThrown);
    }

    public void testKnowsWhenProductIsVerified() {
        Product parsedProduct = null;
        try {
            parsedProduct = ServersProductsParser.parse(
                    "Greenfield BARBERRY GARDEN§0§0§0§0§ORIMI LLC§1§",
                    "123456");
        } catch (final ParseException e) {
            Assert.fail("an unexpected parse exception caught!");
        }

        Assert.assertTrue(parsedProduct.isVerified());

        try {
            parsedProduct = ServersProductsParser.parse(
                    "Greenfield BARBERRY GARDEN§0§0§0§0§ORIMI LLC§0§",
                    "123456");
        } catch (final ParseException e) {
            Assert.fail("an unexpected parse exception caught!");
        }

        Assert.assertFalse(parsedProduct.isVerified());
    }
}
