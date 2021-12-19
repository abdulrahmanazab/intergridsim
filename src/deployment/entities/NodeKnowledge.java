/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deployment.entities;

/**
 *
 * @author nsa
 */
public class NodeKnowledge  {

    private WorkLoad[] loads_ = null;

    /*--------------------------------------------------------------------------
     Constructor
     --------------------------------------------------------------------------*/
    
    /**
     * Size of the network or part of the network 
     * @param size
     */
    public NodeKnowledge (int size)
    {
        loads_ = new WorkLoad[size];
        
        for (int i = 0; i < loads_.length; i++)
        {
            loads_[i] = new WorkLoad();
        }
    }

    
    /**
     * 
     * @return
     */
    public WorkLoad[] getLoads ()
    {
        return loads_;
    }
    
   /**
     * 
     * @param loads_
     */
    public void setLoads (WorkLoad[] loads)
    {
        this.loads_ = loads;
    }
    /**
     * ID of the node 
     * @param ID
     * @return a Copy of the workLoad (not a reference)
     */
    public WorkLoad getLoad (long ID)
    {
        WorkLoad result = new WorkLoad();
        for (int i = 0; i < loads_.length; i++)
        {
            if (loads_[i].getId() == ID)
            {
                result.Copy(loads_[i]);
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
    public boolean setLoad (WorkLoad l, int index)
    {
        boolean result = false;
        if (index < this.loads_.length && l.id_>=0)
        {
            loads_[index].Copy(l);
            result = true;
        }
        return result;
    }

    /**
     * 
     * @param l 
     * load to update filtered by l.getId()
     * @param considerTime
     * true: means that update will occure only if l.getTime > than existing
     * false: will disgard time 
     * @return
     * true if changed
     * false if no change happened or id does not exist or loads is null
     */
    public boolean update (WorkLoad l, boolean considerTime)
    {
        long lId = l.getId();
        boolean response = false;
        if (this.loads_ != null && lId>=0)
        {
            for (int i = 0; i < this.loads_.length; i++)
            {
                if (lId == loads_[i].getId() && considerTime)
                {
                    if (l.getTime()>=0 && l.getTime() >=loads_[i].getTime())
                    {
                        loads_[i].Copy(l);
                        response = true;
                        break;
                    }
                }
                if (lId == loads_[i].getId() && !considerTime)
                {
                    loads_[i].Copy(l);
                    response = true;
                    break;
                }
            }
        }
        return response;
    }
    
    @Override
    public String toString ()
    {
        String s = "";
//        StringBuilder builder = new StringBuilder(s);
//        for (int i = 0; i < loads_.length; i++)
//        {
//            builder.append(loads_[i].toString() + " \n ");
//        }
//        return builder.toString();
        return s;

    }

    /**
     * 
     * @param a
     * @param b
     * @return
     */
    public static NodeKnowledge Merge (NodeKnowledge a, NodeKnowledge b)
    {
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
        if (a != null && b != null && b.loads_.length == a.loads_.length)
        {
            //do merge
            NodeKnowledge result = new NodeKnowledge(a.loads_.length);

            for (int i = 0; i < a.loads_.length; i++)
            {
                if (b.loads_[i].getTime() >= 0 && b.loads_[i].getTime() > a.loads_[i].getTime())
                {
                    result.loads_[i].Copy( b.loads_[i]);
                } else //if (a[i][1] >= 0 && a[i][1] >= b[i][1])
                {
                    result.loads_[i].Copy(a.loads_[i]);
                }
            }
            return result;
        }
        return null;
    }

    public void print ()
    {
        for (int i = 0; i < loads_.length; i++)
        {
            System.out.println(loads_[i].toString());
        }
    }
}
