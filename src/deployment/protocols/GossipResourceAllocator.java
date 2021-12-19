/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deployment.protocols;

import deployment.entities.*;
import peersim.core.*;
import peersim.config.FastConfig;
import peersim.cdsim.CDProtocol;
import java.util.*;

/**
 *
 * @author nsa
 */
public class GossipResourceAllocator implements Identifiable, CDProtocol, DeploymentManager, ResourceAllocator {

    private NodeKnowledge knowledge_ = null;
    protected long id_;
    private boolean isDomainController_ = false;

    public GossipResourceAllocator(String prefix) {
    }

    /**
     * Get the value of id_
     *
     * @return the value of id_
     */
    public long getId() {
        return this.id_;
    }

    /**
     * Set the value of id_
     *
     * @param id_ new value of id_
     */
    public void setId(long id) {
        this.id_ = id;
    }

    /**
     * 
     * @return
     */
    public NodeKnowledge getKnowledge() {
        return this.knowledge_;
    }

    /**
     * 
     * @param networkLoad
     */
    public void setKnowledge(NodeKnowledge networkLoad) {
        this.knowledge_ = networkLoad;
    }

    /**
     * 
     * @param node
     * @param protocolID
     */
    public void shareKnowledge(Node node, int protocolID) {
        int linkableID = FastConfig.getLinkable(protocolID);
        Linkable linkable = (Linkable) node.getProtocol(linkableID);
        if (linkable.degree() > 0) {
            Node peer = linkable.getNeighbor(CommonState.r.nextInt(linkable.degree()));
            // Failure handling
            if (!peer.isUp()) {
                return;
            }
            //Find a neighbor
            GossipResourceAllocator neighbor = (GossipResourceAllocator) peer.getProtocol(protocolID);

            //Merge
            NodeKnowledge merge = NodeKnowledge.Merge(this.getKnowledge(), neighbor.getKnowledge());
            this.setKnowledge(merge);
            neighbor.setKnowledge(merge);
        }
    }

    /**
     * Nothing yet 
     * @param components
     * @return
     * false all the time
     */
    public boolean deployGlobal(Component[] components) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * 
     * @param c
     * @return
     */
    public boolean deployLocal(Component c) {
        boolean ok = false;
        WorkLoad load = this.getKnowledge().getLoad(this.id_);
        ok = load.addComponent(c);
        if (ok) {
            int cycle = peersim.cdsim.CDState.getIntTime();
            load.setTime(cycle);
            ok = this.getKnowledge().update(load, true);
        }
        return ok;

    }

    public boolean undeployLocal(Component c) {
        boolean ok = false;
        WorkLoad load = this.getKnowledge().getLoad(this.id_);
        ok = load.removeComponent(c);
        if (ok) {
            int cycle = peersim.cdsim.CDState.getIntTime();
            load.setTime(cycle);
            ok = this.getKnowledge().update(load, true);
        }
        return ok;
       
    }

    /**
     *   1. Refresh own resource use
     *   2. Share network knoweldge with neighbor.
     * @param node
     * @param protocolID
     */
    public void nextCycle(Node node, int protocolID) {
        //1. Refresh own resource use
        //2. Share network knoweldge with neighbor.
        int cycle = peersim.cdsim.CDState.getIntTime();
        WorkLoad load = this.knowledge_.getLoad(this.id_);
        if (load != null) {
            load.setTime(cycle);
            this.knowledge_.update(load, true);
            this.shareKnowledge(node, protocolID);
        }

    }

    public boolean getIsDomainController() {
        return this.isDomainController_;
    }

    public void setIsDomainController(boolean isDomainController) {
        this.isDomainController_ = isDomainController;
    }

    public void print() {
        System.out.println(this.toString());
        this.knowledge_.print();
        System.out.println();
    }

    @Override
    public Object clone() {
        GossipResourceAllocator sl = null;
        try {
            sl = (GossipResourceAllocator) super.clone();
        } catch (CloneNotSupportedException e) {
        } // never happens
        return sl;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        String s = "Node : " + this.getId();
        if (this.isDomainController_) {
            s += "[DC]";
        }
        s += " \t Runnig Components : " + this.knowledge_.getLoad(this.id_).getComponents().size();
        return s;
    //return s + this.networkLoad_.toString();
    }
}
