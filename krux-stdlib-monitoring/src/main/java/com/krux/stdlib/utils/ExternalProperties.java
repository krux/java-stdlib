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
    private static volatile ExternalProperties instance;

    public static ExternalProperties get() {
        if (instance == null) {
            instance = new ExternalProperties(KruxStdLib.get());
        }

        return instance;
    }

    private Properties kruxExternalProps;

    public ExternalProperties(KruxStdLib stdLib) {
        kruxExternalProps = new Properties();

        InputStream input;
        String propertyFile = stdLib.getPropertyFile();
        if ( propertyFile != null ) {
            LOGGER.debug("loading properties from " + propertyFile);
            try {
                input = new FileInputStream( propertyFile );
                kruxExternalProps.load( input );
            } catch ( Exception ex ) {
                LOGGER.error( "Can't load external properties file " + propertyFile, ex );
                throw new RuntimeException("Can't load external properties file " + propertyFile, ex );
            }
        }
    }

    public String getPropertyValue( String propertyKey ) {
        String value = null;
        if ( kruxExternalProps != null && kruxExternalProps.containsKey( propertyKey ) ) {
            value = kruxExternalProps.getProperty( propertyKey );
            LOGGER.debug("getting {}: {}", propertyKey,  kruxExternalProps.getProperty( propertyKey ) );
        }
        return value;
    }

}
