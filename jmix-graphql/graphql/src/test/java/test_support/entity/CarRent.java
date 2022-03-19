package test_support.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Table(name = "SCR_CAR_RENT")
@JmixEntity
@Entity(name = "scr$CarRent")
public class CarRent {

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CAR_ID")
    protected Car car;

    @Temporal(TemporalType.DATE)
    @Column(name = "FROM_DATE")
    protected Date fromDate;

    @Temporal(TemporalType.TIME)
    @Column(name = "FROM_TIME")
    protected Date fromTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FROM_DATE_TIME")
    protected Date fromDateTime;

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromTime(Date fromTime) {
        this.fromTime = fromTime;
    }

    public Date getFromTime() {
        return fromTime;
    }

    public void setFromDateTime(Date fromDateTime) {
        this.fromDateTime = fromDateTime;
    }

    public Date getFromDateTime() {
        return fromDateTime;
    }


    public void setCar(Car car) {
        this.car = car;
    }

    public Car getCar() {
        return car;
    }


}