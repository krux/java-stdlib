package com.krux.stdlib.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.krux.stdlib.KruxStdLib;

/**
 *
 */

public class ExternalProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger( ExternalProperties.class.getName() );

    private Properties _kruxExternalProps;

    public ExternalProperties() {
        _kruxExternalProps = new Properties();
        InputStream input = null;
        if ( KruxStdLib.PROPERTY_FILE != null ) {
            LOGGER.debug("loading properties from " + KruxStdLib.PROPERTY_FILE);
            try {
                input = new FileInputStream( KruxStdLib.PROPERTY_FILE );
                _kruxExternalProps.load( input );
            } catch ( Exception ex ) {
                LOGGER.error( "Can't load external properties file " + KruxStdLib.PROPERTY_FILE, ex );
            }
        }
    }

    public String getPropertyValue( String propertyKey ) {
        LOGGER.debug("gettting " + propertyKey);
        String value = null;
        if ( _kruxExternalProps != null && _kruxExternalProps.containsKey( propertyKey ) ) {
            value = _kruxExternalProps.getProperty( propertyKey );
            LOGGER.debug("gettting " + propertyKey + ": " + _kruxExternalProps.getProperty( propertyKey ) );
        }
        return value;
    }

}
