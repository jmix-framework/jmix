
# Multitenancy

- [Overview](#overview)
- [Setting](#setting)
- [Predefined Roles](#predefined-roles)
- [Managing Tenants](#managing-tenants)
- [Authentication](#authentication)
- [Common and Tenant-Specific Data](#common-and-tenant-specific-data)
    - [Common Data](#common-data)
    - [Tenant-Specific Data](#tenant-specific-data)
- [Usage](#usage)    

# Overview

The component implements a single database multitenancy support for Jmix applications.

It enables using a single application instance to serve multiple tenants - groups of users that are invisible to each
other and do not share any data they have write access to.

The component supports two types of data:

* common data - shared across tenants. Tenants have read-only access to this type of data;
* tenant-specific data - not accessible to other tenants. Tenants have full access to this type of data.

All tenants have their own admin users which can create tenant users and assign tenant-specific roles and permissions.

# Setting

Tenant-specific entity must have an additional attribute with `@TenantId` annotation to specify the owner of the data. Note,
that the following standard Jmix entities already have an additional column `SYS_TENANT_ID` to support multitenancy:

* EntityLogItem
* SendingMessage
* SendingAttachment
* Report
* ReportGroup
* ResourceRoleEntity
* RowLevelRoleEntity
* FilterConfiguration
* UiTablePresentation

Entities from CUBA compatibility modules with multitenancy support:

* FileDescriptor
* Folder
* ScheduledExecution
* ScheduledTask
* FilterEntity

**Note**. Tenants don't have write access to entities without the attribute with @TenantId. It is also true for Jmix
system entities.

# Predefined Roles

- **tenant-admin-role** - allows user to configure tenants.

# Managing Tenants

To manage tenants go to the *Tenant management -> Tenants* screen.

Global admins create and manage tenants - users that don't belong to any tenant.

Each tenant must have a unique *Tenant Id* and a default administrator assigned.

# Authentication
You can create users with equal usernames for different tenants.
For each user which belongs to a tenant will be added suffix with tenant id into the username.
For example, you will create a user with username - `user1` also it has tenant id - `tenant1`. It means that the user will be saved with username as `tenant1\user1`.

In order to authenticate into an application you can choose one approach for login:
1. You can use the URL parameter when you open the login screen. To enable this option add parameter `jmix.multitenancy.tenantIdUrlParamName` to the application properties file.
   Then add the tenantId parameter to the URL when log in, for example: `http://localhos:8080/#login?tenantId=some_tenant`
   Now, you can use a username without a tenant suffix. Using the example above you can log in as `user1`.
2. You can log in with a full username without a URL parameter. For that, you should use a username with a tenant id. For the example above that, we should use `tenant1\user1` for login.

# Common and Tenant-Specific Data

## Common Data

Tenants have read-only access to all persistent entities that don't have the attribute with @TenantId.

## Tenant-Specific Data

To be tenant-specific, an entity must have the attribute with `@TenantId` annotation.

Every time a tenant user reads tenant-specific data, the system adds **where** condition on `TENANT_ID` to JPQL query in
order to read the data of the current tenant only. Data with no `TENANT_ID` or with different `TENANT_ID` will be
omitted.

**There is no automatic filtering for native SQL, so tenants should not have access to any functionality that provides
access to writing native SQL or Groovy code (JMX Console, SQL/Groovy bands in the Reports etc.)**.

# Usage

1. Add `multitenancy-starter` and `multitenancy-ui-starter` in `build.gradle`.

```groovy
    implementation 'io.jmix.multitenancy:jmix-multitenancy-starter'
    implementation 'io.jmix.multitenancy:jmix-multitenancy-ui-starter'
```

2. Add attribute in `User` entity which will be tenant entity, it must have String type and annotation `@TenantId`. 

```java
    @TenantId
    @Column(name = "TENANT_ID")
    protected String tenantId;
```

3. Implement interface `io.jmix.multitenancy.core.AcceptsTenant` in `User` entity. Method implementation from interface must return value of attribute
   marked with `@TenantId` annotation.

```java
    @Override
    public String getTenantId(){
        return tenantId;
    }
```
4. Add following code in `LoginScreen` class.

```java
    @Autowired
    private MultitenancyUiSupport multitenancyUiSupport;

    @Autowired
    private UrlRouting urlRouting;
```

5. Add code into 'login' method from `LoginScreen` class before try-catch block.

```java
    username = multitenancyUiSupport.getUsernameByUrl(username, urlRouting);
```
6. Add column into the table in `user-browse.xml`

```xml
    <column id="tenantId"/>
```

7. Add combobox for tenant in `user-edit.xml`

```xml
    <comboBox id="tenantIdField" property="tenantId"/>
```

8. Add code in `UserEdit` class

```java
    @Autowired
    private ComboBox<String> tenantIdField;

    @Autowired
    private MultitenancyUiSupport multitenancyUiSupport;
    
    @Subscribe
    public void onInit(InitEvent event){
        tenantIdField.setOptionsList(multitenancyUiSupport.getTenantOptions());
    }
    
    @Subscribe("tenantIdField")
    public void onTenantIdFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        usernameField.setValue(multitenancyUiSupport.getUsernameByTenant(usernameField.getValue(), event.getValue()));
    }
```

Now you can run your application, create tenant-specific entities, create roles for the entities, assign roles to
users and other.