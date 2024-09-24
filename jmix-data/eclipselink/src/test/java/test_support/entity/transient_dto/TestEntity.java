package test_support.entity.transient_dto;

import io.jmix.core.EntityStates;
import io.jmix.core.Metadata;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@JmixEntity
@Table(name = "DTOE_TEST_ENTITY")
@Entity(name = "dtoe_TestEntity")
public class TestEntity {

    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;


    @InstanceName
    @Column(name = "NAME")
    private String name;


    @Column(name = "BOOL_VAR", nullable = false)
    private Boolean boolVar = false;

    @JmixProperty
    private transient TestDto testDto;

    @JmixProperty
    @DependsOnProperties({"boolVar"})
    private transient TestDto someCalculatedDto1;

    @JmixProperty
    @DependsOnProperties({"boolVar"})
    private transient TestDto someCalculatedDto2;

    @JmixProperty
    @DependsOnProperties({"boolVar"})
    public final TestDto getTestDtoForGrid() {
        return Boolean.TRUE.equals(boolVar) ? this.testDto : null;
    }


    public final UUID getId() {
        return this.id;
    }

    public final void setId(UUID var1) {
        this.id = var1;
    }


    public final String getName() {
        return this.name;
    }

    public final void setName(String var1) {
        this.name = var1;
    }


    public final Boolean getBoolVar() {
        return this.boolVar;
    }

    public final void setBoolVar(Boolean var1) {
        this.boolVar = var1;
    }


    public final TestDto getTestDto() {
        return this.testDto;
    }

    public final void setTestDto(TestDto var1) {
        this.testDto = var1;
    }


    public TestDto getSomeCalculatedDto1() {
        return someCalculatedDto1;
    }

    public void setSomeCalculatedDto1(TestDto someCalculatedDto1) {
        this.someCalculatedDto1 = someCalculatedDto1;
    }

    public TestDto getSomeCalculatedDto2() {
        return someCalculatedDto2;
    }

    public void setSomeCalculatedDto2(TestDto someCalculatedDto2) {
        this.someCalculatedDto2 = someCalculatedDto2;
    }

    @PostConstruct
    public void postConstruct(Metadata metadata, EntityStates entityStates) {
        if (!entityStates.isLoaded(this, "boolVar")) {
            return;
        }

        someCalculatedDto1 = metadata.create(TestDto.class);
        someCalculatedDto2 = metadata.create(TestDto.class);

        if (Boolean.TRUE.equals(boolVar)) {
            someCalculatedDto1.setName("main");
            someCalculatedDto2.setName("secondary");
        } else {
            someCalculatedDto1.setName("empty");
            someCalculatedDto2.setName("empty");
        }
    }
}
