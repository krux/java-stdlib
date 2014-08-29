package com.krux.stdlib.utils;

import com.krux.stdlib.KruxStdLib;

import java.util.Properties;

/**
 *
 */
public class ConfigProperties {

    private String _kruxEnvironment;

    private Properties _kruxProps;

    public ConfigProperties() {
        _kruxEnvironment = KruxStdLib.ENV;
        _kruxProps = new Properties();
        try {
            _kruxProps.load(this.getClass().getResourceAsStream("/application.properties"));
        } catch (Exception ex) {
            // ignore the exception
            // deal with missing and null values in the caller
        }
    }

    private String getPropertyValue(String propertyKey) {
        String value = null;
        String envPropertyKey = String.format("%s.%s", _kruxEnvironment, propertyKey);
        if (_kruxProps != null && _kruxProps.containsKey(envPropertyKey)) {
            value = _kruxProps.getProperty(envPropertyKey);
        }
        return value;
    }


    public String getJdbcDriver() {
        return getPropertyValue("jdbc.driver");
    }

    public String getJdbcUrl() {
        return getPropertyValue("jdbc.url");
    }

    public String getJdbcUser() {
        return getPropertyValue("jdbc.user");
    }

    public String getJdbcPassword() {
        return getPropertyValue("jdbc.password");
    }
}
