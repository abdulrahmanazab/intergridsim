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
public class GridBrokerProtocol implements CDProtocol{
    
    long time;
    public long period = 0;
    ArrayList<Integer> random = new ArrayList<Integer>();
    
    //To store how many network Hops are between this broker and each other brokers
    RoutingTable routingTable;
    
    protected static final String PAR_QUEUE = "queueSize";
    protected static final String PAR_EXCHANGE = "exchangePolicy";//For component exchange
    protected static final String PAR_COMPONENT_WAIT_FOREIGN = "flocking.componentwaitinforeignqueue";//Max No. cycles a component can wait (idle) in the queue of a foreign broker before it is returned to its owner broker [Used only in flocking]
    
    protected static final String[] PAR_EXCHANGE_ARRAY = {"random","adaptive","center","flocking","adaptiveflocking","adaptivefuzzy","adaptivefuzzyRT","condorFlockP2P","rankingAdaptive"};
    
    protected static final String PAR_COMPARE = "comparison";//Which dataset to use for comparing broker resource information. sync: use dataset directly from that broker. async: use dataset of that broker from the locally stored copy
    protected static final String[] PAR_COMPARE_ARRAY = {"sync","async"};
    
    public final int queueSize;
    public final String exchangePolicyString;//For component exchange
    public final String isSyncDataComparisonString;
    public static int exchangePolicy;
    public final int componentWaitForeign;
    public static final int RANDOM = 0,ADAPTIVE = 1, CENTER = 2, FLOCKING = 3, ADAPTIVEFLOCKING = 4, ADAPTIVEFUZZY = 5, ADAPTIVEFUZZYRT = 6, CONDORFLOCKP2P = 7, RANKINGADAPTIVE = 8;
    public final boolean isSyncDataComparison;
    // Storing Resource State Information
    GridCDProtocol gcdp;
    GridProtocol gp;
    
    GridNode NODE;
    //Storing Information about where the components are Deployed
    protected Vector<GridComponentRecord> componentRecords;
    //Queue for components
    protected Vector<GridComponent> componentQueue;
    public int queuedComponents,localDeployments;
    public int componentsPassedtoRandomNeighbors;
    
    //Used only in case of sequences-------------------------------------------
    protected Vector<GridComponent> ownDeployedComponents;//own components which are successfully deployed
    public int whenAllOwnComponentsDeployed = 0;//Cycle when all own components are deployed
    public int totalDeploymentTimeOfAllOwnComponents = 0;//No of cycles from the generation of the last component untill all own components are deployed
    //-------------------------------------------------------------------------
    
    GridNodeResourceState rsSafe;
    
    public GridBrokerProtocol(String prefix){
        
        queueSize = Configuration.getInt(prefix + "." + PAR_QUEUE, 10000);
        exchangePolicyString = Configuration.getString(prefix + "." + PAR_EXCHANGE,PAR_EXCHANGE_ARRAY[0]);
        componentWaitForeign = Configuration.getInt(prefix + "." + PAR_EXCHANGE + "." + PAR_COMPONENT_WAIT_FOREIGN,0);
        isSyncDataComparisonString = Configuration.getString(prefix + "." + PAR_COMPARE,PAR_COMPARE_ARRAY[0]);
        
        componentsPassedtoRandomNeighbors = 0;
        
        if(exchangePolicyString.equals(PAR_EXCHANGE_ARRAY[0]))
            exchangePolicy = RANDOM;
        else if(exchangePolicyString.equals(PAR_EXCHANGE_ARRAY[1]))
            exchangePolicy=ADAPTIVE;
        else if(exchangePolicyString.equals(PAR_EXCHANGE_ARRAY[2]))
            exchangePolicy=CENTER;
        else if(exchangePolicyString.equals(PAR_EXCHANGE_ARRAY[3]))
            exchangePolicy=FLOCKING;
        else if(exchangePolicyString.equals(PAR_EXCHANGE_ARRAY[4]))
            exchangePolicy=ADAPTIVEFLOCKING;
        else if(exchangePolicyString.equals(PAR_EXCHANGE_ARRAY[5]))
            exchangePolicy=ADAPTIVEFUZZY;
       else if(exchangePolicyString.equals(PAR_EXCHANGE_ARRAY[6]))
            exchangePolicy=ADAPTIVEFUZZYRT;
       else if(exchangePolicyString.equals(PAR_EXCHANGE_ARRAY[7]))
            exchangePolicy=CONDORFLOCKP2P;
       else if(exchangePolicyString.equals(PAR_EXCHANGE_ARRAY[8]))
            exchangePolicy=RANKINGADAPTIVE;
       else
            throw new IllegalArgumentException("invalid component exhange policy");
        
        if(isSyncDataComparisonString.equals(PAR_COMPARE_ARRAY[0]))
            isSyncDataComparison = true;
        else if(isSyncDataComparisonString.equals(PAR_COMPARE_ARRAY[1]))
            isSyncDataComparison = false;
        else
            throw new IllegalArgumentException("invalid component exhange policy");
    }
    
    void Initialize(){
       
        componentRecords = new Vector<GridComponentRecord>();
        
        componentQueue = new Vector<GridComponent>();
        
        ownDeployedComponents = new Vector<GridComponent>();
        
               
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
        
        //if(exchangePolicy == ADAPTIVEFUZZYRT){//ADAPTIVEFUZZY with Routing table
            //Update Network Hop distance information between this broker and other brokers
                    
            if(CDState.getCycle() <= 2)
            updateRoutingTable(gNode, protocolID);
        //}
        //Deploy component from queue
        
        RemoveExpiredComponentsFromQueue();
        DeployComponentFromLocalQueue(gNode,protocolID);
        
        if(isAllOwnComponentsDeployed())
            whenAllOwnComponentsDeployed = CDState.getCycle();
            
        
        period = System.currentTimeMillis() - time;
        if(GridConfig.print == GridConfig.SIMPLE)
             return;
        
//////        System.out.println("Boker Node "+gNode.getIndex()+" Exchange data with Broker Node "+neighborIndex+"\n");
//////        printResourceData();
        printComponentData();
       
    }
  
    boolean isAllOwnComponentsDeployed(){
        if(GridDeployer.seqArray.size() < 1) return false;//No sequence deployment--> not used
        ComponentSequence seq = null;
        for(ComponentSequence s : GridDeployer.seqArray)
            if(s.broker.getIndex() == NODE.getIndex()){
                seq = s;
                break;
            }
        
        if(seq ==  null) return false;//No sequence associated with this broker
        
        if(seq.remainingComponents > 0)
           return false;//Not all of them are even generated yet
        else if(ownDeployedComponents.size() == seq.totalComponents){//All own components are deployed
            totalDeploymentTimeOfAllOwnComponents = CDState.getCycle() - seq.lastDeploymentCycle;
            return true;
        }
        else return false;
            
    }
    
    
    void updateRoutingTable(GridNode node, int protocolID){
       GridNode broker, broker2;
       routingTable = new RoutingTable(node.getIndex());
       
       Vector<Integer> Set = new Vector<Integer>();//
       Vector<Integer> remainingSet = new Vector<Integer>();//Set of un-located brokers, will be empty at the end
              
       remainingSet = (Vector<Integer>)GridConfig.brokerIndexes.clone();
       remainingSet.removeElement(node.getIndex());//remove the index of the current broker
       
       RoutingTableElement rte;
       int index = 0, bIndex, tableSize;
       while(!remainingSet.isEmpty()){
           Set.clear();
           if(routingTable.table.isEmpty()){
               int j = 0;
               while(j < remainingSet.size()){//Check all brokers to see which is a neighbour, and set route as direct path.
                   bIndex = remainingSet.get(j);
                   broker = (GridNode)Network.get(bIndex);
                   if(((Linkable)node.getProtocol(FastConfig.getLinkable(protocolID))).contains(broker)){//Is Neighbour
                       routingTable.table.add(new RoutingTableElement(bIndex,Set));//Add the index with an empty Hops array
                       remainingSet.removeElement(bIndex);                    
                   }else j++;
               }
           }else{
               tableSize = routingTable.table.size();
               for(int i = 0; i < tableSize; i++){
                   rte = routingTable.table.get(i);
                   if(rte.hops.size() != index) continue;//For those in this stage only, i.e. for index=0 (only routes for neighbours are set) then now set the routes for brokers who are one hop away
                   int j = 0;
                   while(j < remainingSet.size()){
                        bIndex = remainingSet.get(j);
                        broker = (GridNode)Network.get(bIndex);
                        broker2 = (GridNode)Network.get(rte.brokerIndex);
                        if(((Linkable)broker2.getProtocol(FastConfig.getLinkable(protocolID))).contains(broker)){
                            Set = (Vector<Integer>)rte.hops.clone();
                            Set.add(rte.brokerIndex);
                            routingTable.table.add(new RoutingTableElement(bIndex,Set));
                            remainingSet.removeElement(bIndex);
                        }else j++;
                   }
               }
               index++;
               
           }
       }
   }
   
   Vector<Integer> getRoutingPathTo(int targetBrokerIndex){
       for(RoutingTableElement rte : routingTable.table)
           if(rte.brokerIndex == targetBrokerIndex){
               Vector<Integer> path = (Vector<Integer>)rte.hops.clone();
               path.add(rte.brokerIndex);//put the index of the target broker at the end of the hops stream
               return path;
           }
           
       return null;
   }
   int getRoutingDistanceTo(int targetBrokerIndex){
       if(targetBrokerIndex == this.NODE.getIndex()) return 0;
       else
           return getRoutingPathTo(targetBrokerIndex).size();
   }
   public static int getRoutingDistance(int broker1, int broker2){
       
       if(broker1 == broker2) return 0;
       GridNode brokerNode = (GridNode)Network.get(broker1);
       GridBrokerProtocol gbp = (GridBrokerProtocol)brokerNode.getProtocol(GridNode.getBrokerProtocolID(brokerNode));
//       try{
       return gbp.getRoutingDistanceTo(broker2);
//       }catch(Exception ex){
//      return 0;
//       }
       
       
       
   }
   
    //called by the global deployer or another broker
    public boolean getComponentToDeploy(GridNode fromBroker, GridNode thisBroker, GridComponent c, boolean highPeriority){
        
        if(exchangePolicy == CENTER && thisBroker.brokerIdentifier.index > 0 
                && componentQueue.size()>=queueSize)//Exception for central meta-scheduler in case of star topology
             return false;
        
        if(exchangePolicy != CENTER && exchangePolicy != FLOCKING 
                && fromBroker.getIndex() != thisBroker.getIndex()//If the deployment is initialized here by the simulator, don't mind about the max queue size
                && componentQueue.size()>=queueSize)     
            return false;
        
        GridComponent comp = c.Clone();
        comp.queuedAt=CDState.getCycle();
        
        if(highPeriority)              
          if(componentQueue.size() > 1)componentQueue.add(1, comp);
          else componentQueue.add(0, comp);
                      
        else
          componentQueue.add(comp);
        queuedComponents++;
        
        if(gcdp != null)//Maybe gcdp object of this GridNode is not initialized yet
            // Same value as 'queuedComponents' but this one will be read by other brokers for inter-Grid submissions
            gcdp.localNodesResourceInfo.queueLength = componentQueue.size();
        
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
                    c.allocatedAtBrokerIndex = NODE.getIndex();
                    c.networkDistance = GridBrokerProtocol.getRoutingDistance(c.ownerBrokerIndex, NODE.getIndex());
                    c.totalWaitingTime = CDState.getCycle() - c.deployedAt;
                    GridConfig.deployedComponents.add(c);
                    
                    brokerNode = (GridNode)Network.get(c.ownerBrokerIndex);
                    gbp = (GridBrokerProtocol)brokerNode.getProtocol(GridNode.getBrokerProtocolID(brokerNode));
                    gbp.ownDeployedComponents.add(c);
                    
                    GridConfig.Deployments++;
                    componentQueue.removeElement(c);
                    localDeployments++;
                    GridConfig.deployedComponentsPerCycle++;//cleared every cycle in the "GridPrintObserver" object
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
        
        if(c.routingPath.size() > 1 && c.routingPath.get(c.routingPath.size()-1) != NODE.getIndex()){//Contains a routing path and this broker is not the target
            GridNode targetBroker = (GridNode)Network.get(c.routingPath.get(1));
            c.routingPath.removeElementAt(0);
            gbp = (GridBrokerProtocol)targetBroker.getProtocol(GridNode.getBrokerProtocolID(targetBroker));
            gbp.getComponentToDeploy(node, targetBroker,c.Clone(),true);
            componentQueue.removeElement(c);
            GridConfig.ExchangedComponents++;
            return true;
        }
        
        return deployToNeighbourBroker(node, protocolID, c, exchangePolicy);
        
    }
    public boolean deployToNeighbourBroker(GridNode node, int protocolID, GridComponent c, int policy){
        return deployToNeighbourBroker(node, protocolID, c, policy, -1);
    }
    public boolean deployToNeighbourBroker(GridNode node, int protocolID, GridComponent c, int policy, int callingPolicy){
        GridDeployHandleProtocol dhp;
        GridBrokerProtocol gbp;
        GridNode gNode,brokerNode;
        GridBrokerProtocol remote_gbp;
        GridCDProtocol remote_gcdp;
        
        int i, start;
        boolean matchFound = false;
        switch(policy){
            //==========================================================================================================================
            case RANDOM:
                brokerNode = (GridNode)Network.get(((GridNode)node).getRandomNeighbourBrokerIndex());
                gbp = (GridBrokerProtocol)brokerNode.getProtocol(GridNode.getBrokerProtocolID(brokerNode));
                if(gbp.getComponentToDeploy(node, brokerNode,c.Clone(),true)){
                     componentQueue.removeElement(c);
                     GridConfig.ExchangedComponents++;
                     GridConfig.ComponentsExchangingsWithRandomNeighbors ++;
                     componentsPassedtoRandomNeighbors++;//same as above, but related to this broker only
                     return true;
                }else
                    GridConfig.FailedComponentExchangings++;
                
//                i = node.getIndex();
//                int count = 0;
//                                
//                while(count++ < GridConfig.brokerIndexes.size()){//Try to submit to brokers with next index sequentially
//                    if(++i > GridConfig.brokerIndexes.size()-1)
//                        i = 0;
//                    
////                    brokerNode = (GridNode)Network.get(GridConfig.brokerIndexes.get(i));
//                    brokerNode = (GridNode)Network.get(CommonState.r.nextInt(GridConfig.brokerIndexes.size()));
//
//                    if(brokerNode.getIndex()==node.getIndex())continue;
//                    
//                    
//
//                    if(!((Linkable)node.getProtocol(FastConfig.getLinkable(protocolID))).contains(brokerNode)
//                            || !brokerNode.isUp())continue;
//                        
//                    
//
//                    gbp = (GridBrokerProtocol)brokerNode.getProtocol(GridNode.getBrokerProtocolID(brokerNode));
//                     
//
//                    if(gbp.getComponentToDeploy(node, brokerNode,c,true)){
//                       componentQueue.removeElementAt(0);
//                        GridConfig.ExchangedComponents++;
//                        GridConfig.ComponentsExchangingsWithRandomNeighbors ++;
//                        componentsPassedtoRandomNeighbors++;//same as above, but related to this broker only
//                        return true;
//                    }else{ 
//                        GridConfig.FailedComponentExchangings++;
//                        
//                    }
//                }
                break;
            //==========================================================================================================================
            case FLOCKING:
            case ADAPTIVEFLOCKING:
            case CONDORFLOCKP2P:    
                        if(c.ownerBrokerIndex != node.getIndex()){//This is not the owner broker of this component, then retern it to the owner
                            brokerNode = (GridNode)Network.get(c.ownerBrokerIndex);
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
                                return deployToNeighbourBroker(node, protocolID, c, ADAPTIVE, ADAPTIVEFLOCKING);
                            else if(policy == CONDORFLOCKP2P)//Pass to a matching neighbour broker
                                return deployToNeighbourBroker(node, protocolID, c, ADAPTIVEFUZZY, CONDORFLOCKP2P);
                            
                            int foreingBrokerIndex;

                            if(c.lastHostBrokerIndex == node.getIndex()){//Was not flocked to another broker yet, deploy it to the first broker in the flocking list
                                foreingBrokerIndex = 0;
                            }else{//Was flocked to another broker before, deploy it to the next broker in the flocking list
                                foreingBrokerIndex = node.neighborBrokers.indexOf(c.lastHostBrokerIndex);
                                if(++foreingBrokerIndex >= node.neighborBrokers.size())
                                    foreingBrokerIndex = 0;
                            }
                            brokerNode = (GridNode)Network.get(GridConfig.brokerIndexes.get(node.neighborBrokers.get(foreingBrokerIndex)));
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
                    matchFound = false;
                                        
                    start = 0;
                    for(int j = 0;j <gcdp.globalNodesResourceInfo.size();j++)
                        if(node.getIndex() == gcdp.globalNodesResourceInfo.get(j).brokerIndex){
                            start = j;  
                            break;
                        }
                    i = start;
                    
                    for(int j = 0;j <gcdp.globalNodesResourceInfo.size();j++,i++){
                        if(i >= gcdp.globalNodesResourceInfo.size()) i = 0;
                        
                        brokerNode = (GridNode)Network.get(gcdp.globalNodesResourceInfo.get(i).brokerIndex);
                        remote_gbp = (GridBrokerProtocol)brokerNode.getProtocol(GridNode.getBrokerProtocolID(brokerNode));
                        remote_gcdp = (GridCDProtocol)brokerNode.getProtocol(GridNode.getGridCDProtocolID(brokerNode));
                    //for(i = 0;i <gcdp.globalNodesResourceInfo.size();i++){
            
                      if(isSyncDataComparison && remote_gcdp.localNodesResourceInfo.matchComponent(c) > 0
                      || !isSyncDataComparison && gcdp.globalNodesResourceInfo.get(i).matchComponent(c) > 0){
                      

                            matchFound = true;

                            brokerNode = (GridNode)Network.get(gcdp.globalNodesResourceInfo.get(i).brokerIndex);

                            if(brokerNode.getIndex()==node.getIndex())continue;// Don't pass it to myself
                                                    
                            if(!brokerNode.isUp())continue;

                            if(!((Linkable)node.getProtocol(FastConfig.getLinkable(protocolID))).contains(brokerNode)
                            || !brokerNode.isUp())//If the matching node is not attached to a neighbor Broker report failed exchanging
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
        
                    if(matchFound){//There is a match but not in a neighbour --> Pass it to any neighbour
                        brokerNode = (GridNode)Network.get(NODE.getRandomNeighbourBrokerIndex());
                        gbp = (GridBrokerProtocol)brokerNode.getProtocol(GridNode.getBrokerProtocolID(brokerNode));
                
                        if(gbp.getComponentToDeploy(node, brokerNode,c,true)){
                           componentQueue.removeElement(c);
                            GridConfig.ExchangedComponents++;
                            GridConfig.ComponentsExchangingsWithRandomNeighbors ++;
                            componentsPassedtoRandomNeighbors++;//same as above, but related to this broker only
                            return true;
                        }
                    }else{ 
                        return deployToNeighbourBroker(node, protocolID, c, RANDOM);
                        
                    }
            break;
            //==========================================================================================================================
            case ADAPTIVEFUZZY:
            case ADAPTIVEFUZZYRT:    
            case RANKINGADAPTIVE:
                    String print = "";
                    matchFound = false;
                    float match,maxMatch = 0;
                    Vector<Float> FuzzyMatch = new Vector<Float>();
                    Vector<GridNode> matchingBrokers = new Vector<GridNode>();
                    
                    start = 0;
                    Vector<Integer> vector;
                    if(isSyncDataComparison){//Sync: Find this broker's index among all brokers
                        for(int k = 0;k <GridConfig.brokerIndexes.size();k++)
                            if(node.getIndex() == GridConfig.brokerIndexes.get(k)){
                                start = k;  
                                break;
                            }
                        vector = GridConfig.brokerIndexes;
                    }else{//Async: Find this broker's index brokers in the stored gcdp only
                        vector = new Vector<Integer>();
                        for(int k = 0;k <gcdp.globalNodesResourceInfo.size();k++){
                            vector.add(gcdp.globalNodesResourceInfo.get(k).brokerIndex);
                            if(node.getIndex() == gcdp.globalNodesResourceInfo.get(k).brokerIndex)
                                start = k;
                        }
                    }
                    
                    i = start;
                    
//                    for(int k = 0;k <gcdp.globalNodesResourceInfo.size();k++,i++){
//                        if(i >= gcdp.globalNodesResourceInfo.size()) i = 0;
                    for(int k = 0;k <vector.size();k++,i++){
                        if(i >= vector.size()) i = 0;
                    
                    
                                            
                        brokerNode = (GridNode)Network.get(vector.get(i));
                        Vector<Integer> route = getRoutingPathTo(brokerNode.getIndex());
                        remote_gbp = (GridBrokerProtocol)brokerNode.getProtocol(GridNode.getBrokerProtocolID(brokerNode));
                        remote_gcdp = (GridCDProtocol)brokerNode.getProtocol(GridNode.getGridCDProtocolID(brokerNode));
                        
                        if(brokerNode.getIndex() == node.getIndex()) continue;// Don't pass it to myself
                        
                        if(!brokerNode.isUp())continue;
                        
                        if(!((Linkable)node.getProtocol(FastConfig.getLinkable(protocolID))).contains(brokerNode)//If the matching node is not attached to a neighbor Broker report failed exchanging
                                && policy != ADAPTIVEFUZZYRT)//No use of routing table
                             continue;
                        if(callingPolicy == CONDORFLOCKP2P){//Policy for simulation is CONDORFLOCKP2P and there is no room in the owner broker, then pass to a neighbour based on this metric
                            match = remote_gcdp.localNodesResourceInfo.availableCPUs/ (1 + remote_gbp.getCurrentQueueSize());
                            GridConfig.connectionsBetweenBrokersPerCycle++;
                        }else if(policy == RANKINGADAPTIVE)
                            if(isSyncDataComparison){
                                match = (1/ (1 + remote_gbp.getCurrentQueueSize()))
                                        * remote_gcdp.localNodesResourceInfo.matchComponent(c);
                                GridConfig.connectionsBetweenBrokersPerCycle++;
                            }else
                                match = (100/(1 + gcdp.globalNodesResourceInfo.get(i).queueLength))
                                        * gcdp.globalNodesResourceInfo.get(i).matchComponent(c);
                        
                        else if(isSyncDataComparison){
                            match = GridHistogram.getFuzzyAND(remote_gcdp.localNodesResourceInfo.histogram, c
                                    , (float)1 * (remote_gcdp.localNodesResourceInfo.availableCPUs)
                                                /(float)((1 + remote_gbp.getCurrentQueueSize())//+route.size())
                                                   
                                               //*  (1 + CDState.getCycle() - gcdp.globalNodesResourceInfo.get(i).cycle)
                                                         )) ;
                            GridConfig.connectionsBetweenBrokersPerCycle++;
                        }else 
                            match = GridHistogram.getFuzzyAND(gcdp.globalNodesResourceInfo.get(i).histogram, c
                                    , (float)1 * (gcdp.globalNodesResourceInfo.get(i).availableCPUs)
                                               /(float)((1 + gcdp.globalNodesResourceInfo.get(i).queueLength) 
                                             //*  (1 + CDState.getCycle() - gcdp.globalNodesResourceInfo.get(i).cycle)
                                                        )) ;
//////                        print+="("+match+","+gcdp.globalNodesResourceInfo.get(i).brokerIndex+") ";
                        if(match > 0 
                                //&& gcdp.globalNodesResourceInfo.get(i).matchComponent(c) > 0
                           ){
                                //Set the matching value in the match vector with sorting descending    
                                matchFound = true;
                                if (FuzzyMatch.isEmpty()){
                                    FuzzyMatch.add(0, match);
                                    matchingBrokers.add(0, brokerNode);
                                }else{
                                    for(int j = 0; j <= FuzzyMatch.size(); j++){
                                        if(j == FuzzyMatch.size()){
                                            FuzzyMatch.add(match);
                                            matchingBrokers.add(brokerNode);
                                            break;
                                        }else if(match > FuzzyMatch.get(j)){
                                            FuzzyMatch.add(j, match);
                                            matchingBrokers.add(j, brokerNode);
                                            break;
                                        }              
                                    }
                                
                                }
                            }
                    }
//////                    System.err.println(print);
                    
//                        if(FuzzyMatch.size() > 0){
//                            float max = 0; int maxIndex = 0;
//                            for(i = 0; i < FuzzyMatch.size(); i++){
//                                if(FuzzyMatch.get(i) > max){
//                                    max = FuzzyMatch.get(i); maxIndex = i;
//                                }
//                            }   
//                            brokerNode = matchingBrokers.get(maxIndex);
//                            
//                            if(!((Linkable)node.getProtocol(FastConfig.getLinkable(protocolID))).contains(brokerNode)){
//                                c.routingPath = getRoutingPathTo(brokerNode.getIndex());
//                                brokerNode = (GridNode)Network.get(c.routingPath.get(0));                                
//                            }
//                                
//
//                            gbp = (GridBrokerProtocol)brokerNode.getProtocol(GridNode.getBrokerProtocolID(brokerNode));
//                            
//                            if(gbp.getComponentToDeploy(node, brokerNode,c.Clone(),true)){
//
//                               componentQueue.removeElement(c);
//                                GridConfig.ExchangedComponents++;
//                                return true; 
//                            }else{//If the matching broker cannot take it, Pass it to any neighbour
//                                brokerNode = (GridNode)Network.get(((GridNode)node).getRandomNeighbourBrokerIndex());
//                                        
//                                gbp = (GridBrokerProtocol)brokerNode.getProtocol(GridNode.getBrokerProtocolID(brokerNode));
//                                if(gbp.getComponentToDeploy(node, brokerNode,c.Clone(),true)){
//                                    componentQueue.removeElement(c);
//                                    GridConfig.ExchangedComponents++;
//                                    GridConfig.ComponentsExchangingsWithRandomNeighbors ++;
//                                    componentsPassedtoRandomNeighbors++;//same as above, but related to this broker only
//                                    return true; 
//                                }
////                                return deployToNeighbourBroker(node, protocolID, c, RANDOM);
//                            } 
//                                
//                        }
                    if(FuzzyMatch.size() > 0){
                        for(int j = 0; j < FuzzyMatch.size(); j++){//loop on all matching brokers
                            brokerNode = matchingBrokers.get(j);
                            if(!((Linkable)node.getProtocol(FastConfig.getLinkable(protocolID))).contains(brokerNode)){// This case will be 'true' only in case of ADAPTIVEFUZZYRT
                                   try{
                                   c.routingPath = getRoutingPathTo(brokerNode.getIndex());
                                   brokerNode = (GridNode)Network.get(c.routingPath.get(0));                                
                                   }catch(Exception ex){
                                       System.err.println("Failed to find route from "+NODE.getIndex()+" to "+brokerNode.getIndex());
                                       //System.err.println("Routing path size = "+ c.routingPath.size());
                                       System.exit(0);
                                   }
                            }


                            gbp = (GridBrokerProtocol)brokerNode.getProtocol(GridNode.getBrokerProtocolID(brokerNode));

                            if(gbp.getComponentToDeploy(node, brokerNode,c.Clone(),true)){

                                   componentQueue.removeElement(c);
                                    GridConfig.ExchangedComponents++;
                                    return true; 
                            }else if(callingPolicy == CONDORFLOCKP2P){//see only the highst match, no more comparisons
                                break;
                                //return true;
                            }else
                               continue;
                        }
                        // No matching broker accepts the deployment
                        if(callingPolicy != CONDORFLOCKP2P){
                            return deployToNeighbourBroker(node, protocolID, c, RANDOM);
                        }
                    }else{// No matching broker, Pass it to any neighbour
                        if(callingPolicy != CONDORFLOCKP2P){
                            return deployToNeighbourBroker(node, protocolID, c, RANDOM);
                        }
                    }
            break;
            //==========================================================================================================================
            case CENTER:
                    if(this.NODE.brokerIdentifier.index > 0 && GridNode.topology == GridNode.BROKERStar){//Pass it to the central meta-scheduler
                        brokerNode = (GridNode)Network.get(0);
                        gbp = (GridBrokerProtocol)brokerNode.getProtocol(GridNode.getBrokerProtocolID(brokerNode));
                        gbp.getComponentToDeploy(node, brokerNode,c.Clone(true),true);
                        componentQueue.removeElementAt(0);
                        GridConfig.ExchangedComponents++;
                        return true;//local brokers cannot deploy outside
                    }else{
                        if(isSyncDataComparison){
                            //for(i = 0;i <gcdp.globalNodesResourceInfo.size();i++){
                            for(i = 1;i < GridConfig.brokerIndexes.size(); i++){

                                brokerNode = (GridNode)Network.get(GridConfig.brokerIndexes.get(i));
                                //brokerNode = (GridNode)Network.get(gcdp.globalNodesResourceInfo.get(i).brokerIndex);

                                if(!((Linkable)node.getProtocol(FastConfig.getLinkable(protocolID))).contains(brokerNode)
                                        ||!brokerNode.isUp())
                                   continue;
                                gbp = (GridBrokerProtocol)brokerNode.getProtocol(GridNode.getBrokerProtocolID(brokerNode));
                                remote_gcdp = (GridCDProtocol)brokerNode.getProtocol(GridNode.getGridCDProtocolID(brokerNode));
                                
                                GridConfig.connectionsBetweenBrokersPerCycle++;//for the next line
                                if(remote_gcdp.localNodesResourceInfo.matchComponent(c) > 0)
                                    if(gbp.getComponentToDeploy(node, brokerNode,c.Clone(true),true)){
                                       componentQueue.removeElementAt(0);
                                        GridConfig.ExchangedComponents++;
                                        return true;
                                    }else 
                                        GridConfig.FailedComponentExchangings++;
                                else
                                    continue;
                            }
                        }else{
                           for(i = 0;i <gcdp.globalNodesResourceInfo.size();i++){
                                brokerNode = (GridNode)Network.get(gcdp.globalNodesResourceInfo.get(i).brokerIndex);

                                if(!((Linkable)node.getProtocol(FastConfig.getLinkable(protocolID))).contains(brokerNode)
                                        ||!brokerNode.isUp())
                                   continue;
                                gbp = (GridBrokerProtocol)brokerNode.getProtocol(GridNode.getBrokerProtocolID(brokerNode));
                                

                                if(gcdp.globalNodesResourceInfo.get(i).matchComponent(c) > 0)
                                    if(gbp.getComponentToDeploy(node, brokerNode,c.Clone(true),true)){
                                       componentQueue.removeElementAt(0);
                                        GridConfig.ExchangedComponents++;
                                        return true;
                                    }else 
                                        GridConfig.FailedComponentExchangings++;
                                else
                                    continue;
                            } 
                        }// Asynchrounus
                    }
                    break;
            default:
                    break;
                
        }
        return false;
    }
    
    public boolean readDeployedComponentsFromNode(Vector<Integer> IDs, Vector<Integer> StartCycles,NodeIdentifier nI){
        
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
   

}
