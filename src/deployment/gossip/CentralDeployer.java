/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deployment.gossip;

import peersim.config.*;
import peersim.core.*;
import deployment.model.*;
import java.util.*;

/**
 *
 * @author nsa
 */
public class CentralDeployer implements Control {

    private static final String PAR_PROT = "protocol";
    private final int pid;
    private final int totalCycles;
    private final String name;
    private Vector<Long> IDs = new Vector<Long>();

    public CentralDeployer(String name) {
        this.name = name;
        this.totalCycles = Configuration.getInt("simulation.cycles");
        pid = Configuration.getPid(name + "." + PAR_PROT);
    }

    public boolean execute() {

        long frequency = 2;
        long currentCycle = peersim.cdsim.CDState.getTime();
        Component c = new Component();
        long id = (long) (Math.random() * (double) Network.size());
        IDs.add(id);
        int index = (int) id;
        c.setId(id);
        c.setMu(5);
        c.setSigma(1);
        if (currentCycle % frequency == 0) {
            if (index < Network.size()) {
                GossipBase node = (GossipBase) Network.get(index).getProtocol(pid);
                boolean ok = node.deploy(c);
                System.out.println("****************************  Deployment at Cycle : " + currentCycle + "   Deployed: " + ok);
                System.out.println();

                System.out.println("Deployed at node : " + node.getId());
                node.print();

                GossipBase printerNode = (GossipBase) Network.get(1).getProtocol(pid);
                printerNode.print();
            }
        }


        return false;
    }

    public void CentralDeploy(Component c) {
        GossipBase dc = null;
        for (int i = 0; i < Network.size(); i++) {
            GossipBase node = (GossipBase) Network.get(i).getProtocol(pid);
            if (node.isDomainController) {
                dc = (GossipBase) Network.get(i).getProtocol(pid);
                break;
            }
        }
        if (dc != null)
        {
            //Get WAN for firs analysis/
            WANConsumption wan=dc.WAN;
            //Apply fuzzy to find out best sub net.
            int lanIndex=-1;
           // have found a good Lan.
            GossipBase goodDc = (GossipBase) Network.get(lanIndex).getProtocol(pid);
            LANConsumption lan= goodDc.LAN;
            //Fuzzy to find out best node in the LAN
            int nodeIndex=-1;
             GossipBase goodNode = (GossipBase) Network.get(nodeIndex).getProtocol(pid);
             boolean ok= goodNode.deploy(c);
             if(ok)
             {
                 //nice
             }
             else
             {
                 //what to do.
             }

        }
    }
}
