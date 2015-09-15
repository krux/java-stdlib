/**
 * 
 */
package com.krux.stdlib.http.server;

import com.krux.stdlib.AppState;
import com.krux.stdlib.KruxStdLibService;

import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author casspc
 *
 */
public interface HttpService extends KruxStdLibService {
    
    public void setStatusCodeAndMessage(AppState state, String message);
    public void resetStatusCodeAndMessageOK();
    public void addAdditionalStatus(String key, Object value);
    public void registerHttpHandler(String url, ChannelInboundHandlerAdapter handler);

}
