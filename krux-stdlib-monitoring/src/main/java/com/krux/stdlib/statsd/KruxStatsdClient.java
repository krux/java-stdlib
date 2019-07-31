/**
 * 
 */
package com.krux.stdlib.statsd;

import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.krux.stdlib.KruxStdLib;

/**
 * @author cass
 * 
 */
public class KruxStatsdClient extends StatsdClient {

    final static Logger log = LoggerFactory.getLogger( KruxStatsdClient.class );

    private String keyNamespace;

    public KruxStatsdClient( String host, int port, Logger logger, KruxStdLib stdLib ) throws Exception {
        super( host, port, logger );
        init(stdLib);
    }

    protected void init(KruxStdLib stdLib) {
        keyNamespace = stdLib.getAppName().toLowerCase() + ".";
    }

    public boolean gauge( String key, long value ) {
        return stat( StatsdStatType.GAUGE, key, value, 1.0 );
    }

    public boolean stat( StatsdStatType type, String key, long value, double sampleRate ) {
        return super.stat( type, fullKey( key ), value, sampleRate );
    }

    private String fullKey( String appKey ) {
        return keyNamespace + appKey.toLowerCase();
    }
}
