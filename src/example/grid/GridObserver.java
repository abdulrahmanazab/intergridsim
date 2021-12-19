/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package example.grid;

import peersim.core.Control;
import peersim.config.*;
import peersim.core.*;
import java.io.*;
import javax.swing.text.StringContent;
import peersim.cdsim.CDState;

/**
 *
 * @author 2906095
 */
public class GridObserver implements Control{

    
    private static final String PAR_PROT = "protocol";
    //private static final String PAR_FILE = "outfile";
    private final int pid;
    //private final String OutFilePath;
    long currentTime = 0, time = 0;
            
    File f;
    PrintWriter pw;
    FileOutputStream fos; 
    PrintStream ps;
    
    String print,print2;
    
    int fullDataCycle = -1;
    
    GridNode gNode;
        
    GridProtocol gp;
    
    
    
    public GridObserver(String prefix){
        
        pid = Configuration.getPid(prefix + "." + PAR_PROT);
        //OutFilePath = Configuration.getString(prefix + "." + PAR_FILE);
    }
    
    public boolean execute(){
        
          
       int linkableID = FastConfig.getLinkable(pid);
        
        GridDeployHandleProtocol gdh;
        
        GridCDProtocol gcdp,gcdp2;
        
        GridNode gBNode = (GridNode) Network.get(GridConfig.brokerIndexes.get(0));
       
        GridBrokerProtocol gbp =(GridBrokerProtocol)gBNode.getProtocol(GridNode.getBrokerProtocolID(gBNode));
        gcdp =(GridCDProtocol)gBNode.getProtocol(GridNode.getGridCDProtocolID(gBNode));
        
        
        if(gcdp.globalResourceInfoSize()==Network.size()){
            
            if(fullDataCycle == -1)fullDataCycle = CDState.getCycle();
        
            }
        
        int size=0;
        GridNode gnode;
        GridBrokerProtocol gbp2;
        for(int i=0;i<GridConfig.brokerIndexes.size();i++){
            
            gnode = (GridNode)Network.get(GridConfig.brokerIndexes.get(i));
            gbp2=(GridBrokerProtocol)gnode.getProtocol(GridNode.getBrokerProtocolID(gnode));
            
            size+=gbp2.componentQueue.size();
                            
                    
        }
        GridConfig.waitingComponents=size; 
        
        int TotalComponents =GridConfig.waitingComponents+GridConfig.failedComponents+GridConfig.Deployments;
        
        if(TotalComponents< GridConfig.generatedComponents)
            GridConfig.failedDeployments+=(GridConfig.generatedComponents-TotalComponents);
        
         time = System.currentTimeMillis() - currentTime;
         currentTime = System.currentTimeMillis();
         System.out.println("===========================Cycle : " +
                peersim.cdsim.CDState.getTime() +
                "=========================== Cycle time (ms): " + time);
         System.out.println("# Node Types "+getNodesSpecifications()+"\t");
         System.out.println("# Component types "+getComponentsSpecifications());
         System.out.println("Total gernerated components for deployment "+GridConfig.generatedComponents);
         System.out.println("Failed Deployments "+GridConfig.failedDeployments);
         System.out.println("Failed Components "+GridConfig.failedComponents);
         System.out.println("Suceeded deploments "+GridConfig.deployedComponents.size()
                 +"\t#Finished Components "+GridConfig.executedComponents
                 +"\tDeployments/cycle "+GridConfig.deployedComponentsPerCycle);
         System.out.println("Waiting components "+GridConfig.waitingComponents);
         System.out.println("No. of component exchangings "+GridConfig.ExchangedComponents
                  +"\tConnectionsBetweenBrokers/cycle "+GridConfig.connectionsBetweenBrokersPerCycle///GridConfig.brokerIndexes.size()
                 );
         System.out.println("No. of failed component exchangings "+GridConfig.FailedComponentExchangings);
         
         if(fullDataCycle >=0)
         System.out.println("Broker 0 has complete data since cycle "+fullDataCycle);
         
         //---------------------------------------------------------------------
         System.out.println("Component Exchangings With Random Neighbors "
                 + GridConfig.ComponentsExchangingsWithRandomNeighbors);
         //---------------------------------------------------------------------
         /* print = "";
         
         gnode = (GridNode)Network.get(GridConfig.brokerIndexes.get(1));
         for(int i=0;i < gnode.neighborBrokers.size(); i++)
             print+=String.valueOf(gnode.neighborBrokers.get(i))+" ";
         
         System.out.println("Broker 1 has neighbor brokers: "+print); */
         //---------------------------------------------------------------------
//          gbp = (GridBrokerProtocol)
//                         ((GridNode)Network.get(GridConfig.brokerIndexes.get(1))).getProtocol(GridNode.getBrokerProtocolID((GridNode)Network.get(GridConfig.brokerIndexes.get(1))));
//          print = "";
//          for(int i = 0; i<gbp.componentQueue.size()-1; i++)
//              print+= String.valueOf(gbp.componentQueue.get(i).queuedAt)+ " ";
//          System.out.println("Broker 2 queue: "+print);
//          
//          gbp = (GridBrokerProtocol)
//                         ((GridNode)Network.get(GridConfig.brokerIndexes.get(2))).getProtocol(GridNode.getBrokerProtocolID((GridNode)Network.get(GridConfig.brokerIndexes.get(2))));
//          print = "";
//          for(int i = 0; i<gbp.componentQueue.size()-1; i++)
//              print+= String.valueOf(gbp.componentQueue.get(i).queuedAt)+ " ";
//          System.out.println("Broker 3 queue: "+print);
         //---------------------------------------------------------------------
         print = "";
         for(int i : GridConfig.deployedAtBrokers)
             print+=i+" ";
         System.out.println("(Deployed At)Broker Indexes: "+print);
         //---------------------------------------------------------------------
         if(GridConfig.failuresEnabled && GridConfig.failedBrokers != null){
            print = "";
            for(int i = 0; i < GridConfig.failedBrokers.size(); i++)
                print+= String.valueOf(GridConfig.failedBrokers.get(i))+" ";
            System.out.println("@@@@@@Failed Brokers: " + print);
         }
             
         //---------------------------------------------------------------------
         System.out.println("Broker Indexes: ");
         print = "";
         for(int i = 0; i < GridConfig.brokerIndexes.size(); i++){
            print+="(" + String.valueOf(GridConfig.brokerIndexes.get(i))+")\t";
         }
         System.out.println(print);
         //---------------------------------------------------------------------
         //System.out.println("Broker Queue size & Last Host Broker of Q(0): ");
         System.out.println("Broker Queue size & No. of free CPUs ");
         print = ""; print2="";
         for(int i = 0; i < GridConfig.brokerIndexes.size(); i++){
            gNode =(GridNode) Network.get(GridConfig.brokerIndexes.get(i));
            gbp =(GridBrokerProtocol)gNode
               .getProtocol(GridNode.getBrokerProtocolID(gNode));
            gcdp2 =(GridCDProtocol)gNode
               .getProtocol(GridNode.getGridCDProtocolID(gNode));
            print+=String.valueOf(gbp.getCurrentQueueSize())+"\t";
            print2+=String.valueOf(gcdp2.localNodesResourceInfo.availableCPUs)+"\t";
            /*
            if(gbp.componentQueue.size()>0)
                print2+=String.valueOf(gbp.componentQueue.get(0).lastHostBrokerIndex) +"\t";
            else
                print2+="0\t";
            */
         }
         System.out.println(print);
         System.out.println(print2);
         
         //---------------------------------------------------------------------
         System.out.println("Broker Queued Components & No. of components passed to random neighbors: ");
         print = ""; print2 = "";
         for(int i = 0; i < GridConfig.brokerIndexes.size(); i++){
            gNode =(GridNode) Network.get(GridConfig.brokerIndexes.get(i));
            gbp =(GridBrokerProtocol)gNode
               .getProtocol(GridNode.getBrokerProtocolID(gNode));
            print+=String.valueOf(gbp.queuedComponents)+"\t";
            print2+=String.valueOf(gbp.componentsPassedtoRandomNeighbors)+"\t";
        }
        System.out.println(print);
        System.out.println(print2);
        //---------------------------------------------------------------------
        System.out.println("Broker Local Deployments: ");
         print = "";
         for(int i = 0; i < GridConfig.brokerIndexes.size(); i++){
            gNode =(GridNode) Network.get(GridConfig.brokerIndexes.get(i));
            gbp =(GridBrokerProtocol)gNode
               .getProtocol(GridNode.getBrokerProtocolID(gNode));
            print+=String.valueOf(gbp.localDeployments)+"\t";
        }
        System.out.println(print);
        //---------------------------------------------------------------------
//////        System.out.println("Broker protocol run time: ");
//////         print = "";
//////         for(int i = 0; i < GridConfig.brokerIndexes.size(); i++){
//////            gNode =(GridNode) Network.get(GridConfig.brokerIndexes.get(i));
//////            gbp =(GridBrokerProtocol)gNode
//////               .getProtocol(GridNode.getBrokerProtocolID(gNode));
//////            print+=String.valueOf(gbp.period)+"\t";
//////        }
//////        System.out.println(print);
//////        //---------------------------------------------------------------------
//////        System.out.println("Grid CD protocol run time: ");
//////         print = "";
//////         for(int i = 0; i < GridConfig.brokerIndexes.size(); i++){
//////            gNode =(GridNode) Network.get(GridConfig.brokerIndexes.get(i));
//////            gcdp =(GridCDProtocol)gNode
//////               .getProtocol(GridNode.getGridCDProtocolID(gNode));
//////            print+=String.valueOf(gcdp.period)+"\t";
//////        }
//////        System.out.println(print);
        //---------------------------------------------------------------------
        System.out.println("Histogram Data: ");
        for(int i = 0; i < 3; i++){
            gNode =(GridNode) Network.get(GridConfig.brokerIndexes.get(i));
            gcdp =(GridCDProtocol)gNode
                   .getProtocol(GridNode.getGridCDProtocolID(gNode));
            gcdp.localNodesResourceInfo.histogram.print(System.out);
//            System.out.println(gcdp.localNodesResourceInfo.collection.get(0).toString());
        }
         System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        
         
         if(GridConfig.print == GridConfig.SIMPLE)
             return false;
         
         
        for(int i=0;i < Network.size();i++){
         
            try
            {
            
            gNode=(GridNode)Network.get(i);
            
            gp = (GridProtocol)gNode.getProtocol(linkableID);
            
            gdh =(GridDeployHandleProtocol)gNode.getProtocol(pid);
            
            String isBroker = "[Node]";
        
            if (gNode.getIsBrokerState())isBroker="[Broker]";

            String neighbors = "[";

            for(int j=0;j<gp.neighbors.length;j++){

                if(gp.neighbors[j] == null)break;

                if(j>0)neighbors+=",";
                neighbors+=gp.neighbors[j].getIndex();
            }
            neighbors+="]";
            
            
            System.out.println("Node "+gNode.getIndex()+"  "+isBroker+": ");
            System.out.println("Neighbors:\n"+neighbors);
            System.out.println("Resource State:\n"+gNode.resourceState.toString());
        
            System.out.println("Components:");
            if(gdh.components.isEmpty()){
                System.out.println("No current Deployed componernts");
                System.out.println("------------------------------------------------");
                continue;
            }
             String buffer="";
        
            
            for(int j=0;j<gdh.components.size();j++){
            buffer+=gdh.components.get(j).toString()+"\tStarted at cycle "
                    +gdh.componentStartCycle.get(j)+"\n";
            
            
        }
             System.out.println(buffer);
             System.out.println("------------------------------------------------");
            }
            catch(Exception e)
            {continue;}
            
        }
        
        

           
        return false;
    }
    String getNodesSpecifications(){
        
        String s ="";
        for(int i = 0; i < GridConfig.generatedNodesCount.size(); i++){
            s += "Type" + String.valueOf(i+1)
                    +"[OS "+GridFactory.LookupOS(GridConfig.nodesOS.get(i))
                    +", CPU "+String.valueOf(GridConfig.nodesCPU.get(i))
                    +", M "+String.valueOf(GridConfig.nodesMemory.get(i)) +"]"
                    +": "+String.valueOf(GridConfig.generatedNodesCount.get(i))+"\t";    
        }
        
        return s;
        //return "WIN "+String.valueOf(w)+", LINUX "+String.valueOf(l)+", MAC "+String.valueOf(m);
    
    }
    String getComponentsSpecifications(){
        String s ="";
        for(int i = 0; i < GridConfig.generatedComponentsCount.size(); i++){
            s += "Type" + String.valueOf(i+1)
                    +"[OS "+GridFactory.LookupOS(GridConfig.componentsOS.get(i))
                    +", CPU "+String.valueOf(GridConfig.componentsCPU.get(i))
                    +", M "+String.valueOf(GridConfig.componentsMemory.get(i)) +"]"
                    +": "+String.valueOf(GridConfig.generatedComponentsCount.get(i))+"\t";    
        }
        
        return s;
        
        //return "WIN "+String.valueOf(w)+", LINUX "+String.valueOf(l)+", MAC "+String.valueOf(m);
    
    }
    
}
