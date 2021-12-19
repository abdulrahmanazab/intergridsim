/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example.grid;

import peersim.core.*;
/**
 *
 * @author Azab
 */
public class NodeChrunElement {
    public Node node;
    public int lastDownCycle;// In case of UP
    public int lastUPCycle;// In case of DOWN
    public int churnPeriod;//No. of cycle this node will remain up/down
    
    public NodeChrunElement(Node node,int churnPeriod){
        this.node = node;
        this.churnPeriod = churnPeriod;
        lastDownCycle = 0;
        lastUPCycle = 0;
    }
    
}
