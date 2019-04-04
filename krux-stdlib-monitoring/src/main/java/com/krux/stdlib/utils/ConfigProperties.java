package com.krux.stdlib.utils;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ConfigProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger( ConfigProperties.class.getName() );

    private String _kruxEnvironment;

    private Properties _kruxProps;

    public ConfigProperties(String environment) {
        LOGGER.info( "Loading Krux Properties from env: " + environment );
        _kruxEnvironment = environment;
        _kruxProps = new Properties();
        try {
            _kruxProps.load( this.getClass().getResourceAsStream( "/application.properties" ) );
        } catch ( Exception ex ) {
            LOGGER.error( "Can't load properties", ex );
        }
    }

    private String getPropertyValue( String propertyKey ) {
        String value = null;
        String envPropertyKey = String.format( "%s.%s", _kruxEnvironment, propertyKey );
        if ( _kruxProps != null && _kruxProps.containsKey( envPropertyKey ) ) {
            value = _kruxProps.getProperty( envPropertyKey );
        }
        return value;
    }

    public String getJdbcDriver() {
        return getPropertyValue( "jdbc.driver" );
    }

    public String getJdbcUrl() {
        return getPropertyValue( "jdbc.url" );
    }

    public String getJdbcUser() {
        return getPropertyValue( "jdbc.user" );
    }

    public String getJdbcPassword() {
        return getPropertyValue( "jdbc.password" );
    }
}
