/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deployment.model;

import java.util.*;

/**
 *
 * @author nsa
 */
public class WANConsumption {

    public Vector<Histogram> histograms = new Vector<Histogram>();

    public Histogram getHistogram (long ID)
    {
        Histogram result = new Histogram();
        for (int i = 0; i < histograms.size(); i++)
        {
            if (histograms.elementAt(i).getId() == ID)
            {
                result.Copy(histograms.elementAt(i));
                break;
            }
        }
        return result;
    }
     public boolean setHistogram (Histogram hist)
    {
         if(hist==null)
         {
             return false;
         }
        Histogram h=new Histogram();
        h.Copy(hist);
        boolean result = false;
        int index = -1;
        for (int i = 0; i < histograms.size(); i++)
        {
            if (histograms.elementAt(i).getId() == h.id)
            {
                index = i;
                break;
            }
        }
        if (index >= 0)
        {
            if (h.IsDefined())
            {
              
                histograms.set(index,h);
                return true;
            }
        } else
        {
            if (h.IsDefined())
            {
                histograms.add(h);
                return true;
            }
        }

        return result;
    }
    
    public static void Copy (WANConsumption from, WANConsumption to)
    {
        if (from != null)
        {
            if (to.histograms == null)
            {
                to.histograms = new Vector<Histogram>();
            }
            to.histograms.clear();
            for (int i = 0; i < from.histograms.size(); i++)
            {
                Histogram temp = new Histogram();
                temp.Copy(from.histograms.elementAt(i));
                to.histograms.add(temp);

            }
        }
    }

    public static WANConsumption merge (WANConsumption a, WANConsumption b)
    {
        Vector<Histogram> temp = merge(a.histograms, b.histograms);
        WANConsumption result = new WANConsumption();
        result.histograms = temp;
        return result;
    }

    private static Vector<Histogram> merge (Vector<Histogram> a, Vector<Histogram> b)
    {
        Vector<Histogram> result = new Vector<Histogram>();
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
                Histogram temp = new Histogram();
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

    private static void put (Histogram h, Vector<Histogram> vH)
    {
        if (vH != null)
        {
            Histogram temp = new Histogram();
            temp.Copy(h);
            boolean found = false;
            for (int i = 0; i < vH.size(); i++)
            {
                if (h.id == vH.elementAt(i).getId())
                {
                    //Replace
                    if (h.getTime() >= vH.elementAt(i).getTime())
                    {

                        vH.set(i, temp);
                    }
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                vH.add(temp);
            }
        }
    }

    public void print ()
    {
        System.out.println("WAN data : ");
        for (int i = 0; i < histograms.size(); i++)
        {
            System.out.println(histograms.elementAt(i).toString());
        }
    }
}
