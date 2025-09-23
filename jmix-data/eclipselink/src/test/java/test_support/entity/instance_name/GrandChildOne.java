package test_support.entity.instance_name;

import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import javax.persistence.Column;
import javax.persistence.Entity;

@JmixEntity
@Entity
public class GrandChildOne extends ChildOne {

    @InstanceName
    @Column(name = "GRAND_CHILD_NAME")
    private String grandChildName;

    public String getGrandChildName() {
        return grandChildName;
    }

    public void setGrandChildName(String grandChildName) {
        this.grandChildName = grandChildName;
    }

}