package test_support.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Table(name = "SCR_GARAGE")
@JmixEntity
@Entity(name = "scr$Garage")
public class Garage {

    @JmixGeneratedValue
    @Id
    @Column(name = "ID", nullable = false)
    private UUID id;

    @OneToMany(mappedBy = "garage")
    private List<Car> cars;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Column(name = "NAME", nullable = false)
    protected String name;

    @Column(name = "ADDRESS")
    protected String address;

    @JoinTable(name = "SCR_GARAGE_USER_LINK",
            joinColumns = @JoinColumn(name = "GARAGE_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "USER_ID", referencedColumnName = "ID"))
    @OnDelete(DeletePolicy.CASCADE)
    @ManyToMany
    protected List<User> personnel;

    @Column(name = "CAPACITY")
    protected Integer capacity;

    @Column(name = "VAN_ENTRY")
    protected Boolean vanEntry;

    @Column(name = "WORKING_HOURS_FROM")
    protected LocalTime workingHoursFrom;

    @Column(name = "WORKING_HOURS_TO")
    protected LocalTime workingHoursTo;

    @Transient
    @JmixProperty
    protected List<Car> currentCars;

    public List<Car> getCars() {
        return cars;
    }

    public void setCars(List<Car> cars) {
        this.cars = cars;
    }

    public List<Car> getCurrentCars() {
        return currentCars;
    }

    public void setCurrentCars(List<Car> currentCars) {
        this.currentCars = currentCars;
    }

    public LocalTime getWorkingHoursTo() {
        return workingHoursTo;
    }

    public void setWorkingHoursTo(LocalTime workingHoursTo) {
        this.workingHoursTo = workingHoursTo;
    }

    public LocalTime getWorkingHoursFrom() {
        return workingHoursFrom;
    }

    public void setWorkingHoursFrom(LocalTime workingHoursFrom) {
        this.workingHoursFrom = workingHoursFrom;
    }

    public Boolean getVanEntry() {
        return vanEntry;
    }

    public void setVanEntry(Boolean vanEntry) {
        this.vanEntry = vanEntry;
    }

    public List<User> getPersonnel() {
        return personnel;
    }

    public void setPersonnel(List<User> personnel) {
        this.personnel = personnel;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getCapacity() {
        return capacity;
    }


    @InstanceName
    @DependsOnProperties({"name", "address"})
    public String getInstanceName() {
        return String.format("%s %s", name, address);
    }
}