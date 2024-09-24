package test_support.entity;

import io.jmix.core.MetadataTools;
import io.jmix.core.entity.annotation.JmixEmbedded;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.*;
import io.jmix.restds.annotation.RestDataStoreEntity;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@Store(name = "restService2")
@RestDataStoreEntity(remoteName = "Customer")
@JmixEntity
public class Customer2 {
    @JmixGeneratedValue
    @JmixId
    private UUID id;

    private Integer version;

    private String firstName;

    @NotNull
    private String lastName;

    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
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

    @InstanceName
    @DependsOnProperties({"firstName", "lastName"})
    public String getInstanceName(MetadataTools metadataTools) {
        return String.format("%s %s",
                metadataTools.format(firstName),
                metadataTools.format(lastName));
    }
}