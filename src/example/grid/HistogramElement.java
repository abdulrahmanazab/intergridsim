/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package example.grid;

/**
 *
 * @author 2906095
 */
public class HistogramElement {
    
    public int value = 0;
    public int weight =0;
    
    public HistogramElement(int value,  int weight){
        this.value = value;
        this.weight = weight;
        
    }
    
    @Override
    public HistogramElement clone(){
        return new HistogramElement(this.value, this.weight);
    }

}
