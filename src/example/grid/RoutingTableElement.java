/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example.grid;

import java.util.Vector;
/**
 *
 * @author Azab
 */
public class RoutingTableElement {
    public Vector<Integer> hops;
    public int brokerIndex;
    
    public RoutingTableElement(int index){
        hops = new Vector<Integer>();
        brokerIndex = index;    
    }
    
    public RoutingTableElement(int index, Vector<Integer> hopsArray){
        hops = (Vector<Integer>) hopsArray.clone();
        brokerIndex = index;    
    }
}
