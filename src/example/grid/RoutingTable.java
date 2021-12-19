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
public class RoutingTable {
    public Vector<RoutingTableElement> table;
    public int brokerIndex;
    
    public RoutingTable(int index){
        table = new Vector<RoutingTableElement>();
        brokerIndex = index;
    
    }
    
}
