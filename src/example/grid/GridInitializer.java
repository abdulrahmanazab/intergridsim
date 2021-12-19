/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package example.grid;
import peersim.core.*;
import peersim.config.*;
import java.util.Vector;
/**
 *
 * @author Azab
 */
public class GridInitializer implements Control{
    
    
    private static final String PAR_PROT = "protocol";
    
    private final int pid;
    
    static Vector<Integer> poolCount;
    
    public GridInitializer (String prefix)
    {

        pid = Configuration.getPid(prefix + "." + PAR_PROT);
//        nrOfDomains = Configuration.getInt(NR_DOMAIN_NODES);
    }
    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------
    /**
     * Initializes 
     */
    public boolean execute ()
    {
        GridHistogram.ReadConfig();
        
        GridComponent.initializeID();
        
        int linkableID;
        
                
        // For setting resource specifications for each pool in case of using "grid.nodes.xx.poolCount"
        if(GridConfig.poolCount.size() > 0){
            GridNode broker;
            int NodeType = 1;//Type of node specifications, e.g. type 1 and type 2 (1 based not 0 based)
            
            for(int i : GridConfig.brokerIndexes){
                   broker = (GridNode)Network.get(i);
                   for(int j = 0; j < poolCount.size(); j++){
                       if(poolCount.get(j) > 0){
                           poolCount.set(j,poolCount.get(j) - 1);
                           broker.poolNodesType = j + 1;
                           break;
                       }else{
                           //NodeType ++;
                           continue;
                       } 
                   }
            }
        }
        
        GridNodeResourceState rs = null;
        
        for(int i = 0; i< Network.size();i++){
            
            
            linkableID = FastConfig.getLinkable(pid);
            
            GridNode node = (GridNode)Network.get(i);
            
                       
            //node.InitializeNode(GridConfig.nodesMemory, GridConfig.nodesCPU, GridConfig.nodesOS, node.getIsBrokerState());
            try{
            if(GridConfig.poolCount.size() > 0){//Node specifications are specifically defined
                rs = GridFactory.getGridNodeSpecificationByType(node.getBroker().poolNodesType);
                GridConfig.generatedNodesCount.set(node.getBroker().poolNodesType - 1,
                    GridConfig.generatedNodesCount.get(node.getBroker().poolNodesType - 1) + 1);
            }else{
                rs = GridFactory.getRandomGridNodeSpecification();
                GridConfig.generatedNodesCount.set(0,GridConfig.generatedNodesCount.get(0) + 1);
            }
            }catch(Exception e){
                System.err.println(e.getMessage()+"###############"+String.valueOf(node.getBroker().poolNodesType));
                System.exit(0);
            }
            
            node.InitializeNode(rs, node.getIsBrokerState());
            
            GridProtocol gp = (GridProtocol) node.getProtocol(linkableID);
             GridDeployHandleProtocol gdh = (GridDeployHandleProtocol)node.getProtocol(pid);
              
             gdh.Initialize(); 
             
            if(node.getIsBrokerState()){
                node.initializeNeighborBrokers();
                node.InitializeHistogram();
                
            }
        }
        return false;
    }
}
