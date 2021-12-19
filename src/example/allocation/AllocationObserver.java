/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example.allocation;

import peersim.config.*;
import peersim.core.*;
import peersim.vector.*;
import peersim.util.IncrementalStats;

/**
 *
 * @author nsa
 */
public class AllocationObserver implements Control {

    // /////////////////////////////////////////////////////////////////////
    // Constants
    // /////////////////////////////////////////////////////////////////////
    /**
     * Config parameter that determines the accuracy for standard deviation
     * before stopping the simulation. If not defined, a negative value is used
     * which makes sure the observer does not stop the simulation
     * 
     * @config
     */
    private static final String PAR_ACCURACY = "accuracy";
    /**
     * The protocol to operate on.
     * 
     * @config
     */
    private static final String PAR_PROT = "protocol";
    private static final String MAIN = "Main_Node_Index";
    // /////////////////////////////////////////////////////////////////////
    // Fields
    // /////////////////////////////////////////////////////////////////////
    /**
     * The name of this observer in the configuration. Initialized by the
     * constructor parameter.
     */
    private final String name;
    /**
     * Accuracy for standard deviation used to stop the simulation; obtained
     * from config property {@link #PAR_ACCURACY}.
     */
    private final double accuracy;
    /** Protocol identifier; obtained from config property {@link #PAR_PROT}. */
    private final int pid;
    private int mainIndex = -1;
    private final int totalCycles;
    private ResultHolder resultHolder;

    // /////////////////////////////////////////////////////////////////////
    // Constructor
    // /////////////////////////////////////////////////////////////////////
    /**
     * Creates a new observer reading configuration parameters.
     */
    public AllocationObserver (String name) {
        this.name = name;
        this.totalCycles = Configuration.getInt("simulation.cycles");
        accuracy = Configuration.getDouble(name + "." + PAR_ACCURACY, -1);
        pid = Configuration.getPid(name + "." + PAR_PROT);
        mainIndex = Configuration.getInt("init.alloci" + "." + MAIN);
        long ID =Network.get(mainIndex).getID();
        resultHolder = new ResultHolder(ID,this.totalCycles,Network.size());
    }

    // /////////////////////////////////////////////////////////////////////
    // Methods
    // /////////////////////////////////////////////////////////////////////
    /**
     * Orgenize data seen by the main node for each cycle
     * Prints the result at the end 
     */
    public boolean execute () {

        for(int i=0;i<Network.size();i++)
        {            
            if(Network.get(i).getID()==resultHolder.mainNodeID_)
            {
                AllocationFunction mainPeer = (AllocationFunction) Network.get(i).getProtocol(pid);
                int currentCycle= (int)peersim.cdsim.CDState.getTime();
                resultHolder.networkKnowledge_[currentCycle].resources_=mainPeer.resources_;
                break;
            }
        } 
        if(peersim.cdsim.CDState.getTime()== this.totalCycles-1)
        {
            resultHolder.Print();
        }
        return false;
    }

   
}
