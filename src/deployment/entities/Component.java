/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deployment.entities;

/**
 *
 * @author Nejm
 */
public abstract class Component implements Identifiable {

    protected long id_ = -1;
    private int minPerDomain_ = 0;
    private int maxPerDomain_ = 0;

    public Component()
    {}
    public Component(Component c)
    {
        this.id_=c.id_;
        this.maxPerDomain_= c.maxPerDomain_;
        this.minPerDomain_= c.minPerDomain_;
    }
    public long getId() {
        return this.id_;
    }

    public void setId(long id) {
        this.id_ = id;
    }

    public int getMaxPerDomain() {
        return maxPerDomain_;
    }

    public void setMaxPerDomain(int maxPerDomain) {
        this.maxPerDomain_ = maxPerDomain;
    }

    public int getMinPerDomain() {
        return minPerDomain_;
    }

    public void setMinPerDomain(int minPerDomain) {
        this.minPerDomain_ = minPerDomain;
    }
    public abstract int getCurrentUse();
    public abstract int getMaxResourceNeeds();
    public abstract int getMinResourceNeeds();
    public abstract void setMaxResources (int maxResources);
    public abstract void setMinResources (int minResources);
    public abstract boolean isDefined();


}
