/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package example.grid;
import peersim.config.Configuration;
import peersim.core.Network;
import java.util.Vector;
/**
 *
 * @author 2906095
 */
public class GridConfig {
    
    public static int secondsPerSimulationCycle = 0;
    public static int cycles = 0;
    
    static boolean endSimulation = false;//Flag to end simulation after specific number of cycles, used in the 'GridDeployer'
        
    public static int components = 0;
    public static int componentsMinCycles = 0;
    public static Vector<Integer> componentsCPU = new Vector<Integer>();
    public static Vector<Integer> componentsMemory = new Vector<Integer>();
    public static Vector<Integer> componentsOS = new Vector<Integer>();
    public static Vector<Integer> componentsCycles = new Vector<Integer>();
    public static Vector<Integer> componentsCount = new Vector<Integer>(); // Is associated with the types of components and each element contains the number of components which requirements are of that type
    
    public static int nodes = 0;
    public static Vector<Integer> nodesCPU = new Vector<Integer>();
    public static Vector<Integer> nodesMemory = new Vector<Integer>();
    public static Vector<Integer> nodesOS = new Vector<Integer>();
    public static Vector<Integer> poolCount = new Vector<Integer>();// Is associated with the types of nodes and each element contains the number of pools which node specifications are of that type
    
    public static Vector<Integer> generatedNodesCount = new Vector<Integer>();//Only for statistcs and printing 
    public static Vector<Integer> generatedComponentsCount = new Vector<Integer>();//Only for statistcs and printing
    
    
    private static String printString;
    private static final String[] printChoices = {"simple","detailed"};
    public static int print;
    public static final int SIMPLE =0,DETAILED=1;
    public static int brokers = 0;  
    public static int dataAge = 0;  
    
    protected static final String SEC_SIM_CYCLE = "simulation.cycleCapacity";
    protected static final String PAR_CYCLES = "simulation.cycles";
    
    protected static final String COMPONENT = "grid.components"; 
    protected static final String NUM_COMPONENT = "grid.components.no"; //No. of Types of components (variaty "not number")
    protected static final String COMPONENT_MIN_CYCLES = "grid.components.mincycles";//Min. No. of cycle which any component under any condition can run
    protected static final String TYPE_COMPONENT = "grid.components.type"; //in case of >1 types, how components are going to be generated
    protected static final String COMPONENT_CPU = "grid.components.cpu"; // MAX No. CPU cores per components (will be used as one value in case of one component type)
    protected static final String COMPONENT_MEMORY = "grid.components.memory";// MAX Memory Size GBs per components (will be used as one value in case of one component type)
    protected static final String COMPONENT_OS = "grid.components.os";// MAX OS index value per components (will be used as one value in case of one component type)
    protected static final String COMPONENT_CYCLES = "grid.components.cycles";// No. of cycles the component will run
    
    protected static final String NODE = "grid.nodes";
    protected static final String NUM_NODE = "grid.nodes.no";//No. of Types of nodes (variaty "not number")
    protected static final String TYPE_NODE = "grid.nodes.type"; //in case of >1 types, how nodes specifications are going to be generated
    protected static final String NODES_CPU = "grid.nodes.cpu";// MAX No. CPU cores per Node (will be used as one value in case of one component type)
    protected static final String NODES_MEMORY = "grid.nodes.memory";// MAX Memory Size GBs per Node (will be used as one value in case of one component type)
    protected static final String NODES_OS = "grid.nodes.os";// MAX OS index value per Node (will be used as one value in case of one component type)
    protected static final String PAR_PRINT = "grid.print";
    protected static final String PAR_BROKERS = "grid.brokers";
    protected static final String PAR_AGE = "grid.dataAge";
    
    private static final String[] COMPONENT_NODE_TYPE = {"random","specific"};
    
    public static boolean isSpecificNodeType = false, isSpecificComponentType = false; // random [false] or specific [true]. used only in case of "grid.components" and/or "grid.nodes" > 1
    
    public static Vector<Long> brokerIDs;
    public static Vector<Integer> brokerIndexes;
    
    public static boolean failuresEnabled = false;
    public static Vector<Integer> failedBrokers;
    
    //public static boolean onlyExchangeHistogram = false;// True in case of Fuzzy or FuzzyRT policies where only the histogram is used in comparison or in case of condorP2P where #cpus/queue size is used
        
    /**
     * Stores the Indexes of brokers where components were deployed.
     * Used for "loadbalancing" and "distributed" deployment methods
     * Updated by a <b>GridDeployerProtocol</b> object
     */
    public static Vector<Integer> deployedAtBrokers;//Brokers at which components are deployed by the 'GridDeployer' object
    public static Vector<GridComponent> deployedComponents;//All components which are successfully deployed to nodes
    
    
    //Performance parameters
    public static int failedDeployments = 0;
    /**
     * components that completely failed and killed
     */
    public static int failedComponents = 0;
    public static int failedDeploymentsRate = 0;
    public static int generatedComponents = 0;
    public static int Deployments = 0;
    public static int executedComponents = 0; //Components which have completed its execution time
    public static int waitingComponents = 0;
    public static int ExchangedComponents = 0;
    public static int ComponentsExchangingsWithRandomNeighbors = 0;
    public static int FailedComponentExchangings = 0;
    public static int frequency = 0;
    public static int componentsPerDeployment = 0;
    public static int deployedComponentsPerCycle = 0;//cleared every cycle in the "GridPrintObserver" object
    public static int connectionsBetweenBrokersPerCycle = 0;//cleared every cycle in the "GridPrintObserver" object
    public static int maxComponentWaitingTime = 0;
    
    public static int saturatedPools = 0;//No. of fully saturated pools (All Nodes are full both memory and cpu)
    
    
    public static void Initialize(){
        
        System.err.println("************************************************");
        System.err.println("Application is >>>>> Grid");
        System.err.println("************************************************");
       
        secondsPerSimulationCycle = Configuration.getInt(SEC_SIM_CYCLE, 60);
        cycles = Configuration.getInt(PAR_CYCLES);
        
        dataAge = Configuration.getInt(PAR_AGE, 20);
        //----------------------------------------COMPONENTS--------------------------------------------
        components = Configuration.getInt(NUM_COMPONENT, 4);
        componentsMinCycles = Configuration.getInt(COMPONENT_MIN_CYCLES, cycles);
        if(components == 1){//One type components
            componentsCPU.add(Configuration.getInt(COMPONENT_CPU, 1));
            componentsMemory.add(Configuration.getInt(COMPONENT_MEMORY, 1));
            componentsOS.add(GridFactory.LookupOS(Configuration.getString(COMPONENT_OS,"WIN")));
            componentsCycles.add(Configuration.getInt(COMPONENT_CYCLES, cycles));
            
            generatedComponentsCount.add(0);//Only for statistcs and printing (will contain only one element in this case since there are no specific types)
        }else{//multi type components
            try{//If attribute "grid.components.type" doesn't exist then assume random generation
                if(Configuration.getString(TYPE_COMPONENT, COMPONENT_NODE_TYPE[0]).equalsIgnoreCase(COMPONENT_NODE_TYPE[1]))
                  isSpecificComponentType = true;  
            }catch(Exception ex){}
            
            if(!isSpecificComponentType){//Specifications of component types will be generated randomly
                componentsCPU.add(Configuration.getInt(COMPONENT_CPU, components));
                componentsMemory.add(Configuration.getInt(COMPONENT_MEMORY, 2 * components));
                componentsOS.add(GridFactory.LookupOS(Configuration.getString(COMPONENT_OS,"MAC")));
                componentsCycles.add(Configuration.getInt(COMPONENT_CYCLES, cycles));
                
                generatedComponentsCount.add(0);//Only for statistcs and printing (will contain only one element in this case since there are no specific types)
            }else{//Specifications of component types will be specified for each type
                for(int i = 1; i <= components; i++){
                    componentsCPU.add(Configuration.getInt(COMPONENT+"."+String.valueOf(i) +".cpu", 1));    
                    componentsMemory.add(Configuration.getInt(COMPONENT+"."+String.valueOf(i) +".memory", 2));
                    componentsOS.add(GridFactory.LookupOS(Configuration.getString(COMPONENT+"."+String.valueOf(i) +".os","WIN")));
                    componentsCycles.add(Configuration.getInt(COMPONENT+"."+String.valueOf(i) +".cycles", cycles));
                    componentsCount.add(Configuration.getInt(COMPONENT+"."+String.valueOf(i) +".count", components/2));
                    
                    generatedComponentsCount.add(0);//Only for statistcs and printing
                }
                GridDeployer.componentsCount = (Vector<Integer>)componentsCount.clone();
            }
        }
        
        //----------------------------------------NODES--------------------------------------------
        nodes = Configuration.getInt(NUM_NODE, 3);
        if(nodes == 1){//One type nodes
            nodesCPU.add(Configuration.getInt(NODES_CPU, 1));
            nodesMemory.add(Configuration.getInt(NODES_MEMORY, 1));
            nodesOS.add(GridFactory.LookupOS(Configuration.getString(NODES_OS,"WIN")));
            
            generatedNodesCount.add(0);//Only for statistcs and printing (will contain only one element in this case since there are no specific types)
        }else{//multi type nodes
            try{//If attribute "grid.nodes.type" doesn't exist then assume random generation
                if(Configuration.getString(TYPE_NODE, COMPONENT_NODE_TYPE[0]).equalsIgnoreCase(COMPONENT_NODE_TYPE[1]))
                  isSpecificNodeType = true;  
            }catch(Exception ex){}
            
            if(!isSpecificNodeType){//Specifications of node types will be generated randomly
                nodesCPU.add(Configuration.getInt(NODES_CPU, nodes));
                nodesMemory.add(Configuration.getInt(NODES_MEMORY, 2 * nodes));
                nodesOS.add(GridFactory.LookupOS(Configuration.getString(NODES_OS,"MAC")));
                
                generatedNodesCount.add(0);//Only for statistcs and printing (will contain only one element in this case since there are no specific types)
                
            }else{//Specifications of node types will be specified for each type
                for(int i = 1; i <= nodes; i++){
                    nodesCPU.add(Configuration.getInt(NODE+"."+String.valueOf(i) +".cpu", 1));    
                    nodesMemory.add(Configuration.getInt(NODE+"."+String.valueOf(i) +".memory", 2));
                    nodesOS.add(GridFactory.LookupOS(Configuration.getString(NODE+"."+String.valueOf(i) +".os","WIN")));
                    poolCount.add(Configuration.getInt(NODE+"."+String.valueOf(i) +".poolcount", 2));
                    
                    generatedNodesCount.add(0);//Only for statistcs and printing
                }
                GridInitializer.poolCount = (Vector<Integer>)poolCount.clone();
            }
        }
        
       
        
        printString = Configuration.getString(PAR_PRINT, printChoices[1]);
        if(printString.equalsIgnoreCase(printChoices[0]))
            print = SIMPLE;
        else if(printString.equalsIgnoreCase(printChoices[1]))
            print = DETAILED;
        ////brokers = Configuration.getInt(PAR_BROKERS, 1);
        
//        GridFactory.generateComponents(components,componentsCPU,componentsMemory,componentsCycles,componentsOS);
//        GridFactory.generateGridNodeSpecifications(nodes,nodesCPU,nodesMemory,nodesOS);
        GridFactory.generateComponents();
        GridFactory.generateGridNodeSpecifications();
        
        System.err.println(components+" Grid components generated");
        GridFactory.PrintComponents();
        
        System.err.println(nodes+" Grid node specifications generated");
        GridFactory.PrintGridNodeSpecifications();
        
////        InitializeGridBorokerSize();
        
        ////PrintBrokers();
        
        System.out.println("----------------------------------------------------");
        
        
        
    }
    
//    static void InitializeGridBorokerSize(){
//        
//        brokerIDs = new Vector<Long>();
//        brokerIndexes = new Vector<Integer>();
//////        int brokerCount =0;
//////        boolean isBroker = true;
//        
//        for(int i = 0;i < Network.size();i++){
//            
//            
//            ////if(++brokerCount > brokers) isBroker = false;
//            
//            ((GridNode)Network.get(i)).InitializeNode(nodesMemory, nodesCPU, false);
//            
//            ////if (isBroker) brokerIDs[i] = Network.get(i).getID();
//            
//        }
//    }
    
    static void PrintBrokers(){
        
        System.out.println("Grid Brokers:");
        System.out.println("-----------------------------------");
        
        for(int i=0;i<brokers;i++){
            
            System.out.println("Broker "+i+"   ID: "+brokerIDs.get(i));
            
        }
        
        System.out.println("-----------------------------------");
    }

}
