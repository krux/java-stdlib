package com.krux.stdlib.utils;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Siddharth Sharma
 */
public class DbUtilsTest {

    @Test
    @Ignore
    public void getDbConnection() {
        Assert.assertNotNull(DbUtils.getDbConnection("com.mysql.jdbc.Driver", "jdbc:mysql://127.0.0.1:33306/rb_krux",
            "writer",
            "writer"));
        Assert.assertNotNull(DbUtils.getDbConnection("com.mysql.cj.jdbc.Driver", "jdbc:mysql://127.0.0"
            + ".1:33306/rb_krux", "writer", "writer"));
    }
}
