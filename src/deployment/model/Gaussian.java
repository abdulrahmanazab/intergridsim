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
public class Gaussian {

   protected double mu=0;
   protected double sigma=0;

    public double getMu ()
    {
        return this.mu;
    }

    public double getSigma ()
    {
        return sigma;
    }            
    public double Realize()
    {
        return Probability.gaussian(mu, sigma);
    }
      public double getMax()
    {
        return mu+3*sigma;
    }
    public double getMin()
    {
        return mu-3*sigma;
    }
   
}
