/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package deployment.entities;

/**
 *
 * @author nsa
 */
public interface ILoadManager {
   /**
    * 
    * @return
    */
   public WorkLoad getCurrentLoad();
   
   /**
    * 
    * @param l
    */
   public void updateLoad ( double resources);
   

}
