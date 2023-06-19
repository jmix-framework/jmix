package io.jmix.quartz.model;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.lang.Nullable;

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

    private ScheduleType scheduleType;

    private Date startDate;

    private Date endDate;

    private Date lastFireDate;

    private Date nextFireDate;

    private String cronExpression;

    @Min(-1)
    private Integer repeatCount;

    @Positive
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

    @Nullable
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @Nullable
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Nullable
    public Date getLastFireDate() {
        return lastFireDate;
    }

    public void setLastFireDate(Date lastFireDate) {
        this.lastFireDate = lastFireDate;
    }

    @Nullable
    public Date getNextFireDate() {
        return nextFireDate;
    }

    public void setNextFireDate(Date nextFireDate) {
        this.nextFireDate = nextFireDate;
    }

    @Nullable
    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    @Nullable
    public Integer getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(Integer repeatCount) {
        this.repeatCount = repeatCount;
    }

    @Nullable
    public Long getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(Long repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    @SuppressWarnings("ConstantConditions")
    @Transient
    @JmixProperty
    @Nullable
    public String getScheduleDescription() {
        if (getScheduleType() == null) {
            return null;
        }

        if (getScheduleType() == ScheduleType.CRON_EXPRESSION) {
            return cronExpression;
        }

        if (Objects.isNull(repeatCount) || repeatCount < 0) {
            return String.format("Execute forever every %s seconds", repeatInterval / 1000);
        } else if (repeatCount == 0) {
            return "Execute once";
        } else {
            return String.format("Execute %s times every %s seconds", repeatCount + 1, repeatInterval / 1000);
        }
    }

}
