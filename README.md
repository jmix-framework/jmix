
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

