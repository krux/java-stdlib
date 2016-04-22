// KruxStdLib.PROPERTY_FILE


import com.krux.stdlib.KruxStdLib;
import com.krux.stdlib.utils.ExternalProperties;
import joptsimple.OptionSet;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertEquals;

public class ExternalPropertiesTest {

    @Test
    public void testGetExternalPropertyValue() {

        URL testPropertyFileURL = Thread.currentThread().getContextClassLoader().getResource("test_properties.properties");

        KruxStdLib k = new KruxStdLib();
        OptionSet options = k.initialize(null, false, null, null, null, null, null, null, null, null, testPropertyFileURL.getPath());

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
