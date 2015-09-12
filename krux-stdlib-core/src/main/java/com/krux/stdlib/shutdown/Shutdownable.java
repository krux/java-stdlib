package com.krux.stdlib.shutdown;

public interface Shutdownable extends Runnable, Comparable<ShutdownTask> {

    public int getPriority();

}
