/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deployment.protocols;

import peersim.config.*;
import peersim.core.*;
import deployment.entities.*;
import java.util.*;

/**
 *
 * @author nsa
 */
public class CentralServiceDeployer implements Control {

    /**
     * The protocol to operate on.
     * 
     * @config
     */
    private static final String PAR_PROT = "protocol";
    private final int pid;
    private final int totalCycles;
    private final String name;

    public CentralServiceDeployer (String name)
    {
        this.name = name;
        this.totalCycles = Configuration.getInt("simulation.cycles");
        pid = Configuration.getPid(name + "." + PAR_PROT);
    }

    public boolean execute ()
    {

        long frequency = 1;
        long currentCycle = peersim.cdsim.CDState.getTime();
        GaussianComponent c = new GaussianComponent();
        c.setId(currentCycle);
        c.setMaxResources(30);
        c.setMinResources(10);

        if (currentCycle % frequency == 0)
        {
            
            System.out.println("**********************************************  Deployment at Cycle : "+ currentCycle);
            System.out.println();
            // find nodes for deployment
            Vector<Long> ids = findLocation(c);
           
            if (ids != null)
            {

                for (int i = 0; i < ids.size(); i++)
                {
                    int index = nodeIndex(ids.get(i));
                    if (index >= 0)
                    {
                        GossipResourceAllocator peer = (GossipResourceAllocator) Network.get(index).getProtocol(pid);
                        // print before deployment
                         System.out.println();
                         System.out.println("--------Before deployment--------");
                        peer.print();
                        boolean ok = peer.deployLocal(c);
                        System.out.println("Choosen Node ID : " + peer.getId() + " \t Deployed : " + ok);
                        // print after deployment
                        System.out.println();
                        System.out.println("--------After deployment--------- ");
                        peer.print();
//                        if (ok)
//                        {
//                            break;
//                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * a component to be deployed
     * @param c
     * 
     * @return A vector of node IDs suitable for the that component 
     */
    private Vector<Long> findLocation (Component c)
    {
        Vector<Long> nodeIDS = new Vector<Long>();
        GossipResourceAllocator peer = (GossipResourceAllocator) Network.get(1).getProtocol(pid);
        NodeKnowledge knowledge = peer.getKnowledge();
        System.out.println("//////////////Deployment Contact node Status ");
        peer.print();                
        WorkLoad[] loads = knowledge.getLoads();
        if (loads != null)
        {
            for (int i = 0; i < loads.length; i++)
            {

                boolean tolerates =loads[i].tolerates(c.getMaxResourceNeeds());
                if (tolerates)
                {
                    nodeIDS.add(loads[i].getId());
                }
            }
        }
         System.out.println("Possible Locations : "+ nodeIDS.size()+ "\t IDs : "+ nodeIDS.toString());
         System.out.println("//////////////////////////////////////////");
         System.out.println();
        /**
         * 
         * @param l
         * @param c
         * @return
         */
        return nodeIDS;
    }

    /**
     * 
     * @param nodeID
     * @return
     */
    private int nodeIndex (Long nodeID)
    {
        int index = -1;
        for (int i = 0; i < Network.size(); i++)
        {
            long id = Network.get(i).getID();
            if (id == nodeID)
            {
                index = i;
                break;
            }
        }
        return index;
    }
}


