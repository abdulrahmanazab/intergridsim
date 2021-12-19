/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example.allocation;

import peersim.core.*;
import peersim.config.FastConfig;
import peersim.vector.SingleValueHolder;
import peersim.cdsim.CDProtocol;

/**
 *
 * @author nsa
 */
public class AllocationFunction extends SingleValueHolder implements CDProtocol {

    /**
     * An array that Resource state at a given cycle for a given node
     */
    protected double[][] resources_ = null;
    private boolean isResponsible_ = false;

    public AllocationFunction (String prefix) {
        super(prefix);

    }

    public void setResponsible (boolean b) {
        this.isResponsible_ = b;
        System.out.println("Ops");
    }

    public boolean getResponsible () {
        return this.isResponsible_;
    }

    /**
     * Using an underlying {@link Linkable} protocol choses a neighbor and
     *exchange resource information and merge that information.
     * 
     * @param node
     *            the node on which this component is run.
     * @param protocolID
     *            the id of this protocol in the protocol array.
     */
    public void nextCycle (Node node, int protocolID) {
        int linkableID = FastConfig.getLinkable(protocolID);
        Linkable linkable = (Linkable) node.getProtocol(linkableID);
        if (linkable.degree() > 0) {
            Node peer = linkable.getNeighbor(CommonState.r.nextInt(linkable.degree()));

            // Failure handling
            if (!peer.isUp()) {
                return;
            }
            AllocationFunction neighbor = (AllocationFunction) peer.getProtocol(protocolID);
            //Merge resource arrays.
            this.resources_[node.getIndex()][0] = this.value;
            int cycle = peersim.cdsim.CDState.getIntTime();
            this.resources_[node.getIndex()][1] = cycle;

            double[][] tmp = Merge(this.resources_, neighbor.resources_);

            if (tmp != null) {
                this.resources_ = tmp;
                neighbor.resources_ = tmp;
            }
             
        }
       

    }

    private double[][] Merge (double[][] a, double[][] b) {
        if (a == null && b != null) {
            return b;
        }
        if (a != null && b == null) {
            return a;
        }
        if (a == null && b == null) {
            return new double[Network.size()][2];
        }
        if (a != null && b != null && a.length == b.length && a[0].length == b[0].length) {
            //do merge
            double[][] result = new double[Network.size()][2];
            for (int i = 0; i < a.length; i++) {
                if (b[i][1] >= 0 && b[i][1] >= a[i][1]) {
                    result[i][0] = b[i][0];
                    result[i][1] = b[i][1];
                } else if (a[i][1] >= 0 && a[i][1] >= b[i][1]) {
                    result[i][0] = a[i][0];
                    result[i][1] = a[i][1];
                }
            }
            return result;
        }
        return null;
    }

   
    
   
}