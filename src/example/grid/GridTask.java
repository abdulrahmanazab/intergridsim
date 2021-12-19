/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package example.grid;

/**
 *
 * @author Azab
 */
public class GridTask {

    protected long cpuCycles = 0;
    protected double memoryUsage = 0;
    
    public long getCPUCycles()
    {
    return cpuCycles;
    }
    
    public double getMemoryUsage()
    {
    return memoryUsage;
    }
    
    public void setCPUCycles(long newValue)
    {
     cpuCycles = newValue;
    }
    
    public void setMemoryUsage(double newValue)
    {
     memoryUsage = newValue;
    }
    
    public String generateRandomLoad()
    {
    return "";
    }
}
