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
 * @author 2906095
 */
public class GridBrokerInitializer implements Control{

    private static final String PAR_PROT = "protocol";
    
    private final int pid;
    
    public GridBrokerInitializer (String prefix)
    {

        pid = Configuration.getPid(prefix + "." + PAR_PROT);

    }
    
    @Override
    public boolean execute(){
        
        for(int i = 0;i < Network.size();i++){
            
            GridNode gNode = (GridNode) Network.get(i);
            
            if(!gNode.getIsBrokerState()) return false;
            
            GridBrokerProtocol gbp = (GridBrokerProtocol)gNode.getProtocol(pid);
            
            gbp.Initialize();
            
        }
        
        return false;
    }
    
}
