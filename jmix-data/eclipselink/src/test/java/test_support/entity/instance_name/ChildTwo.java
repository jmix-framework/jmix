package test_support.entity.instance_name;

import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@JmixEntity
@Entity
public class ChildTwo extends Parent {
    @Column(name = "CHILD_CODE")
    private String childCode;

    public String getChildCode() {
        return childCode;
    }

    public void setChildCode(String childCode) {
        this.childCode = childCode;
    }

    @InstanceName
    @DependsOnProperties("childCode")
    private String getInstanceName() {
        return "[ChildTwo-" + this.childCode + "]";
    }
}