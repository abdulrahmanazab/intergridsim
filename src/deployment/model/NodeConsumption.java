/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deployment.model;

import java.text.*;
import java.util.*;
import deployment.math.*;

/**
 *
 * @author nsa
 */
public class NodeConsumption extends Gaussian implements Identifiable {

    protected long id = -1;
    private final DecimalFormat FORMAT = new DecimalFormat("#0.00");
    private int max = 100;
    private int time = -1;
    private Vector<Component> components = new Vector<Component>();
  
    /**
     * Get the value of id_
     *
     * @return the value of id_
     */
    public long getId ()
    {
        return this.id;
    }

    /**
     * Set the value of id_
     *
     * @param id_ new value of id_
     */
    public void setId (long i)
    {
        this.id = i;
    }

    private boolean update ()
    {
        double totalMu = 0;
        double sigmaS = 0;
        for (int i = 0; i < components.size(); i++)
        {
            totalMu += components.get(i).getMu();
            sigmaS += Math.pow(components.get(i).getSigma(), 2);

        }
        if ((totalMu + 3 * Math.sqrt(sigmaS)) < this.max)
        {
            this.mu = totalMu;
            this.sigma = Math.sqrt(sigmaS);
            return true;
        } else
        {
            return false;
        }
    }

    public boolean tolerates (Component c)
    {
        double totalMu = c.getMu();
        double sigmaS = Math.pow(c.getSigma(), 2);
        for (int i = 0; i < components.size(); i++)
        {
            totalMu += components.get(i).getMu();
            sigmaS += Math.pow(components.get(i).getSigma(), 2);

        }
        if ((totalMu + 3 * Math.sqrt(sigmaS)) < this.max)
        {
            return true;
        } else
        {
            return false;
        }
    }

    public boolean addComponent (Component c)
    {
        if (this.tolerates(c))
        {
            this.components.add(c);
            return this.update();
        } else
        {
            return false;
        }
    }

    /**
     * 
     * @param consumption
     * @return
     */
    public boolean removeComponent (Component c)
    {
        return removeComponent(c.id);
    }

    public boolean removeComponent (long componentID)
    {
        if (componentID < 0)
        {
            return false;
        }
        int index = -1;
        for (int i = 0; i < this.components.size(); i++)
        {
            if (this.components.elementAt(i).id == componentID)
            {
                index = i;
                break;
            }
        }
        if (index >= 0)
        {
            this.components.remove(index);
            return this.update();
        }
        return false;
    }

    public double simulateLoad ()
    {
        return this.Realize();
    }

    /**
     * Get the value of time_
     *
     * @return the value of time_
     */
    public int getTime ()
    {
        return this.time;
    }

    /**
     * Set the value of time_
     *
     * @param time_ new value of time_
     */
    public void setTime (int t)
    {
        this.time = t;
    }

    @Override
    public String toString ()
    {
        return "ID : " + this.id + "  Load(Mu | sigma) :  " +
                this.FORMAT.format(this.mu) + " | " + this.FORMAT.format(this.getSigma()) + "    Time : " + this.time;
    }

    public void Copy (NodeConsumption source)
    {
        this.id = source.id;
        this.max = source.max;
        this.time = source.time;
        this.mu=source.mu;
        this.sigma=source.sigma;
        this.components.clear();
        for (int i = 0; i < source.components.size(); i++)
        {
            this.components.add(source.components.elementAt(i));
        }

    }

    /**
     * 
     * @return
     */
    public int totalComponents ()
    {
        return components.size();
    }

    /**
     * 
     * @return
     */
    public boolean IsDefined ()
    {
        return this.id >= 0 && this.max > 0 && this.time >= 0;
    }

    public Vector<Component> getComponents() {
        return components;
    }

    public void setComponents(Vector<Component> components) {
        this.components = components;
    }




 
   
}
