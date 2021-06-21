
# Multitenancy

- [Overview](#overview)
- [Setting](#setting)
- [Predefined Roles](#predefined-roles)
- [Managing Tenants](#managing-tenants)
- [Common and Tenant-Specific Data](#common-and-tenant-specific-data)
    - [Common Data](#common-data)
    - [Tenant-Specific Data](#tenant-specific-data)

# Overview

The component implements a single database multitenancy support for Jmix applications.

It enables using a single application instance to serve multiple tenants - groups of users that are invisible to each
other and do not share any data they have write access to.

The component supports two types of data:

* common data - shared across tenants. Tenants have read-only access to this type of data;
* tenant-specific data - not accessible to other tenants. Tenants have full access to this type of data.

All tenants have their own admin users which can create tenant users and assign tenant-specific roles and permissions.

# Setting

Tenant-specific entity must have an additional attribute which have @TenanId to specify the owner of the data. Note,
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

Entities from CUBA compatibility modules also have special attributes for multitenancy support

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

Tenants are created and managed by global admins - users that don't belong to any tenant.

Each tenant must have a unique *Tenant Id* and a default administrator assigned.

# Tenant Permissions

Tenant permissions are compiled at runtime during the user logs in and stored in the user session. For implementation,
see `MultiTenancyAttributeConstraint` and `MultiTenancyNonTenantEntityConstraint`.

If a user has read-only access to an entity, so the user can't permit other users to modify it, but can prohibit users
to read the entity.

**Specific** and **UI** permissions have been hidden from tenants.

# Login
You can create users with equal logins for different tenants.
For each user which belongs to a tenant will be added suffix with tenant id into the username.
For example, you will create a user with username - `user1` also it has tenant id - `tenant1`. It means that the user will be saved with username as `tenant1\user1`.

In order to login into an application you can choose one approach for login:
1. You can use the URL parameter when you open the login screen. The parameter name defined by `tenantIdUrlParamName` application property. Now, you can use a username without a tenant suffix. Using the example above you can log in as `user1`.
2. You can log in with a full username without a URL parameter. For that, you should use a username with a tenant id. For the example above that, we should use `tenant1\user1` for login.

# Common and Tenant-Specific Data

## Common Data

Tenants have read-only access to all persistent entities that don't have the attribute with @TenantId.

## Tenant-Specific Data

To be tenant-specific, an entity must have the attribute with @TenantId.

Every time a tenant user reads tenant-specific data, the system adds **where** condition on `TENANT_ID` to JPQL query in
order to read the data of the current tenant only. Data with no `TENANT_ID` or with different `TENANT_ID` will be
omitted.

**There is no automatic filtering for native SQL, so tenants should not have access to any functionality that provides
access to writing native SQL or Groovy code (JMX Console, SQL/Groovy bands in reports etc.)**.

# Setting empty Jmix project for using multitenancy

This is a sample multitenancy project. It creates from a single-module Jmix project if you want to create the same
project you can use the following instructions.

Steps for getting multitenancy project from empty Jmix project:

1. Add multitenancy-starter and multitenancy-ui-starter in build.gradle

```groovy
    implementation 'io.jmix.multitenancy:jmix-multitenancy-starter'
    implementation 'io.jmix.multitenancy:jmix-multitenancy-ui-starter'
```

2. Add attribute in UserEntity which will be tenant Id, it must have String type and annotation TenantId. For example

```java
    @TenantId
    @Column(name = "TENANT_ATTRIBUTE")
    protected String tenantAttribute;
```

3. Implement interface TenantSupport in UserEntity. Method implementation from interface must return value of attribute
   marked TenantId annotation

```java
    @Override
    public String getTenantId(){
        return tenantAttribute;
    }
```

4. Add method in UserEdit class

```java
    @Subscribe("tenantIdField")
    public void onTenantIdFieldValueChange(HasValue.ValueChangeEvent<String> event) {
        usernameField.setValue(multitenancyUsernameSupport.getMultitenancyUsername(usernameField.getValue(), event.getValue()));
    }
```

5. Add following code in LoginScreen class

```java
    @Autowired
    private MultitenancyUsernameSupport multitenancyUsernameSupport;

    @Autowired
    private UrlRouting urlRouting;
```

6. Add code into 'login' method from LoginScreen class before try-catch block

```java
    username = multitenancyUsernameSupport.getMultitenancyUsername(username, urlRouting.getState().getParams());
```

7. Add combobox for tenant in user-edit.xml

```xml
    <comboBox id="tenantIdField" property="tenantAttribute"/>
```

8. Add code in UserEdit class

```java
    @Autowired
    private ComboBox<String> tenantIdField;
    @Autowired
    private DataManager dataManager;
    @Subscribe
    public void onInit(InitEvent event){
        tenantIdField.setOptionsList(dataManager.load(Tenant.class)
        .query("select t from mten_Tenant t")
        .list()
        .stream()
        .map(Tenant::getTenantId)
        .collect(Collectors.toList()));
    }
```

9. Add column into table in user-browse.xml

```xml
    <column id="tenantAttribute"/>
```

Now you can run your application, create tenant-specific entities, create roles for the entities, assignment roles for
users and other.