package com.krux.stdlib.utils;
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
        OptionSet options = k.parse(new KruxStdLib.ArgBuilder().withPropertyFileName(testPropertyFileURL.getPath()));
        k.initialize(options, new DummyStatusHandler());

        assertEquals(
                ExternalProperties.get().getPropertyValue("test.com.krux.dummy_property"),
                "dummy_value"
        );

        assertEquals(
                ExternalProperties.get().getPropertyValue("test.com.krux.non_existent_property"),
                null
        );

    }

}
