/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deployment.gossip;

import deployment.model.*;
import peersim.core.*;
import peersim.config.FastConfig;
import peersim.cdsim.CDProtocol;

/**
 *
 * @author nsa
 */
public class GossipAllocation extends GossipBase implements CDProtocol {

    public GossipAllocation (String prefix)
    {
      
    }

    @Override
    public void nextCycle (Node node, int protocolID)
    {
        //update local resouce use + time
        //upadate histogram data +time    
        int cycle = peersim.cdsim.CDState.getIntTime();
        NodeConsumption temp = this.LAN.getConsumption(id);
        temp.setTime(cycle);
        this.LAN.setConsumption(temp);
        Histogram hist = this.WAN.getHistogram(id);
        hist.setTime(cycle);
        this.LAN.makeHistogram(hist);
        this.WAN.setHistogram(hist);

        //Find a neighbour
        int linkableID = FastConfig.getLinkable(protocolID);
        Linkable linkable = (Linkable) node.getProtocol(linkableID);
        if (linkable.degree() > 0)
        {
            Node neighbour = linkable.getNeighbor(CommonState.r.nextInt(linkable.degree()));


            // Failure handling
            if (!neighbour.isUp())
            {
                return;
            }
            //share with neigbhour    
            this.share(node, neighbour, protocolID);
        }
    }

    @Override
    public Object clone ()
    {
        GossipAllocation sl = null;
        try
        {
            sl = (GossipAllocation) super.clone();
        } catch (CloneNotSupportedException e)
        {
        } // never happens
        return sl;
    }
}
