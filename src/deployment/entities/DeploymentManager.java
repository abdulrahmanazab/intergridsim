/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deployment.entities;

/**
 *This interface provides the operation required for object deployment
 * @author nsa
 */
public interface DeploymentManager {

    /**
     * 
     * @param c
     * @return
     */
    public boolean deployLocal (Component c);

    /**
     * 
     * @param c
     * @return
     */
    public boolean undeployLocal (Component c);

    /**
     * 
     * @param components
     * @return
     */
    public boolean deployGlobal (Component[] components);
}
