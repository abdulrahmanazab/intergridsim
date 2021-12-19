/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package example.grid;

import java.util.ArrayList;
import java.util.Vector;
import peersim.Simulator;
import peersim.cdsim.CDSimulator;
import peersim.cdsim.CDState;
import peersim.core.CommonState;

/**
 *
 * @author 2906095
 */


public class GridNodeInfo {

    protected long NodeID = -1;
    
    protected long CPUSpeed = 0;
    
    protected long TotalMemorySize =0;
    
    protected long simulationCycles = 0;
    
    protected long i = 0; //Common counter
    
    protected GridNodeResourceState State ; // to be used for setting next state
    
    protected ArrayList<GridNodeResourceState> resourceState= new ArrayList<GridNodeResourceState>();
    
    public GridNodeInfo(long ID){
        
////        NodeID = ID;
////        simulationCycles = CDState.getEndTime();
////
////        for(i = 0; i< simulationCycles;i++)
////            {
////                resourceState.add(new GridNodeResourceState(1, TotalMemorySize, i));
////                               
////            }
    }
    
    public void setNodeID(long ID)
    {NodeID = ID;}
    public long getNodeID(long ID)
    {return NodeID;}
    
    public void setCPUSpeed(long speed)
    {CPUSpeed = speed;}
    public long getCPUSpeed()
    {return CPUSpeed;}
    
    public void setTotalMemorySize(long memorySizeMB)
    {TotalMemorySize = memorySizeMB;}
    public long getTotalMemorySize()
    {return TotalMemorySize;}
    
    public void setResourceInfo(double AvailableCPU,long AvailableMemory,long CurrentCycle){
        
////         State = new GridNodeResourceState(AvailableCPU,AvailableMemory,CurrentCycle);
////         
////         
////         resourceState.set((int)CurrentCycle, State);
        
        
    }
    
    
}
