/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package example.grid;

import peersim.core.CommonState;

/**
 *
 * @author 2906095
 */
public class GridNodeResourceState {
    
    public int NodeIndex = 0;
    
    public long NodeID = 0;
    
    public int AvailabbleCPU = 0; // Available CPU Cores
    
    public int availableMemory = 0; //Available memory size in GB
    
    public int OS = 0;// Operating system
    
    public long Cycle = 0; // The associated cycle for this reading
    
       
    public GridNodeResourceState(int index, long ID,int AvailableCPUCores, int availableMemoryGB, int OS, long associatedCycle){
    
    NodeIndex = index;
    NodeID = ID;
    AvailabbleCPU = AvailableCPUCores;
    availableMemory = availableMemoryGB;
    this.OS = OS;
    Cycle = associatedCycle;
    
    }
    
    public static double generateRandomAvailableCPUState(int TotalCPUSpeed){
        return CommonState.r.nextInt(TotalCPUSpeed/2);
        
    }
    
    public static long generateRandomAvailableMemoryState(long TotalMemorySize){
        return CommonState.r.nextLong(TotalMemorySize*4/5);
        
    }
    @Override
    public GridNodeResourceState clone(){
        return new GridNodeResourceState(NodeIndex, NodeID, AvailabbleCPU, availableMemory,OS, Cycle);
    }
    @Override        
    public String toString(){
        
        return "Node "+NodeIndex+":"+AvailabbleCPU+" Cores, "+availableMemory+" GB, OS: "+OS+", Cycle: "+Cycle;
    }
    

}
