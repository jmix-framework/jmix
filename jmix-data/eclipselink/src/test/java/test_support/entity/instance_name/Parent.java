package test_support.entity.instance_name;

import io.jmix.core.metamodel.annotation.JmixEntity;
import javax.persistence.Column;
import javax.persistence.Entity;

@JmixEntity
@Entity
public class Parent extends BaseEntity {

    @Column(name = "NAME")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}