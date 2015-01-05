package com.krux.stdlib.cluster;

import java.util.Collections;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.utils.EnsurePath;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

public class CuratorGroupMembershipDemo {

    private final CuratorFramework _curator;
    private final Watcher _watcher;
    private String _memberName = null;
    private boolean _iAmLeader = false;
    private List<String> _children = null;

    public CuratorGroupMembershipDemo( String zkConnect ) throws Exception {
        _watcher = new Watcher() {
            @Override
            public void process( WatchedEvent event ) {
                try {
                    _children = _curator.getChildren().usingWatcher( this ).forPath( "/demo/group" );
                    Collections.sort( _children );
                    if ( _children.size() > 0 ) {
                        _iAmLeader = _children.get( 0 ).equals( _memberName );
                    }
                    System.out.println( "Processed event: Group members: " + _children );
                    System.out.println( "Processed event: Leader: " + _iAmLeader );
                } catch ( Exception e ) {
                    throw new RuntimeException( e );
                }
            }
        };

        _curator = CuratorFrameworkFactory.newClient( zkConnect, 10000, 2000, new RetryOneTime( 2000 ) );
        _curator.start();

        // Ensure the group node exists
        new EnsurePath( "/demo/group" ).ensure( _curator.getZookeeperClient() );

        // Start watching for children changes
        _children = _curator.getChildren().usingWatcher( _watcher ).forPath( "/demo/group" );
        System.err.println( "Group members: " + _children );

        // Register a new member
        String namePath = _curator.create().withMode( CreateMode.EPHEMERAL_SEQUENTIAL )
                .forPath( "/demo/group/member-", "data".getBytes() );
        _memberName = namePath.substring( namePath.lastIndexOf( "/" ) + 1 );
        System.out.println( "this member id: " + _memberName );
    }

    public synchronized void close() throws InterruptedException {
        _curator.close();
    }

    public static void main( String[] args ) throws Exception {
        String zkConnect = "localhost:" + 2181;
        CuratorGroupMembershipDemo demo = new CuratorGroupMembershipDemo( zkConnect );
        Thread.sleep( 30000 );
        demo.close();
    }
}