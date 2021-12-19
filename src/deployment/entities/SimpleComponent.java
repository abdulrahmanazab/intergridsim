/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deployment.entities;

/**
 *A component is part of a Service. 
 * 
 * @author nsa
 */
public class SimpleComponent extends Component{

 
   
    private int minResources_ = 20;
    private int maxResources_ = 50;

    @Override
    public boolean isDefined() {
      return this.minResources_>=0 && this.minResources_>=0 && this.maxResources_>= this.minResources_;
    }

    public SimpleComponent()
    {
        super();
    }
    public SimpleComponent( SimpleComponent c)
    {
        super((Component)c);
        this.maxResources_= c.maxResources_;
        this.minResources_=c.minResources_;
    }

    public void setMaxResources (int maxResources)
    {
        this.maxResources_ = maxResources;
    }

    public void setMinResources (int minResources)
    {
        this.minResources_ = minResources;
    }

    public int getMaxResourceNeeds ()
    {
        return maxResources_;
    }

    public int getMinResourceNeeds ()
    {
        return minResources_;
    }    
   
     public int getCurrentUse()
     {
         return this.maxResources_;
     }
    
}
