/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package example.grid;

import peersim.config.*;
import peersim.core.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;
import peersim.cdsim.CDState;
/**
 *
 * @author 2906095
 */
public class GridPrintObserver implements Control{
    
    //private static final String PAR_PROT = "protocol";
    private static final String PAR_FILE = "outfile";
    private static final String PAR_FILE_TEST = "outfile.test";
    private static final String PAR_FILE_ACC = "outfile.acc";
    private static final String PAR_NODES_DIRECTORY = "nodes.outdirectory";
    private static final String PAR_NODES_CYCLESTEP = "nodes.cyclestep";
    private static final String PAR_DATA = "datatype";
    //private final int pid;
    private final String OutFilePath, OutFileTestPath;
    private final String ACCumulatedOutFilePath;
    private final String nodeSpecificationDirectoryPath;
    private final int nodeSpecificationCycleStep;
    private final String printDataType;
    private final String[] printDataTypeArray = 
     {"brokerResourceKnowledge","historyDeviation","components","brokerQueue","histogram"};
     
     int frequencyCounter=0;
    
    File f, f2, fTest;
    PrintWriter pw, pw2;
    FileOutputStream fos, fos2, fosTest; 
    PrintStream ps, ps2, psTest;
    
    GridNode gNode,gBrokerNode;
    ArrayList<Integer> list;
    //Vector<Integer> indexes;
    
    static boolean acc_printed = false;
    static boolean nodes_printed = false;
    
    long time = 0, currentTime = 0;
    
    
    static Vector<RoutingTable> rtv; static Vector<String> rtvs;
    
    public GridPrintObserver(String prefix){
        
        //pid = Configuration.getPid(prefix + "." + PAR_PROT);
        OutFilePath = Configuration.getString(prefix + "." + PAR_FILE);
        OutFileTestPath = Configuration.getString(prefix + "." + PAR_FILE_TEST);
        ACCumulatedOutFilePath = Configuration.getString(prefix + "." + PAR_FILE_ACC);
        nodeSpecificationDirectoryPath = Configuration.getString(prefix + "." + PAR_NODES_DIRECTORY);
        nodeSpecificationCycleStep = Configuration.getInt(prefix + "." + PAR_NODES_CYCLESTEP);
        printDataType = Configuration.getString(prefix + "." + PAR_DATA);
    }
    
    public boolean execute(){
        
        //Define the connection to the output file        
        
       
       
       try{
            f = new File(OutFilePath);
            fTest = new File(OutFileTestPath);
            //fTest.delete();
            //f.createNewFile();
            //pw = new PrintWriter(f);
            fos = new FileOutputStream(f,true);
            fosTest = new FileOutputStream(fTest,true);
            
            ps = new PrintStream(fos);
            psTest = new PrintStream(fosTest);
       }
       catch(Exception e){
           System.err.println(e.getMessage());
       }
       
       if(CDState.getCycle() >= 2){
           //for(int i:GridConfig.brokerIndexes)
           //    printRoutingTable(i);
           //psTest.close();
//           GridBrokerProtocol gbp;
//           rtv = new Vector<RoutingTable>();
//           rtvs = new Vector<String>();
//           for(int i:GridConfig.brokerIndexes){
//               gBrokerNode = (GridNode) Network.get(i);
//               gbp = (GridBrokerProtocol)gBrokerNode.getProtocol(GridNode.getBrokerProtocolID(gBrokerNode));
//               rtv.add(gbp.routingTable);
//               rtvs.add(gbp.routingTable.brokerIndex+">"+gbp.routingTable.table.size());
//           }
//           int xxx=0;           
       }
       if(CDState.getCycle() == CDState.getEndTime()-1 || GridConfig.endSimulation){
           if(!acc_printed){
               acc_printed = true;
               printComponentsAccumulative();
           }
           try{
               ps.close() ;ps2.close();
           }
           catch(Exception e){}
           
           return false;
       }
       
        //Print the 'current' node specifications, e.g. current free CPUs, of all nodes
       // every nodeSpecificationCycleStep:
       if(CDState.getCycle() % nodeSpecificationCycleStep == 0){
           
            printNodes(GridConfig.brokerIndexes);
           
           return false;
       }
       
       if(printDataType.equalsIgnoreCase(printDataTypeArray[0]))
           printBrokerResourceKnowledge();
       else if(printDataType.equalsIgnoreCase(printDataTypeArray[1]))
       printBrokerStandardDiviationForResourceDataStorageCyclesFromCurrentCycle(true);
       else if(printDataType.equalsIgnoreCase(printDataTypeArray[2]))
       printComponents();
       else if(printDataType.equalsIgnoreCase(printDataTypeArray[3]))
           printBrokersQueueCapacity(GridConfig.deployedAtBrokers);
       else if(printDataType.equalsIgnoreCase(printDataTypeArray[4]))
           printBrokerHistograms();
       
       
       
//       if(GridConfig.frequency==0 && CDState.getCycle()>0&&(GridConfig.Deployments+GridConfig.failedDeployments)==GridConfig.componentsPerDeployment)
//          return true;
       
       return false;
    }
    
    void printBrokerHistograms(){
       gBrokerNode =(GridNode) Network.get(GridConfig.brokerIndexes.get(1));
       GridCDProtocol gcdp =(GridCDProtocol)gBrokerNode
               .getProtocol(GridNode.getGridCDProtocolID(gBrokerNode));
       gcdp.localNodesResourceInfo.histogram.print(ps);
     }

    void printBrokerResourceKnowledge(){
        
        gBrokerNode =(GridNode) Network.get(GridConfig.brokerIndexes.get(0));
       
       
       GridCDProtocol gcdp =(GridCDProtocol)gBrokerNode
               .getProtocol(GridNode.getGridCDProtocolID(gBrokerNode));
        
        ps.println(CDState.getCycle()+"\t"+gcdp.globalResourceInfoSize());
        
    }
    
    void printRoutingTable(int index){
        gBrokerNode = (GridNode) Network.get(index);
        GridBrokerProtocol gbp = (GridBrokerProtocol)gBrokerNode.getProtocol(GridNode.getBrokerProtocolID(gBrokerNode));
        RoutingTable rt = gbp.routingTable;
        String rts = String.valueOf(rt.brokerIndex)+"\r";
        String rtes;
        for(RoutingTableElement rte:rt.table){
            rtes = "\t"+ String.valueOf(rte.brokerIndex)+" > ";
            for(int hop:rte.hops)
                rtes+=String.valueOf(hop)+",";
            rts+=rtes+"\r";
        }
        psTest.println("Routing Table of Broker "+String.valueOf(index)+":\r"
                + "----------------------------------\r"
                + rts
                +"===============================================================================\r"
                +"===============================================================================\r");
        
        
        
        
   }
    void printBrokersQueueCapacity(Vector<Integer> indexes){
        String print = "";
        float mean = 0, mean2 = 0;
        int median;
        double std;
        list = new ArrayList<Integer>();
        GridBrokerProtocol gbp;
        
               
        for(int i = 0; i < indexes.size(); i++){
            gBrokerNode =(GridNode) Network.get(indexes.get(i));
            gbp =(GridBrokerProtocol)gBrokerNode
               .getProtocol(GridNode.getBrokerProtocolID(gBrokerNode));
            list.add(gbp.getCurrentQueueSize());
            mean += gbp.getCurrentQueueSize();
            mean2 += gbp.getCurrentQueueSize() * gbp.getCurrentQueueSize();
            print+=String.valueOf(gbp.getCurrentQueueSize()) +"\t";
        }
        median = getMedian(list);
        mean = mean / GridConfig.brokerIndexes.size();
        mean2 = mean2 / GridConfig.brokerIndexes.size();
        std = Math.sqrt(mean2 - mean * mean);
        print += "\t" + String.valueOf(median) + "\t" + String.valueOf(mean) +"\t"+ String.valueOf(std);
        ps.println(print);
    
    }
    
    double printBrokerStandardDiviationForResourceDataStorageCyclesFromCurrentCycle(boolean print){
        
        gBrokerNode =(GridNode) Network.get(GridConfig.brokerIndexes.get(0));
       
       
       GridCDProtocol gcdp =(GridCDProtocol)gBrokerNode
               .getProtocol(GridNode.getGridCDProtocolID(gBrokerNode));
       
       double Mean=0,Mean2=0,sd;
       
       for(int i = 0; i < gcdp.globalNodesResourceInfo.size();i++){
           
//////           try
//////           {
//////           if (Network.getWithID(gbp.NodesResourceInfo.get(i).NodeIndex
//////                   , gbp.NodesResourceInfo.get(i).NodeID).getFailState()!= Node.OK ){
//////                gbp.NodesResourceInfo.removeElementAt(i);
//////               continue;
//////           }
//////           }catch(Exception e){continue;}
           
//           Mean+= gcdp.globalNodesResourceInfo.get(i).cycle;
//           Mean2+=Math.pow(gcdp.globalNodesResourceInfo.get(i).cycle, 2) ;
           
           Mean+= Math.pow(CDState.getCycle() - gcdp.globalNodesResourceInfo.get(i).cycle,2);
           
           
       }
       
       Mean/=gcdp.globalNodesResourceInfo.size();
//       Mean2/=gcdp.globalNodesResourceInfo.size();
//       
//       sd=Math.pow(Mean2-Math.pow(Mean, 2), 0.5);
       sd = Math.pow(Mean, 0.5);
       if(print)
           ps.println(CDState.getCycle()+"\t"+sd);
       return sd;
        
    }
    
    void printComponents(){//Waiting components
        
////////        if(frequencyCounter>0)
////////            frequencyCounter--;
////////        else{
////////            frequencyCounter=GridConfig.frequency;
////////            GridConfig.failedDeployments=0;
////////        }
        
       if(CDState.getCycle() <= 1)
           ps.println("Cycle\t#Wait_cmp\t#fail_cmp\t#full-pools\t#dep/cycle\t#conn/cycle\tcycle-time(ms)");
       
       time = System.currentTimeMillis() - currentTime;
       currentTime = System.currentTimeMillis(); 
       ps.println(CDState.getCycle()+"\t"+GridConfig.waitingComponents+"\t"
              /* +GridConfig.Deployments+"\t"*/+GridConfig.failedDeployments
               +"\t"+GridConfig.saturatedPools
               +"\t"+GridConfig.deployedComponentsPerCycle
               +"\t"+GridConfig.connectionsBetweenBrokersPerCycle//GridConfig.brokerIndexes.size()
               +"\t"+printBrokerStandardDiviationForResourceDataStorageCyclesFromCurrentCycle(false)
               +"\t"+time
               );
               //+"\t"+GridConfig.ExchangedComponents);
               GridConfig.deployedComponentsPerCycle = 0;
               GridConfig.connectionsBetweenBrokersPerCycle = 0;
    }
    
    void printNodes(Vector<Integer> indexes){
        
        GridCDProtocol gcdp; 
        
        try{
            f2 = new File(nodeSpecificationDirectoryPath+"\\"+String.valueOf(CDState.getCycle())+".txt");
            //f.delete();
            //f.createNewFile();
            //pw = new PrintWriter(f);
            fos2 = new FileOutputStream(f2,true);

            ps2 = new PrintStream(fos2);
        }catch(Exception e){
           System.err.println(e.getMessage());
        }         
        //ps2.println("broker\tnode\tCPU\tmemory\tOS");
        String dict_broker,dict_node;
        
        for(int index = 0; index < indexes.size(); index++){
            gBrokerNode =(GridNode) Network.get(indexes.get(index));
            gcdp = (GridCDProtocol)gBrokerNode.getProtocol(GridNode.getGridCDProtocolID(gBrokerNode));
            dict_broker = "{'broker':"+index+",'nodes':[";
            for(GridNodeResourceState nodeInfo : gcdp.localNodesResourceInfo.collection){
                dict_node = "{'node':"+nodeInfo.NodeIndex+",'cpu':"+nodeInfo.AvailabbleCPU
                        +",'memory':"+nodeInfo.availableMemory+",'os':"+nodeInfo.OS+"}";
                dict_broker+=dict_node+',';
                //ps2.println(index+"\t"+nodeInfo.NodeIndex+"\t"+nodeInfo.AvailabbleCPU
                //        +"\t"+nodeInfo.availableMemory+"\t"+nodeInfo.OS);
            }
            dict_broker = dict_broker.substring(0, dict_broker.length()-1)+"]}";
            ps2.println(dict_broker);
            
        }
        ps2.close();
    }
    void printComponentsAccumulative(){
       try{
            f2 = new File(ACCumulatedOutFilePath);
            //f.delete();
            //f.createNewFile();
            //pw = new PrintWriter(f);
            fos2 = new FileOutputStream(f2,true);

            ps2 = new PrintStream(fos2);
       }
       catch(Exception e){
           System.err.println(e.getMessage());
       }
        GridBrokerProtocol gbp;
        Vector<Integer> c_NetworkDist = new Vector<Integer>();
        Vector<Integer> deploy_trials = new Vector<Integer>();
        Vector<Integer> totDeploymentTime = new Vector<Integer>();
        Vector<Float> c_AvgWaitingTime = new Vector<Float>();
        
        int count,sum;
        for(int i = 0; i < GridConfig.brokerIndexes.size(); i++){
            count = 0;
            for(GridComponent c : GridConfig.deployedComponents){
                if(c.networkDistance == i) count ++;
            }
            c_NetworkDist.add(count);
        }
        
        for(int i = 0; i < GridConfig.brokerIndexes.size(); i++){
           count = 0;
           for(GridComponent c : GridConfig.deployedComponents){
               if(GridDeployer.deployTrails - c.remainingDeployTrials == i) count ++;
           }
           deploy_trials.add(count);
           
           
        }
        
        
        
        for(int i = 0; i < GridConfig.brokerIndexes.size(); i++){
            count = 0; sum = 0;
            for(GridComponent c : GridConfig.deployedComponents){
                if(c.ownerBrokerIndex == GridConfig.brokerIndexes.get(i)){
                    sum += c.totalWaitingTime;
                    count++;
                }
            }
            if(count > 0)
                c_AvgWaitingTime.add((float)(sum/count));
            else
                c_AvgWaitingTime.add((float)0);
            
            gbp = (GridBrokerProtocol)Network.get(GridConfig.brokerIndexes.get(i))
                    .getProtocol(GridNode.getBrokerProtocolID(Network.get(GridConfig.brokerIndexes.get(i))));
            
            totDeploymentTime.add(gbp.totalDeploymentTimeOfAllOwnComponents);
            
        }
        
        int length = c_NetworkDist.size();
        if(c_AvgWaitingTime.size() > length)length = c_AvgWaitingTime.size();
        
        ps2.println("br_idx\t#N_dist\t#deploy\tAvg_wt\tTot_dep_t");
        
        int dist,trails,depTime; float wt;
        for(int i = 0; i < length; i++){
            if(c_NetworkDist.size() > i)
                dist = c_NetworkDist.get(i);
            else
                dist = 0;
            
            if(deploy_trials.size() > i)
                trails = deploy_trials.get(i);
            else
                trails = 0;
            
            if(c_AvgWaitingTime.size() > i)
                wt = c_AvgWaitingTime.get(i);
            else
                wt = 0;
            
            if(totDeploymentTime.size() > i)
                depTime = totDeploymentTime.get(i);
            else
                depTime = 0;
            
            ps2.println(i + "\t" + dist + "\t" + trails + "\t" + wt + "\t" + depTime);
        }
        
        acc_printed = true;
        
    }
    int getMedian(ArrayList<Integer> l){
       Collections.sort(l);
       if(l.size() % 2 == 0)
            return (l.get(l.size() / 2 - 1) + l.get(l.size() / 2))/2;
        else
            return l.get((l.size() + 1) / 2);
        
       
    }
    
    
}
