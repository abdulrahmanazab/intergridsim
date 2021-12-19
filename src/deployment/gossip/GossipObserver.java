/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deployment.gossip;

import peersim.config.*;
import peersim.core.*;
import deployment.model.*;
import peersim.cdsim.CDState;

/**
 *
 * @author nsa
 */
public class GossipObserver implements Control{

    private static final String PAR_PROT = "protocol";
    private final int pid;
    private final int totalCycles;
    private final String name;

    public GossipObserver (String name)
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
        System.out.println("#####"+name+"#######"+Network.prototype.getClass().getName() );
        for (int i = 0; i < Network.size(); i++)
        {
            GossipAllocation peer = (GossipAllocation) Network.get(i).getProtocol(pid);
            
            //System.err.println("Start peer ("+i+") print:***************************"+CDState.getCycle());
            peer.print();
            //System.err.println("End peer ("+i+") print ********************************"+CDState.getCycle());
        }       
        return false;
    }
}
