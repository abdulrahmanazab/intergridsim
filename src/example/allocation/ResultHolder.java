/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package example.allocation;

import peersim.core.*;
import peersim.config.FastConfig;
import peersim.vector.SingleValueHolder;
import peersim.cdsim.CDProtocol;

/**
 *
 * @author nsa
 */
public class ResultHolder {

    protected long mainNodeID_;
    protected int totalCycles_;
    protected int networkSize_;
    protected NetworkKnowledge[] networkKnowledge_;

    public ResultHolder (long id, int cycles, int networkSize) {
        this.mainNodeID_ = id;
        this.totalCycles_ = cycles;
        this.networkSize_ = networkSize;
        this.networkKnowledge_ = new NetworkKnowledge[this.totalCycles_];
        for (int i = 0; i < networkKnowledge_.length; i++) {
            networkKnowledge_[i] = new NetworkKnowledge(this.networkSize_);
        }

    }

    public void Print () {
         System.out.println("-----------------------------");
        System.out.println("Main Node : " + mainNodeID_);
        for (int i = 0; i < networkKnowledge_.length; i++) {
            System.out.println("Cycle: " + i);
            networkKnowledge_[i].Print();
        }

    }

    public class NetworkKnowledge {

        protected double[][] resources_;

        public NetworkKnowledge (int size) {
            resources_ = new double[size][2];
            Init();
         
        }

        private void Init () {
            for (int i = 0; i < resources_.length; i++) {
                resources_[i][0] = 0;
                resources_[i][1] = -1;
            }
        }

        public void Update (int index, double resource, int cycle) {
            boolean ok = false;
            if (index >= 0 && index < resources_.length) {
                ok = true;
            }
            if (ok) {
                ok = (resource >= 0);
            }
            if (ok) {
                ok = (cycle >= 0);
            }
            if (ok) {
                if (cycle > resources_[index][1]) {
                    resources_[index][0] = resource;
                    resources_[index][1] = index;
                }
            }
        }

        public void Print () {
            
            System.out.println("Node \t\t\tResource \t\t\t Cycle ");
            java.text.DecimalFormat format=new java.text.DecimalFormat("#0.00");
            for (int i = 0; i < resources_.length; i++) {
                System.out.println(i +"\t\t\t"+format.format(resources_[i][0]) + " \t\t\t " + resources_[i][1]);
            }
        }
    }
}

