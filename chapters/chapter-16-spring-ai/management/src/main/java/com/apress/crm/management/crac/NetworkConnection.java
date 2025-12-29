package com.apress.crm.management.crac;

/**
 * Simulates a network connection that needs to be managed during CRaC checkpoints.
 * In a real application, this would represent an actual socket, database connection,
 * or other I/O resource that cannot be serialized in a checkpoint.
 */
public class NetworkConnection {

    private boolean isOpen = false;
    private String connectionId;

    /**
     * Opens the network connection.
     * In a real application, this would establish a socket connection,
     * database connection pool, etc.
     */
    public void open() {
        this.connectionId = "conn-" + System.currentTimeMillis();
        this.isOpen = true;
        System.out.println("NetworkConnection [" + connectionId + "] opened");
    }

    /**
     * Closes the network connection.
     * This must be called before a CRaC checkpoint is taken.
     */
    public void close() {
        if (isOpen) {
            System.out.println("NetworkConnection [" + connectionId + "] closed");
            this.isOpen = false;
            this.connectionId = null;
        }
    }

    /**
     * Simulates sending data over the network.
     * @param data the data to send
     * @return true if successful, false if connection is closed
     */
    public boolean send(String data) {
        if (!isOpen) {
            throw new IllegalStateException("Cannot send data: connection is closed");
        }
        System.out.println("NetworkConnection [" + connectionId + "] sending: " + data);
        return true;
    }

    /**
     * Checks if the connection is currently open.
     * @return true if open, false otherwise
     */
    public boolean isOpen() {
        return isOpen;
    }

    /**
     * Gets the connection identifier.
     * @return the connection ID, or null if closed
     */
    public String getConnectionId() {
        return connectionId;
    }
}
