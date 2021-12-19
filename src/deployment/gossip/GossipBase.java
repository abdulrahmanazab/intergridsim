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
 *  This class has the basic methods needed for the protocol implementation.
 * Basic idea:
 * a node is either a domain controller or not: 
 * in case it is domain controller, the node shares information within a domain 
 * and cross domains (LAN or WAN).
 * 
 * @author nsa
 */
public class GossipBase implements Identifiable {

    protected long id = -1;
    protected boolean isDomainController = false;
    protected LANConsumption LAN = null;
    protected WANConsumption WAN = null;

    public GossipBase ()
    {
      
    }

    /**
     * Get the value of id_
     *
     * @return the value of id_
     */
    public long getId ()
    {
        return this.id;
    }

    /**
     * Set the value of id_
     *
     * @param id_ new value of id_
     */
    public void setId (long i)
    {
        this.id = i;

    }

    /**
     * 
     * @return true if the node is a domain controller node
     */
    public boolean isIsDomainController ()
    {
        return isDomainController;
    }

    /**
     * flagg the node to be domain controller or not
     * @param isDomainContr
     */
    public void setIsDomainController (boolean isDomainContr)
    {
        this.isDomainController = isDomainContr;
    }

    public void share (Node node, Node neigbh, int protocolID)
    {
        GossipBase local = (GossipBase) node.getProtocol(protocolID);
        GossipBase neighbour = (GossipBase) neigbh.getProtocol(protocolID);
        
        if (neighbour.isDomainController && local.isDomainController)
        {

            //Merge
            WANConsumption merge = WANConsumption.merge(this.WAN, neighbour.WAN);
            //assign
            WANConsumption.Copy(merge, WAN);
            WANConsumption.Copy(merge, neighbour.WAN);

        }
        else
        { //Merge
            LANConsumption merge = LANConsumption.merge(this.LAN, neighbour.LAN);
            //assign
            LANConsumption.Copy(merge, LAN);
            LANConsumption.Copy(merge, neighbour.LAN);

        }
        

    }

    /**
     * 
     * @param c  component to deploy at this node
     * @return true if the component resource needs fits into the node available resources
     */
    public boolean deploy (Component c)
    {
        NodeConsumption temp = LAN.getConsumption(id);
        if (temp != null)
        {
            boolean ok = temp.addComponent(c);
            LAN.setConsumption(temp);
            return ok;
        } else
        {
            return false;
        }
    }

    /**
     *
     * @param c component to remove from this node
     * @return true if the node removes the component (i.e the component was actually running at this node ) 
     */
    public boolean undeploy (Component c)
    {
        NodeConsumption temp = LAN.getConsumption(id);
        if (temp != null)
        {
            boolean ok = temp.removeComponent(c);
            LAN.setConsumption(temp);
            return ok;
        } else
        {
            return false;
        }
    }

    public LANConsumption getLAN() {
        return LAN;
    }

    public void setLAN(LANConsumption LAN) {
        this.LAN = LAN;
    }

    public WANConsumption getWAN() {
        return WAN;
    }

    public void setWAN(WANConsumption WAN) {
        this.WAN = WAN;
    }


    /**
     * 
     */
    public void print ()
    {
        System.out.println(this.toString());
        this.LAN.print();     
        this.WAN.print();
         System.out.println();
       
    }

    @Override
    public String toString ()
    {
        String s = "Node : " + this.getId();
        if (this.isDomainController)
        {
            s += "[DC]";
        }
        //s += " \t Runnig Components : " + this.LAN.getConsumption(id).totalComponents();
        return s;
    }
}
