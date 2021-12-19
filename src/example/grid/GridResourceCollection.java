/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example.grid;

import java.util.Vector;
import peersim.cdsim.CDState;
import peersim.core.Network;
/**
 *
 * @author Azab
 */
public class GridResourceCollection {
    public Vector<GridNodeResourceState> collection;
    public int cycle, brokerIndex, brokerNodeID;
    public GridHistogram histogram;
    public int queueLength;
    public float avgCPUUtilization;
    public int availableCPUs, availableMemoryGB;
    
    public boolean isSaturated;
    
    
    public GridResourceCollection(){
        collection  =  new Vector<GridNodeResourceState>();
        histogram = new GridHistogram();
        cycle = -1;// For the update process which take the cycle value as a sign for the first node update to work
        queueLength = 0;//Current number of waiting jobs in the queue of this Grid domain
        availableCPUs = 0;//Number of free CPUs in the domain
        availableMemoryGB = 0;//Number of total available GB in the domain
        avgCPUUtilization = 0;//(Number of free CPUs)/(Total number of CPUs in the domain)
        isSaturated = false;//True if the pool is saturated (All Nodes are full both memory and cpu)
        
    }
    public GridResourceCollection(int brokerIndex){
        collection  =  new Vector<GridNodeResourceState>();
        histogram = new GridHistogram();
        this.brokerIndex = brokerIndex;
        cycle = -1;// For the update process which take the cycle value as a sign for the first node update to work
    }
    public GridResourceCollection(int brokerIndex, int brokerNodeID){
        collection  =  new Vector<GridNodeResourceState>();
        histogram = new GridHistogram();
        this.brokerIndex = brokerIndex;
        this.brokerNodeID = brokerNodeID;
        cycle = -1;// For the update process which take the cycle value as a sign for the first node update to work
    }
    
    public void addElement(GridNodeResourceState rs){
        
         boolean isUpdate = false;
         if(collection.isEmpty())
            collection.add(rs.clone());
         else{
            
            for(int i=0; i<collection.size();i++){
                
                if(i >= collection.size())break;
                if(collection.get(i).NodeID == rs.NodeID){
                    
//////                     if(rs.NodeIndex == 20)
//////                        System.err.println("("+collection.get(i).AvailabbleCPU+","+rs.AvailabbleCPU+")"+"("+collection.get(i).availableMemory+","+rs.availableMemory+")"+
//////                        "("+collection.get(i).OS+","+rs.OS+")");
                    
                    this.histogram.updateNodeParameters(collection.get(i),rs);
                    collection.setElementAt(rs.clone(), i);
                                          
                    isUpdate = true;
                    break;
                    
                }
                
                    
            }
            if(!isUpdate){ 
                collection.add(rs.clone());
                this.histogram.addNodeParameters(rs);
            }
            
        }
                  
        //updateResourceUtilization();
    }
    
    public int matchComponent(GridComponent c){
        int match = 0;
        for(GridNodeResourceState rs : collection)
            if (GridComponent.Match(c, rs))                
                match++;
        
        return match;
    }
    /*
     * There is no function here for the Avg <b>Memory</p>
     * usage yet
     */
 public void updateResourceUtilization(){
        GridNode gNode;
        int freeCPU = 0,totalCPU = 0;
        int freeMemoryGB = 0, totalMemoryGB = 0;
        GridBrokerProtocol gbp;
        gNode = (GridNode)Network.get(brokerIndex);
        gbp = (GridBrokerProtocol)gNode.getProtocol(GridNode.getBrokerProtocolID(gNode));
        
        cycle = CDState.getCycle();
        queueLength = gbp.queuedComponents;
        
        for(GridNodeResourceState rs : collection){
            gNode = (GridNode)Network.get(rs.NodeIndex);
            totalCPU+= gNode.TotalCPU; totalMemoryGB+=gNode.TotalMemory;
            freeCPU+= gNode.FreeCPU; freeMemoryGB+=gNode.FreeMemory;
            
        }
        availableCPUs = freeCPU;
        availableMemoryGB = freeMemoryGB;
        if(totalCPU > 0)
            avgCPUUtilization = freeCPU/totalCPU;
        else
            avgCPUUtilization = 0;
        
        if(availableCPUs == 0 && availableMemoryGB == 0){
            if(!isSaturated) GridConfig.saturatedPools++;
            isSaturated = true;
        }else{
            if(isSaturated) GridConfig.saturatedPools--;
            isSaturated = false;
        }
        
    }
    public void clear(){
        collection.clear();
        availableCPUs = 0;
        avgCPUUtilization = 0;
    }
    public GridResourceCollection clone(){
        GridResourceCollection grc = new GridResourceCollection();
        grc.brokerIndex = brokerIndex;
        grc.brokerNodeID = brokerNodeID;
        grc.cycle = cycle;
        
        if(GridBrokerProtocol.exchangePolicy != GridBrokerProtocol.ADAPTIVEFUZZY
                && GridBrokerProtocol.exchangePolicy != GridBrokerProtocol.ADAPTIVEFUZZYRT
                && GridBrokerProtocol.exchangePolicy != GridBrokerProtocol.CONDORFLOCKP2P)// Because in this case, node resource information of workers are not used
        for(GridNodeResourceState rs : collection)
            grc.collection.add(rs.clone());
        
        grc.histogram = histogram.clone();
                
        grc.availableCPUs = availableCPUs;
        grc.availableMemoryGB = availableMemoryGB;
        grc.avgCPUUtilization = avgCPUUtilization;
        grc.queueLength = queueLength;
        
        return grc;
    
    }
    
}
