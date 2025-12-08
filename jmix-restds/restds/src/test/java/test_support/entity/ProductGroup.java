package test_support.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.Store;
import io.jmix.restds.annotation.RestDataStoreEntity;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@JmixEntity
@Store(name = "restService1")
@RestDataStoreEntity(remoteName = "common_ProductGroup")
public class ProductGroup {
    @JmixGeneratedValue
    @JmixId
    private UUID id;

    private Integer version;

    @InstanceName
    @NotNull
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

}