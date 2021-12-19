/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package example.grid;

import peersim.cdsim.CDState;
import peersim.config.*;
import peersim.core.*;
import java.util.Vector;

/**
 *
 * @author 2906095
 */
public class GridFailureControl implements Control{
    
    private static final String PAR_PROT = "protocol";
    //private static final String PAR_FILE = "outfile";
    private final int pid;
    
    //private static final String PAR_FAULT = "failures";
    private static final String PAR_FALLIBLE_BROKERS = "fallibleBrokers";//How many brokers will fail, in case of 'CHURN': How many brokers are fallible (i.e. will have churn joining and leaving the overlay)
    private static final String PAR_MAX = "maxInterval";//maximum number of cycles between each two "Broker" failures, in case of 'CHURN': Max cycles a failed broker will stay up/down
    private static final String PAR_MIN = "minInterval";//minimum number of cycles between each two "Broker" failures, in case of 'CHURN': Max cycles a failed broker will stay up/down
    private static final String PAR_MAX_FAILURES = "maxFailures";//The total maximum number of failures. If exceeded, then no more failures.
    
    protected static final String PAR_FAILURE_MODE = "mode";
    protected static final String[] FAILURE_MODE_ARRAY = {"permanent","churn"};
    
    protected static final String PAR_CHILD = "childNodes";//The action of child nodes upon broker failure: go down, or join another broker
    protected static final String[] PAR_CHILD_ARRAY = {"on","down","divide"};
    //private static final String PAR_FILE = "outfile";
    private final int fallibleBrokers;//Total No.of failures.
    private final int maxInterval;
    private final int minInterval;
    private int maxFailures;
    private final int failureMode;
    private final String failureModeString;
    
    private final int childNodes;
    private final String childNodesString;
    
    private final int PERMANENT = 0, CHURN = 1;
    
    int frequency = 0;
    int failureCounter =0;
    int nextFailure = 0;
    
    /*
     * In case of Churn
     */
    Vector<NodeChrunElement> nodeChurnList;
    
    public GridFailureControl(String prefix){
        
        pid = Configuration.getPid(prefix + "." + PAR_PROT);
        fallibleBrokers = Configuration.getInt(prefix + "." + PAR_FALLIBLE_BROKERS);
        maxInterval = Configuration.getInt(prefix + "." + PAR_MAX,20);
        minInterval = Configuration.getInt(prefix + "." + PAR_MIN,10);
        maxFailures = Configuration.getInt(prefix + "." + PAR_MAX_FAILURES,100);
        failureModeString = Configuration.getString(prefix + "." + PAR_FAILURE_MODE,FAILURE_MODE_ARRAY[0]);
        childNodesString = Configuration.getString(prefix + "." + PAR_CHILD,PAR_CHILD_ARRAY[0]);
        
        nodeChurnList = new Vector<NodeChrunElement>();
        
        if(failureModeString.equals(FAILURE_MODE_ARRAY[0])){
            failureMode = PERMANENT;
            maxFailures = fallibleBrokers;//Since brokers permamently fails, then the maximum No. of failures = No. of fallible brokers
        }else
            failureMode = CHURN;
        
        if(childNodesString.equals(PAR_CHILD_ARRAY[0]))
            childNodes = GridNode.DOMAIN_ON;
        else if(childNodesString.equals(PAR_CHILD_ARRAY[1]))
            childNodes = GridNode.DOMAIN_DOWN;
        else 
            childNodes = GridNode.DOMAIN_DIVIDED;
        
        GridConfig.failedBrokers = new Vector<Integer>();
        GridConfig.failuresEnabled = true;
    }
    
    public boolean execute(){
        
        if(failureMode == CHURN)
            return manageBrokersChurn();
        
        
        if(fallibleBrokers==0)return false;
        //if(frequency==0)frequency = (int)(CDState.getEndTime()/faults);
        
        if(nextFailure==0 || failureCounter ==maxFailures){//Initialial state OR all failures have happened
            nextFailure = CDState.getCycle() + CommonState.r.nextInt(maxInterval);
            if(nextFailure < CDState.getCycle()+minInterval)nextFailure=CDState.getCycle()+minInterval;
            
            return false;
            
        }else if(CDState.getCycle()>=nextFailure){
        
            nextFailure = CDState.getCycle() + CommonState.r.nextInt(maxInterval);
            if(nextFailure < CDState.getCycle()+minInterval)nextFailure=CDState.getCycle()+minInterval;
            
            
            failureCounter++;
            
            int rand = getBrokerIndex();
            
            GridNode node = (GridNode)(Network.get
                    (GridConfig.brokerIndexes.get(rand)));        
             
            GridBrokerProtocol  gbp=(GridBrokerProtocol)node.getProtocol(GridNode.getBrokerProtocolID(node));
            GridCDProtocol  gcdp=(GridCDProtocol)node.getProtocol(GridNode.getGridCDProtocolID(node));
            //Consider all tasks in the queue of this broker as failed            
            GridConfig.failedDeployments+=gbp.componentQueue.size();
            GridConfig.failedComponents+=gbp.componentQueue.size();
            
                       
            node.setFailState(Node.DOWN);//kill the broker
            node.setDomainFailState(childNodes);//Set the fail state of child nodes in the domain
            if(childNodes == GridNode.DOMAIN_DOWN || childNodes == GridNode.DOMAIN_DIVIDED)
                gcdp.localNodesResourceInfo.clear();//Clear all resource information of child nodes
            GridConfig.failedBrokers.add(node.getIndex());
            /*
            for(int i : GridConfig.brokerIndexes){//Remove the killed broker from the flocking list of other brokers
                //node = (GridNode)(Network.get(GridConfig.brokerIndexes.get(i)));
                node = (GridNode)(Network.get(i));
                if(node.neighborBrokers.contains(GridConfig.brokerIndexes.get(rand)))
                    node.neighborBrokers.removeElement(GridConfig.brokerIndexes.get(rand));
                
                //Remove the down broker from the routing tables of other brokers
                gbp = (GridBrokerProtocol)node.getProtocol(GridNode.getBrokerProtocolID(node));
                if(gbp.routingTable == null)continue;
                int j = 0;
                while(j < gbp.routingTable.table.size())
                    if(gbp.routingTable.table.get(j++).brokerIndex == i){
                        gbp.routingTable.table.removeElementAt(j);
                        break;
                    }
                
            }
            
            int index = GridConfig.brokerIndexes.indexOf(GridConfig.brokerIndexes.get(rand));
            
            
            GridConfig.brokerIndexes.removeElementAt(rand);
            GridConfig.brokerIDs.removeElementAt(rand);
            GridConfig.brokers--;
            */
            System.err.println("****************************Broker ("+rand+") Failed**************************");
            
            
        
        }
        
        return false;
    }
    
    boolean manageBrokersChurn(){
        
        GridNode broker;
        if(nodeChurnList.isEmpty()){//First cycle --> initialize fallible brokers churn list
            int period;
            for(int i = 0; i < fallibleBrokers; i++){
                broker = (GridNode)Network.get(getBrokerIndex());
                broker.setDomainFailState(childNodes);
                period = CommonState.r.nextInt(maxInterval);
                if(period < minInterval)period = minInterval;
                nodeChurnList.add(new NodeChrunElement(broker, period));
            }
            return false;
        }
        
        for(NodeChrunElement nci:nodeChurnList){
            if(!nci.node.isUp() && (CDState.getCycle() - nci.lastUPCycle) > nci.churnPeriod){//Node is down
                nci.node.setFailState(Node.OK);
                nci.lastDownCycle = CDState.getCycle();
                                
                GridConfig.failedBrokers.removeElement(nci.node.getIndex());
                
            }else if(nci.node.isUp() && (CDState.getCycle() - nci.lastDownCycle) > nci.churnPeriod){//Node is up
                if(++failureCounter > maxFailures)//Maximum number of failures exceeded.
                    continue;
                nci.node.setFailState(Node.DOWN);
                nci.lastUPCycle = CDState.getCycle();
                
                GridConfig.failedBrokers.add(nci.node.getIndex());
            }
        }
        return false;
    }
    
    int getBrokerIndex(){
        int index = CommonState.r.nextInt(GridConfig.brokerIndexes.size());
        Node n = Network.get(index);
        if(GridConfig.brokerIndexes.get(index)==0 || !n.isUp()
                || GridConfig.deployedAtBrokers.contains(index))// Not one of the brokers where the deployments take place
            index=getBrokerIndex();//And also make sure that the broker to kill is not the one with index "0" which central deployments are made at
        
        return index;
    }

}
