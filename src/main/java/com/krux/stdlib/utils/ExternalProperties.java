package com.krux.stdlib.utils;

import com.krux.stdlib.KruxStdLib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads properties from an external file, set via the --property-file
 * command-line option.
 *
 * For tests in your project, implement a helper class that reads the
 * resource from test/resources.
 */

public class ExternalProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger( ExternalProperties.class.getName() );

    private static Properties _kruxExternalProps;

    static {
        _kruxExternalProps = new Properties();
        InputStream input;
        if ( KruxStdLib.PROPERTY_FILE != null ) {
            LOGGER.debug("loading properties from " + KruxStdLib.PROPERTY_FILE);
            try {
                input = new FileInputStream( KruxStdLib.PROPERTY_FILE );
                _kruxExternalProps.load( input );
            } catch ( Exception ex ) {
                LOGGER.error( "Can't load external properties file " + KruxStdLib.PROPERTY_FILE, ex );
                throw new RuntimeException("Can't load external properties file " + KruxStdLib.PROPERTY_FILE, ex );
            }
        }
    }

    public static String getPropertyValue( String propertyKey ) {
        String value = null;
        if ( _kruxExternalProps != null && _kruxExternalProps.containsKey( propertyKey ) ) {
            value = _kruxExternalProps.getProperty( propertyKey );
            LOGGER.debug("getting " + propertyKey + ": " + _kruxExternalProps.getProperty( propertyKey ) );
        }
        return value;
    }

}
