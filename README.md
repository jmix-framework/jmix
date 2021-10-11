# Jmix LDAP

## Overview

The addon provides an easy way to enable LDAP authentication in Jmix applications. It also provides a means for user synchronization and obtaining authorities.

The addon uses the Spring Security LDAP and provides a ready-to-go security configuration that could be easily enabled by including the addon in the application. The process and main settings of the LDAP authentication are well-described in the [Spring documentation](https://docs.spring.io/spring-security/site/docs/current/reference/html5/#servlet-authentication-ldap), so this manual is primarily focused on features related to Jmix.

## Installation

To include LDAP functionality in your application, add the following dependency to your `build.gradle` file:

```java
implementation 'io.jmix.ldap:jmix-ldap-starter'
```

## Quick start

After adding the starter, you need to configure LDAP main properties. The following is an example of such configuration:

```text
jmix.ldap.urls = ldap://ldap.forumsys.com:389/
jmix.ldap.baseDn = dc=example,dc=com
jmix.ldap.managerDn = cn=read-only-admin,dc=example,dc=com
jmix.ldap.managerPassword = password
jmix.ldap.userSearchFilter = (uid={0})
jmix.ldap.defaultRoles = ui-minimal
```

You can find out more about the meaning of each property in the [Jmix LDAP properties](#jmix-ldap-properties) section.
`jmix.ldap.defaultRoles` contains a list of roles that will be assigned to every user authenticated in LDAP. It is needed since user without any roles will not be able to login to the application.

If you are going to set up integration with Active Directory, it is recommended to set the property `jmix.ldap.useActiveDirectoryConfiguration` = `true`, since the addon provides a separate Security Configuration special for this case. Find out more about usage with Active Directory [here](#usage-with-active-directory).

By default, the addon authenticate users from the application against LDAP. If a user is successfully authenticated in LDAP, but it does not have a `UserDetails` in the application, it will be automatically synchronized and `UserDetails` will be created in accordance with the corresponding LDAP entry. An alternative option is to not maintain users in the application at all, and to manage the users fully in LDAP. If you want to implement this scenario, see [the corresponding section](#in-memory-user-management).

Let's consider we want to implement the default scenario when users are maintained in the application.

After setting the main application properties related to LDAP, we need to describe the way we want users to be synchronized (for example, how the mapping of user attributes should be performed). In order to do this, declare the bean implementing `LdapUserDetailsSynchronizationStrategy` interface. The addon comes with a basic abstract implementation: `AbstractLdapUserDetailsSynchronizationStrategy`, so in a simple case the strategy can be declared by only specifying a concrete `User` class and implementing a simple mapping:

```java
@Component("sample_MyUserSynchronizationStrategy")
public class MyUserSynchronizationStrategy extends AbstractLdapUserDetailsSynchronizationStrategy<User> {
  @Override
  protected Class<User> getUserClass() {
    return User.class;
  }

  @Override
  protected void mapUserDetailsAttributes(User userDetails, DirContextOperations ctx) {
    userDetails.setFirstName(ctx.getStringAttribute("givenName"));
    userDetails.setLastName(ctx.getStringAttribute("sn"));
  }
}
```

Note that `AbstractLdapUserDetailsSynchronizationStrategy` also persists role assignments that was obtained during the role mapping flow. After each synchronization execution, role assignments are rewritten with the new ones. It is done in order to not preserve the obsolete role assignments. If you want to disable role assignments synchronization since you want to manage user roles manually in the application, set the `jmix.ldap.synchronizeRoleAssignments` to `false`.

After synchronization strategy is declared, users will be synchronized on every login. If you want to disable user synchronization on login (for example, if you want to load users from LDAP beforehand and synchronize them once a day on schedule task), set the `jmix.ldap.synchronizeUserOnLogin` to `false`.

## Obtaining user authorities

In case when roles are synchronized in accordance with LDAP, they are obtained in a few steps:

* **Obtaining roles from the groups user is member of.**
  The resulting list of authorities consists of a group names user is member of. By default the `cn` attribute of the group is used to obtain the authority name. This attribute can be changed in the `jmix.ldap.groupRoleAttribute` property.
* **Obtaining roles from user attributes.**
  Optionally user roles can be added based on their own attributes. This can be implemented by overriding the method `getAdditionalRoles()` of the `AbstractLdapUserDetailsSynchronizationStrategy`.
* **Applying default roles.**
  The property `jmix.ldap.defaultRoles` contains a comma-separated list of roles that will be assigned to every user authenticated in LDAP.

The resulting list of authorities is passed through `JmixLdapGrantedAuthoritiesMapper` in order to get the final collection of authorities. This mapper is used to map previously obtained authorities to Jmix `RoleGrantedAuthority`s.
For example, consider that the initial list contains a simple authority with the value of: `Administrators`. First, the mapper tries to find a resource role with the same role-code. If the role hasn't been found, it searches for a row-level role with the same code. In the role isn't found, it won't be added to the final list.

It is also possible to specify a mapping function with the `setAuthorityToRoleCodeMapper()` in order to describe a matching of authorities names, for example:

```java
Map<String, String> authorityMap = new HashMap<>();
authorityMap.put("Administrators", "system-full-access");
        
authoritiesMapper.setAuthorityToRoleCodeMapper(s -> authorityMap.getOrDefault(s, s));
```

## In-memory user management

The addon supports the case when users are managed fully in LDAP. To do this, set the property:

```text
jmix.ldap.userDetailsSource = ldap
```

In this case the standard `LdapUserDetails` objects are created by Spring Security after each authentication and are preserved in memory (instead of being taken from DB as the `User` entity). User authorities are obtained by the role mapping process described above.

### LDAP User Repository

In case when users are primarily managed in LDAP there are still can be places in the application where you need to choose a user, or find a user in `SuggestionField`. For this purpose the addon comes with the LDAP implementation of `UserRepository`. This user repository provides an access to users from LDAP, but it does not provide the `system` or `anonymous` user. Also, you may want users from DB be available for searching as well.
To achieve this, you need to declare the `CompositeUserRepository` bean, which comprises all instances of `UserRepository`s in an application (for example `LdapUserRepository` and `DatabaseUserRepository`) and make this `UserRepository` primary in the application:

```java
@Bean
@Primary
UserRepository userRepository() {
  return new CompositeUserRepository();
}
```

## Usage with Active Directory

The addon provides a separate Security Configuration for using with Active Directory. Since Active Directory supports its own non-standard authentication options (such as authentication with the domain username (in the form `user@domain`) instead of an LDAP distinguished name), Spring Security provides a separate `AuthenticationProvider`, which is customized for a typical Active Directory setup. If you are going to work with the Active Directory, set the property:

```text
jmix.ldap.useActiveDirectoryConfiguration = true
```

By setting this property, you will be using the `ActiveDirectoryLdapAuthenticationProvider` which is more suitable for usage with Active Directory.

## Global user synchronization

The addon provides the `LdapUserSynchronizationManager` bean that synchronizes users from the particular LDAP group specified in the `jmix.ldap.groupForSynchronization` property. Typically, this bean would be invoked on schedule task execution (for example, once a day), but also can be run via JMX bean. The synchronization also relies on `LdapUserDetailsSynchronizationStrategy` declared in the application (as well as synchronization on login described above).

## Jmix LDAP properties

#### jmix.ldap.enabled

* **Description:** defines if the LDAP authentication is enabled.
* **Default value:** true

#### jmix.ldap.userDetailsSource

* **Description:** defines a source of `UserDetails` objects returned after successful authentication.
* **Possible values:**
    * `app`: `UserDetails` obtained from the primary `UserDetailsService` of the application, typically from DB.
    * `ldap`: standard `LdapUserDetails` objects created by Spring Security are used.
* **Default value:** app

#### jmix.ldap.urls

* **Description:** an LDAP server URL. An example would be `ldap://ldap.company.com:389`. LDAPS URLs (SSL-secured) may be used as well, given that Spring Security is able to connect to the server. Note that these URLs must not include the base DN. Multiple urls can be specified separated by comma. In this case connection will be established to any of the provided LDAP server URLs.

#### jmix.ldap.baseDn

* **Description:** defines a base DN. If configured, all LDAP operations on contexts retrieved from this ContextSource
  relate to this DN. The default value is an empty distinguished name (i.e. all operations relate to the directory root).

#### jmix.ldap.managerDn

* **Description:** indicates a DN used for authentication. This is normally the distinguished name of the "manager" user.

#### jmix.ldap.managerPassword

* **Description:** defines a password for "manager" user

#### jmix.ldap.userSearchBase

* **Description:** search base for user searches. Used with `jmix.ldap.userSearchFilter`.

#### jmix.ldap.userSearchFilter

* **Description:** the LDAP filter used to search for users. For example `(uid={0})`. The substituted parameter is the user's login name.

#### jmix.ldap.usernameAttribute

* **Description:** the LDAP attribute corresponding to the username. Used during user synchronization to assign the correct username for the user.
* **Default value:** uid

#### jmix.ldap.groupRoleAttribute

* **Description:** the attribute of the LDAP group corresponding to the authority name.
* **Default value:** cn

#### jmix.ldap.groupSearchBase

* **Description:** search base for group searches. Defaults to "".

#### jmix.ldap.groupSearchSubtree

* **Description:** if set to true, a subtree scope search will be performed. If false a single-level search is used.
* **Default value:** false

#### jmix.ldap.groupSearchFilter

* **Description:** the LDAP filter used to search for user's groups (optional). For example `(uniqueMember={0})`. The substituted parameter is the user's login name.
* **Default value:** `(uniqueMember={0})`

#### jmix.ldap.useActiveDirectoryConfiguration

* **Description:** defines if Active Directory specific security configuration should be used instead of the default one.
* **Default value:** false

#### jmix.ldap.activeDirectoryDomain

* **Description:** used only with the ActiveDirectory configuration. Specifies the Active Directory domain name, for example: "mydomain.com".

#### jmix.ldap.groupForSynchronization

* **Description:** the DN of the group containing users to be synchronized in the application.

#### jmix.ldap.synchronizeUserOnLogin

* **Description:** defines whether to synchronize user after successful LDAP authentication or not. The synchronization will be performed only if the corresponding strategy is declared (see the [Quick start](#quick-start)).
* **Default value:** true

#### jmix.ldap.synchronizeRoleAssignments

* **Description:** defines whether to save role assignments during user synchronization or not.
* **Default value:** true

#### jmix.ldap.defaultRoles

* **Description:** defines a list of roles that will be assigned to every user after successful authentication.

#### jmix.ldap.standardAuthenticationUsers

* **Description:** defines a list of users that should be always authenticated with the standard authentication.