package test_support.entity.instance_name;

import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import javax.persistence.Column;
import javax.persistence.Entity;

@JmixEntity
@Entity
public class ChildOne extends Parent {

    @InstanceName
    @Column(name = "CHILD_NAME")
    private String childName;

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

}