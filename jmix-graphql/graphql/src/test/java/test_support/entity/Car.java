package test_support.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.validation.group.RestApiChecks;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import test_support.entity.constraints.PurchasedAfterManufactured;
import test_support.entity.constraints.ReliabilityPolicyCompliant;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@PurchasedAfterManufactured
@ReliabilityPolicyCompliant
@Table(name = "SCR_CAR")
@JmixEntity
@Entity(name = "scr$Car")
public class Car {

    @JmixGeneratedValue
    @Id
    @Column(name = "ID", nullable = false)
    private UUID id;

    @NotNull(message = "{msg://com.company.scr.entity/manufacturerEmpty}", groups = {RestApiChecks.class})
    @Column(name = "MANUFACTURER", nullable = false)
    protected String manufacturer;

    @Column(name = "MODEL")
    protected String model;

    @Size(min = 0, max = 5)
    @Pattern(regexp = "[a-zA-Z]{2}\\d{3}")
    @Column(name = "REG_NUMBER", length = 5)
    protected String regNumber;

    @PastOrPresent
    @Temporal(TemporalType.DATE)
    @Column(name = "PURCHASE_DATE")
    protected Date purchaseDate;

    @PastOrPresent
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "MANUFACTURE_DATE")
    protected Date manufactureDate;

    @Column(name = "WHEEL_ON_RIGHT")
    protected Boolean wheelOnRight;

    @Column(name = "CAR_TYPE", nullable = false)
    protected String carType;

    @Column(name = "ECO_RANK")
    protected Integer ecoRank;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GARAGE_ID")
    protected Garage garage;

    @Digits(fraction = 0, integer = 10)
    @Column(name = "MAX_PASSENGERS")
    protected Integer maxPassengers;

    @Positive
    @Digits(fraction = 4, integer = 10)
    @Column(name = "PRICE")
    protected BigDecimal price;

    @PositiveOrZero
    @Digits(fraction = 0, integer = 10)
    @Column(name = "MILEAGE")
    protected Double mileage;

    @OnDelete(DeletePolicy.CASCADE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TECHNICAL_CERTIFICATE_ID")
    protected TechnicalCertificate technicalCertificate;

    @Column(name = "VERSION", nullable = false)
    @Version
    private Integer version;

    @CreatedBy
    @Column(name = "CREATED_BY")
    private String createdBy;

    @CreatedDate
    @Column(name = "CREATED_DATE")
    @Temporal(TemporalType.DATE)
    private Date createdDate;

    @LastModifiedBy
    @Column(name = "LAST_MODIFIED_BY")
    private String lastModifiedBy;

    @LastModifiedDate
    @Column(name = "LAST_MODIFIED_DATE")
    @Temporal(TemporalType.DATE)
    private Date lastModifiedDate;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }


    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
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

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "PHOTO_ID")
//    protected FileDescriptor photo;

    public Date getManufactureDate() {
        return manufactureDate;
    }

    public void setManufactureDate(Date manufactureDate) {
        this.manufactureDate = manufactureDate;
    }

//    public void setPhoto(FileDescriptor photo) {
//        this.photo = photo;
//    }
//
//    public FileDescriptor getPhoto() {
//        return photo;
//    }


    public void setTechnicalCertificate(TechnicalCertificate technicalCertificate) {
        this.technicalCertificate = technicalCertificate;
    }

    public TechnicalCertificate getTechnicalCertificate() {
        return technicalCertificate;
    }


    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setMileage(Double mileage) {
        this.mileage = mileage;
    }

    public Double getMileage() {
        return mileage;
    }


    public void setModel(String model) {
        this.model = model;
    }

    public String getModel() {
        return model;
    }


    public void setMaxPassengers(Integer maxPassengers) {
        this.maxPassengers = maxPassengers;
    }

    public Integer getMaxPassengers() {
        return maxPassengers;
    }


    public void setGarage(Garage garage) {
        this.garage = garage;
    }

    public Garage getGarage() {
        return garage;
    }


    public void setEcoRank(EcoRank ecoRank) {
        this.ecoRank = ecoRank == null ? null : ecoRank.getId();
    }

    public EcoRank getEcoRank() {
        return ecoRank == null ? null : EcoRank.fromId(ecoRank);
    }


    public void setCarType(CarType carType) {
        this.carType = carType == null ? null : carType.getId();
    }

    public CarType getCarType() {
        return carType == null ? null : CarType.fromId(carType);
    }


    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }


    public void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
    }

    public String getRegNumber() {
        return regNumber;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setWheelOnRight(Boolean wheelOnRight) {
        this.wheelOnRight = wheelOnRight;
    }

    public Boolean getWheelOnRight() {
        return wheelOnRight;
    }

    @InstanceName
    @DependsOnProperties({"manufacturer", "model"})
    public String getDisplayName() {
        return String.format("%s - %s", manufacturer, model);
    }

}
