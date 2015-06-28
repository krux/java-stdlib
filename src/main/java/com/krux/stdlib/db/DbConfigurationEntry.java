package com.krux.stdlib.db;

/**
 * @author Vivek S. Vaidya
 */
public class DbConfigurationEntry {

    private String _driver;

    private String _url;

    private String _user;

    private String _password;

    public DbConfigurationEntry(String driver, String url, String user, String password) {
        _driver = driver;
        _url = url;
        _user = user;
        _password = password;
    }

    public String getDriver() {
        return _driver;
    }

    public String getUrl() {
        return _url;
    }

    public String getUser() {
        return _user;
    }

    public String getPassword() {
        return _password;
    }
}
