package test_support.entity.event_clearing;

import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.Composition;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.pessimisticlocking.PessimisticLock;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@PessimisticLock(timeoutSec = 10)
@JmixEntity
@Table(name = "DYNAT_PROJECT")
@Entity
public class Project {
    @JmixGeneratedValue
    @Column(name = "ID", nullable = false)
    @Id
    private UUID id;

    @Column(name = "TOTAL_ESTIMATED_EFFORTS")
    private Integer totalEstimatedEfforts;

    @Composition
    @OneToMany(mappedBy = "project")
    private List<Task> tasks;

    @InstanceName
    @Column(name = "NAME", nullable = false)
    @NotNull
    private String name;

    @DeletedBy
    @Column(name = "DELETED_BY")
    private String deletedBy;

    @DeletedDate
    @Column(name = "DELETED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deletedDate;

    public Integer getTotalEstimatedEfforts() {
        return totalEstimatedEfforts;
    }

    public void setTotalEstimatedEfforts(Integer totalEstimatedEfforts) {
        this.totalEstimatedEfforts = totalEstimatedEfforts;
    }

    public Date getDeletedDate() {
        return deletedDate;
    }

    public void setDeletedDate(Date deletedDate) {
        this.deletedDate = deletedDate;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}