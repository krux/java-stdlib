/**
 * 
 */
package com.krux.stdlib;

import com.typesafe.config.Config;

/**
 * @author casspc
 *
 */
public interface KruxStdLibService {

    public void start();

    public void stop();

    public void initialize(Config config);

}
