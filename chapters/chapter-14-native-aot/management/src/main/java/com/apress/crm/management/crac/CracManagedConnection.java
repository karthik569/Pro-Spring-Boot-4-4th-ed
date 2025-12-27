package com.apress.crm.management.crac;

import org.crac.Context;
import org.crac.Core;
import org.crac.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Demonstrates CRaC (Coordinated Restore at Checkpoint) resource management.
 *
 * This component manages a network connection that must be properly closed
 * before a checkpoint is taken and reopened after restoration.
 *
 * CRaC allows taking a snapshot of a running JVM and restoring it later
 * with near-instant startup times while preserving JIT optimizations.
 *
 * This component is disabled by default and can be enabled by setting:
 * management.crac.enabled=true in application.properties
 */
@Component
@ConditionalOnProperty(name = "management.crac.enabled", havingValue = "true", matchIfMissing = false)
public class CracManagedConnection implements Resource {

    private NetworkConnection connection;

    /**
     * Constructor registers this resource with the global CRaC context.
     * This ensures that beforeCheckpoint() and afterRestore() hooks
     * are called at the appropriate times.
     */
    public CracManagedConnection() {
        Core.getGlobalContext().register(this); // <1>
    }

    /**
     * Opens the network connection after the bean is constructed.
     * This is called during normal application startup.
     */
    @PostConstruct
    public void openConnection() {
        this.connection = new NetworkConnection(); // Simulate opening a connection
        this.connection.open();
    }

    /**
     * Closes the network connection when the bean is destroyed.
     * This is called during normal application shutdown.
     */
    @PreDestroy
    public void closeConnection() {
        if (this.connection != null) {
            this.connection.close();
        }
    }

    /**
     * CRaC lifecycle hook called before a checkpoint is taken.
     *
     * We must close all open resources (file handles, sockets, etc.)
     * because they cannot be serialized into the checkpoint image.
     *
     * @param context the CRaC context
     * @throws Exception if the resource cannot be safely closed
     */
    @Override
    public void beforeCheckpoint(Context<? extends Resource> context) throws Exception {
        System.out.println("CRaC: Closing network connection before checkpoint...");
        closeConnection(); // <2>
    }

    /**
     * CRaC lifecycle hook called after the JVM is restored from a checkpoint.
     *
     * We re-establish all the resources that were closed before the checkpoint.
     * The application resumes exactly where it left off, but with fresh I/O handles.
     *
     * @param context the CRaC context
     * @throws Exception if the resource cannot be reopened
     */
    @Override
    public void afterRestore(Context<? extends Resource> context) throws Exception {
        System.out.println("CRaC: Re-opening network connection after restore...");
        openConnection(); // <3>
    }

    /**
     * Provides access to the managed connection for other components.
     * In a real application, this might be used by services that need
     * to send data over the network.
     *
     * @return the network connection
     */
    public NetworkConnection getConnection() {
        return connection;
    }
}
