package uk.ac.warwick.util.queue.activemq;

interface MassListenerController {

    /**
     * Stop all listeners on this queue from listening.
     */
    public abstract void stopAllListeners();

    /**
     * Generally only used after having called stopAllListeners,
     * this calls 
     */
    public abstract void startAllListeners();

}