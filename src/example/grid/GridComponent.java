/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package example.grid;

import java.util.Vector;

/**
 *
 * @author 2906095
 */
public class GridComponent {
    
public int MemoryUsage = 0; //GB
public int CPUUsage = 0;//No. of CPU Cores
public int OS = 0;
public int TotalSimulationCycles = 0;//Execution time of the component
public int typeID;// ID of the component type. In case of only one generated type all typeIDs will equal to 0
public int id; //a unique id of the component itself
//used only when trying to deploy the component and pass it between brokers
public int remainingDeployTrials = 0;
public int maxWaitingTime = 0;
public int deployedAt = 0;//Cycle when the component was deployed by the 'GridDeployer' for execution
public int totalWaitingTime = 0;//Total Time between the deployment time and the allocation time
public int queuedAt =0;//Cycle when it was (last) put in a broker queue
public int ownerBrokerIndex =0;
public int lastHostBrokerIndex = 0;//The last broker to which the component was deployed
public int allocatedAtBrokerIndex = -1, networkDistance = -1;//The broker in which domain this component is allocated, and the number of hops between this broker and the owner broker
public Vector<Integer> flockingBrokers;// used in case of adaptive-flocking only
public Vector<Integer> routingPath;//The path (Hops) tp reach the target broker. Used in case of using routing table

public int componentType;// Used in case of "grid.components.no" > 1 and "grid.components.type" = "specific"

public static final int WIN = 0;
public static final int LINUX = 1;
public static final int MAC = 2;

static int componentID;

public GridComponent(int Memory, int CPU, int Cycles, int OS){
    
    MemoryUsage = Memory;
    CPUUsage = CPU;
    this.OS = OS;
    TotalSimulationCycles = Cycles;
    typeID = -1;
    this.id = generateID();
    flockingBrokers = new Vector<Integer>();
    routingPath = new Vector<Integer>();
    
        
}
public GridComponent(int Memory, int CPU, int Cycles, int OS, int typeID){
    
    MemoryUsage = Memory;
    CPUUsage = CPU;
    this.OS = OS;
    TotalSimulationCycles = Cycles;
    this.typeID = typeID;
    this.id = generateID();
    flockingBrokers = new Vector<Integer>();
    routingPath = new Vector<Integer>();    
}

public static int generateID(){
    return componentID++;
}

public static void initializeID(){
    componentID = 0;
}

public static boolean Match(GridComponent c,GridNodeResourceState rs){
    
    if (rs.AvailabbleCPU>=c.CPUUsage && rs.availableMemory>=c.MemoryUsage && rs.OS == c.OS)
        return true;
    else
        return false;
}
@Override
public String toString(){
        
        return "Component "+typeID+" > CPU Usage: "+CPUUsage+" MHz\tMemory Usage: "+MemoryUsage
                +" MB\tTotal Simulation Cycles: "+TotalSimulationCycles;
    }

public GridComponent Clone(boolean newID){
        GridComponent c = new GridComponent(this.MemoryUsage, this.CPUUsage, this.TotalSimulationCycles, this.OS, this.typeID);
        c.TotalSimulationCycles = this.TotalSimulationCycles;
        c.remainingDeployTrials = this.remainingDeployTrials;
        c.maxWaitingTime = this.maxWaitingTime;
        c.deployedAt = this.deployedAt;
        c.allocatedAtBrokerIndex = this.allocatedAtBrokerIndex;
        c.networkDistance = this.networkDistance;
        c.queuedAt = this.queuedAt;
        c.totalWaitingTime = this.totalWaitingTime;
        c.ownerBrokerIndex = this.ownerBrokerIndex;
        c.lastHostBrokerIndex = this.lastHostBrokerIndex;
        c.typeID = this.typeID;
        if(newID)
            c.id = generateID();
        else
            c.id = this.id;
        
        c.flockingBrokers = (Vector<Integer>)this.flockingBrokers.clone();
        c.routingPath = (Vector<Integer>)this.routingPath.clone();
        
        return c;
    }
    public GridComponent Clone(){
        return Clone(false);
    }
}
