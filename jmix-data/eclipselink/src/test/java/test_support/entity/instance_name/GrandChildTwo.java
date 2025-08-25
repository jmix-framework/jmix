package test_support.entity.instance_name;

import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@JmixEntity
@Entity
public class GrandChildTwo extends ChildTwo {
    @Column(name = "GRAND_CHILD_CODE")
    private String grandChildCode;

    @Column(name = "NUMBER_")
    private Long number;

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public String getGrandChildCode() {
        return grandChildCode;
    }

    public void setGrandChildCode(String grandChildCode) {
        this.grandChildCode = grandChildCode;
    }

    @InstanceName
    @DependsOnProperties({"grandChildCode", "number"})
    private String getInstanceName() {
        return "[GrandChildTwo-" + this.grandChildCode + "-" + this.number + "]";
    }

}