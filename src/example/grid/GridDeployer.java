/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package example.grid;
import peersim.Simulator;
import peersim.cdsim.CDState;
import peersim.core.*;
import peersim.config.*;
import peersim.core.CommonState;
import peersim.core.Network;
import java.util.Vector;
/**
 *
 * @author 2906095
 */
public class GridDeployer implements Control{

    private static final String PAR_PROT = "protocol";
    private static final String PAR_METHOD = "method";//deployment method. "random": deploy on any node in the network. "broker": deploy only on brokers
    private static final String PAR_FRQUENCY = "frequency";//Time between each two deployments
    private static final String PAR_COMPONENTS_PER_TIME = "componentsPerTime";
    private static final String PAR_TOTAL_COMPONENTS = "totalComponents";
    private static final String PAR_WAITING = "waitingTime";
    private static final String PAR_DEPLOY_TRIALS = "deployTrails";
    private static final String PAR_DEPLOY_MODE = "deployMode";
    
    //If the deployment will be on random nodes
    private static final String[] PAR_METHOD_ARRAY = {"random","broker"};
    private static final String[] PAR_DEPLOY_MODE_ARRAY = {"center","loadbalancing", "distributed","sequences"};
    
    private static final String PAR_SEQ_NUM = "sequences.num";//Total number of component sequences
    //private static final String PAR_SEQ_COMP = "sequences.components";//Total number of components in each sequence
    //private static final String PAR_SEQ_FREQ = "sequences.freq";//Frequency of component submission in each sequence
    private final int seq_num;
    private int seq_comp;
    
    private final int pid;
    private final String methodString;
    private final String deployNodeString;
    private final int frequency; // No. of deployed components in each cycle
    private final int componentsPerDeployment;//No. of components per deployment
    private final int totalComponents;
    public static int waitingTime;//max no. of cycles a component can wait in queues before dying
    public static int deployTrails;//max no. of trials for deploying the component
    public final int RANDOM = 0, BROKER = 1;
    public final int CENTER = 0, LOAD_BALANCING = 1, DISTRIBUTED = 2, SEQUENCES = 3;
    int method, deployMode;
    
    public int frequencyCounter = 0;
    static int brokerIndex=0;
    int brokerCounter = 0;
    
    public static Vector<ComponentSequence> seqArray;
    public static Vector<Integer> componentsCount; // Is associated with the types of components and each element contains the number of components which requirements are of that type 
       
    static int endsimulationTimer;//
    
    public GridDeployer(String prefix){
        
        pid = Configuration.getPid(prefix + "." + PAR_PROT);
        frequency = Configuration.getInt(prefix + "." +PAR_FRQUENCY, 1);
        GridConfig.frequency = frequency;
        componentsPerDeployment = Configuration.getInt(prefix + "." +PAR_COMPONENTS_PER_TIME, 1);
        totalComponents = Configuration.getInt(prefix + "." +PAR_TOTAL_COMPONENTS, 100000);
        GridConfig.componentsPerDeployment = componentsPerDeployment;
        methodString = Configuration.getString(prefix + "." + PAR_METHOD,PAR_METHOD_ARRAY[0]);
        waitingTime = Configuration.getInt(prefix + "." + PAR_WAITING,(int)CDState.getEndTime());
        deployTrails = Configuration.getInt(prefix + "." + PAR_DEPLOY_TRIALS,(int)CDState.getEndTime());
        deployNodeString = Configuration.getString(prefix + "."+ PAR_DEPLOY_MODE,PAR_DEPLOY_MODE_ARRAY[0]);
        
        seq_num = Configuration.getInt(prefix + "." +PAR_SEQ_NUM, 1);
        //seq_comp = Configuration.getInt(prefix + "." +PAR_SEQ_COMP, 1000);
        //seq_freq = Configuration.getInt(prefix + "." +PAR_SEQ_FREQ, 1);
        
        GridConfig.deployedAtBrokers = new Vector<Integer>();
        GridConfig.deployedComponents = new Vector<GridComponent>();
        seqArray = new Vector<ComponentSequence>();
        
        if(methodString.equals(PAR_METHOD_ARRAY[0]))
            method = RANDOM;
        else if(methodString.equals(PAR_METHOD_ARRAY[1]))
            method = BROKER;
        else
            throw new IllegalArgumentException("invalid deploy method");
        
        if(deployNodeString.equals(PAR_DEPLOY_MODE_ARRAY[0]))
            deployMode = CENTER;
        else if(deployNodeString.equals(PAR_DEPLOY_MODE_ARRAY[1]))
            deployMode = LOAD_BALANCING;
        else if(deployNodeString.equals(PAR_DEPLOY_MODE_ARRAY[2]))
            deployMode = DISTRIBUTED;
        else if(deployNodeString.equals(PAR_DEPLOY_MODE_ARRAY[3]))
            deployMode = SEQUENCES;
        else
            throw new IllegalArgumentException("invalid deploy method");
        
        GridConfig.generatedComponentsCount = new Vector<Integer>();
        for(int i=0;i<GridConfig.components;i++)
            GridConfig.generatedComponentsCount.add(0);
        
    }
    
       
    public boolean execute(){
        
       if((GridConfig.Deployments + GridConfig.failedDeployments) >= totalComponents && !GridConfig.endSimulation){
           GridConfig.endSimulation = true;
           endsimulationTimer = 2;
       }
       if(GridConfig.endSimulation)
           if( --endsimulationTimer < 0)
            System.exit(0); 
       
       if(deployMode == SEQUENCES)
           return deploySequences();
       
       brokerCounter = 0;
        
        
       if(frequencyCounter >1 ){//Wait for 'frequency' between each two deployment shots
           frequencyCounter--;
           return false;
       }
       //comes here if frequencyCounter ==0
       
       frequencyCounter = frequency;//Initialize 
       
       if(deployMode == DISTRIBUTED){
               brokerIndex = getRandomBrokerIndex();
               
       }
       
       if(GridConfig.generatedComponents >= totalComponents)
           return false;
       
       GridConfig.deployedAtBrokers.add(brokerIndex);       
       
       for(int i = 0; i < componentsPerDeployment ; i++){
           
           if(GridConfig.generatedComponents >= totalComponents)
           return false;
           
            if(method == RANDOM)DeployToRandomWorker();// 
            else{

                boolean succeed = false;

                    for(int j=0;j<10;j++){
                        succeed = DeployToBroker();
                        if(succeed){
                            break;   
                        }

                    }

                //if(!succeed)GridConfig.failedDeployments++;

            }
           
       } 
       
//////      if((deployNode == DISTRIBUTED) && ++brokerIndex >= GridConfig.brokerIndexes.size())
//////          brokerIndex = 0;        
       
       
        return false;
    }
    
    
    boolean DeployToBroker(){
        
        GridComponent c = getComponent();
        
        c.remainingDeployTrials = deployTrails;
        c.maxWaitingTime = waitingTime;
        c.deployedAt = CDState.getCycle();
        
        int i = 0;
        
        
        if(deployMode == LOAD_BALANCING){//Divide component collection among brokers in each deployment shot
            if(++brokerCounter >= GridConfig.brokerIndexes.size())brokerCounter = 0;
            i = GridConfig.brokerIndexes.get(brokerCounter);
        }else if(deployMode == DISTRIBUTED)//Deploy a collection of components to a different broker each time
            i = GridConfig.brokerIndexes.get(brokerIndex);
            
        GridNode gNode = (GridNode)Network.get(i);
        
        GridBrokerProtocol gbp = (GridBrokerProtocol)gNode.getProtocol(GridNode.getBrokerProtocolID(gNode));
        
        c.ownerBrokerIndex = gNode.getIndex();
        c.lastHostBrokerIndex = gNode.getIndex();
        
        if(!gbp.getComponentToDeploy(gNode, gNode,c,false))return false;
        //GridConfig.waitingComponents++;
                
        if(GridConfig.print == GridConfig.SIMPLE)
             return true;
        else
            System.out.println("Component: "+c.id+" of type: "+ c.typeID +" Deployed at Broker: "+i);
              
        
        return true;
    }
    
    boolean deploySequences(){
        GridNode broker;
        seq_comp = totalComponents/seq_num;
        if(seqArray.isEmpty()){//First cycle --> initialize the sequences list
            int freq;
            for(int i = 0; i < seq_num; i++){
                if(i == seq_num-1)seq_comp += totalComponents%seq_num;//Add remaninig components to the last sequence
                broker = (GridNode)Network.get(getSequenceBrokerIndex());
                CommonState.r.setSeed(System.currentTimeMillis());
                freq = CommonState.r.nextInt(frequency);
                if(freq < frequency/2)freq = frequency/2;
                seqArray.add(new ComponentSequence(broker, seq_comp, componentsPerDeployment, freq));
                GridConfig.deployedAtBrokers.add(broker.getIndex());
            }
            return false;
        }else{
            for(ComponentSequence seq : seqArray){
                seq.deploy();
            }
        }
        
        return false;
    }
    
    void DeployToRandomWorker(){
        
                
        GridComponent c = getComponent();
        
        c.remainingDeployTrials =waitingTime;
        
        int i = getRandomNodeIndex();
        
        c.ownerBrokerIndex = ((GridNode)Network.get(i)).getBroker().getIndex();
        c.lastHostBrokerIndex = ((GridNode)Network.get(i)).getBroker().getIndex();
        
        GridDeployHandleProtocol gdh = (GridDeployHandleProtocol)Network.get(i).getProtocol(pid);
        
        boolean deployed = gdh.DeployComponent(c,Network.get(i),pid);
        
             
        
        if(deployed){
            
            GridConfig.Deployments++;
            if(GridConfig.print == GridConfig.DETAILED)
                System.out.println("Component: "+c.id+" of type: "+ c.typeID +" Deployed at Node: "+i);
            
        }
        else{
            GridConfig.failedDeployments++;
            if(GridConfig.print == GridConfig.DETAILED)
                System.out.println("Component: "+c.id+" of type: "+ c.typeID +" Failed to Deploy at Node: "+i);
            
        
        }
    }
    int getSequenceBrokerIndex(){
    int index = getRandomBrokerIndex();
    if(!GridConfig.deployedAtBrokers.contains(index))
        return index;
    else
        return getSequenceBrokerIndex();
    }
    int getRandomBrokerIndex(){
        Node n;
        int index = CommonState.r.nextInt(GridConfig.brokerIndexes.size());
        n = Network.get(index);
        if(n.isUp())
            return index;
        else
            return getRandomBrokerIndex();
    }
    int getRandomNodeIndex(){
        Node n;
        int index = CommonState.r.nextInt(Network.size());
        n = Network.get(index);
        if(n.isUp())
            return index;
        else
            return getRandomNodeIndex();
    }
    static GridComponent getComponent(){
        
        GridComponent c = null;
        
        if(componentsCount.size() > 0){
            int componentType = 1;
            for(int j = 0; j < componentsCount.size(); j++){
              if(componentsCount.get(j) > 0){
                 componentsCount.set(j,componentsCount.get(j) - 1);
                 c = GridFactory.getComponentByType(componentType);
                 GridConfig.generatedComponentsCount.set(componentType - 1,
                         GridConfig.generatedComponentsCount.get(componentType - 1) + 1);
                 break;
             }else{
                      componentType ++;
                      continue;
              } 
            }
        }else{
            c = GridFactory.getRandomComponent();
            GridConfig.generatedComponentsCount.set(0,GridConfig.generatedComponentsCount.get(0) + 1);
        }
        
        GridConfig.generatedComponents++;
        
        
        return c;
    }
}

    
    
    
    