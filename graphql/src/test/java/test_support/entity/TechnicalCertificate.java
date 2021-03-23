package test_support.entity;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;

import javax.persistence.*;
import java.util.UUID;

@Table(name = "SCR_TECHNICAL_CERTIFICATE")
@JmixEntity
@Entity(name = "scr$TechnicalCertificate")
public class TechnicalCertificate {

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

    @InstanceName
    @Column(name = "CERT_NUMBER")
    protected String certNumber;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "technicalCertificate")
    protected Car car;

    public void setCertNumber(String certNumber) {
        this.certNumber = certNumber;
    }

    public String getCertNumber() {
        return certNumber;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public Car getCar() {
        return car;
    }


}