/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package example.grid;

import java.util.Vector;
import peersim.cdsim.CDState;
import peersim.config.FastConfig;
import peersim.core.*;

/**
 *
 * @author 2906095
 */
public class GridNode extends GeneralNode{
    
    //private long ID;
    protected boolean isBroker = false;
    
    protected NodeIdentifier brokerIdentifier;
    
    public static int topology = 0;
    
    public static final int BROKERFull = 0;
    public static final int BROKERRing = 1;
    public static final int BROKERWKout = 2;
    public static final int BROKERHyperCube = 3;
    public static final int BROKERStar = 4;
    
    public static final int WIN = 0, LINUX = 1, MAC = 2;
    /*
     * Fail state of the domain in case of broker failure:
     * DOMAIN_ON: The domain nodes are still sending their information to the broker (The broker in this case must be DOWN not DEAD) --> used in case of churn failure mode.
     * DOMAIN_DOWN: The domain nodes will be set to DOWN upon the broker failure --> used in case of permanent failure mode.
     * DOMAIN_DIVIDED: The domain nodes will be joining other domains upon the broker failure --> used in case of permanent failure mode.
     */
    public static final int DOMAIN_ON= 0, DOMAIN_DOWN = 1, DOMAIN_DIVIDED = 2;
    protected int domainFailState;
    
    protected Vector<Integer> neighborBrokers = new Vector<Integer>();// In case of broker node
    
    protected int TotalMemory =0;// GB
    protected int TotalCPU = 0;// No. of CPU Cores
    protected int OS = 0;// OS
    
    protected int FreeMemory = 0;// GB
    protected int FreeCPU = 0;// No. of vacant CPU Cores
    
    
    protected GridNodeResourceState resourceState;
    
     //public Vector<GridComponent> components = new Vector<GridComponent>();
     //public Vector<Integer> componentStartCycle = new Vector<Integer>(); 
    
    //For usage by Brokers only
    protected Vector<GridNodeResourceState> localResourceInfo = new Vector<GridNodeResourceState>();
    protected Vector<GridNodeResourceState> globalNodesResourceInfo = new Vector<GridNodeResourceState>();
    protected int poolNodesType; // When all nodes in the pool have the same Type (specifications)

    protected GridHistogram histogram;
    
    public GridNode (String prefix){
    
        super(prefix);
    }

    public void InitializeNode(int MemorySize,int CPUCores,int OS, boolean isBrokerNode){
        
        isBroker = isBrokerNode;
        
        TotalMemory = MemorySize;// GB
        TotalCPU = CPUCores;
        this.OS = OS;
        
        FreeMemory = TotalMemory;
        FreeCPU = TotalCPU;
        
        resourceState = new GridNodeResourceState(this.getIndex(),this.getID(), FreeCPU, FreeMemory, this.OS, CDState.getCycle());
        
        
    }
    
    public void InitializeNode(GridNodeResourceState rs, boolean isBrokerNode){
        
        isBroker = isBrokerNode;
        
        TotalMemory = rs.availableMemory;// GB
        TotalCPU = rs.AvailabbleCPU;
        this.OS = rs.OS;
        
        FreeMemory = TotalMemory;
        FreeCPU = TotalCPU;
        
        resourceState = new GridNodeResourceState(this.getIndex(),this.getID(), FreeCPU, FreeMemory, this.OS, CDState.getCycle());
        
                   
        
    }
    
    public GridNodeResourceState getResourceState(){
        
        resourceState.NodeIndex =this.getIndex();
        resourceState.Cycle = CDState.getCycle();
        return resourceState;
    }
            
    
    public void setIsBrokerState(boolean isBrokerNode){
        
        isBroker = isBrokerNode;
    }
    
    public boolean getIsBrokerState(){
        
        return isBroker ;
    }
    
    //For non-Brokers
    public void setBroker(NodeIdentifier ident){
        
        brokerIdentifier = ident;
    }
    // For Broker nodes only
    public void initializeNeighborBrokers(){
        
        neighborBrokers = new Vector<Integer>();
        
        for(int i = 0; i < GridConfig.brokerIndexes.size(); i++){
             if(((Linkable)this.getProtocol(getLinkableProtocolID(this)))
                .contains((GridNode)Network.get(GridConfig.brokerIndexes.get(i))))
                   this.addNeighborBroker(i);
             
             
        }
    }
    
    // For Broker nodes only
    public int getRandomNeighbourBrokerIndex(){
        int index = neighborBrokers.get(CommonState.r.nextInt(neighborBrokers.size()));
        Node n = Network.get(index);
        if(n.isUp())
            return index;
        else
            return getRandomNeighbourBrokerIndex();
    }
    // For Broker nodes only
    public void addNeighborBroker(int index){
    
        neighborBrokers.add(index);
    }
    
    // For Broker nodes only
    public void removeNeighborBroker(int index){
    
        neighborBrokers.remove(index);
    }
    //For non-Brokers
    public GridNode getBroker(){
        
        return (GridNode)Network.getWithID(brokerIdentifier.index, brokerIdentifier.ID);
    }
    //For Brokers only
    
    
    public static int getBrokerProtocolID(Node node){
        
       for (int i = 0; i < node.protocolSize(); i++){
            
            if(node.getProtocol(i) instanceof GridBrokerProtocol)
                return i;
          
        } 
        return -10;
    }
    
     public static int getDeployHandleProtocolID(Node node){
        
       for (int i = 0; i < node.protocolSize(); i++){
            
            if(node.getProtocol(i) instanceof GridDeployHandleProtocol)
                return i;
          
        } 
        return -10;
    }
     
     public static int getLinkableProtocolID(Node node){
        
       for (int i = 0; i < node.protocolSize(); i++){
            
            if(node.getProtocol(i) instanceof Linkable)
                return i;
          
        } 
        return -10;
    }
    
    public static int getGridCDProtocolID(Node node){
        
       for (int i = 0; i < node.protocolSize(); i++){
            
            if(node.getProtocol(i) instanceof GridCDProtocol)
                return i;
          
        } 
        return -10;
    } 
    public int getDomainFailState(){
        return domainFailState;
    }
    public boolean setDomainFailState(int FailState){
        if(FailState < 0 || FailState > 2) return false;
        else{
            domainFailState = FailState;
            return true;
        }
    }
    @Deprecated
    public boolean  readResourceStateFromNode(GridNodeResourceState rs){
        
        if(!getIsBrokerState())return false;
        
       if(localResourceInfo.isEmpty())
           globalNodesResourceInfo.add(rs);
       else{
            
            for(int i=0; i<globalNodesResourceInfo.size();i++){
                
                if(globalNodesResourceInfo.get(i).NodeID == rs.NodeID){
                    
                    localResourceInfo.get(i).NodeIndex = rs.NodeIndex;
                    localResourceInfo.get(i).availableMemory = rs.availableMemory;
                    localResourceInfo.get(i).AvailabbleCPU= rs.AvailabbleCPU;
                    localResourceInfo.get(i).Cycle = rs.Cycle;
                    return true;
                }
                    
            }
            globalNodesResourceInfo.add(rs);
            return true;
       }
        
        return true;
    }
    //For Brokers
    public void InitializeHistogram(){
    
    //histogram = new GridHistogram();
    System.out.println("Histogram At Node "+this.getIndex()+" Initialized");
    
}
//    @Deprecated
//    public boolean DeployComponent(GridComponent c){
//        
//        if (this.FreeCPU>=c.CPUUsage && this.FreeMemory>=c.MemoryUsage){
//            
//            components.add(c);
//            componentStartCycle.add(CDState.getCycle());
//            
//            this.FreeCPU -=c.CPUUsage;
//            this.FreeMemory -=c.MemoryUsage;
//            
//            resourceState.AvailabbleCPU = FreeCPU;
//            resourceState.availableMemory=FreeMemory;
//            resourceState.Cycle=CDState.getCycle();
//            
//            return true;
//            
//        }else return false;
//        
//    }
    
    //Run every cycle
//    @Deprecated
//    public void RemoveExpiredComponents(){
//        
//        int currentCycle = CDState.getCycle();
//        
//        for(int i = 0; i< components.size();i++){
//            
//            if((currentCycle - componentStartCycle.get(i)) >= components.get(i).TotalSimulationCycles){
//                
//                FreeCPU += components.get(i).CPUUsage;
//                FreeMemory += components.get(i).MemoryUsage;
//                
//                components.removeElementAt(i);
//                componentStartCycle.removeElementAt(i);
//            }
//            
//        }
//    }
    
    
    public String print(){
        
        return "";
    }
}


