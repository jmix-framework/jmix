package test_support.entity.transient_dto;


import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import java.util.UUID;

@JmixEntity(name = "dtoe_TestDto")
public class TestDto {

    @JmixGeneratedValue
    @JmixId
    private UUID id;

    @InstanceName
    private String name = "test";


    public final UUID getId() {
        return this.id;
    }

    public final void setId(UUID id) {
        this.id = id;
    }

    public final String getName() {
        return this.name;
    }

    public final void setName(String name) {
        this.name = name;
    }

}
