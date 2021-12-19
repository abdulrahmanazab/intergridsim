/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deployment.entities;

import java.text.*;
import java.util.*;

/**
 *Defines the load per Node 
 * @author nsa
 */
public class WorkLoad implements Identifiable {

    /**----------------------------------------------------------------------
     * Fields 
     * ----------------------------------------------------------------------
     */
    private final DecimalFormat FORMAT = new DecimalFormat("#0.00");
    private int max_ = 100;    // Defines the current consumption of the resources
    private int time_ = -1;
    protected long id_ = -1;
    private Vector<Component> components_ = new Vector<Component>();

    /**
     * Get the value of id_
     *
     * @return the value of id_
     */
    public long getId() {
        return this.id_;
    }

    /**
     * Set the value of id_
     *
     * @param id_ new value of id_
     */
    public void setId(long id) {
        this.id_ = id;
    }

    public WorkLoad() {
        max_ = 100;
        components_ = new Vector<Component>();
        time_ = -1;
        id_ = -1;
    }

    public WorkLoad(long id) {
        this.id_ = id;
        max_ = 100;
        components_ = new Vector<Component>();
        time_ = -1;
    }

    /**
     * Get the value of consumption_
     *
     * @return the value of consumption_
     */
    public int getConsumption() {
        int consumption = 0;
        for (int i = 0; i < this.components_.size(); i++) {
            consumption += this.components_.elementAt(i).getCurrentUse();
        }
        return consumption;
    }

    /**
     * 
     * @param consumption
     * @return
     */
    public boolean tolerates(int consumption) {
        int maxConsumption=0;
          for (int i = 0; i < this.components_.size(); i++) {
            maxConsumption += this.components_.elementAt(i).getMaxResourceNeeds();
        }
        if (maxConsumption + consumption < this.max_ && this.IsDefined()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Adds the input to the current consumption
     * @param consumption
     * @return
     * true: if possible (doesn t exceed max load)
     * false : if not possible.
     * 
     */
    public boolean addComponent(Component c) {
        if(this.tolerates(c.getMaxResourceNeeds()))
        {
            this.components_.add(c);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * 
     * @param consumption
     * @return
     */
    public boolean removeComponent(Component c)
    {
       return removeComponent(c.id_);
    }
    public boolean removeComponent(long componentID)
    {
        if(componentID<0) return false;

        int index=-1;
         for (int i = 0; i < this.components_.size(); i++) {
            if(this.components_.elementAt(i).id_==componentID)
            {
                index=i;
                break;
            }
        }
        if(index>=0)
        {
            this.components_.remove(index);
            return true;
        }
        return false;
    }
    /**
     * Return resource consumption as a proportion of the total resource 
     * @return
     */
    public double getConsumptionProportion() {
        if (max_ > 0) {
            double result = (double) this.getConsumption() / max_;
            return result;
        }
        return Double.NaN;
    }

    /**
     * Get the value of max_
     *
     * @return the value of max_
     */
    public int getMax() {
        return max_;
    }

    /**
     * Set the value of max_
     *
     * @param max_ new value of max_
     */
    public void setMax(int max) {
        this.max_ = max;
    }

    /**
     * Get the value of time_
     *
     * @return the value of time_
     */
    public int getTime() {
        return this.time_;
    }

    /**
     * Set the value of time_
     *
     * @param time_ new value of time_
     */
    public void setTime(int time) {
        this.time_ = time;
    }

    public boolean IsDefined() {
        return this.id_ >= 0 && this.max_ > 0 && this.time_ >= 0;
    }

    public Vector<Component> getComponents() {
        return components_;
    }

    public void setComponents(Vector<Component> components) {
        this.components_ = components;
    }

    @Override
    public String toString() {
        return "ID:" + this.id_ + "  Load : " +
                this.FORMAT.format(this.getConsumptionProportion() * 100) + " %" + "    Time : " + this.time_;
    }

    /**
     * 
     * @param source
     */
    public void Copy(WorkLoad source) {
        this.id_ = source.id_;
        this.max_ = source.max_;
        this.time_ = source.time_;
        this.components_.clear();
        for (int i = 0; i < source.components_.size(); i++) {
            this.components_.add(source.components_.elementAt(i));
        }

    }
}
 
