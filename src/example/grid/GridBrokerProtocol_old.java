/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package example.grid;

import peersim.cdsim.CDProtocol;
import peersim.core.*;
import peersim.config.*;
import peersim.config.FastConfig;
import java.util.ArrayList;
import java.util.Vector;
import peersim.cdsim.CDState;
/**
 *
 * @author 2906095
 */
public class GridBrokerProtocol_old implements CDProtocol{
    
    long time;
    public long period = 0;
    ArrayList<Integer> random = new ArrayList<Integer>();
    
    protected static final String PAR_QUEUE = "queueSize";
    protected static final String PAR_EXCHANGE = "exchangePolicy";//For component exchange
    protected static final String PAR_COMPONENT_WAIT_FOREIGN = "flocking.componentwaitinforeignqueue";//Max No. cycles a component can wait (idle) in the queue of a foreign broker before it is returned to its owner broker [Used only in flocking]
    
    protected static final String[] PAR_EXCHANGE_ARRAY = {"random","adaptive","center","flocking","adaptiveflocking"};
    
    public final int queueSize;
    public final String exchangePolicyString;//For component exchange
    public final int exchangePolicy;
    public final int componentWaitForeign;
    public final int RANDOM = 0,ADAPTIVE = 1, CENTER = 2, FLOCKING = 3, ADAPTIVEFLOCKING = 4;
    // Storing Resource State Information
    GridCDProtocol gcdp;
    GridProtocol gp;
    protected Vector<GridNodeResourceState> NodesResourceInfo ;//Global
    protected Vector<GridNodeResourceState> localNodesResourceInfo ;
    protected Vector<GridNodeResourceState> exchange ;
    
    GridNode NODE;
    //Storing Information about where the components are Deployed
    protected Vector<GridComponentRecord> componentRecords;
    
    
       
    //Queue for components
    protected Vector<GridComponent> componentQueue;
    public int queuedComponents,localDeployments;
    
    GridNodeResourceState rsSafe;
    
    public GridBrokerProtocol_old(String prefix){
        
        queueSize = Configuration.getInt(prefix + "." + PAR_QUEUE, 10000);
        exchangePolicyString = Configuration.getString(prefix + "." + PAR_EXCHANGE,PAR_EXCHANGE_ARRAY[0]);
        componentWaitForeign = Configuration.getInt(prefix + "." + PAR_EXCHANGE + "." + PAR_COMPONENT_WAIT_FOREIGN,0);
        
        if(exchangePolicyString.equals(PAR_EXCHANGE_ARRAY[0]))
            exchangePolicy = RANDOM;
        else if(exchangePolicyString.equals(PAR_EXCHANGE_ARRAY[1]))
            exchangePolicy=ADAPTIVE;
        else if(exchangePolicyString.equals(PAR_EXCHANGE_ARRAY[2]))
            exchangePolicy=CENTER;
        else
            exchangePolicy=FLOCKING;
    }
    
    void Initialize(){
//////        NodesResourceInfo = new Vector<GridNodeResourceState>();
//////        localNodesResourceInfo = new Vector<GridNodeResourceState>();
//////        exchange = new Vector<GridNodeResourceState>();
        componentRecords = new Vector<GridComponentRecord>();
        
        componentQueue = new Vector<GridComponent>();
        
        for(int i =0;i< GridFactory.components.size();i++){
            
            componentRecords.add(new GridComponentRecord(GridFactory.components.get(i).typeID));
        }
        
    }
    @Override
    public void nextCycle(Node node, int protocolID){
        time = System.currentTimeMillis(); period = 0;
        
        GridNode gNode = (GridNode)node;
           
        NODE=gNode;
        
        gcdp = (GridCDProtocol)NODE.getProtocol(GridNode.getGridCDProtocolID(NODE));
        
        int linkableID = FastConfig.getLinkable(protocolID);
        
        gp =(GridProtocol)node.getProtocol(linkableID);
        
        if(! gNode.getIsBrokerState())return;
        
//////         //choose broker
//////        int neighborIndex;
//////        
//////        random.clear();
//////        
//////        for(int i=0;i<GridConfig.brokerIndexes.size();i++){
//////        
//////            Node n = Network.getWithID(GridConfig.brokerIndexes.get(i),GridConfig.brokerIDs.get(i));
//////            if(gp.contains(n))
//////                random.add(n.getIndex());
//////          }
//////        if(random.size()==0){
//////            System.err.println("Broker Node "+gNode.getIndex()+" Is not connected to the broker network");
//////            return;
//////        }
//////        else if(random.size()==1)
//////            neighborIndex = random.get(0);
//////        else
//////        neighborIndex = random.get(CommonState.r.nextInt(random.size()-1));
//////        
//////        
//////        for(int i = 0;i<NodesResourceInfo.size();i++){
//////            if(i>=NodesResourceInfo.size())break;
//////            if((CDState.getCycle()-NodesResourceInfo.get(i).Cycle)>=GridConfig.dataAge);
//////            
//////                NodesResourceInfo.removeElementAt(i);
//////                
//////                
//////        }
//////        
//////        //Exchange data with choosen neighbor broker
//////        
//////               
//////        GridBrokerProtocol gbp = (GridBrokerProtocol)Network.get(neighborIndex)
//////                .getProtocol(GridNode.getBrokerProtocolID(Network.get(neighborIndex)));
//////        
//////        NodesResourceInfo = gbp.ExChangeResourceData(NodesResourceInfo);
//////        
//////           
        
        
        //Deploy component from queue
        
        RemoveExpiredComponentsFromQueue();
        DeployComponentFromLocalQueue(gNode,protocolID);
        
        period = System.currentTimeMillis() - time;
        if(GridConfig.print == GridConfig.SIMPLE)
             return;
        
//////        System.out.println("Boker Node "+gNode.getIndex()+" Exchange data with Broker Node "+neighborIndex+"\n");
//////        printResourceData();
        printComponentData();
        
    }
    
    
    //called by the global deployer or another broker
    public boolean getComponentToDeploy(GridNode fromBroker, GridNode gNode, GridComponent c, boolean highPeriority){
        
        if(exchangePolicy == CENTER && gNode.brokerIdentifier.index > 0 && componentQueue.size()>=queueSize)//Exception for central meta-scheduler in case of star topology
             return false;
        
        if(exchangePolicy != CENTER && exchangePolicy != FLOCKING && componentQueue.size()>=queueSize)     
            return false;
        
        GridComponent comp = c.Clone();
        comp.queuedAt=CDState.getCycle();
        
        if(highPeriority)              
          componentQueue.add(0, comp);
                      
        else
          componentQueue.add(comp);
        queuedComponents++;
        
        return true;
            
                
    }
    
    public int getCurrentQueueSize(){
        return componentQueue.size();
    }
    
    
//    private int componentBrokerMatchLocal(GridComponent c, int brokerIndex){
//           //int i = 0;
//           for(int i = 0; i)
//           NodesResourceInfo 
//           return 0;
//            
//     }
    
    
    public void RemoveExpiredComponentsFromQueue(){
        
        int idx=0;
        boolean isOwnerBrokerUp;
        while(true){
            
            if(idx >= componentQueue.size()-1)break;
            
            isOwnerBrokerUp = ((GridNode)Network.get(GridConfig.brokerIndexes.get(componentQueue.get(idx).ownerBrokerIndex))).getFailState() == Fallible.OK;
            
            if(componentQueue.get(idx).remainingDeployTrials <=0 
               ||componentQueue.get(idx).maxWaitingTime < (CDState.getCycle()-componentQueue.get(idx).deployedAt)
               || ! isOwnerBrokerUp ){
               
                componentQueue.removeElementAt(idx);
                
                GridConfig.failedDeployments ++;
                GridConfig.failedComponents++;
                //GridConfig.waitingComponents--;
                continue;
            }
            idx++;
        }
        
    }
    
    public boolean DeployComponentFromLocalQueue(GridNode node, int protocolID){
        
        boolean succeed;
        
        if(componentQueue.isEmpty())
            return false;
        
        GridDeployHandleProtocol dhp;
        GridBrokerProtocol gbp;
        
        GridComponent c=null;
        
        GridNode gNode,brokerNode;
        
        if(componentQueue.size()==0)return false;
        
        //Remove all components with expired deployment trials
        for(int i = componentQueue.size()-1; i >= 0; i--)
            if(componentQueue.get(i).remainingDeployTrials <= 0){
                componentQueue.removeElementAt(i);
                GridConfig.failedDeployments++;
                GridConfig.failedComponents++;
            }
        c = null;
        for(int i = 0;i < componentQueue.size(); i++){
            if(componentQueue.get(i).queuedAt < CDState.getCycle()){ 
                c = componentQueue.get(i);
                break;
            }
//        //If it is expired, remove it
//         if(c.remainingDeployTrials <=0){
//            componentQueue.removeElementAt(0);
//            GridConfig.failedDeployments++;
//            continue;
//         }              
//         else break;
        }
        if(c == null) 
            return false;
         //--------------------------------------------------------------------------------------------------
        // Try to deploy it locally
        //--------------------------------------------------------------------------------------------------
        if(!(exchangePolicy==CENTER && this.NODE.brokerIdentifier.index == 0 && GridNode.topology == GridNode.BROKERStar)){//In CENTER Deployment policy, the central broker cannot deploy anything locally
           
         for(int i = 0;i < gcdp.localNodesResourceInfo.collection.size(); i++){


             if(GridComponent.Match(c, gcdp.localNodesResourceInfo.collection.get(i))){

                 gNode = (GridNode)Network.get(gcdp.localNodesResourceInfo.collection.get(i).NodeIndex);

                 dhp = (GridDeployHandleProtocol)
                         gNode.getProtocol(GridNode.getDeployHandleProtocolID(gNode));

                 
                succeed = dhp.DeployComponent(c, gNode, GridNode.getDeployHandleProtocolID(gNode));

                if(succeed){
                    componentQueue.removeElementAt(0);
                    GridConfig.Deployments++;
                    localDeployments++;
                    //GridConfig.waitingComponents--;
                    return true;    
                    }

             }
         }
        }
        //--------------------------------------------------------------------------------------------------       
        //Deploy to another broker which has a matching node
        //--------------------------------------------------------------------------------------------------
        c.remainingDeployTrials-=1;
        if(exchangePolicy==CENTER)//In CENTER Deployment policy, local brokers cannot exchange components with each others
            if(this.NODE.brokerIdentifier.index > 0 && GridNode.topology == GridNode.BROKERStar)
                return false;
            else{
                for(int i = 1;i < GridConfig.brokerIndexes.size(); i++){

                    brokerNode = (GridNode)Network.get(GridConfig.brokerIndexes.get(i));

                    if(!((Linkable)node.getProtocol(FastConfig.getLinkable(protocolID))).contains(brokerNode))
                       continue;
                    

                    gbp = (GridBrokerProtocol)brokerNode.getProtocol(GridNode.getBrokerProtocolID(brokerNode));

                        if(gbp.getComponentToDeploy(node, brokerNode,c,true)){
                           componentQueue.removeElementAt(0);
                            GridConfig.ExchangedComponents++;
                            return true;
                        }else 
                            GridConfig.FailedComponentExchangings++;
                }
                
            }
                
        else if(exchangePolicy==RANDOM){//Deploy to any neighboring broker 
         
            int i = node.getIndex();
            int count = 0;
            while(count++ < GridConfig.brokerIndexes.size()){//Try to submit to brokers with next index sequentially
                if(++i > GridConfig.brokerIndexes.size()-1)
                        i = 0;
                            
                
                
                
                
            //}
            //for(int i=0;i<GridConfig.brokerIndexes.size();i++){

                brokerNode = (GridNode)Network.get(GridConfig.brokerIndexes.get(i));

                if(brokerNode.getIndex()==node.getIndex())continue;

                if(!((Linkable)node.getProtocol(FastConfig.getLinkable(protocolID))).contains(brokerNode)){
                   
                    //GridConfig.FailedComponentExchangings++;
                    continue;
                }

                gbp = (GridBrokerProtocol)brokerNode.getProtocol(GridNode.getBrokerProtocolID(brokerNode));
                     

                    if(gbp.getComponentToDeploy(node, brokerNode,c,true)){
                       componentQueue.removeElementAt(0);
                        GridConfig.ExchangedComponents++;
                        return true;
                    }else{ 
                        GridConfig.FailedComponentExchangings++;
                        
                    }
            }
        
        
        }
        else if(exchangePolicy == FLOCKING || exchangePolicy == ADAPTIVEFLOCKING){//Deploy to a neighboring brokers based on condor flocking policy
            
            if(c.ownerBrokerIndex != node.getIndex()){//This is not the owner broker of this component, then retern it to the owner
                brokerNode = (GridNode)Network.get(GridConfig.brokerIndexes.get(c.ownerBrokerIndex));
                gbp = (GridBrokerProtocol)brokerNode.getProtocol(GridNode.getBrokerProtocolID(brokerNode));
                c.lastHostBrokerIndex = node.getIndex();
                if(gbp.getComponentToDeploy(node, brokerNode,c,true)){
                    componentQueue.removeElementAt(0);
                        GridConfig.ExchangedComponents++;
                        return true;
                }else{
                    componentQueue.removeElementAt(0);
                    GridConfig.FailedComponentExchangings++;
                }
                
            }else{//This is the owner broker of this component, try to deploy it to one of those brokers in the list
                int foreingBrokerIndex;
                
                if(exchangePolicy == ADAPTIVEFLOCKING){
                    
                }
                
                if(c.lastHostBrokerIndex == NODE.getIndex()){//Was not flocked to another broker yet, deploy it to the first broker in the flocking list
                    foreingBrokerIndex = 0;
                }else{//Was flocked to another broker before, deploy it to the next broker in the flocking list
                    foreingBrokerIndex = NODE.neighborBrokers.indexOf(c.lastHostBrokerIndex);
                    if(++foreingBrokerIndex >= NODE.neighborBrokers.size())
                        foreingBrokerIndex = 0;
                }
                    brokerNode = (GridNode)Network.get(GridConfig.brokerIndexes.get(NODE.neighborBrokers.get(foreingBrokerIndex)));
                    gbp = (GridBrokerProtocol)brokerNode.getProtocol(GridNode.getBrokerProtocolID(brokerNode));
                    if(gbp.getComponentToDeploy(node, brokerNode,c,true)){
                    componentQueue.removeElementAt(0);
                        GridConfig.ExchangedComponents++;
                        return true;
                    }else{
                        componentQueue.removeElementAt(0);
                        GridConfig.FailedComponentExchangings++;
                    }
                    
                
            }
        }
        else if(exchangePolicy==ADAPTIVE){//Deploy to a neighboring broker which has matching nodes only
        boolean matchFound = false;
        for(int i = 0;i <gcdp.globalNodesResourceInfo.size();i++){
            
                      
            if(gcdp.globalNodesResourceInfo.get(i).matchComponent(c) > 0){
                
                matchFound = true;
                
                brokerNode = (GridNode)Network.get(gcdp.globalNodesResourceInfo.get(i).brokerIndex);
                
                if(brokerNode.getIndex()==node.getIndex())continue;// Don't pass it to myself
                
                if(!((Linkable)node.getProtocol(FastConfig.getLinkable(protocolID))).contains(brokerNode)){//If the matching node is not attached to a neighbor Broker report failed exchanging
                
                    //GridConfig.FailedComponentExchangings++;
                    continue;
                }
                gbp = (GridBrokerProtocol)brokerNode.getProtocol(GridNode.getBrokerProtocolID(brokerNode));
                
                if(gbp.getComponentToDeploy(node, brokerNode,c,true)){
                    
                   componentQueue.removeElementAt(0);
                    GridConfig.ExchangedComponents++;
                    return true; 
                   }//If the neighboring broker's queue is full, report failed exchanging 
                else 
                    GridConfig.FailedComponentExchangings++;
            }
            
        }
        
        if(matchFound){// Pass it to random neighboor
        
//////            for(int i=0;i<100;i++){
            
            
            brokerNode = (GridNode)Network.get(NODE.neighborBrokers.get(CommonState.r.nextInt(NODE.neighborBrokers.size())));
            
//////            if(brokerNode.getIndex()==node.getIndex())continue;
//////            
//////            if(!((Linkable)node.getProtocol(FastConfig.getLinkable(protocolID))).contains(brokerNode)){
//////                
//////                GridConfig.FailedComponentExchangings++;
//////                continue;
//////            }
                   
            gbp = (GridBrokerProtocol)brokerNode.getProtocol(GridNode.getBrokerProtocolID(brokerNode));
                
                if(gbp.getComponentToDeploy(node, brokerNode,c,true)){
                   componentQueue.removeElementAt(0);
                    GridConfig.ExchangedComponents++;
                    return true;
                }
        }
//////        }
        
        }
        
        return false;
    }
    
    public boolean deployToNeighbourBroker(GridNode node, int protocolID, GridComponent c, int policy){
        
        GridDeployHandleProtocol dhp;
        GridBrokerProtocol gbp;
        GridNode gNode,brokerNode;
        int i;
        
        switch(policy){
            //==========================================================================================================================
            case RANDOM:
                i = node.getIndex();
                int count = 0;
                while(count++ < GridConfig.brokerIndexes.size()){//Try to submit to brokers with next index sequentially
                    if(++i > GridConfig.brokerIndexes.size()-1)
                        i = 0;
                            
                    brokerNode = (GridNode)Network.get(GridConfig.brokerIndexes.get(i));

                    if(brokerNode.getIndex()==node.getIndex())continue;

                    if(!((Linkable)node.getProtocol(FastConfig.getLinkable(protocolID))).contains(brokerNode))continue;
                        
                    

                    gbp = (GridBrokerProtocol)brokerNode.getProtocol(GridNode.getBrokerProtocolID(brokerNode));
                     

                    if(gbp.getComponentToDeploy(node, brokerNode,c,true)){
                       componentQueue.removeElementAt(0);
                        GridConfig.ExchangedComponents++;
                        return true;
                    }else{ 
                        GridConfig.FailedComponentExchangings++;
                        
                    }
                }
                break;
            //==========================================================================================================================
            case FLOCKING:
            case ADAPTIVEFLOCKING:
                        if(c.ownerBrokerIndex != node.getIndex()){//This is not the owner broker of this component, then retern it to the owner
                            brokerNode = (GridNode)Network.get(GridConfig.brokerIndexes.get(c.ownerBrokerIndex));
                            gbp = (GridBrokerProtocol)brokerNode.getProtocol(GridNode.getBrokerProtocolID(brokerNode));
                            c.lastHostBrokerIndex = node.getIndex();
                            if(gbp.getComponentToDeploy(node, brokerNode,c,true)){
                                componentQueue.removeElementAt(0);
                                    GridConfig.ExchangedComponents++;
                                    return true;
                            }else{
                                componentQueue.removeElementAt(0);
                                GridConfig.FailedComponentExchangings++;
                            }

                        }else{//This is the owner broker of this component, try to deploy it to one of those brokers in the list
                            if (policy == ADAPTIVEFLOCKING)//Pass to a matching neighbour broker
                                return deployToNeighbourBroker(node, protocolID, c, ADAPTIVE);
                            
                            int foreingBrokerIndex;

                            if(c.lastHostBrokerIndex == NODE.getIndex()){//Was not flocked to another broker yet, deploy it to the first broker in the flocking list
                                foreingBrokerIndex = 0;
                            }else{//Was flocked to another broker before, deploy it to the next broker in the flocking list
                                foreingBrokerIndex = NODE.neighborBrokers.indexOf(c.lastHostBrokerIndex);
                                if(++foreingBrokerIndex >= NODE.neighborBrokers.size())
                                    foreingBrokerIndex = 0;
                            }
                            brokerNode = (GridNode)Network.get(GridConfig.brokerIndexes.get(NODE.neighborBrokers.get(foreingBrokerIndex)));
                            gbp = (GridBrokerProtocol)brokerNode.getProtocol(GridNode.getBrokerProtocolID(brokerNode));
                            if(gbp.getComponentToDeploy(node, brokerNode,c,true)){
                                componentQueue.removeElementAt(0);
                                    GridConfig.ExchangedComponents++;
                                    return true;
                            }else{
                                    componentQueue.removeElementAt(0);
                                    GridConfig.FailedComponentExchangings++;
                            }
                        }
                        break;
            //==========================================================================================================================
            case ADAPTIVE:
                    boolean matchFound = false;
                    for(i = 0;i <gcdp.globalNodesResourceInfo.size();i++){
            
                      
                        if(gcdp.globalNodesResourceInfo.get(i).matchComponent(c) > 0){

                            matchFound = true;

                            brokerNode = (GridNode)Network.get(gcdp.globalNodesResourceInfo.get(i).brokerIndex);

                            if(brokerNode.getIndex()==node.getIndex())continue;// Don't pass it to myself

                            if(!((Linkable)node.getProtocol(FastConfig.getLinkable(protocolID))).contains(brokerNode))//If the matching node is not attached to a neighbor Broker report failed exchanging
                                continue;
                            
                            gbp = (GridBrokerProtocol)brokerNode.getProtocol(GridNode.getBrokerProtocolID(brokerNode));

                            if(gbp.getComponentToDeploy(node, brokerNode,c,true)){

                               componentQueue.removeElementAt(0);
                                GridConfig.ExchangedComponents++;
                                return true; 
                            }//If the neighboring broker's queue is full, report failed exchanging 
                            else 
                                GridConfig.FailedComponentExchangings++;
                        }
            
                    }
        
                    if(matchFound){// Pass it to any neighbour
                        brokerNode = (GridNode)Network.get(NODE.neighborBrokers.get(CommonState.r.nextInt(NODE.neighborBrokers.size())));
                        gbp = (GridBrokerProtocol)brokerNode.getProtocol(GridNode.getBrokerProtocolID(brokerNode));
                
                        if(gbp.getComponentToDeploy(node, brokerNode,c,true)){
                           componentQueue.removeElementAt(0);
                            GridConfig.ExchangedComponents++;
                            return true;
                        }
                    }
                    break;
            //==========================================================================================================================
            case CENTER:
                    if(this.NODE.brokerIdentifier.index > 0 && GridNode.topology == GridNode.BROKERStar)
                        return false;//local brokers cannot deploy outside
                    else{
                        for(i = 1;i < GridConfig.brokerIndexes.size(); i++){

                            brokerNode = (GridNode)Network.get(GridConfig.brokerIndexes.get(i));

                            if(!((Linkable)node.getProtocol(FastConfig.getLinkable(protocolID))).contains(brokerNode))
                               continue;


                            gbp = (GridBrokerProtocol)brokerNode.getProtocol(GridNode.getBrokerProtocolID(brokerNode));

                                if(gbp.getComponentToDeploy(node, brokerNode,c,true)){
                                   componentQueue.removeElementAt(0);
                                    GridConfig.ExchangedComponents++;
                                    return true;
                                }else 
                                    GridConfig.FailedComponentExchangings++;
                        }
                    }
                    break;
            default:
                    break;
                
        }
        return false;
    }
    
    public boolean readDeployedComponentsFromNode(Vector<Long> IDs, Vector<Integer> StartCycles,NodeIdentifier nI){
        
        //Remove all records related to this node
        for(int i = 0;i < componentRecords.size();i++){
            
            int idx=0;
            
            while(true){
                
                if(idx >= componentRecords.get(i).nodes.size())break;
                
                if(componentRecords.get(i).nodes.get(idx).ID == nI.ID){
                    componentRecords.get(i).nodes.removeElementAt(idx);
                    componentRecords.get(i).StartCycle.removeElementAt(idx);
                    componentRecords.get(i).RecordedAt.removeElementAt(idx);
                   }
                else
                    idx++;
                
            }
            
        }
        
        //Adds new data
        for(int i = 0;i< componentRecords.size();i++){
            
            int idx=0;
            
            if(IDs.contains(componentRecords.get(i).ID)){
                idx = IDs.lastIndexOf(componentRecords.get(i).ID);
                componentRecords.get(i).nodes.add(nI);
                componentRecords.get(i).StartCycle.add(StartCycles.get(idx));
                componentRecords.get(i).RecordedAt.add(CDState.getCycle());
            }
            
        }
        
        
        return true;
        
    }
    
    public void printComponentData(){
        System.out.println("Stored Component Data:#############");
        
         System.out.println("Component Queue length = "+componentQueue.size()+"\n");
        
        for(int i = 0;i<componentRecords.size();i++){
            
            System.out.println("Component "+componentRecords.get(i).ID);
            
            if(componentRecords.get(i).nodes.isEmpty())
                System.out.println("No Stored locations");
            else{
                for(int j=0;j< componentRecords.get(i).nodes.size();j++){
                    
                  System.out.print("Node "+componentRecords.get(i).nodes.get(j).index
                          +" : Start Cycle "+componentRecords.get(i).StartCycle.get(j)
                          +" (Read time: "+componentRecords.get(i).RecordedAt.get(j)+ ")|\t|");  
                }
                System.out.println();
            }
            
        }
        
        
        System.out.println("#######################################");
        
    }
    
    @Override
    public Object clone (){
        GridBrokerProtocol sl = null;
        try
        {
            sl = (GridBrokerProtocol) super.clone();
        } catch (CloneNotSupportedException e)
        {
        } // never happens
        return sl;
    }
    
    
/*****************************************************************************************************************************/    
    
    @Deprecated
    public boolean  readResourceStateFromNode(GridNodeResourceState rs){
        
        if(NODE!=null)
        if(NODE.getIndex()==rs.NodeIndex)rsSafe=rs.clone();
        
        boolean addedToList = false;
        
//        int vv=10;
//                 if(rs.NodeIndex==0&&CDState.getCycle()>300)
//                 {vv=NODE.getIndex();
//                        vv=7;}
        
        //Insert in the collection for local resources
       if(localNodesResourceInfo.isEmpty())
           localNodesResourceInfo.add(rs);
       else{
            
            for(int i=0; i<localNodesResourceInfo.size();i++){
                
                if(localNodesResourceInfo.get(i).NodeID == rs.NodeID){
                    
                    localNodesResourceInfo.setElementAt(rs, i);
                      
                    addedToList = true;
                }
                    
            }
           if(!addedToList) localNodesResourceInfo.add(rs);
            
            
       }
        
        addedToList = false;
        //Insert in the collection for global resources
        if(NodesResourceInfo.isEmpty())
           NodesResourceInfo.add(rs);
       else{
            
            for(int i=0; i<NodesResourceInfo.size();i++){
                
                if(NodesResourceInfo.get(i).NodeID == rs.NodeID){
                    
                    NodesResourceInfo.setElementAt(rs, i);
                                       
                    return true;
                }
                    
            }
            NodesResourceInfo.add(rs);
            return true;
       }
        
        return true;
    }
    
    @Deprecated
    public void printResourceData(){
        System.out.println("Stored Resource Data: >>>>>>>");
        
        if(NodesResourceInfo.isEmpty())return;
        
        for(int i = 0; i<NodesResourceInfo.size();i++ ){
            
            System.out.println(NodesResourceInfo.get(i).toString());
            
        }
        System.out.println("<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>");
        
    }
    
    @Deprecated
    public Vector<GridNodeResourceState> ExChangeResourceData(Vector<GridNodeResourceState> resourceStates){
        
        GridNodeResourceState rs1,rs2;
        
         exchange.clear();
         
         for(int i = 0;i < Network.size();i++){
             
             if(NodesResourceInfo.isEmpty() && resourceStates.isEmpty())
                 break;
             else if(NodesResourceInfo.isEmpty() && !resourceStates.isEmpty()){
                 
                 for(int j = 0; j < resourceStates.size();j++)
                     if((CDState.getCycle()-resourceStates.get(j).Cycle)<GridConfig.dataAge)
                     exchange.add(resourceStates.get(j));
                 
                 break;
             }else if(!NodesResourceInfo.isEmpty() && resourceStates.isEmpty()){
                 
                 for(int j = 0; j < NodesResourceInfo.size();j++)
                     if((CDState.getCycle()-NodesResourceInfo.get(j).Cycle)<GridConfig.dataAge)
                     exchange.add(NodesResourceInfo.get(j));
                 
                 break;
             }
                 
             
             
             rs1 = cutElementFromDataColloection(Network.get(i).getID(),NodesResourceInfo);
             rs2 = cutElementFromDataColloection(Network.get(i).getID(),resourceStates);
             
             int vv=10;
                 if(Network.get(i).getIndex()==0&&CDState.getCycle()>300)
                 {vv=NODE.getIndex();
                        vv=7;}
             
             if(rs1 != null && rs2 != null){
             
                 if(rs1.Cycle >= rs2.Cycle)
                     exchange.add(rs1);
                 else
                    exchange.add(rs2); 
                 
                 
                 
             }
             else if (rs1 != null)
                exchange.add(rs1);
             else if (rs2 != null)
                exchange.add(rs2);
                        
             
             
         }         
        boolean addedToList = false;
        NodesResourceInfo = (Vector<GridNodeResourceState>) exchange.clone();
        
        
        if(rsSafe!=null){
        for(int i=0; i<NodesResourceInfo.size();i++){
                
                if(NodesResourceInfo.get(i).NodeID == rsSafe.NodeID){
                    
                    NodesResourceInfo.setElementAt(rsSafe, i);
                    exchange.setElementAt(rsSafe, i);
                      
                    addedToList = true;
                }
                    
            }
           if(!addedToList) {
               localNodesResourceInfo.add(rsSafe);
                  exchange.add(rsSafe);
           }
           
        }
        return (Vector<GridNodeResourceState>) exchange.clone();
        
        
        
    }
    
    @Deprecated
    GridNodeResourceState cutElementFromDataColloection(long ID,Vector<GridNodeResourceState> vector){
        
        GridNodeResourceState ret;
        
        for(int i = 0;i < vector.size();i++){
            if((CDState.getCycle()-vector.get(i).Cycle)>=GridConfig.dataAge)vector.removeElementAt(i);
            if(i>=vector.size())break;
            
            if (vector.get(i).NodeID == ID){
                ret = new GridNodeResourceState(vector.get(i).NodeIndex
                        , vector.get(i).NodeID, vector.get(i).AvailabbleCPU
                        , vector.get(i).availableMemory, vector.get(i).OS, vector.get(i).Cycle);
                
                vector.removeElementAt(i);
                return ret;
                
            }
           }
        
        return null;
        
    }

}
