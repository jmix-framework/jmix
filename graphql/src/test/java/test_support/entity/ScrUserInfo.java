package test_support.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.List;
import java.util.UUID;


/**
 * Non-persistent user info with additional fields for using in REST.
 */
@JmixEntity(name = "ScrUserInfo")
public class ScrUserInfo {


    @JmixGeneratedValue
    @Id
    @Column(name = "ID", nullable = false)
    private UUID id;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @JmixProperty
    String firstName;

    @JmixProperty
    String lastName;

    @JmixProperty
    List<Car> favouriteCars;

    public ScrUserInfo(String firstName, String lastName, List<Car> favouriteCars) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.favouriteCars = favouriteCars;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<Car> getFavouriteCars() {
        return favouriteCars;
    }

    public void setFavouriteCars(List<Car> favouriteCars) {
        this.favouriteCars = favouriteCars;
    }
}
