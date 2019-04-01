package com.krux.stdlib.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.krux.stdlib.KruxStdLib;
import com.krux.stdlib.statsd.StatsdClient;

/**
 * A collection of utility methods to interact with the Krux Console RDS
 * instance.
 * 
 * @author Vivek S. Vaidya
 */
public class DbUtils {

    /**
     * Get a database connection for the specified environment.
     * 
     * @return the database connection if one could be established; null
     *         otherwise
     */
    private static final Logger log = LoggerFactory.getLogger( DbUtils.class );
    private static volatile DbUtils instance;

    /**
     * Returns a statically tracked instance, if not yet initialized, a new instance is created and returned.
     * @return
     */
    public static DbUtils get() {
        if (instance == null) {
            set(new DbUtils());
        }

        return instance;
    }

    /**
     * Utility method to allow callers to set an already configured instance for the static context.
     * @param instance
     */
    public static void set(DbUtils instance) {
        DbUtils.instance = instance;
    }

    private KruxStdLib stdLib;
    private StatsdClient statsd;

    public DbUtils() {
        this(KruxStdLib.get());
    }

    public DbUtils(KruxStdLib stdLib) {
        this(stdLib, stdLib.getStatsdClient());
    }

    public DbUtils(KruxStdLib stdLib, StatsdClient statsd) {
        this.stdLib = stdLib;
        this.statsd = statsd;
    }

    public Connection getDbConnection() {
        ConfigProperties kprops = new ConfigProperties(stdLib.getEnv());
        return getDbConnection( kprops.getJdbcDriver(), kprops.getJdbcUrl(), kprops.getJdbcUser(), kprops.getJdbcPassword() );
    }

    public Connection getDbConnection( String jdbcDriver, String jdbcUrl, String jdbcUser, String jdbcPassword ) {
        Connection conn = null;
        long start = System.currentTimeMillis();
        try {
            Class.forName( jdbcDriver );
            log.info( "Connecting to " + jdbcUrl + " as user " + jdbcUser );
            conn = DriverManager.getConnection( jdbcUrl, jdbcUser, jdbcPassword );
        } catch ( Exception ex ) {
            log.error( "db_util", ex );
            statsd.count( "db_util_get_db_conn_err", 1 );
            conn = null;
        }

        long time = System.currentTimeMillis() - start;
        log.info( "DB get connection took " + time + "ms." );
        statsd.time( "get_db_conn", time );
        return conn;
    }

    /**
     * Close the specified database connection.
     * 
     * @param conn
     *            the database connection to close.
     */
    public void close( Connection conn ) {
        long start = System.currentTimeMillis();
        if ( conn != null ) {
            try {
                conn.close();
            } catch ( SQLException sqe ) {
                // an error occured while closing the connection.
                // there's really not much we can do at this point, so we log
                // the exception and return
                String errorMsg = sqe.getMessage();
                log.error( "db_util", errorMsg );
                statsd.count( "db_util_close_db_conn_err", 1 );
            }
        }
        long time = System.currentTimeMillis() - start;
        log.info( "DB connection close took " + time + "ms." );
        statsd.time( "close_db_conn", time );
    }

    /**
     * Close the specified (Prepared) Statement
     * 
     * @param ps
     *            the statement to close
     */
    public void close( Statement ps ) {
        long start = System.currentTimeMillis();
        if ( ps != null ) {
            try {
                ps.close();
            } catch ( SQLException sqe ) {
                // an error occured while closing the statement
                // there's really not much we can do at this point, so we log
                // the exception and return
                String errorMsg = sqe.getMessage();
                log.error( "db_util", errorMsg );
                statsd.count( "db_util_close_ps_err", 1 );
            }
        }
        long time = System.currentTimeMillis() - start;
        log.info( "DB PreparedStatement close took " + time + "ms." );
        statsd.time( "close_db_ps", time );
    }

    /**
     * Close the specified result set.
     * 
     * @param rs
     *            the result set to close
     */
    public void close( ResultSet rs ) {
        long start = System.currentTimeMillis();
        if ( rs != null ) {
            try {
                rs.close();
            } catch ( SQLException sqe ) {
                // an error occured while closing the statement
                // there's really not much we can do at this point, so we log
                // the exception and return
                String errorMsg = sqe.getMessage();
                log.error( "db_util", errorMsg );
                statsd.count( "db_util_close_rs_err", 1 );
            }
        }
        long time = System.currentTimeMillis() - start;
        log.info( "DB ResultSet close took " + time + "ms." );
        statsd.time( "close_db_rs", time );
    }
}
