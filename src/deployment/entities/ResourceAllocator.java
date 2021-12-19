/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package deployment.entities;
import peersim.core.*;
/**
 *
 * @author nsa
 */
public interface ResourceAllocator extends Identifiable{
    /**
     * 
     * @return
     */
    public NodeKnowledge getKnowledge();
    /**
     * 
     * @param networkLoad
     */
    public void setKnowledge (NodeKnowledge knowledge);
    /**
     * 
     */
    public void shareKnowledge(Node node, int protocolID);
    /**
     * 
     * @param isDomainController
     */
    public void setIsDomainController(boolean isDomainController);
    /**
     * 
     * @return
     */
    public boolean getIsDomainController();
    /**
     * 
     */
    public void print();

}
