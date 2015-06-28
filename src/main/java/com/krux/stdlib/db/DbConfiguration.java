package com.krux.stdlib.db;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.krux.stdlib.KruxStdLib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * The Krux platform uses multiple databases (RDS, Redshift) and often times an appliction needs
 * to connect to multiple databases.  Instead of specifying multiple db connection properties in
 * a properties file, the DbConvfiguration aims to encapsulate all the database connection configuration
 * in a central location and provides one mechanism to connect to the desired database using
 * a "key" that identifies the database.
 *
 * The database connection information along with the keys that can be used to reference the
 * individual databases can be found in resources/db.json.  Users of this class can also pass
 * a file that contains the connection configuration for a custom environment in the same JSON
 * format.
 *
 * The configuration file contaiins one or more entries of the following format:
 * {"config_key": {"driver": "JDBC_DRIVER_CLASS", "url": "JDBC_URL", "user": "JDBC_USER", "password": "JDBC_PASSWORD"}}
 *
 * All four values (driver, url, user and password) must be specified.
 *
 * @author Vivek S. Vaidya
 */
public class DbConfiguration {

    private static final String MSG_PREFIX = "db_config";

    private static final Logger LOGGER = LoggerFactory.getLogger(DbConfiguration.class.getName());

    private static final String DB_CONFIG_FILE = "/db.json";

    private static final String JDBC_DRIVER = "driver";

    private static final String JDBC_URL = "url";

    private static final String JDBC_USER = "user";

    private static final String JDBC_PASSWORD = "password";

    /**
     * As of now, we support JDBC drivers for MySQL and Postgres.
     * To add support for other databases, simply add an entry for the JDBC driver
     * of desired database to the list below.
     */
    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
    }

    private Map<String, DbConfigurationEntry> _dbConfigurations = new HashMap<String, DbConfigurationEntry>();

    /**
     * Create an instance of the DbConfiguration class using the default database configuration
     * as defined in /resources/db.json
     */
    public DbConfiguration() {
        readDbConfigurations(new InputStreamReader(getClass().getResourceAsStream(DB_CONFIG_FILE)));
    }

    /**
     * Create an instance of the DbConfiguration class using the database configuration
     * contained in the specified filename
     *
     * @param dbConfigFile the filename that contains the database configuration information
     * @throws FileNotFoundException thrown back up to the caller if the file does not exist
     */
    public DbConfiguration(String dbConfigFile) throws FileNotFoundException {
        readDbConfigurations(new FileReader(dbConfigFile));
    }

    /**
     * Read the database configuration JSON data and load the DbConfigurationEntry objects
     * in the _dbConfigurations map.
     *
     * @param reader the source of the JSON db connection configuration
     */
    private void readDbConfigurations(Reader reader) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Map<String, String>> configContainer = mapper.readValue(
                    reader,
                    new TypeReference<Map<String, Map<String, String>>>(){}
            );
            for (Map.Entry<String, Map<String, String>> entry : configContainer.entrySet()) {
                String configKey = entry.getKey();
                Map<String, String> configData = entry.getValue();
                String driver = configData.get(JDBC_DRIVER);
                String url = configData.get(JDBC_URL);
                String user = configData.get(JDBC_USER);
                String password = configData.get(JDBC_PASSWORD);
                if (driver != null && url != null && user != null && password != null) {
                    _dbConfigurations.put(
                            configKey,
                            new DbConfigurationEntry(driver, url, user, password)
                    );
                }
            }
        } catch (IOException ioe) {
            LOGGER.error(MSG_PREFIX, ioe.getMessage());
            KruxStdLib.STATSD.count(String.format("%s_init", MSG_PREFIX), 1);
        }
    }

    /**
     * Get an RDS database connection from the specified db configuration.
     *
     * This version returns a connection that has autoCommit=False
     *
     * @param dbConfiguration the db configuration to use (see resources/db.json)
     *
     * @return the database connection if one can be established; null otherwise
     */
    public Connection getRDSConnection(String dbConfiguration) {
        return getDbConnection(dbConfiguration, false);
    }

    /**
     * Get an RDS database connection from the specified db configuration using the
     * specified "autoCommit" setting.

     * @param dbConfiguration the db configuration to use (see resources/db.json)
     * @param autoCommit the desired autoCommit value
     * @return the database connection if one can be established; null otherwise
     */
    public Connection getDbConnection(String dbConfiguration, boolean autoCommit) {
        DbConfigurationEntry configEntry = _dbConfigurations.get(dbConfiguration);
        Connection conn = null;
        if (configEntry != null) {
            String jdbcDriver = configEntry.getDriver();
            String jdbcUrl = configEntry.getUrl();
            String jdbcUser = configEntry.getUser();
            String jdbcPassword = configEntry.getPassword();
            if (jdbcDriver != null && jdbcUrl != null && jdbcUser != null & jdbcPassword != null) {
                try {
                    conn = DriverManager.getConnection(
                            jdbcUrl,
                            jdbcUser,
                            jdbcPassword
                    );
                    conn.setAutoCommit(autoCommit);
                    conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    conn = null;
                }
            }
        }
        return conn;
    }

    /**
     * Rollback the specified connection
     * @param connection
     */
    public static void rollback(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException sqe) {
                // an error occured while rolling the transaction back
                // there's really not much we can do at this point, so we eat the exception and return
            }
        }
    }

    /**
     * Rollback the specified connection
     * @param connection
     */
    public static void commit(Connection connection) {
        if (connection != null) {
            try {
                connection.commit();
            } catch (SQLException sqe) {
                // an error occured while rolling the transaction back
                // there's really not much we can do at this point, so we eat the exception and return
            }
        }
    }

    /**
     * Stupid, stupid workaround for MySQL's default Isolation level setting (REPEATABLE_READ) that
     * results in over-aggressive locking
     * @param ps
     * @param isBatch
     */
    public static void executeStatement(PreparedStatement ps, boolean isBatch) {
        int attempts = 0;
        boolean tryAgain = true;
        while (attempts < 5 && tryAgain) {
            try {
                if (isBatch) {
                    ps.executeBatch();
                } else {
                    ps.executeUpdate();
                }
                // update was successful, no need to try again
                tryAgain = false;
            } catch (SQLException sqe) {
                String message = sqe.getMessage().toLowerCase();
                if (message.indexOf("lock") > 0 && message.indexOf("exceeded") > 0) {
                    // exception due to deadlock
                    // increment attempts and try again
                    attempts++;
                } else {
                    // update failed because of some other reason than deadlock
                    sqe.printStackTrace();
                    tryAgain = false;
                }
            }
            if (tryAgain) {
                try {
                    Thread.sleep(30000L);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        for (String dbConfig : args) {
            LOGGER.info("Checking configuration: " + dbConfig);
            DbConfiguration dbConfiguration = new DbConfiguration();
            Connection conn = dbConfiguration.getRDSConnection(dbConfig);
            if (conn != null) {
                conn.close();
            } else {
                LOGGER.error("FAILED to establush  database connection using configuration '" + dbConfig + "'");
            }
        }
    }
}