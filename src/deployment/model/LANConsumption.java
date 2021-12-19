/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deployment.model;

import java.text.*;
import java.util.*;

/**
 *
 * @author nsa
 */
public class LANConsumption {

    private Vector<NodeConsumption> nodes = new Vector<NodeConsumption>();
    
    /**
     * ID of the node 
     * @param ID
     * @return a Copy of the workLoad (not a reference)
     */
    public NodeConsumption getConsumption (long ID)
    {
        NodeConsumption result = null;
        for (int i = 0; i < nodes.size(); i++)
        {
            if (nodes.elementAt(i).getId() == ID)
            {
                result = new NodeConsumption();
                result.Copy(nodes.elementAt(i));
                break;
            }
        }
        return result;
    }

    /**
     * 
     * @param l WorkLoad that will replace the current one (not by reference)
     * @param index index in the array
     * @return true if replaced, false other wise
     */
    public boolean setConsumption (NodeConsumption consum)
    {
        if (consum == null)
        {
            return false;
        }
        NodeConsumption c = new NodeConsumption();
        c.Copy(consum);
        boolean result = false;
        int index = -1;
        for (int i = 0; i < nodes.size(); i++)
        {
            if (nodes.elementAt(i).getId() == c.id)
            {
                index = i;
                break;
            }
        }
        if (index >= 0)
        {
            if (c.IsDefined())
            {

                nodes.set(index, c);
                return true;
            }
        } else
        {
            if (c.IsDefined())
            {
                nodes.add(c);
                return true;
            }
        }

        return result;
    }

    public void print ()
    {
        System.out.println("LAN data : ");
        for (int i = 0; i < nodes.size(); i++)
        {
            System.out.println(nodes.elementAt(i).toString());
        }
    }
    //Fills a histogram about the resouce used at the LAN.
    public void makeHistogram (Histogram result)
    {
        if (result != null)
        {
            Vector<Double> data = new Vector<Double>();
            int totalSimulations = 10000;
            //Find out the total distribution
            for (int i = 0; i < totalSimulations; i++)
            {
                for (int j = 0; j < nodes.size(); j++)
                {
                    data.add(nodes.elementAt(j).Realize());
                }
            }
            //Build from data               
            result.Build(data, totalSimulations);
        }
    }

    public static void Copy (LANConsumption from, LANConsumption to)
    {
        if (from != null)
        {
            if (to.nodes == null)
            {
                to.nodes = new Vector<NodeConsumption>();
            }
            to.nodes.clear();
            for (int i = 0; i < from.nodes.size(); i++)
            {
                to.setConsumption(from.nodes.elementAt(i));
            }
        }
    }

    public static LANConsumption merge (LANConsumption a, LANConsumption b)
    {
        Vector<NodeConsumption> temp = merge(a.nodes, b.nodes);
        LANConsumption result = new LANConsumption();
        result.nodes = temp;
        return result;
    }

    private static Vector<NodeConsumption> merge (Vector<NodeConsumption> a, Vector<NodeConsumption> b)
    {
        Vector<NodeConsumption> result = new Vector<NodeConsumption>();
        if (a != null && b == null)
        {
            return a;
        }
        if (a == null && b != null)
        {
            return b;
        }
        if (a == null && b == null)
        {
            return null;
        }
        if (a != null && b != null)
        {
            //do merge
            //handle a
            for (int i = 0; i < a.size(); i++)
            {
                NodeConsumption temp = new NodeConsumption();
                temp.Copy(a.elementAt(i));
                result.add(temp);
            }
            // handle b
            for (int i = 0; i < b.size(); i++)
            {
                put(b.elementAt(i), result);
            }

        }
        return result;
    }

    /**
     * 
     * @param c
     * @param v
     */
    private static void put (NodeConsumption c, Vector<NodeConsumption> v)
    {
        if (v != null)
        {
            NodeConsumption temp = new NodeConsumption();
            temp.Copy(c);
            boolean found = false;
            for (int i = 0; i < v.size(); i++)
            {
                if (c.id == v.elementAt(i).getId())
                {
                    //Replace
                    if (c.getTime() >= v.elementAt(i).getTime())
                    {

                        v.set(i, temp);
                    }
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                v.add(temp);
            }
        }
    }

    public Vector<NodeConsumption> getNodesConsumptions() {
        return nodes;
    }

    public void setNodesConsumptions(Vector<NodeConsumption> nodes) {
        this.nodes = nodes;
    }


}
