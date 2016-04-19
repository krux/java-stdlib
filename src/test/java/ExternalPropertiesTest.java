// KruxStdLib.PROPERTY_FILE


import java.util.Date;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import com.krux.stdlib.KruxStdLib;
import com.krux.stdlib.utils.ExternalProperties;

import joptsimple.OptionSet;
import java.net.URL;
import java.io.File;

public class ExternalPropertiesTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetExternalPropertyValue() {

        URL testProptertyFileURL = Thread.currentThread().getContextClassLoader().getResource("test_properties.properties");

        KruxStdLib k = new KruxStdLib();
        OptionSet options = k.initialize(null, false, null, null, null, null, null, null, null, null, testProptertyFileURL.getPath());

        ExternalProperties externalPropertyReader = new ExternalProperties();

        assertEquals(
                externalPropertyReader.getPropertyValue("test.com.krux.dummy_property"),
                "dummy_value"
        );

        assertEquals(
                externalPropertyReader.getPropertyValue("test.com.krux.non_existent_property"),
                null
        );

    }

}
