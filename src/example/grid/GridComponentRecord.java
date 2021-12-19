/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package example.grid;

import java.util.Vector;

/**
 *
 * @author 2906095
 */
public class GridComponentRecord {
    
    public long ID;
    public Vector<NodeIdentifier> nodes;
    public Vector<Integer> StartCycle ;
    public Vector<Integer> RecordedAt;
    
    public GridComponentRecord(long setID){
        
        ID = setID;
        nodes = new Vector<NodeIdentifier>();
        StartCycle = new Vector<Integer>();
        RecordedAt = new Vector<Integer>();
    }
    

}
