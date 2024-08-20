package test_support.entity;

import io.jmix.core.MetadataTools;
import io.jmix.core.entity.annotation.JmixEmbedded;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.*;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

@Store(name = "restService1")
@JmixEntity
public class Customer {
    @JmixGeneratedValue
    @JmixId
    private UUID id;

    private Integer version;

    private String createdBy;

    private OffsetDateTime createdDate;

    private String lastModifiedBy;

    private OffsetDateTime lastModifiedDate;

    private String firstName;

    @NotNull
    private String lastName;

    private String email;

    private CustomerRegionDto region;

    @Composition(inverse = "customer")
    private Set<CustomerContact> contacts;

    @JmixEmbedded
    private CustomerAddress address;

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

    public CustomerRegionDto getRegion() {
        return region;
    }

    public void setRegion(CustomerRegionDto region) {
        this.region = region;
    }

    public Set<CustomerContact> getContacts() {
        return contacts;
    }

    public void setContacts(Set<CustomerContact> contacts) {
        this.contacts = contacts;
    }

    public CustomerAddress getAddress() {
        return address;
    }

    public void setAddress(CustomerAddress address) {
        this.address = address;
    }

        public OffsetDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(OffsetDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public OffsetDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(OffsetDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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