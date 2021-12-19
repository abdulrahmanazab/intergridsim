/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package example.grid;


import peersim.cdsim.CDProtocol;
import peersim.core.*;
import peersim.config.*;
import java.util.ArrayList;
import java.util.Vector;
import peersim.cdsim.CDState;
import peersim.edsim.EDProtocol;


/**
 *
 * @author 2906095
 */
public class GridCDProtocol implements CDProtocol, EDProtocol{
    long time;
    public long period = 0;
    
    public Vector<GridResourceCollection> globalNodesResourceInfo ;//Global
    //public Vector<GridNodeResourceState> NodesResourceInfo ;//Global
    public GridResourceCollection localNodesResourceInfo ;
    public Vector<GridResourceCollection> exchange1, exchange2 ;
    
    GridNodeResourceState rsSafe;
    
    GridNode gNode, bNodeBroker;
    
    GridProtocol gp;
    
    GridBrokerProtocol gbp;
    
    GridCDProtocol gcdp;
    
    GridDeployHandleProtocol gdhp;
    
    int nextNeighborBorkerIndex = 0;//next broker to exchange resource information with
    
    protected static final String PAR_EXCH_PERIOD = "exchangePeriod";
    public int dataExchangePeriod;
    public int exchangePeriod;
    
    int previousDataExchangeCycle = 0;
    
    int previousUpdateSentToBroker = 0;//[For workers] Cycle in which previous resource information update is sent to the broker
            
    ArrayList<Integer> neighbours = new ArrayList<Integer>();//[For Brokers] List of neighbour brokers
    private int linkableID; 
    private int BrokerProtocolID = -10;
    private int GridCDProtocolID = -10;
    
    public GridCDProtocol(String prefix){
        dataExchangePeriod = Configuration.getInt(prefix + "." + PAR_EXCH_PERIOD, 1);
                
    }
    @Override
    public void processEvent( Node node, int pid, Object event ){
        
    }        
    void Initialize(){
        globalNodesResourceInfo = new Vector<GridResourceCollection>();
        //NodesResourceInfo = new Vector<GridNodeResourceState>();
        localNodesResourceInfo = new GridResourceCollection();
        exchange1 = new Vector<GridResourceCollection>();
        exchange2 = new Vector<GridResourceCollection>();
    }
    
    @Override
    public void nextCycle (Node node, int protocolID){
        
        if(CDState.getCycle() == 0)exchangePeriod = CommonState.r.nextInt(node.getIndex()+1)/2;
        
        time = System.currentTimeMillis(); period = 0;
          
        linkableID = FastConfig.getLinkable(protocolID);
        
        gNode = (GridNode)node;
        
        gp = (GridProtocol) gNode.getProtocol(linkableID);
        
        bNodeBroker = gNode.getBroker();
        
              
        
        //-----------------------------------------------------------------------
        // Reaction to the failure of the broker of this node
        //-----------------------------------------------------------------------
        if (!bNodeBroker.isUp()){
            brokerFailureReaction(node,bNodeBroker); 
            return;//start communicating with the new broker from the next cycle
        }       
                
        //-----------------------------------------------------------------------
        //Update this node's resource information in the database of its broker
        //-----------------------------------------------------------------------
        if(BrokerProtocolID == -10)BrokerProtocolID = GridNode.getBrokerProtocolID(node);
        if(GridCDProtocolID == -10)GridCDProtocolID = GridNode.getGridCDProtocolID(node);
        
        //get the objects for the protocols of the broker of this node
        gbp = (GridBrokerProtocol)bNodeBroker.getProtocol(BrokerProtocolID);
        gcdp = (GridCDProtocol)bNodeBroker.getProtocol(GridCDProtocolID);
        
        //get the objects for the protocols of this node
        gdhp = (GridDeployHandleProtocol)node.getProtocol(GridNode.getDeployHandleProtocolID(node));
        
        //Deliver resource information data of this node to its broker
        if(CDState.getCycle() < 2 || gdhp.resourceCaseChanged 
                //|| CDState.getCycle() - previousUpdateSentToBroker > dataExchangePeriod
                ){
            gcdp.readResourceStateFromNode(gNode.getResourceState(),gNode.brokerIdentifier.index);
            gdhp.resourceCaseChanged = false;
            previousUpdateSentToBroker = CDState.getCycle();// Send update at least every 3 cycles
        }
        
        //--------------------------------------------------------------------------------
        //If This is a broker node, exchange resource information with a neighbor broker
        //--------------------------------------------------------------------------------
        if(gNode.isBroker){
                                    
            int neighborIndex = getNextBrokerIndex();

            localNodesResourceInfo.updateResourceUtilization();
            
//            
//            
//            neighbours.clear();
//            
//            
//
//            for(int i=0;i<GridConfig.brokerIndexes.size();i++){
//
//                Node n = Network.getWithID(GridConfig.brokerIndexes.get(i),GridConfig.brokerIDs.get(i));
//                if(gp.contains(n))
//                    neighbours.add(n.getIndex());
//              }
//            
//            if(neighbours.size()==0){
//                System.err.println("Broker Node "+gNode.getIndex()+" Is not connected to the broker network");
//                return;
//            }
//            else if(neighbours.size()==1)
//                neighborIndex = neighbours.get(0);
//            else{
////////            neighborIndex = random.get(CommonState.r.nextInt(random.size()-1));
//            
//                if(nextNeighborBorkerIndex >= neighbours.size())nextNeighborBorkerIndex = 0;
//                neighborIndex = neighbours.get(nextNeighborBorkerIndex++);
//            }


            for(int i = 0;i < globalNodesResourceInfo.size();i++){
                if(i >= globalNodesResourceInfo.size())break;
                if((CDState.getCycle() - globalNodesResourceInfo.get(i).cycle)>=GridConfig.dataAge)

                    globalNodesResourceInfo.removeElementAt(i);


            }

            //Exchange data with choosen neighbor broker

            if(CDState.getCycle() < 2 
                    || CDState.getCycle() - previousDataExchangeCycle >= exchangePeriod){
                previousDataExchangeCycle = CDState.getCycle();
                if(CDState.getCycle() > 2) exchangePeriod = dataExchangePeriod;
                gcdp = (GridCDProtocol)Network.get(neighborIndex)
                        .getProtocol(GridNode.getGridCDProtocolID(Network.get(neighborIndex)));

                globalNodesResourceInfo = gcdp.ExchangeResourceData((GridNode)Network.get(neighborIndex)
                        ,gNode,globalNodesResourceInfo);
                
                GridConfig.connectionsBetweenBrokersPerCycle++;
            }



            if(!(GridConfig.print == GridConfig.SIMPLE)){
                System.out.println("Boker Node "+gNode.getIndex()+" Exchange data with Broker Node "+neighborIndex+"\n");
                printResourceData();
            }

        }
        
        period = System.currentTimeMillis() - time;
        
          
        
        
    } 
    
    void brokerFailureReaction(Node node, GridNode broker){
        
            if(broker.getDomainFailState() == GridNode.DOMAIN_ON){//The broker is DOWN but The domain nodes are still sending their information to the broker --> used in case of churn failure mode.
                return;
            }else if(broker.getDomainFailState() == GridNode.DOMAIN_DOWN){//The broker is DOWN and The domain nodes will be set to DOWN upon the broker failure --> used in case of permanent failure mode.
                node.setFailState(Node.DOWN);
                return;
            }else if(broker.getDomainFailState() == GridNode.DOMAIN_DIVIDED){//The broker is DOWN but The domain nodes will be joining other domains upon the broker failure --> used in case of permanent failure mode.
            
                bNodeBroker=(GridNode)Network.get(getRandomBrokerIndex());
                
                gp.addNeighbor(bNodeBroker);
                gNode.setBroker(new NodeIdentifier(bNodeBroker.getIndex(), bNodeBroker.getID()));
                
                System.err.println("Broker of Node "+node.getIndex()+" Failed \t New Broker is Broker " + bNodeBroker.getIndex());
            }
    }
    
    public Vector<GridResourceCollection> ExchangeResourceData(GridNode calledNode,GridNode node, Vector<GridResourceCollection> resourceCollections){
        
        GridResourceCollection rs1,rs2;
        
        exchange1.clear();//For the local broker
        exchange2.clear();//For the calling broker
         
        gNode = calledNode;
         
         
        for(int i = 0;i < GridConfig.brokerIndexes.size();i++){
             
             if(globalNodesResourceInfo.isEmpty() && resourceCollections.isEmpty())
                 break;
             else if(globalNodesResourceInfo.isEmpty() && !resourceCollections.isEmpty()){
                 
                 for(int j = 0; j < resourceCollections.size();j++)
                     if((CDState.getCycle()-resourceCollections.get(j).cycle) < GridConfig.dataAge){
                        insertElementInResourceCollection(gNode, resourceCollections.get(j)
                                , exchange1);
                        insertElementInResourceCollection(node, resourceCollections.get(j)
                                , exchange2);
                 }
                 break;
             }else if(!globalNodesResourceInfo.isEmpty() && resourceCollections.isEmpty()){
                 
                 for(int j = 0; j < globalNodesResourceInfo.size();j++)
                     if((CDState.getCycle()-globalNodesResourceInfo.get(j).cycle)<GridConfig.dataAge){
                         insertElementInResourceCollection(gNode, globalNodesResourceInfo.get(j)
                                , exchange1);
                         insertElementInResourceCollection(node, globalNodesResourceInfo.get(j)
                                , exchange2);
                     }
                     
                 
                 break;
             }
                 
             
             
             rs1 = cutElementFromDataColloection(GridConfig.brokerIndexes.get(i),globalNodesResourceInfo);
             rs2 = cutElementFromDataColloection(GridConfig.brokerIndexes.get(i),resourceCollections);
             
//             int vv=10;
//                 if(Network.get(i).getIndex()==0&&CDState.getCycle()>300)
//                 {vv=gNode.getIndex();
//                        vv=7;}
             
             if(rs1 != null && rs2 != null){
             
                 if(rs1.cycle >= rs2.cycle){
                     insertElementInResourceCollection(gNode, rs1
                                , exchange1);
                     insertElementInResourceCollection(node, rs1
                                , exchange2);
                 }else{
                     insertElementInResourceCollection(gNode, rs2
                                , exchange1);
                     insertElementInResourceCollection(node, rs2
                                , exchange2);
                 }
                 
                 
             }
             else if (rs1 != null){
                insertElementInResourceCollection(gNode, rs1
                                , exchange1);
                insertElementInResourceCollection(node, rs1
                                , exchange2);
             }else if (rs2 != null){
                insertElementInResourceCollection(gNode, rs2
                                , exchange1);
                insertElementInResourceCollection(node, rs2
                                , exchange2);
                  
             }
             
         }         
        
        globalNodesResourceInfo = (Vector<GridResourceCollection>) exchange1.clone();
        
//////        boolean addedToList = false;
//////        if(rsSafe!=null){
//////        for(int i=0; i<NodesResourceInfo.size();i++){
//////                
//////                if(NodesResourceInfo.get(i).NodeID == rsSafe.NodeID){
//////                    
//////                    NodesResourceInfo.setElementAt(rsSafe, i);
//////                    exchange.setElementAt(rsSafe, i);
//////                      
//////                    addedToList = true;
//////                }
//////                    
//////            }
//////           if(!addedToList) {
//////               localNodesResourceInfo.add(rsSafe);
//////                  exchange.add(rsSafe);
//////           }
//////           
//////        }
        
        return (Vector<GridResourceCollection>) exchange2.clone();
        
        
        
    }
    void insertElementInResourceCollection(GridNode node, GridResourceCollection element, Vector<GridResourceCollection> collection){
        
        GridProtocol GP = (GridProtocol)node.getProtocol(GridNode.getLinkableProtocolID(node));
        
        if(GP.contains(Network.get(element.brokerIndex)))
             collection.add(0,element);
        else
             collection.add(element);
        
    }
    
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
    
    GridResourceCollection cutElementFromDataColloection(int brokerIndex, Vector<GridResourceCollection> vector){
        
        GridResourceCollection ret;
        
        for(int i = 0;i < vector.size();i++){
            if((CDState.getCycle()-vector.get(i).cycle)>=GridConfig.dataAge)vector.removeElementAt(i);
            
            if(i>=vector.size())break;// because elements may be removed during the loop
            
            if (vector.get(i).brokerIndex == brokerIndex){
                ret = vector.get(i).clone();
                
                vector.removeElementAt(i);
                return ret;
                
            }
           }
        
        return null;
        
    }
    
    public int globalResourceInfoSize(){
        int size = 0;
        for(GridResourceCollection rc : globalNodesResourceInfo)
            size+= rc.collection.size();
        
        return size;
    }
    
    //Called in brokers by nodes to update their resource information in the broker's database
    public boolean  readResourceStateFromNode(GridNodeResourceState rs, int brokerIndex){
        
        if(localNodesResourceInfo.cycle < CDState.getCycle()){// This is the first update in this cycle
            localNodesResourceInfo.cycle = CDState.getCycle();
            localNodesResourceInfo.brokerIndex = brokerIndex;
            
        }
        
        
        if(gNode!=null)
            if(gNode.getIndex()==rs.NodeIndex)rsSafe=rs.clone();
        
                
        //Insert in the collection for local resources
//////       if(localNodesResourceInfo.collection.isEmpty())
//////           localNodesResourceInfo.collection.add(rs);
//////       else{
//////            
//////            for(int i=0; i<localNodesResourceInfo.collection.size();i++){
//////                
//////                if(localNodesResourceInfo.collection.get(i).NodeID == rs.NodeID){
//////                    
//////                    localNodesResourceInfo.collection.setElementAt(rs, i);
//////                      
//////                    addedToList = true;
//////                }
//////                    
//////            }
//////           if(!addedToList) localNodesResourceInfo.collection.add(rs);
//////            
//////            
//////       }
        localNodesResourceInfo.addElement(rs);
              
        
        
        //Insert in the collection for global resources
        if(globalNodesResourceInfo.isEmpty()){
           globalNodesResourceInfo.add(localNodesResourceInfo.clone());
           return true;
        }else{
            
            for(int i=0; i<globalNodesResourceInfo.size();i++){
                
                if(globalNodesResourceInfo.get(i).brokerIndex == localNodesResourceInfo.brokerIndex){
                    
                    globalNodesResourceInfo.setElementAt(localNodesResourceInfo.clone(), i);
                                       
                    return true;
                }
                    
            }
            globalNodesResourceInfo.add(localNodesResourceInfo.clone());
            return true;
       }
        
        
    }
    
    
    public void printResourceData(){
        System.out.println("Stored Resource Data: >>>>>>>");
        
        if(globalNodesResourceInfo.isEmpty())return;
        
        for(int i = 0; i<globalNodesResourceInfo.size();i++ ){
            for(int j = 0; j<globalNodesResourceInfo.get(i).collection.size();j++ )
            System.out.println(globalNodesResourceInfo.get(i).collection.get(j).toString());
                        
        }
        System.out.println("<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>");
        
    }
       
//    @Deprecated
//    void exchangeDataWithNeighborBroker(){
//        //choose broker
//        int neighborIndex;
//        
//        random.clear();
//        
//        for(int i=0;i<GridConfig.brokerIndexes.size();i++){
//        
//            Node node = Network.getWithID(GridConfig.brokerIndexes.get(i),GridConfig.brokerIDs.get(i));
//            if(gp.contains(node))
//                random.add(node.getIndex());
//          }
//        if(random.size()==0){
//            System.err.println("Broker Node "+gNode.getIndex()+" Is not connected to the broker network");
//            return;
//        }
//        else if(random.size()==1)
//            neighborIndex = random.get(0);
//        else
//        neighborIndex = random.get(CommonState.r.nextInt(random.size()-1));
//        
//        //Exchange data with choosen neighbor broker
//        
//        System.out.println("Boker Node "+gNode.getIndex()+" Exchange data with Broker Node "+neighborIndex+"\n");
//    }
//     @Deprecated   
//     void PrintNodeData(){
//        
//        String isBroker = "[Node]";
//        
//        if (gNode.getIsBrokerState())isBroker="[Broker]";
//        
//        String neighbors = "[";
//        
//        for(int i=0;i<gp.neighbors.length;i++){
//            
//            if(gp.neighbors[i] == null)break;
//            
//            if(i>0)neighbors+=",";
//            neighbors+=gp.neighbors[i].getIndex();
//        }
//        neighbors+="]";
//        
//        
//        System.out.println("Node "+gNode.getIndex()+"  "+isBroker+": ");
//        System.out.println("Neighbors:\n"+neighbors);
//        System.out.println("Resource State:\n"+gNode.resourceState.toString());
//        
//        System.out.println("Components:");
//        if(gNode.components.isEmpty()){
//            System.out.println("No current Deployed componernts");
//            return;
//        }
//        String buffer="";
//        
//            
//            for(int i=0;i<gNode.components.size();i++){
//            buffer+=gNode.components.get(i).toString()+"\tStarted at cycle "
//                    +gNode.componentStartCycle.get(i)+"\n";
//        }         
//            
//            
//        System.out.println(buffer);
//        
//    }
    
    int getNextBrokerIndex(){
                
        if(gNode.neighborBrokers.size()==1)
                return gNode.neighborBrokers.get(0);
        else if(gNode.neighborBrokers.size()==0){
                System.err.println("Broker Node "+gNode.getIndex()+" Is not connected to the broker network");
                return 0;
        }else{
                if(nextNeighborBorkerIndex >= gNode.neighborBrokers.size())nextNeighborBorkerIndex = 0;
                return gNode.neighborBrokers.get(nextNeighborBorkerIndex++);
        }
        
    }
    
    int getRandomBrokerIndex(){
        CommonState.r.setSeed(System.currentTimeMillis());
        int index = CommonState.r.nextInt(GridConfig.brokerIndexes.size());
        Node n = Network.get(index);
        try{
            if(!n.isUp()){
                Thread.sleep(10);
                index = getRandomBrokerIndex();
            }//Make sure that the broker to kill is not the one with index "0" which central deployments are made at
        }catch(Exception ex){
            System.err.println("##########>>>>> "+ex.getMessage());
        }
        return index;
    }
    
    @Override
    public Object clone ()
    {
        GridCDProtocol sl = null;
        try
        {
            sl = (GridCDProtocol) super.clone();
        } catch (CloneNotSupportedException e)
        {
        } // never happens
        return sl;
    }

}
