/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package example.grid;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Vector;
//import javax.security.auth.login.Configuration;
import peersim.config.*;

/**
 *
 * @author 2906095
 */
public class GridHistogram {
    
   protected static final String HIST_MAX_CPU = "GridHistogram.maxCPU";
   protected static final String HIST_MAX_Memory = "GridHistogram.maxMemory";
   protected static final String HIST_CPU_Step = "GridHistogram.CPUStep";
   protected static final String HIST_Memory_Step = "GridHistogram.MemoryStep";
   protected static final String PAR_ADD_METHOD = "GridHistogram.addMethod";
   protected static final String[] PAR_ADD_METHOD_ARRAY = {"separative","accumulative"};
   protected static final int CPUCORES = 0, MEMORYGB = 1, OS = 2;
   protected static final int SEPARATE = 0, ACCUMULATE = 1;
   
   public static String addMethodString;
   public static int addMethod;
   
   public static long maxCPU, maxMemory;
   
   public static int CPUStep, MemoryStep;
   
   public static int stepsCPU,stepsMemory;
   
   
   public Vector<HistogramElement> CPUelements;
   public Vector<HistogramElement> Memoryelements;
   public Vector<HistogramElement> OSelements;
   public int dataSize;//size of data stored in the histogram
    
   public GridHistogram(){
       CPUelements = new Vector<HistogramElement>();
       Memoryelements = new Vector<HistogramElement>();
       OSelements = new Vector<HistogramElement>();
       dataSize = 0;
       for(int i = 0; i <= maxCPU; i++)
           CPUelements.add(new HistogramElement(i, 0));
       for(int i = 0; i <= maxMemory; i++)
           Memoryelements.add(new HistogramElement(i, 0));
       for(int i = 0; i <= 3; i++)
           OSelements.add(new HistogramElement(i, 0));
   }
   public GridHistogram(boolean initializeCharts){
       CPUelements = new Vector<HistogramElement>();
       Memoryelements = new Vector<HistogramElement>();
       OSelements = new Vector<HistogramElement>();
       dataSize = 0;
       if(initializeCharts){
           for(int i = 0; i <= maxCPU; i++)
                CPUelements.add(new HistogramElement(i, 0));
           for(int i = 0; i <= maxMemory; i++)
                Memoryelements.add(new HistogramElement(i, 0));
           for(int i = 0; i <= 3; i++)
                OSelements.add(new HistogramElement(i, 0));
       }
   }
    public static void ReadConfig(){
        
        maxCPU = Configuration.getLong(HIST_MAX_CPU, 8);
        maxMemory = Configuration.getLong(HIST_MAX_Memory, 8);
        
        CPUStep = Configuration.getInt(HIST_CPU_Step, 1);
        MemoryStep = Configuration.getInt(HIST_Memory_Step, 1);
        
        addMethodString = Configuration.getString(PAR_ADD_METHOD,PAR_ADD_METHOD_ARRAY[1]);
        
        if(addMethodString.equals(PAR_ADD_METHOD_ARRAY[0]))
            addMethod = SEPARATE;
        else if(addMethodString.equals(PAR_ADD_METHOD_ARRAY[1]))
            addMethod=ACCUMULATE;
    }
            
    
    public void addNodeParameters(GridNodeResourceState rs){
        addValue(rs.AvailabbleCPU, CPUCORES);
        addValue(rs.availableMemory, MEMORYGB);
        addValue(rs.OS, OS);
    }
    public void updateNodeParameters(GridNodeResourceState rsOld, GridNodeResourceState rsNew){
        removeValue(rsOld.AvailabbleCPU, CPUCORES);
        removeValue(rsOld.availableMemory, MEMORYGB);
        removeValue(rsOld.OS, OS);
        
        addValue(rsNew.AvailabbleCPU, CPUCORES);
        addValue(rsNew.availableMemory, MEMORYGB);
        addValue(rsNew.OS, OS);
    }
    
       
    public boolean addValue(int value, int category){
       
        boolean isUpdate =false;
       
        switch(category){
           case CPUCORES:
                        if(CPUelements.isEmpty()){
                            CPUelements.add(new HistogramElement(value, 1));
                            break;
                        }
                        for(HistogramElement hel : CPUelements){
                            if (value == hel.value){// Add it to its associated bar anyway
                             hel.weight ++;
                             isUpdate = true;
                             break;
                             }else if(value > hel.value && addMethod == ACCUMULATE)// Add it to the bars with less values in this case only
                                hel.weight ++;
                         }
                         if(!isUpdate)
                             if(value < CPUelements.get(0).value)
                                 CPUelements.insertElementAt(new HistogramElement(value, 1), 0);
                             else                                 
                                CPUelements.add(new HistogramElement(value, 1));                         
                         break;
             case MEMORYGB:
                        if(Memoryelements.isEmpty()){
                            Memoryelements.add(new HistogramElement(value, 1));
                            break;
                        }
                        for(HistogramElement hel : Memoryelements){
                            if (value == hel.value){// Add it to its associated bar anyway
                                hel.weight ++;
                                isUpdate = true;
                                break;
                            }else if(value > hel.value && addMethod == ACCUMULATE)// Add it to the bars with less values in this case only
                                hel.weight ++;
                            }
                            if(!isUpdate)
                             if(value < Memoryelements.get(0).value)
                                 Memoryelements.insertElementAt(new HistogramElement(value, 1), 0);
                             else                                 
                                Memoryelements.add(new HistogramElement(value, 1));                         
                         break;
             case OS:
                         if(OSelements.isEmpty()){
                            OSelements.add(new HistogramElement(value, 1));
                            break;
                         }
                         for(HistogramElement hel : OSelements){
                            if (value == hel.value){// Add it to its associated bar only
                                hel.weight ++;
                                isUpdate = true;
                                break;
                            }
                         }
                         if(!isUpdate)
                             if(value < OSelements.get(0).value)
                                 OSelements.insertElementAt(new HistogramElement(value, 1), 0);
                             else                                 
                                OSelements.add(new HistogramElement(value, 1));                         
                         break;
             default:
                 return false;
        }
        return true;
    }
    
    public boolean removeValue(int value, int category){
       
        boolean isUpdate =false;
       
        switch(category){
           case CPUCORES:
                        for(HistogramElement hel : CPUelements){
                            if (value == hel.value){// Remove it from its associated bar anyway
                                hel.weight --;
                                if(hel.weight < 0)hel.weight = 0;
                                isUpdate = true;
                                break;
                            }else if(value > hel.value && addMethod == ACCUMULATE){// Add it to the bars with less values in this case only
                                hel.weight --;
                                if(hel.weight < 0)hel.weight = 0;
                            }
                         }
                         
                         break;
             case MEMORYGB:
                        for(HistogramElement hel : Memoryelements){
                            if (value == hel.value){// Add it to its associated bar anyway
                                hel.weight --;
                                if(hel.weight < 0)hel.weight = 0;
                                isUpdate = true;
                                break;
                            }else if(value > hel.value && addMethod == ACCUMULATE){// Add it to the bars with less values in this case only
                                hel.weight --;
                                if(hel.weight < 0)hel.weight = 0;
                            }
                        }                          
                         break;
             case OS:
                         for(HistogramElement hel : OSelements){
                            if (value == hel.value){// Add it to its associated bar only
                                hel.weight --;
                                if(hel.value < 0)hel.value = 0;
                                isUpdate = true;
                                break;
                            }
                         }
                            
                         break;
             default:
                 return false;
        }
        return true;
    }
    
    public static float getFuzzyAND(GridHistogram hist, GridComponent c, float ruleWeight){
        return ruleWeight *
                Math.min(getFuzzificationValue(hist.CPUelements, c.CPUUsage, true),
                    Math.min(getFuzzificationValue(hist.Memoryelements, c.MemoryUsage, true),
                        getFuzzificationValue(hist.OSelements, c.OS, false)));
    
    }
    
    public static float getFuzzyOR(GridHistogram hist, GridComponent c, float ruleWeight){
        return ruleWeight * 
                Math.max(getFuzzificationValue(hist.CPUelements, c.CPUUsage, true),
                    Math.max(getFuzzificationValue(hist.Memoryelements, c.MemoryUsage, true),
                        getFuzzificationValue(hist.OSelements, c.OS, false)));
    
    }
    
    static float getFuzzificationValue(Vector<HistogramElement> bar, int value, boolean isAccumulativeParameter){
        float sum = 0, match = 0;
                
        for(HistogramElement hel : bar){
            if(addMethod == ACCUMULATE && hel.value == 0 && isAccumulativeParameter)
                sum  = hel.weight;
            if(hel.value == value)
                match = hel.weight;
            if(addMethod == SEPARATE || !isAccumulativeParameter)
                sum += hel.weight;
        }
        if(sum == 0)
            return 0;
        else
            return match/sum;
    }
    
    public void print(PrintStream ps){
    String txt = "";
    for(HistogramElement hel : CPUelements)
        txt += String.valueOf(hel.value)+","+String.valueOf(hel.weight)+"\t";
    txt+="#";
    for(HistogramElement hel : Memoryelements)
        txt += String.valueOf(hel.value)+","+String.valueOf(hel.weight)+"\t";
    txt+="#";
    for(HistogramElement hel : OSelements)
        txt += String.valueOf(hel.value)+","+String.valueOf(hel.weight)+"\t";
    txt+="#";
    
    ps.println(txt);
    
    }
    
    public GridHistogram clone(){
        GridHistogram hist = new GridHistogram(false);
        for(HistogramElement hel : CPUelements)
            hist.CPUelements.add(hel.clone());
        for(HistogramElement hel : Memoryelements)
            hist.Memoryelements.add(hel.clone());
        for(HistogramElement hel : OSelements)
            hist.OSelements.add(hel.clone());
        
        hist.dataSize = dataSize;
        
        return hist;
    }
    
    
    
//////    public boolean InitializeElements(){
//////        stepsCPU =(int) maxCPU/CPUStep;
//////        stepsMemory =(int) maxMemory/MemoryStep;
//////        
//////        int value;
//////        //int i;
//////        for(int i=0; i<stepsCPU;i++){
//////                        
//////            CPUelements.add(new HistogramElement(i,0));
//////        }
//////        
//////        for(int i=0; i<stepsMemory;i++){
//////                        
//////            CPUelements.add(new HistogramElement(i,0));
//////        }
//////        
//////        return true;
//////    }

}
