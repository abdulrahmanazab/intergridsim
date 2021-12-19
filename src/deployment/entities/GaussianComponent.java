/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deployment.entities;
import deployment.math.*;

/**
 *
 * @author Nejm
 */
public class GaussianComponent extends Component {

    private int minResources_ = 20;
    private int maxResources_ = 50;

    @Override
    public int getCurrentUse() {
        int result = 0;
        double mean=0;
        double stdv=0;
        if (this.isDefined())
        {
            mean= ((double)this.maxResources_ +(double)this.minResources_ )/2;
            stdv= (this.maxResources_- mean)/3;
            result=(int) Probability.gaussian(mean, stdv);
        }

        return result;
    }

    @Override
    public int getMaxResourceNeeds() {
        return this.maxResources_;
    }

    @Override
    public int getMinResourceNeeds() {
        return this.minResources_;
    }

    @Override
    public void setMaxResources(int maxResources) {
        this.maxResources_ = maxResources;
    }

    @Override
    public void setMinResources(int minResources) {
        this.minResources_ = minResources;
    }
     @Override
    public boolean isDefined() {
      return this.minResources_>=0 && this.minResources_>=0 && this.maxResources_>= this.minResources_;
    }
}
