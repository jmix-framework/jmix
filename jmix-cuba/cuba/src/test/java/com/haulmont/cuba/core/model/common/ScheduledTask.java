/*
 * Copyright (c) 2008-2016 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.haulmont.cuba.core.model.common;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.entity.Creatable;
import com.haulmont.cuba.core.entity.SoftDelete;
import com.haulmont.cuba.core.entity.Updatable;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;

/**
 * Entity that stores an information about a scheduled task.
 */
@Entity(name = "test$ScheduledTask")
@JmixEntity
@Table(name = "TEST_SCHEDULED_TASK")
@NamePattern("#name|beanName,methodName,className,scriptName")
@SystemLevel
public class ScheduledTask extends BaseUuidEntity implements Creatable, Updatable, SoftDelete {

    private static final long serialVersionUID = -2330884126746644884L;

    // ScheduledTask has no @Version field because it is locked pessimistically when processed.
    // Moreover unfortunately OpenJPA issues a lot of unnecessary "select version from ..." when loads versioned
    // objects with PESSIMISTIC lock type.

    @Column(name = "CREATE_TS")
    protected Date createTs;

    @Column(name = "CREATED_BY", length = 50)
    protected String createdBy;

    @Column(name = "UPDATE_TS")
    protected Date updateTs;

    @Column(name = "UPDATED_BY", length = 50)
    protected String updatedBy;

    @Column(name = "DELETE_TS")
    protected Date deleteTs;

    @Column(name = "DELETED_BY", length = 50)
    protected String deletedBy;

    @Column(name = "DEFINED_BY")
    protected String definedBy;

    @Column(name = "SYS_TENANT_ID")
    protected String sysTenantId;

    @Column(name = "BEAN_NAME")
    protected String beanName;

    @Column(name = "METHOD_NAME")
    protected String methodName;

    @Column(name = "CLASS_NAME")
    protected String className;

    @Column(name = "SCRIPT_NAME")
    protected String scriptName;

    @Column(name = "USER_NAME")
    protected String userName;

    @Column(name = "IS_SINGLETON")
    protected Boolean singleton;

    @Column(name = "IS_ACTIVE")
    protected Boolean active;

    @Column(name = "PERIOD_")
    protected Integer period;

    @Column(name = "TIMEOUT")
    protected Integer timeout;

    @Column(name = "START_DATE")
    protected Date startDate;

    @Column(name = "CRON")
    protected String cron;

    @Column(name = "SCHEDULING_TYPE")
    protected String schedulingType;

    @Column(name = "TIME_FRAME")
    protected Integer timeFrame;

    @Column(name = "START_DELAY")
    protected Integer startDelay;

    @Column(name = "PERMITTED_SERVERS")
    protected String permittedServers;

    @Column(name = "LOG_START")
    protected Boolean logStart;

    @Column(name = "LOG_FINISH")
    protected Boolean logFinish;

    @Column(name = "LAST_START_TIME")
    protected Date lastStartTime;

    @Column(name = "LAST_START_SERVER")
    protected String lastStartServer;

    @Column(name = "METHOD_PARAMS")
    protected String methodParamsXml;

    @Column(name = "DESCRIPTION", length = 1000)
    protected String description;

    //the following field is part of private API, please do not use it
    @Transient
    protected volatile long currentStartTimestamp;

    @Override
    public Date getCreateTs() {
        return createTs;
    }

    @Override
    public void setCreateTs(Date createTs) {
        this.createTs = createTs;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public Date getUpdateTs() {
        return updateTs;
    }

    @Override
    public void setUpdateTs(Date updateTs) {
        this.updateTs = updateTs;
    }

    @Override
    public String getUpdatedBy() {
        return updatedBy;
    }

    @Override
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public Boolean isDeleted() {
        return deleteTs != null;
    }

    @Override
    public Date getDeleteTs() {
        return deleteTs;
    }

    @Override
    public void setDeleteTs(Date deleteTs) {
        this.deleteTs = deleteTs;
    }

    @Override
    public String getDeletedBy() {
        return deletedBy;
    }

    public String getSysTenantId() {
        return sysTenantId;
    }

    public void setSysTenantId(String sysTenantId) {
        this.sysTenantId = sysTenantId;
    }

    @Override
    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Boolean getSingleton() {
        return singleton;
    }

    public void setSingleton(Boolean singleton) {
        this.singleton = singleton;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Integer getTimeFrame() {
        return timeFrame;
    }

    public void setTimeFrame(Integer timeFrame) {
        this.timeFrame = timeFrame;
    }

    public Integer getStartDelay() {
        return startDelay;
    }

    public void setStartDelay(Integer startDelay) {
        this.startDelay = startDelay;
    }

    public String getPermittedServers() {
        return permittedServers;
    }

    public void setPermittedServers(String permittedServers) {
        this.permittedServers = permittedServers;
    }

    public Boolean getLogStart() {
        return logStart;
    }

    public void setLogStart(Boolean logStart) {
        this.logStart = logStart;
    }

    public Boolean getLogFinish() {
        return logFinish;
    }

    public void setLogFinish(Boolean logFinish) {
        this.logFinish = logFinish;
    }

    public Date getLastStartTime() {
        return lastStartTime;
    }

    public void setLastStartTime(Date lastStartTime) {
        this.lastStartTime = lastStartTime;
    }

    public long getLastStart() {
        return lastStartTime == null ? 0 : lastStartTime.getTime();
    }

    public String getLastStartServer() {
        return lastStartServer;
    }

    public void setLastStartServer(String lastStartServer) {
        this.lastStartServer = lastStartServer;
    }

    public ScheduledTaskDefinedBy getDefinedBy() {
        return ScheduledTaskDefinedBy.fromId(definedBy);
    }

    public void setDefinedBy(ScheduledTaskDefinedBy definedBy) {
        this.definedBy = ScheduledTaskDefinedBy.getId(definedBy);
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public String getMethodParamsXml() {
        return methodParamsXml;
    }

    public void setMethodParamsXml(String methodParamsXml) {
        this.methodParamsXml = methodParamsXml;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public SchedulingType getSchedulingType() {
        return SchedulingType.fromId(schedulingType);
    }

    public void setSchedulingType(SchedulingType schedulingType) {
        this.schedulingType = SchedulingType.getId(schedulingType);
    }

    public long getCurrentStartTimestamp() {
        return currentStartTimestamp;
    }

    public void setCurrentStartTimestamp(long currentStartTimestamp) {
        this.currentStartTimestamp = currentStartTimestamp;
    }

    @JmixProperty
    public String name() {
        if (beanName != null && methodName != null) {
            return beanName + "." + methodName;
        } else if (className != null) {
            return className;
        } else {
            return scriptName;
        }
    }

    @JmixProperty
    public String getMethodParametersString() {
        return "";
    }
}
