package io.jmix.quartz.model;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.lang.Nullable;

import java.util.Date;
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

    private String misfireInstructionId;

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

    @Nullable
    public String getMisfireInstructionId() {
        return misfireInstructionId;
    }

    public void setMisfireInstructionId(String misfireInstructionId) {
        this.misfireInstructionId = misfireInstructionId;
    }

}
