
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


package peersim.dynamics;

import peersim.graph.*;
import peersim.core.*;
import peersim.config.*;

/**
 * 
 */
public class WireDomainStar extends WireGraph {

//--------------------------------------------------------------------------
//Parameters
//--------------------------------------------------------------------------

  /**
     * Nr of nodes that are domain root nodes
     */
  private static final String NR_DOMAIN_NODES = "domain_nodes";  

//--------------------------------------------------------------------------
//Fields
//--------------------------------------------------------------------------

 /**
     * 
     */
 private final int nrOfDomains;

//--------------------------------------------------------------------------
//Initialization
//--------------------------------------------------------------------------

/**
 * Standard constructor that reads the configuration parameters.
 * Invoked by the simulation engine.
 * @param prefix the configuration prefix for this class
 */
public WireDomainStar(String prefix)
{
	super(prefix);
	nrOfDomains = Configuration.getInt(prefix + "." + NR_DOMAIN_NODES);
}

//--------------------------------------------------------------------------
//Methods
//--------------------------------------------------------------------------

/** Calls {@link GraphFactory#domainInStar}. */
public void wire(Graph g) {

	GraphFactory.domainInStar(g,nrOfDomains);
         for (int i = 0; i < Network.size(); i++)
        {
            System.out.println(" node " + i + "  Neighbours ");
            java.util.Collection<Integer> c = g.getNeighbours(i);
            System.out.print(c);
            System.out.println(" ----------------------");
        }
}

}