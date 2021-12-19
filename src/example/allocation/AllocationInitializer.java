/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author nsa
 */
package example.allocation;

import peersim.config.*;
import peersim.core.*;
import peersim.vector.SingleValue;

/**
 * 
 * @author nsa
 */
public class AllocationInitializer implements Control {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The load at the peak node.
     * 
     * @config
     */
    private static final String MAIN = "Main_Node_Index";
    private static final String PAR_PROT = "protocol";
    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------
    private int mainIndex_ = -1;
    private final int pid;
    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    /**
     * Creates a new instance and read parameters from the config file.
     */
    public AllocationInitializer (String prefix) {
        mainIndex_ = Configuration.getInt(prefix + "." + MAIN);
        pid = Configuration.getPid(prefix + "." + PAR_PROT);
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------
    /**
     * Initializes the resource array seen by each node
     * Assign a responsible node, the one contacted by the client
     */
    public boolean execute () {

        AllocationFunction main = (AllocationFunction) Network.get(mainIndex_).getProtocol(pid);
        main.setResponsible(true);

        for (int i = 0; i < Network.size(); i++) {
            AllocationFunction peer = (AllocationFunction) Network.get(i).getProtocol(pid);
            peer.resources_ = new double[Network.size()][2];

            for (int j = 0; j < peer.resources_.length; j++) {
                peer.resources_[j][0] = 0;
                peer.resources_[j][1] = -1;

            }


        }

        return false;
    }
}
