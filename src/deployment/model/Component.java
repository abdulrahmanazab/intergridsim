/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package deployment.model;
import deployment.math.*;

/**
 *
 * @author nsa
 */
public class Component extends Gaussian implements  IComponent{
  
    protected long id = -1;
   
    /**
     * Get the value of id_
     *
     * @return the value of id_
     */
    public long getId() {
        return this.id;
    }

    /**
     * Set the value of id_
     *
     * @param id_ new value of id_
     */
    public void setId(long i) {
        this.id = i;
    }
    
    
    public double simulateLoad()
    {
        return this.Realize();
    }
                         
    
    public void setMu(double m)
    {
        this.mu=m;
      
    }
    public void setSigma(double sig)
    {
        this.sigma=sig;
        
    }

    
    
    
}
