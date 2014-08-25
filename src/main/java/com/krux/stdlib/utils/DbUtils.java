package com.krux.stdlib.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A collection of utility methods to interact with the Krux Console RDS instance.
 *
 * @author Vivek S. Vaidya
 */
public class DbUtils {

    /**
     * Get a database connection for the specified environment.
     *
     * @param env the environment for the job (dev or prod)
     * @return the database connection if one could be established; null otherwise
     */
    public static Connection getDbConnection(String env) {
        ConfigProperties kprops = new ConfigProperties(env);
        Connection conn = null;
        String errorMsg = null;
        try {
            Class.forName(kprops.getJdbcDriver());
            conn = DriverManager.getConnection(kprops.getJdbcUrl(), kprops.getJdbcUser(),
                    kprops.getJdbcPassword());
        } catch (Exception ex) {
            errorMsg = ex.getMessage();
            conn = null;
        }
        return conn;
    }

    /**
     * Close the specified database connection.
     *
     * @param conn the database connection to close.
     */
    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException sqe) {
                // an error occured while closing the connection.
                // there's really not much we can do at this point, so we eat the exception and return
            }
        }
    }

    /**
     * Close the specified (Prepared) Statement
     *
     * @param ps the statement to close
     */
    public static void close(Statement ps) {
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException sqe) {
                // an error occured while closing the statement
                // there's really not much we can do at this point, so we eat the exception and return
            }
        }
    }

    /**
     * Close the specified result set.
     *
     * @param rs the result set to close
     */
    public static void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException sqe) {
                // an error occured while closing the statement
                // there's really not much we can do at this point, so we eat the exception and return
            }
        }
    }
}
