/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deployment.protocols;

import deployment.entities.*;
import peersim.config.*;
import peersim.core.*;
import java.util.*;


/**
 *
 * @author nsa
 */
public class GossipResourceAllocatorInitializer implements Control {
    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The load at the peak node.
     * 
     * @config
     */
    //private static final String MAIN = "Main_Node_Index";
    private static final String PAR_PROT = "protocol";
    private static final String NR_DOMAIN_NODES = "init.rnd.domain_nodes";
    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------
    //private int mainIndex_ = -1;
    private final int pid;
    private final int nrOfDomains;
    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    /**
     * Creates a new instance and read parameters from the config file.
     */
    public GossipResourceAllocatorInitializer (String prefix)
    {
       // mainIndex_ = Configuration.getInt(prefix + "." + MAIN);
        pid = Configuration.getPid(prefix + "." + PAR_PROT);
        nrOfDomains = Configuration.getInt(  NR_DOMAIN_NODES);
    }
    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------
    /**
     * Initializes NetworkLoad seen at each node
     */
    public boolean execute ()
    {
       // GossipResourceManager main = (GossipResourceManager) Network.get(mainIndex_).getProtocol(pid);
      //  main.setResponsible(true);
        for (int i = 0; i < Network.size(); i++)
        {            
            long ID = Network.get(i).getID();
            GossipResourceAllocator peer = (GossipResourceAllocator) Network.get(i).getProtocol(pid);
            peer.setKnowledge(new NodeKnowledge(Network.size()));
            if(i<nrOfDomains)
            {
                peer.setIsDomainController(true);
            }
            //peer.setComponents(new Vector<Component>());
            peer.setId(ID);   
            
        
            WorkLoad load=new WorkLoad();
            load.setId(ID);
            load.setMax(100);
//            int consumption = CommonState.r.nextInt(100);
//            load.addComponent(consumption);
            load.setTime(0);
            boolean test =peer.getKnowledge().setLoad(load, i);
            int a=0;
            peer.print();
            
        }
        System.out.println("Finished  Initialization ");
        System.out.println();
        return false;
    }
}
