package test_support.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import java.math.BigDecimal;
import java.util.UUID;

@JmixEntity(name = "scr_CarDto")
public class CarDto {

    @JmixGeneratedValue
    @JmixId
    @JmixProperty(mandatory = true)
    private UUID id;

    @JmixProperty
    private String manufacturer;

    @JmixProperty
    private BigDecimal price;

    @JmixProperty
    private String model;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}