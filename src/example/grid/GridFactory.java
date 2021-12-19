/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package example.grid;
import java.util.Vector;
import javax.management.BadAttributeValueExpException;
import peersim.cdsim.CDState;
import peersim.core.CommonState;
/**
 *
 * @author 2906095
 */
public class GridFactory {
    
    public static Vector<GridComponent> components = new Vector<GridComponent>();
    public static Vector<GridNodeResourceState> gridNodeSpecification = new Vector<GridNodeResourceState>();
    
    public static final int WIN = 0;
    public static final int LINUX = 1;
    public static final int MAC = 2;
    
    public static int LookupOS(String osName){
        if(osName.equalsIgnoreCase("WIN")) return 0;
        else if(osName.equalsIgnoreCase("LINUX")) return 1;
        else if(osName.equalsIgnoreCase("MAC")) return 2;
        else return 0;
    }
    public static String LookupOS(int osIndex){
        if(osIndex == 0) return "WIN";
        else if(osIndex == 1) return "LINUX";
        else if(osIndex == 2) return "MAC";
        else return "WIN";
    }
    public static void generateGridNodeSpecifications(){
        if(GridConfig.nodes == 1){//Only one node type
            gridNodeSpecification.add(new  GridNodeResourceState(-1, 0, GridConfig.nodesCPU.get(0),
                    GridConfig.nodesMemory.get(0),GridConfig.nodesOS.get(0), 0));
        }else if(GridConfig.isSpecificNodeType){
            for(int i=0; i<GridConfig.nodes ; i++){
                gridNodeSpecification.add(new  GridNodeResourceState(-1, 0, GridConfig.nodesCPU.get(i),
                    GridConfig.nodesMemory.get(i),GridConfig.nodesOS.get(i), 0));
            }
        }else if(!GridConfig.isSpecificNodeType){//Specifications of node types will be generated randomly
            int cpu,memory,os;
            for(int i=0; i<GridConfig.nodes ; i++){
                cpu = GridConfig.nodesCPU.get(0) - i;
                if (cpu<1) cpu=1;

                memory = GridConfig.nodesMemory.get(0) - i;
                if (memory<1) memory=1;

                os = GridConfig.nodesOS.get(0) - i;
                if (os<0) os=0;

                gridNodeSpecification.add(new  GridNodeResourceState(-1, 0, cpu, memory, os, 0) );
            }
        }
    }
    public static void generateComponents(){
        if(GridConfig.components == 1){//Only one node type
            components.add(new  GridComponent(GridConfig.componentsMemory.get(0),GridConfig.componentsCPU.get(0),
                    GridConfig.componentsCycles.get(0),GridConfig.componentsOS.get(0),0));
        }else if(GridConfig.isSpecificComponentType){
            for(int i=0; i<GridConfig.components ; i++){
                components.add(new  GridComponent(GridConfig.componentsMemory.get(i),GridConfig.componentsCPU.get(i),
                    GridConfig.componentsCycles.get(i),GridConfig.componentsOS.get(i),0));
            }
        }else if(!GridConfig.isSpecificComponentType){//Specifications of node types will be generated randomly
            int cpu,memory,os,cycles;
            for(int i=0; i<GridConfig.components ; i++){
                cpu = GridConfig.componentsCPU.get(0) - i;
                if (cpu<1) cpu=1;

                memory = GridConfig.componentsMemory.get(0) - i;
                if (memory<1) memory=1;

                os = GridConfig.componentsOS.get(0) - i;
                if (os<0) os=0;
                
                cycles = CommonState.r.nextInt(GridConfig.cycles);            
                if (cycles < GridConfig.componentsMinCycles) cycles = GridConfig.componentsMinCycles;
                components.add(new  GridComponent(memory, cpu, cycles, os, i) );
            }
        }
    }
    @Deprecated
    public static void generateGridNodeSpecifications(int number, int maxCPUCores, int maxMemorySize ,Vector<Integer> OS){
        
               
        int cpu,memory,os;
        
        if(number == 1){//Only one component type
            cpu = maxCPUCores; memory = maxMemorySize; os = OS.get(0);
            gridNodeSpecification.add(new  GridNodeResourceState(-1,0, cpu, memory, os,0) );
        }else{
        
            for(int i=0; i<number ; i++){

                cpu = maxCPUCores - i;
                if (cpu<1) cpu=1;

                memory = maxMemorySize - i;
                if (memory<1) memory=1;

                os = OS.get(i);
                if (os<0) os=0;

                
                gridNodeSpecification.add(new  GridNodeResourceState(-1, 0, cpu, memory, os, 0) );
            }
        }
    }
    @Deprecated
    public static void generateComponents(int number, int maxCPUCores, int maxMemorySize, int componentCycles ,Vector<Integer> OS){
        
               
        int cpu,memory,cycles,os;
        
        if(number == 1){//Only one component type
            cpu = maxCPUCores; memory = maxMemorySize; os = OS.get(0); cycles = componentCycles;
            components.add(new  GridComponent(memory, cpu, cycles, os, 0) );
        }else{
        
            for(int i=0; i<number ; i++){

                cpu = maxCPUCores - i;
                if (cpu<1) cpu=1;

                memory = maxMemorySize - i;
                if (memory<1) memory=1;

                os = OS.get(i);
                if (os<0) os=0;
                
                cycles = CommonState.r.nextInt(componentCycles);            
                if (cycles<1) cycles = 1;
                //cycles = (int)CDState.getEndTime();
                //if (cpu<2) cycles=2;



                components.add(new  GridComponent(memory, cpu, cycles, os, i) );
            }
        }
    }
    
    public static GridComponent getRandomComponent(){
        
        int i = 0;
        
        if (components.size() > 1) 
            i = CommonState.r.nextInt(components.size()-1);
        
        
        
        
        return new GridComponent(components.get(i).MemoryUsage,
                components.get(i).CPUUsage, components.get(i).TotalSimulationCycles,components.get(i).OS,components.get(i).typeID);
        
    }
    
    
    public static GridComponent getComponentByType(int typeIndex){// 1 based not 0 based
        
        GridComponent c = new GridComponent(components.get(typeIndex - 1).MemoryUsage,
                components.get(typeIndex - 1).CPUUsage, components.get(typeIndex - 1).TotalSimulationCycles,
                components.get(typeIndex - 1).OS,components.get(typeIndex - 1).typeID);
        
        c.componentType = typeIndex;
        return c;
        
    }
    
    public static GridNodeResourceState getGridNodeSpecificationByType(int typeIndex){// 1 based not 0 based
       
        return new GridNodeResourceState(-1,0, gridNodeSpecification.get(typeIndex - 1).AvailabbleCPU, gridNodeSpecification.get(typeIndex - 1).availableMemory, gridNodeSpecification.get(typeIndex - 1).OS,0);
        
    }
    
    public static GridNodeResourceState getRandomGridNodeSpecification(){
        
        int i = 0;
        
        if (gridNodeSpecification.size() > 1) 
            i = CommonState.r.nextInt(gridNodeSpecification.size()-1);
        
        
        
        
        return new GridNodeResourceState(-1,0, gridNodeSpecification.get(i).AvailabbleCPU, gridNodeSpecification.get(i).availableMemory, gridNodeSpecification.get(i).OS,0);
        
    }
    
    public static GridNodeResourceState getGridNodeSpecification(int os){
        
        int i = 0;
        
        if (gridNodeSpecification.size() > 1){ 
         for(int j = 0; j < gridNodeSpecification.size();j++)
             if(gridNodeSpecification.get(j).OS == os){
                 i = j;
                 break;
             }
        }
        
              
        
        return new GridNodeResourceState(-1,0, gridNodeSpecification.get(i).AvailabbleCPU, gridNodeSpecification.get(i).availableMemory, gridNodeSpecification.get(i).OS,0);
        
    }
   
    public static GridComponent getComponent(int os){
        
        int i = 0;
        
        if (components.size() > 1){ 
         for(int j = 0; j < components.size();j++)
             if(components.get(j).OS == os){
                 i = j;
                 break;
             }
        }
        
        
        
        return new GridComponent(components.get(i).MemoryUsage,
                components.get(i).CPUUsage, components.get(i).TotalSimulationCycles,components.get(i).OS,components.get(i).typeID);
        
    }
    
    public static void PrintComponents(){
        
        for(int i=0; i<components.size() ; i++){
            
            System.out.println("Component "+i+": CPU Usage: "+components.get(i).CPUUsage+" Cores"
                                +"\t Memory Usage: "+components.get(i).MemoryUsage+" GB"
                                +"\t OS: "+components.get(i).OS
                                +"\t Total execution cycles: "+components.get(i).TotalSimulationCycles);
        }
        
         System.out.println("----------------------------------------------------");
    }
    public static void PrintGridNodeSpecifications(){
        
        for(int i=0; i<gridNodeSpecification.size() ; i++){
            
            System.out.println("Node Template "+i+": CPU: "+gridNodeSpecification.get(i).AvailabbleCPU+" Cores"
                                +"\t Memory Size: "+gridNodeSpecification.get(i).availableMemory+" GB"
                                +"\t OS: "+gridNodeSpecification.get(i).OS);
        }
        
         System.out.println("----------------------------------------------------");
    }
}
