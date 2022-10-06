package io.jmix.oidc.usermapper;

import io.jmix.core.SaveContext;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.security.UserRepository;
import io.jmix.data.PersistenceHints;
import io.jmix.oidc.claimsmapper.ClaimsRolesMapper;
import io.jmix.oidc.user.JmixOidcUser;
import io.jmix.security.authentication.RoleGrantedAuthority;
import io.jmix.security.role.assignment.RoleAssignmentRoleType;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementation of the {@link OidcUserMapper} that not only maps the external user object to the persistent user
 * entity, but also stores the user and optionally their role assignment to the database.
 *
 * @param <T>
 */
//todo make the class generic and move it to jmix-security in order to share it with jmix-ldap and jmix-oidc
public abstract class SynchronizingOidcUserMapper<T extends JmixOidcUser> extends BaseOidcUserMapper<T> {

    private static final Logger log = LoggerFactory.getLogger(SynchronizingOidcUserMapper.class);

    protected UnconstrainedDataManager dataManager;

    protected UserRepository userRepository;

    protected ClaimsRolesMapper claimsRolesMapper;

    protected boolean synchronizeRoleAssignments;

    public SynchronizingOidcUserMapper(UnconstrainedDataManager dataManager,
                                       UserRepository userRepository,
                                       ClaimsRolesMapper claimsRolesMapper) {
        this.dataManager = dataManager;
        this.userRepository = userRepository;
        this.claimsRolesMapper = claimsRolesMapper;
    }

    /**
     * Returns a class of the user used by the application. This user is set to the security context.
     */
    protected abstract Class<T> getApplicationUserClass();

    /**
     * Extracts username from the {@code oidcUser}
     */
    protected String getOidcUserUsername(OidcUser oidcUser) {
        return oidcUser.getName();
    }

    @Override
    protected T initJmixUser(OidcUser oidcUser) {
        String username = getOidcUserUsername(oidcUser);
        T jmixUserDetails;
        try {
            jmixUserDetails = (T) userRepository.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            log.info("User with login {} wasn't found in user repository", username);
            jmixUserDetails = dataManager.create(getApplicationUserClass());
        }
        return jmixUserDetails;
    }

    @Override
    protected void populateUserAuthorities(OidcUser oidcUser, T jmixUser) {
        Collection<? extends GrantedAuthority> grantedAuthorities = claimsRolesMapper.toGrantedAuthorities(oidcUser.getClaims());
        jmixUser.setAuthorities(grantedAuthorities);
    }

    @Override
    protected void performAdditionalModifications(OidcUser oidcUser, T jmixUser) {
        super.performAdditionalModifications(oidcUser, jmixUser);
        saveJmixUserAndRoleAssignments(oidcUser, jmixUser);
    }

    protected void saveJmixUserAndRoleAssignments(OidcUser oidcUser, T jmixUser) {
        SaveContext saveContext = new SaveContext();

        if (synchronizeRoleAssignments) {
            String username = getOidcUserUsername(oidcUser);
            //disable soft-deletion to completely remove role assignment records from the database
            saveContext.setHint(PersistenceHints.SOFT_DELETION, false);
            List<RoleAssignmentEntity> existingRoleAssignmentEntities = dataManager.load(RoleAssignmentEntity.class)
                    .query("select e from sec_RoleAssignmentEntity e where e.username = :username")
                    .parameter("username", username)
                    .list();
            //todo do not remove all assignments but only assignments missing in new user authorities
            saveContext.removing(existingRoleAssignmentEntities);

            Collection<RoleAssignmentEntity> newRoleAssignmentEntities = buildRoleAssignmentEntities(username, jmixUser.getAuthorities());
            saveContext.saving(newRoleAssignmentEntities);
        }

        saveContext.saving(jmixUser);

        //persist user details and roles if needed
        dataManager.save(saveContext);
    }

    protected Collection<RoleAssignmentEntity> buildRoleAssignmentEntities(String username, Collection<? extends GrantedAuthority> grantedAuthorities) {
        List<RoleAssignmentEntity> roleAssignmentEntities = new ArrayList<>();
        for (GrantedAuthority grantedAuthority : grantedAuthorities) {
            if (grantedAuthority instanceof RoleGrantedAuthority) {
                RoleGrantedAuthority roleGrantedAuthority = (RoleGrantedAuthority) grantedAuthority;
                String roleCode = roleGrantedAuthority.getAuthority();
                String roleType;
                if (roleCode.startsWith("row_level_role:")) {
                    roleType = RoleAssignmentRoleType.ROW_LEVEL;
                    roleCode = roleCode.substring("row_level_role:".length());
                } else {
                    roleType = RoleAssignmentRoleType.RESOURCE;
                }

                RoleAssignmentEntity roleAssignmentEntity = dataManager.create(RoleAssignmentEntity.class);
                roleAssignmentEntity.setRoleCode(roleCode);
                roleAssignmentEntity.setUsername(username);
                roleAssignmentEntity.setRoleType(roleType);
                roleAssignmentEntities.add(roleAssignmentEntity);
            }
        }
        return roleAssignmentEntities;
    }

    /**
     * Enables role assignment entities synchronization. If true then role assignment entities will be stored to the
     * database.
     */
    public void setSynchronizeRoleAssignments(boolean synchronizeRoleAssignments) {
        this.synchronizeRoleAssignments = synchronizeRoleAssignments;
    }

    public boolean isSynchronizeRoleAssignments() {
        return synchronizeRoleAssignments;
    }
}
