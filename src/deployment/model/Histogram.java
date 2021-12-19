/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deployment.model;

import java.util.*;
import java.text.*;

/**
 *
 * @author nsa
 */
public class Histogram implements Identifiable {

    private final DecimalFormat FORMAT = new DecimalFormat("#0.00");
    protected long id = -1;
    private int time = -1;
    private int maxSamples = 20;
    public double[][] histogram = null;

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

    public double[][] getHistogram ()
    {
        return histogram;
    }

    public void setHistogram (double[][] histo)
    {
        this.histogram = Arrays.copyOf(histo, histo.length);
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

    public void Copy (Histogram source)
    {
        this.id = source.id;
        this.time = source.time;
        if (source.histogram != null)
        {
            this.histogram = Arrays.copyOf(source.histogram, source.histogram.length);
        }
    }

    @Override
    public String toString ()
    {
        String s = "ID : " + this.id + " Time " + this.time + " \t  Histogram : ";
       


        if (this.histogram != null)
        {
             MyComparator mc = new MyComparator(); // instance of comparator
        Arrays.sort(histogram, mc); // sort 2D array using comparator to handle 2'nd dim
            String temp = "";
            for (int i = 0; i < this.histogram.length; i++)
            {
                temp += "[" + histogram[i][0] + " |" + this.FORMAT.format(histogram[i][1]) + "]";

            }
            s += temp;
        } else
        {
            s += "NULL ";
        }
        return s;
    }

    public boolean IsDefined ()
    {
        return this.id >= 0 && this.maxSamples > 0 && this.time >= 0;
    }

    public void Build (Vector<Double> data, int maxsamples)
    {
        if (data != null)
        {
            this.maxSamples = maxsamples;
            // Find out the value range
            double min, max;
            min = max = data.elementAt(0);
            for (int i = 0; i < data.size(); i++)
            {
                if (data.elementAt(i) < min)
                {
                    min = data.elementAt(i);
                }
                if (data.elementAt(i) > max)
                {
                    max = data.elementAt(i);
                }
            }
            double range = max - min;
            double[][] result;

            if (range < maxSamples)
            {
                result = new double[(int) range + 1][2];
            } else
            {
                result = new double[maxSamples][2];
            }

            //Fill the the histogram
            int total = 0;
            int cumulation = (int) (range + 1) / result.length;
            for (int i = 0; i < data.size(); i++)
            {

                int index = (int) (data.elementAt(i) - min) / cumulation;
                result[index][0] = (index * cumulation) + (int) min;
                result[index][1] += 1;
                total++;
            }
            // normalize the historgram. 
            for (int i = 0; i < result.length; i++)
            {
                result[i][1] /= total;
            }

            this.setHistogram(result);
        }

    }

    public class MyComparator implements Comparator {

        public int compare (Object o1, Object o2)
        {
           return comp((double[])o1,(double[])o2);
        }

        
        public int comp (double[] o1, double[] o2)
        {
            int d=0;
            if (o1[d] > o2[d])
            {
                return 1; // -1 for descending order    
            } else if (o1[d] < o2[d])
            {
                return -1; // 1 for descending order     
            } else
            {
                return 0;
            }
        }
    }
}




