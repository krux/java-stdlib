package com.krux.stdlib.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.krux.stdlib.KruxStdLib;

/**
 * A collection of utility methods to interact with the Krux Console RDS
 * instance.
 * 
 * @author Vivek S. Vaidya
 */
public class DbUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbUtils.class.getName());

    /**
     * Get a database connection for the specified environment.
     *
     * @return the database connection if one could be established; null
     *         otherwise
     */
    public static Connection getDbConnection() {
        ConfigProperties kprops = new ConfigProperties();
        return getDbConnection( kprops.getJdbcDriver(), kprops.getJdbcUrl(), kprops.getJdbcUser(), kprops.getJdbcPassword() );
    }

    public static Connection getDbConnection( String jdbcDriver, String jdbcUrl, String jdbcUser, String jdbcPassword ) {
        Connection conn = null;
        long start = System.currentTimeMillis();
        try {
            Class.forName( jdbcDriver );
            LOGGER.info("Connecting to " + jdbcUrl + " as user " + jdbcUser);
            conn = DriverManager.getConnection( jdbcUrl, jdbcUser, jdbcPassword );
        } catch ( Exception ex ) {
            LOGGER.error("db_util", ex);
            KruxStdLib.STATSD.count( "db_util_get_db_conn_err", 1 );
            conn = null;
        }
        long time = System.currentTimeMillis() - start;
        LOGGER.info("DB get connection took " + time + "ms.");
        KruxStdLib.STATSD.time( "get_db_conn", time );
        return conn;
    }

    /**
     * Close the specified database connection.
     * 
     * @param conn
     *            the database connection to close.
     */
    public static void close( Connection conn ) {
        long start = System.currentTimeMillis();
        if ( conn != null ) {
            try {
                conn.close();
            } catch ( SQLException sqe ) {
                // an error occured while closing the connection.
                // there's really not much we can do at this point, so we LOGGER
                // the exception and return
                String errorMsg = sqe.getMessage();
                LOGGER.error("db_util", errorMsg);
                KruxStdLib.STATSD.count( "db_util_close_db_conn_err", 1 );
            }
        }
        long time = System.currentTimeMillis() - start;
        LOGGER.info("DB connection close took " + time + "ms.");
        KruxStdLib.STATSD.time( "close_db_conn", time );
    }

    /**
     * Close the specified (Prepared) Statement
     * 
     * @param ps
     *            the statement to close
     */
    public static void close( Statement ps ) {
        long start = System.currentTimeMillis();
        if ( ps != null ) {
            try {
                ps.close();
            } catch ( SQLException sqe ) {
                // an error occured while closing the statement
                // there's really not much we can do at this point, so we log
                // the exception and return
                String errorMsg = sqe.getMessage();
                LOGGER.error("db_util", errorMsg);
                KruxStdLib.STATSD.count( "db_util_close_ps_err", 1 );
            }
        }
        long time = System.currentTimeMillis() - start;
        LOGGER.info("DB PreparedStatement close took " + time + "ms.");
        KruxStdLib.STATSD.time( "close_db_ps", time );
    }

    /**
     * Close the specified result set.
     * 
     * @param rs
     *            the result set to close
     */
    public static void close( ResultSet rs ) {
        long start = System.currentTimeMillis();
        if ( rs != null ) {
            try {
                rs.close();
            } catch ( SQLException sqe ) {
                // an error occured while closing the statement
                // there's really not much we can do at this point, so we log
                // the exception and return
                String errorMsg = sqe.getMessage();
                LOGGER.error("db_util", errorMsg);
                KruxStdLib.STATSD.count( "db_util_close_rs_err", 1 );
            }
        }
        long time = System.currentTimeMillis() - start;
        LOGGER.info("DB ResultSet close took " + time + "ms.");
        KruxStdLib.STATSD.time( "close_db_rs", time );
    }
}
