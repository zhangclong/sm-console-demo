package com.uh.framework.dbproxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 *  Manages operations on SQL statements, including execution and tracking of write operations.
 */
public class StatementOperationManager {

    protected final Logger logger = LoggerFactory.getLogger("db-proxy");

    private final Queue<StatementOperation> queue = new ArrayDeque<>();

    private boolean activeProxy = true; // 是否启用代理机制

    private final static StatementOperationManager myInstance = new StatementOperationManager();

    public static StatementOperationManager getInstance() {
        return myInstance;
    }

    /**
     * Adds a new operation to the queue.
     *
     * @param op the operation to add
     * @return true if the operation was added successfully, false otherwise
     */
    public synchronized boolean addOperation(StatementOperation op) {
        logger.info("Adding operation: {}", op);
        //return queue.add(op); //TODO
        return false;
    }

    public synchronized List<StatementOperation> pollBatch(int batchSize) {
        List<StatementOperation> batch = new ArrayList<>();
        for (int i = 0; i < batchSize && !queue.isEmpty(); i++) {
            batch.add(queue.poll());
        }
        return batch;
    }

    public boolean isActiveProxy() {
        return activeProxy;
    }

    public void setActiveProxy(boolean activeProxy) {
        this.activeProxy = activeProxy;
    }
}
