/**
 * 
 */
package com.krux.stdlib.logging;

import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.rolling.RollingPolicy;
import ch.qos.logback.core.rolling.RolloverFailure;
import ch.qos.logback.core.rolling.helper.CompressionMode;

/**
 * @author casspc
 *
 */
public class KruxRollingPolicy implements RollingPolicy {

    /**
     * 
     */
    public KruxRollingPolicy() {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see ch.qos.logback.core.spi.LifeCycle#isStarted()
     */
    @Override
    public boolean isStarted() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see ch.qos.logback.core.spi.LifeCycle#start()
     */
    @Override
    public void start() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see ch.qos.logback.core.spi.LifeCycle#stop()
     */
    @Override
    public void stop() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see ch.qos.logback.core.rolling.RollingPolicy#getActiveFileName()
     */
    @Override
    public String getActiveFileName() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see ch.qos.logback.core.rolling.RollingPolicy#getCompressionMode()
     */
    @Override
    public CompressionMode getCompressionMode() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see ch.qos.logback.core.rolling.RollingPolicy#rollover()
     */
    @Override
    public void rollover() throws RolloverFailure {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see ch.qos.logback.core.rolling.RollingPolicy#setParent(ch.qos.logback.core.FileAppender)
     */
    @Override
    public void setParent( FileAppender arg0 ) {
        // TODO Auto-generated method stub

    }

}
