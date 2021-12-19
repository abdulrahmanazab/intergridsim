/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deployment.gossip;

import peersim.config.*;
import peersim.core.*;
import deployment.model.*;

/**
 *
 * @author nsa
 */
public class GossipInitializer implements Control {

    private static final String PAR_PROT = "protocol";
    private static final String NR_DOMAIN_NODES = "init.rnd.domain_nodes";    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------
    private int mainIndex = -1;
    private final int pid;
    private final int nrOfDomains;

    public GossipInitializer (String prefix)
    {

        pid = Configuration.getPid(prefix + "." + PAR_PROT);
        nrOfDomains = Configuration.getInt(NR_DOMAIN_NODES);
    }
    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------
    /**
     * Initializes 
     */
    public boolean execute ()
    {
        // GossipResourceManager main = (GossipResourceManager) Network.get(mainIndex_).getProtocol(pid);
        //  main.setResponsible(true);
        for (int i = 0; i < Network.size(); i++)
        {
            long ID = Network.get(i).getID();
            GossipAllocation peer = (GossipAllocation) Network.get(i).getProtocol(pid);
            if (i < nrOfDomains)
            {
                peer.setIsDomainController(true);
            }
            peer.setId(ID);
            
            peer.LAN=new LANConsumption();
            peer.WAN=new WANConsumption();   
            
            NodeConsumption c = new NodeConsumption();
            c.setId(ID);
            c.setTime(0);
            peer.LAN.setConsumption(c);

            Histogram hist = new Histogram();
            hist.setId(ID);
            hist.setTime(0);
            peer.WAN.setHistogram(hist);


            peer.print();
        }
        System.out.println("Finished  Initialization ");
        System.out.println();
        return false;
    }
}
