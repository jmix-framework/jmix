package io.jmix.quartz.model;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@JmixEntity(name = "quartz_TriggerModel")
public class TriggerModel {

    @JmixGeneratedValue
    @JmixId
    private UUID id;

    private String triggerName;

    private String triggerGroup;

    @NotNull
    private ScheduleType scheduleType;

    private Date startDate;

    private Date endDate;

    private Date lastFireDate;

    private Date nextFireDate;

    private String cronExpression;

    @Positive
    private Integer repeatCount;

    @Positive
    @NotNull
    private Long repeatInterval;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTriggerName() {
        return triggerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    public String getTriggerGroup() {
        return triggerGroup;
    }

    public void setTriggerGroup(String triggerGroup) {
        this.triggerGroup = triggerGroup;
    }

    public void setScheduleType(ScheduleType scheduleType) {
        this.scheduleType = scheduleType;
    }

    public ScheduleType getScheduleType() {
        return scheduleType;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getLastFireDate() {
        return lastFireDate;
    }

    public void setLastFireDate(Date lastFireDate) {
        this.lastFireDate = lastFireDate;
    }

    public Date getNextFireDate() {
        return nextFireDate;
    }

    public void setNextFireDate(Date nextFireDate) {
        this.nextFireDate = nextFireDate;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public Integer getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(Integer repeatCount) {
        this.repeatCount = repeatCount;
    }

    public Long getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(Long repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    @Transient
    @JmixProperty
    public String getScheduleDescription() {
        if (getScheduleType() == null) {
            return null;
        }

        if (getScheduleType() == ScheduleType.CRON_EXPRESSION) {
            return cronExpression;
        }

        if (Objects.nonNull(repeatCount) && repeatCount > 0) {
            return String.format("Repeat %s times every %s seconds", repeatCount, repeatInterval / 1000);
        } else {
            return String.format("Repeat forever every %s seconds", repeatInterval / 1000);
        }
    }

}
