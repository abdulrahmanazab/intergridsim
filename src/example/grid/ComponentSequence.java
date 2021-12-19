/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example.grid;
import java.util.Vector;
import peersim.cdsim.CDState;
import peersim.core.*;
/**
 *
 * @author Azab
 */
public class ComponentSequence {
    public GridNode broker;
    public int totalComponents;//Total number of components to deploy
    public int remainingComponents;//remaining undeployed components in the sequence
    public int componentsPerTime;//No. of deployments per time
    public int frequency;//distance between each two deployments
    int previousDeploymentCycle;//Cycle for the previous deployment
    int lastDeploymentCycle;//Cycle when the last component of this sequence is generated
    
    public ComponentSequence(GridNode broker, int totalComponents, int componentsPerTime, int frequency){
        this.broker = broker;
        this.totalComponents = totalComponents;
        this.remainingComponents = totalComponents;
        this.componentsPerTime  = componentsPerTime;
        this.frequency = frequency;
        previousDeploymentCycle = -10000;//In order to make the first deployment stream in the next cycle and not wait [frequency] cycles
    }
    public boolean deploy(){
        CommonState.r.setSeed(System.currentTimeMillis());
        if(CDState.getCycle() - previousDeploymentCycle < frequency || remainingComponents <= 0)
            return true;
        
        
        GridComponent c;
                
        GridBrokerProtocol gbp = (GridBrokerProtocol)broker.getProtocol(GridNode.getBrokerProtocolID(broker));
        
        int i = 0;
        while(i++ < componentsPerTime){
            c = GridDeployer.getComponent();
            c.remainingDeployTrials = GridDeployer.deployTrails;
            c.maxWaitingTime = GridDeployer.waitingTime;
            c.deployedAt = CDState.getCycle();
            c.ownerBrokerIndex = broker.getIndex();
            c.lastHostBrokerIndex = broker.getIndex();
            c.TotalSimulationCycles = CommonState.r.nextInt(c.TotalSimulationCycles);
            if(c.TotalSimulationCycles < GridConfig.componentsMinCycles)
                c.TotalSimulationCycles = GridConfig.componentsMinCycles;
            
            gbp.getComponentToDeploy(broker, broker,c,false);
            
            if(--remainingComponents <= 0){
                lastDeploymentCycle = CDState.getCycle();
                return true;
            }
        }
        
        previousDeploymentCycle = CDState.getCycle();
        return true;
    
    }
    
    
}
