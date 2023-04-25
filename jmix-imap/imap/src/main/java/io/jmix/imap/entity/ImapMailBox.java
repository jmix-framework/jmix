/*
 * Copyright 2020 Haulmont.
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
 */

package io.jmix.imap.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.FileRef;
import io.jmix.core.annotation.DeletedBy;
import io.jmix.core.annotation.DeletedDate;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.Listeners;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.metamodel.annotation.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Listeners({"imap_MailboxListener"})
@Table(name = "IMAP_MAIL_BOX")
@Entity(name = "imap_MailBox")
@JmixEntity
public class ImapMailBox {
    @Id
    @Column(name = "ID")
    @JmixGeneratedValue
    protected UUID id;

    @Version
    @Column(name = "VERSION")
    protected Integer version;

    @CreatedDate
    @Column(name = "CREATE_TS")
    protected Date createTs;

    @CreatedBy
    @Column(name = "CREATED_BY", length = 50)
    protected String createdBy;

    @LastModifiedDate
    @Column(name = "UPDATE_TS")
    protected Date updateTs;

    @LastModifiedBy
    @Column(name = "UPDATED_BY", length = 50)
    protected String updatedBy;

    @DeletedDate
    @Column(name = "DELETE_TS")
    protected Date deleteTs;

    @DeletedBy
    @Column(name = "DELETED_BY", length = 50)
    protected String deletedBy;

    @NotNull
    @Column(name = "NAME", nullable = false)
    protected String name;

    @Column(name = "HOST", nullable = false)
    protected String host;

    @Column(name = "PORT", nullable = false)
    protected Integer port = 143;

    @Column(name = "SECURE_MODE")
    protected String secureMode;

    @Column(name = "ROOT_CERTIFICATE")
    protected FileRef rootCertificate;

    @Column(name = "AUTHENTICATION_METHOD", nullable = false)
    protected String authenticationMethod;

    @Composition
    @OnDelete(DeletePolicy.CASCADE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AUTHENTICATION_ID")
    protected ImapSimpleAuthentication authentication;

    @OnDelete(DeletePolicy.CASCADE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROXY_ID")
    protected ImapProxy proxy;

    @Column(name = "JMIX_FLAG")
    protected String jmixFlag = "jmix-imap";

    @Column(name = "TRASH_FOLDER_NAME")
    protected String trashFolderName;

    @Transient
    @JmixProperty
    protected ImapFolder trashFolder;

    @OrderBy("enabled DESC, name")
    @OnDelete(DeletePolicy.CASCADE)
    @Composition
    @OneToMany(mappedBy = "mailBox")
    protected List<ImapFolder> folders;

    @Column(name = "EVENTS_GENERATOR_CLASS")
    protected String eventsGeneratorClass;

    @NotNull
    @Column(name = "FLAGS_SUPPORTED", nullable = false)
    protected Boolean flagsSupported = false;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Date getCreateTs() {
        return createTs;
    }

    public void setCreateTs(Date createTs) {
        this.createTs = createTs;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getUpdateTs() {
        return updateTs;
    }

    public void setUpdateTs(Date updateTs) {
        this.updateTs = updateTs;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getDeleteTs() {
        return deleteTs;
    }

    public void setDeleteTs(Date deleteTs) {
        this.deleteTs = deleteTs;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setEventsGeneratorClass(String eventsGeneratorClass) {
        this.eventsGeneratorClass = eventsGeneratorClass;
    }

    public String getEventsGeneratorClass() {
        return eventsGeneratorClass;
    }

    public Boolean getFlagsSupported() {
        return flagsSupported;
    }

    public void setFlagsSupported(Boolean flagsSupported) {
        this.flagsSupported = flagsSupported;
    }

    public void setProxy(ImapProxy proxy) {
        this.proxy = proxy;
    }

    public ImapProxy getProxy() {
        return proxy;
    }

    public List<ImapFolder> getFolders() {
        return folders;
    }

    public List<ImapFolder> getProcessableFolders() {
        return folders.stream()
                .filter(f -> Boolean.TRUE.equals(f.getEnabled()) && !Boolean.TRUE.equals(f.getDeleted()))
                .collect(Collectors.toList());
    }

    public void setFolders(List<ImapFolder> folders) {
        this.folders = folders;
    }

    public ImapSimpleAuthentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(ImapSimpleAuthentication authentication) {
        this.authentication = authentication;
    }

    public void setJmixFlag(String jmixFlag) {
        this.jmixFlag = jmixFlag;
    }

    public String getJmixFlag() {
        return jmixFlag;
    }

    public void setTrashFolderName(String trashFolderName) {
        this.trashFolderName = trashFolderName;
    }

    public String getTrashFolderName() {
        return trashFolderName;
    }

    public ImapFolder getTrashFolder() {
        return trashFolder;
    }

    public void setTrashFolder(ImapFolder trashFolder) {
        this.trashFolder = trashFolder;
        setTrashFolderName(trashFolder != null ? trashFolder.getName() : null);
    }

    public ImapAuthenticationMethod getAuthenticationMethod() {
        return authenticationMethod == null ? null : ImapAuthenticationMethod.fromId(authenticationMethod);
    }

    public void setAuthenticationMethod(ImapAuthenticationMethod authenticationMethod) {
        this.authenticationMethod = authenticationMethod == null ? null : authenticationMethod.getId();
    }

    public ImapSecureMode getSecureMode() {
        return secureMode == null ? null : ImapSecureMode.fromId(secureMode);
    }

    public void setSecureMode(ImapSecureMode secureMode) {
        this.secureMode = secureMode == null ? null : secureMode.getId();
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setRootCertificate(FileRef rootCertificate) {
        this.rootCertificate = rootCertificate;
    }

    public FileRef getRootCertificate() {
        return rootCertificate;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    @InstanceName
    @DependsOnProperties({"host", "port", "name"})
    public String getInstanceName() {
        return String.format("%s:%s (%s)", host, port, name);
    }
}