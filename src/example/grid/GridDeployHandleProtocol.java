/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package example.grid;

import peersim.cdsim.CDProtocol;
import peersim.core.*;
import peersim.cdsim.CDState;
import java.util.Vector;
/**
 *
 * @author 2906095
 */
public class GridDeployHandleProtocol implements CDProtocol{
    
    
    public Vector<GridComponent> components ;
    public Vector<Integer> componentStartCycle ; 
    public Vector<Integer> componentIDs;
    
    GridBrokerProtocol gbp;
    GridCDProtocol gcdp;
    
    public boolean resourceCaseChanged;//True if components were deployed/removed to this node in this cycle
    
    public GridDeployHandleProtocol(String Prefix){
        
    }
    
    void Initialize(){
        
        components = new Vector<GridComponent>();
        componentStartCycle = new Vector<Integer>();
        componentIDs = new Vector<Integer>();
    }
    
    @Override
    public void nextCycle(Node node, int ProtocolID){
        
        GridNode gNode=(GridNode)node;
        
        int currentCycle = CDState.getCycle();
        // Remove Expired components
        for(int i = 0; i< components.size();i++){
            
            if((currentCycle - componentStartCycle.get(i)) >= components.get(i).TotalSimulationCycles){
                
                gNode.FreeCPU += components.get(i).CPUUsage;
                gNode.FreeMemory += components.get(i).MemoryUsage;
                
                gNode.resourceState.AvailabbleCPU = gNode.FreeCPU;
                gNode.resourceState.availableMemory=gNode.FreeMemory;
                gNode.resourceState.Cycle=CDState.getCycle();
                
                components.removeElementAt(i);
                componentIDs.removeElementAt(i);
                componentStartCycle.removeElementAt(i);
                
                resourceCaseChanged = true;
                
                GridConfig.executedComponents++;
            }
        }
        
        
        
        
        GridNode brokerNode = gNode.getBroker();

        gcdp = (GridCDProtocol)brokerNode.getProtocol(GridNode.getGridCDProtocolID(brokerNode));
        
        // Pass Deployed components Information to Broker Node
        gbp = (GridBrokerProtocol)brokerNode.getProtocol(GridNode.getBrokerProtocolID(brokerNode));
        gbp.readDeployedComponentsFromNode(componentIDs, componentStartCycle
                , new NodeIdentifier(node.getIndex(), node.getID()));
        
    }

    
    public boolean DeployComponent(GridComponent c,Node node, int protocolID){
        
        GridNode gNode=(GridNode)node;
        if (gNode.FreeCPU>=c.CPUUsage && gNode.FreeMemory>=c.MemoryUsage && gNode.OS == c.OS){
            
            components.add(c);
            componentIDs.add(c.id);
            componentStartCycle.add(CDState.getCycle());
            
            gNode.FreeCPU -=c.CPUUsage;
            gNode.FreeMemory -=c.MemoryUsage;
            
            gNode.resourceState.AvailabbleCPU = gNode.FreeCPU;
            gNode.resourceState.availableMemory=gNode.FreeMemory;
            gNode.resourceState.Cycle=CDState.getCycle();
            
            resourceCaseChanged = true;
            
            return true;
            
        }else
            return false;
        
    }
    
   public boolean containsComponent(int typeID){
       
       if (components.isEmpty())return false;
       
       for (int i = 0; i< components.size();i++){
           
           if (typeID == components.get(i).typeID)
               return true;
       }
       return false;
   } 
    
    
    
@Override
    public Object clone ()
    {
        GridDeployHandleProtocol sl = null;
        try
        {
            sl = (GridDeployHandleProtocol) super.clone();
        } catch (CloneNotSupportedException e)
        {
        } // never happens
        return sl;
    }


}
