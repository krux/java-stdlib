package com.krux.stdlib.shutdown;

public abstract class ShutdownTask implements Shutdownable {
    
    private int _priority;
    
    public ShutdownTask( int priority ) {
        _priority = priority;
    }

    @Override
    public int compareTo( ShutdownTask task ) {
        return this.getPriority() - task.getPriority();
    }

    @Override
    public int getPriority() {
        return _priority;
    }

}
