/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deployment.protocols;

import peersim.config.*;
import peersim.core.*;
import deployment.entities.*;

/**
 *
 * @author nsa
 */
public class GossipResourceAllocatorObserver implements Control {

    /**
     * The protocol to operate on.
     * 
     * @config
     */
    private static final String PAR_PROT = "protocol";
    private final int pid;
    private final int totalCycles;
    private final String name;

    public GossipResourceAllocatorObserver (String name)
    {
        this.name = name;
        this.totalCycles = Configuration.getInt("simulation.cycles");
        pid = Configuration.getPid(name + "." + PAR_PROT);
    }

    public boolean execute ()
    {
        System.out.println("----------------------Cycle : " +
                peersim.cdsim.CDState.getTime() +
                "  --------------------------------------");
        for (int i = 0; i < Network.size(); i++)
        {
            GossipResourceAllocator peer = (GossipResourceAllocator) Network.get(i).getProtocol(pid);
            System.err.println("Start peer ("+i+") print:***************************");
            peer.print();
            System.err.println("End peer ("+i+") print ********************************");
        }       
        return false;
    }
    
}
