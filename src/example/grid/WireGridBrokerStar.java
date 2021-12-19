/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package example.grid;

/**
 *
 * @author 2906095
 */


import peersim.graph.*;
import peersim.core.*;
import peersim.config.*;
import peersim.dynamics.*;

/**
 * 
 */
public class WireGridBrokerStar extends WireGraph {

//--------------------------------------------------------------------------
//Parameters
//--------------------------------------------------------------------------
    /**
     * Nr of nodes that are domain root nodes
     */
    private static final String NR_BROKER_NODES = "brokers";//--------------------------------------------------------------------------
//Fields
//--------------------------------------------------------------------------
    /**
     * 
     */
    private final int nrOfBrokers;

//--------------------------------------------------------------------------
//Initialization
//--------------------------------------------------------------------------
    /**
     * Standard constructor that reads the configuration parameters.
     * Invoked by the simulation engine.
     * @param prefix the configuration prefix for this class
     */
    public WireGridBrokerStar (String prefix)
    {
        super(prefix);
        nrOfBrokers = Configuration.getInt(prefix + "." + NR_BROKER_NODES,1);
    }

//--------------------------------------------------------------------------
//Methods
//--------------------------------------------------------------------------
    /** Calls {@link GraphFactory#domainInRing}. */
    public void wire (Graph g)
    {

        GraphFactory.GridBrokerStar(g, nrOfBrokers);
        
        String isBroker;
        for (int i = 0; i < Network.size(); i++)
        {
            if(((GridNode)Network.get(i)).isBroker) isBroker ="[Broker]";
            else isBroker ="[Node]";
            
            System.out.println(" node " + i + "  "+isBroker+"  Neighbours ");
            java.util.Collection<Integer> c = g.getNeighbours(i);
            System.out.print(c);
            System.out.println(" ----------------------");
        }
    }
}
