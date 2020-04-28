/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.rest.store;

import com.google.common.base.Strings;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.Metadata;
import io.jmix.core.TimeSource;
import io.jmix.core.cluster.ClusterManager;
import io.jmix.core.security.NoUserSessionException;
import io.jmix.core.security.UserSession;
import io.jmix.core.security.impl.UserSessionsImpl;
import io.jmix.rest.entity.AccessToken;
import io.jmix.rest.entity.RefreshToken;
import io.jmix.rest.rest.RestUserSessionInfo;
import io.jmix.rest.rest.ServerTokenStore;
import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Component(ServerTokenStore.NAME)
public class ServerTokenStoreImpl implements ServerTokenStore {

//    @Inject
//    protected AuthenticationManager authenticationManager;

    // todo UserSessionLog
//    @Inject
//    protected UserSessionLog userSessionLog;

    @Inject
    protected TokenStoreSupport tokenStoreSupport;

    @Inject
    protected UserSessionsImpl userSessions;

    @Inject
    protected ClusterManager clusterManagerAPI;

//    @Inject
//    protected RestProperties restProperties;

//    @Inject
//    protected Persistence persistence;

    @Inject
    protected DataManager dataManager;

    @Inject
    protected Metadata metadata;

    @Inject
    protected TimeSource timeSource;

    private static final Logger log = LoggerFactory.getLogger(ServerTokenStoreImpl.class);

    protected ReadWriteLock lock = new ReentrantReadWriteLock();

    private ConcurrentHashMap<String, byte[]> accessTokenValueToAccessTokenStore = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, byte[]> accessTokenValueToAuthenticationStore = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, byte[]> authenticationToAccessTokenStore = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, RestUserSessionInfo> accessTokenValueToSessionInfoStore = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> accessTokenValueToAuthenticationKeyStore = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> accessTokenValueToUserLoginStore = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, byte[]> refreshTokenValueToRefreshTokenStore = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, byte[]> refreshTokenValueToAuthenticationStore = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> refreshTokenValueToAccessTokenValueStore = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> refreshTokenValueToUserLoginStore = new ConcurrentHashMap<>();

    private final DelayQueue<TokenExpiry> accessTokensExpiryQueue = new DelayQueue<>();
    private final DelayQueue<TokenExpiry> refreshTokensExpiryQueue = new DelayQueue<>();

    @PostConstruct
    public void init() {
        initClusterListeners();
    }

    protected void initClusterListeners() {
        // todo clusterManagerAPI
//        clusterManagerAPI.addListener(TokenStoreAddAccessTokenMsg.class, new ClusterListener<TokenStoreAddAccessTokenMsg>() {
//            @Override
//            public void receive(TokenStoreAddAccessTokenMsg message) {
//                storeAccessTokenToMemory(message.getAccessTokenValue(),
//                        message.getAccessTokenBytes(),
//                        message.getAuthenticationKey(),
//                        message.getAuthenticationBytes(),
//                        message.getTokenExpiry(),
//                        message.getUserLogin(),
//                        message.getRefreshTokenValue());
//            }
//
//            @Override
//            public byte[] getState() {
//                if (accessTokenValueToAccessTokenStore.isEmpty() &&
//                        accessTokenValueToAuthenticationStore.isEmpty() &&
//                        authenticationToAccessTokenStore.isEmpty() &&
//                        accessTokenValueToSessionInfoStore.isEmpty() &&
//                        accessTokenValueToAuthenticationKeyStore.isEmpty() &&
//                        accessTokenValueToUserLoginStore.isEmpty() &&
//                        refreshTokenValueToRefreshTokenStore.isEmpty() &&
//                        refreshTokenValueToAuthenticationStore.isEmpty() &&
//                        refreshTokenValueToAccessTokenValueStore.isEmpty() &&
//                        refreshTokenValueToUserLoginStore.isEmpty()) {
//                    return new byte[0];
//                }
//
//                ByteArrayOutputStream bos = new ByteArrayOutputStream();
//
//                lock.readLock().lock();
//                try {
//                    ObjectOutputStream oos = new ObjectOutputStream(bos);
//                    oos.writeObject(accessTokenValueToAccessTokenStore);
//                    oos.writeObject(accessTokenValueToAuthenticationStore);
//                    oos.writeObject(authenticationToAccessTokenStore);
//                    oos.writeObject(accessTokenValueToSessionInfoStore);
//                    oos.writeObject(accessTokenValueToAuthenticationKeyStore);
//                    oos.writeObject(accessTokenValueToUserLoginStore);
//                    oos.writeObject(refreshTokenValueToRefreshTokenStore);
//                    oos.writeObject(refreshTokenValueToAuthenticationStore);
//                    oos.writeObject(refreshTokenValueToAccessTokenValueStore);
//                    oos.writeObject(refreshTokenValueToUserLoginStore);
//                } catch (IOException e) {
//                    throw new RuntimeException("Unable to serialize ServerTokenStore fields for cluster state", e);
//                } finally {
//                    lock.readLock().unlock();
//                }
//
//                return bos.toByteArray();
//            }
//
//            @SuppressWarnings("unchecked")
//            @Override
//            public void setState(byte[] state) {
//                if (state == null || state.length == 0) {
//                    return;
//                }
//
//                ByteArrayInputStream bis = new ByteArrayInputStream(state);
//                lock.writeLock().lock();
//                try {
//                    ObjectInputStream ois = new ObjectInputStream(bis);
//                    accessTokenValueToAccessTokenStore = (ConcurrentHashMap<String, byte[]>) ois.readObject();
//                    accessTokenValueToAuthenticationStore = (ConcurrentHashMap<String, byte[]>) ois.readObject();
//                    authenticationToAccessTokenStore = (ConcurrentHashMap<String, byte[]>) ois.readObject();
//                    accessTokenValueToSessionInfoStore = (ConcurrentHashMap<String, RestUserSessionInfo>) ois.readObject();
//                    accessTokenValueToAuthenticationKeyStore = (ConcurrentHashMap<String, String>) ois.readObject();
//                    accessTokenValueToUserLoginStore = (ConcurrentHashMap<String, String>) ois.readObject();
//                    refreshTokenValueToRefreshTokenStore = (ConcurrentHashMap<String, byte[]>) ois.readObject();
//                    refreshTokenValueToAuthenticationStore = (ConcurrentHashMap<String, byte[]>) ois.readObject();
//                    refreshTokenValueToAccessTokenValueStore = (ConcurrentHashMap<String, String>) ois.readObject();
//                    refreshTokenValueToUserLoginStore = (ConcurrentHashMap<String, String>) ois.readObject();
//                } catch (IOException | ClassNotFoundException e) {
//                    log.error("Error receiving state", e);
//                } finally {
//                    lock.writeLock().unlock();
//                }
//            }
//        });
//
//        clusterManagerAPI.addListener(TokenStorePutSessionInfoMsg.class, new ClusterListenerAdapter<TokenStorePutSessionInfoMsg>() {
//            @Override
//            public void receive(TokenStorePutSessionInfoMsg message) {
//                _putSessionInfo(message.getTokenValue(), message.getSessionInfo());
//            }
//        });
//
//        clusterManagerAPI.addListener(TokenStoreRemoveAccessTokenMsg.class, new ClusterListenerAdapter<TokenStoreRemoveAccessTokenMsg>() {
//            @Override
//            public void receive(TokenStoreRemoveAccessTokenMsg message) {
//                removeAccessTokenFromMemory(message.getTokenValue());
//            }
//        });
//
//        clusterManagerAPI.addListener(TokenStoreAddRefreshTokenMsg.class, new ClusterListenerAdapter<TokenStoreAddRefreshTokenMsg>() {
//            @Override
//            public void receive(TokenStoreAddRefreshTokenMsg message) {
//                storeRefreshTokenToMemory(message.getTokenValue(),
//                        message.getTokenBytes(),
//                        message.getAuthenticationBytes(),
//                        message.getTokenExpiry(),
//                        message.getUserLogin());
//            }
//        });
//
//        clusterManagerAPI.addListener(TokenStoreRemoveRefreshTokenMsg.class, new ClusterListenerAdapter<TokenStoreRemoveRefreshTokenMsg>() {
//            @Override
//            public void receive(TokenStoreRemoveRefreshTokenMsg message) {
//                removeAccessTokenFromMemory(message.getTokenValue());
//            }
//        });
    }

    @Override
    public byte[] getAccessTokenByAuthentication(String authenticationKey) {
        byte[] accessTokenBytes;
        accessTokenBytes = getAccessTokenByAuthenticationFromMemory(authenticationKey);
        if (accessTokenBytes == null && tokenStoreSupport.isRestStoreTokensInDb()) {
            AccessToken accessToken = getAccessTokenByAuthenticationKeyFromDatabase(authenticationKey);
            if (accessToken != null) {
                accessTokenBytes = accessToken.getTokenBytes();
                restoreAccessTokenIntoMemory(accessToken);
            }
        }
        return accessTokenBytes;
    }

    protected byte[] getAccessTokenByAuthenticationFromMemory(String authenticationKey) {
        return authenticationToAccessTokenStore.get(authenticationKey);
    }

    @Override
    public Set<String> getAccessTokenValuesByUserLogin(String userLogin) {
        Set<String> tokenValues = getAccessTokenValuesByUserLoginFromMemory(userLogin);

        if (tokenStoreSupport.isRestStoreTokensInDb()) {
            tokenValues.addAll(getAccessTokenValuesByUserLoginFromDatabase(userLogin));
        }
        return tokenValues;
    }

    protected Set<String> getAccessTokenValuesByUserLoginFromMemory(String userLogin) {
        return accessTokenValueToUserLoginStore.entrySet().stream()
                .filter(entry -> userLogin.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    protected Set<String> getAccessTokenValuesByUserLoginFromDatabase(String userLogin) {
        // todo EntityManager
        List<String> result = dataManager.loadValue(
                "select e.tokenValue from sys$AccessToken e where e.userLogin = :userLogin", String.class)
                .parameter("userLogin", userLogin)
                .list();
        return new HashSet<>(result);

    }

    @Override
    public Set<String> getRefreshTokenValuesByUserLogin(String userLogin) {
        Set<String> tokenValues = getRefreshTokenValuesByUserLoginFromMemory(userLogin);
        if (tokenStoreSupport.isRestStoreTokensInDb()) {
            tokenValues.addAll(getRefreshTokenValuesByUserLoginFromDatabase(userLogin));
        }
        return tokenValues;
    }

    protected Set<String> getRefreshTokenValuesByUserLoginFromMemory(String userLogin) {
        return refreshTokenValueToUserLoginStore.entrySet().stream()
                .filter(entry -> userLogin.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    protected Set<String> getRefreshTokenValuesByUserLoginFromDatabase(String userLogin) {
        // todo EntityManager
        List<String> result = dataManager.loadValue(
                "select e.tokenValue from sys$RefreshToken e where e.userLogin = :userLogin", String.class)
                .parameter("userLogin", userLogin)
                .list();
        return new HashSet<>(result);
    }

    @Override
    public void storeAccessToken(String tokenValue,
                                 byte[] accessTokenBytes,
                                 String authenticationKey,
                                 byte[] authenticationBytes,
                                 Date tokenExpiry,
                                 String userLogin,
                                 Locale locale,
                                 String refreshTokenValue) {
        storeAccessTokenToMemory(tokenValue, accessTokenBytes, authenticationKey, authenticationBytes, tokenExpiry,
                userLogin, refreshTokenValue);
        if (tokenStoreSupport.isRestStoreTokensInDb()) {
//            try (Transaction tx = persistence.getTransaction()) {
//                removeAccessTokenFromDatabase(tokenValue);
//                storeAccessTokenToDatabase(tokenValue, accessTokenBytes, authenticationKey, authenticationBytes,
//                        tokenExpiry, userLogin, locale, refreshTokenValue);
//                tx.commit();
//            }

            removeAccessTokenFromDatabase(tokenValue);
            storeAccessTokenToDatabase(tokenValue, accessTokenBytes, authenticationKey, authenticationBytes,
                    tokenExpiry, userLogin, locale, refreshTokenValue);
        }
        if (tokenStoreSupport.isRestSyncTokenReplication()) {
            clusterManagerAPI.sendSync(new TokenStoreAddAccessTokenMsg(tokenValue, accessTokenBytes, authenticationKey,
                    authenticationBytes, tokenExpiry, userLogin, refreshTokenValue));
        } else {
            clusterManagerAPI.send(new TokenStoreAddAccessTokenMsg(tokenValue, accessTokenBytes, authenticationKey,
                    authenticationBytes, tokenExpiry, userLogin, refreshTokenValue));
        }
    }

    protected void storeAccessTokenToMemory(String accessTokenValue,
                                            byte[] accessTokenBytes,
                                            String authenticationKey,
                                            byte[] authenticationBytes,
                                            Date tokenExpiry,
                                            String userLogin,
                                            @Nullable String refreshTokenValue) {
        lock.writeLock().lock();
        try {
            accessTokenValueToAccessTokenStore.put(accessTokenValue, accessTokenBytes);
            authenticationToAccessTokenStore.put(authenticationKey, accessTokenBytes);
            accessTokenValueToAuthenticationStore.put(accessTokenValue, authenticationBytes);
            accessTokenValueToAuthenticationKeyStore.put(accessTokenValue, authenticationKey);
            accessTokenValueToUserLoginStore.put(accessTokenValue, userLogin);
            if (!Strings.isNullOrEmpty(refreshTokenValue)) {
                refreshTokenValueToAccessTokenValueStore.put(refreshTokenValue, accessTokenValue);
            }
        } finally {
            lock.writeLock().unlock();
        }

        if (tokenExpiry != null) {
            TokenExpiry expiry = new TokenExpiry(accessTokenValue, tokenExpiry);
            this.accessTokensExpiryQueue.put(expiry);
        }
    }

    protected void storeAccessTokenToDatabase(String tokenValue,
                                              byte[] accessTokenBytes,
                                              String authenticationKey,
                                              byte[] authenticationBytes,
                                              Date tokenExpiry,
                                              String userLogin,
                                              @Nullable Locale locale,
                                              @Nullable String refreshTokenValue) {
        // todo EntityManager
        AccessToken accessToken = metadata.create(AccessToken.class);
        accessToken.setCreateTs(timeSource.currentTimestamp());
        accessToken.setTokenValue(tokenValue);
        accessToken.setTokenBytes(accessTokenBytes);
        accessToken.setAuthenticationKey(authenticationKey);
        accessToken.setAuthenticationBytes(authenticationBytes);
        accessToken.setExpiry(tokenExpiry);
        accessToken.setUserLogin(userLogin);
        accessToken.setLocale(locale != null ? locale.toString() : null);
        accessToken.setRefreshTokenValue(refreshTokenValue);
        dataManager.save(accessToken);
    }


    @Override
    public void storeRefreshToken(String refreshTokenValue,
                                  byte[] refreshTokenBytes,
                                  byte[] authenticationBytes,
                                  Date tokenExpiry,
                                  String userLogin) {
        storeRefreshTokenToMemory(refreshTokenValue, refreshTokenBytes, authenticationBytes, tokenExpiry, userLogin);
        if (tokenStoreSupport.isRestStoreTokensInDb()) {
            // todo
            removeRefreshTokenFromDatabase(refreshTokenValue);
            storeRefreshTokenToDatabase(refreshTokenValue, refreshTokenBytes, authenticationBytes,
                    tokenExpiry, userLogin);
        }
    }

    protected void storeRefreshTokenToMemory(String refreshTokenValue,
                                             byte[] refreshTokenBytes,
                                             byte[] authenticationBytes,
                                             Date tokenExpiry,
                                             String userLogin) {
        refreshTokenValueToRefreshTokenStore.put(refreshTokenValue, refreshTokenBytes);
        refreshTokenValueToAuthenticationStore.put(refreshTokenValue, authenticationBytes);
        refreshTokenValueToUserLoginStore.put(refreshTokenValue, userLogin);

        if (tokenExpiry != null) {
            TokenExpiry expiry = new TokenExpiry(refreshTokenValue, tokenExpiry);
            this.refreshTokensExpiryQueue.put(expiry);
        }
    }

    protected void storeRefreshTokenToDatabase(String tokenValue,
                                               byte[] tokenBytes,
                                               byte[] authenticationBytes,
                                               Date tokenExpiry,
                                               String userLogin) {
        RefreshToken refreshToken = metadata.create(RefreshToken.class);
        refreshToken.setCreateTs(timeSource.currentTimestamp());
        refreshToken.setTokenValue(tokenValue);
        refreshToken.setTokenBytes(tokenBytes);
        refreshToken.setAuthenticationBytes(authenticationBytes);
        refreshToken.setExpiry(tokenExpiry);
        refreshToken.setUserLogin(userLogin);
        dataManager.save(refreshToken);
    }

    @Override
    public byte[] getAccessTokenByTokenValue(String accessTokenValue) {
        byte[] accessTokenBytes;
        accessTokenBytes = getAccessTokenByTokenValueFromMemory(accessTokenValue);
        if (accessTokenBytes == null && tokenStoreSupport.isRestStoreTokensInDb()) {
            AccessToken accessToken = getAccessTokenByTokenValueFromDatabase(accessTokenValue);
            if (accessToken != null) {
                accessTokenBytes = accessToken.getTokenBytes();
                restoreAccessTokenIntoMemory(accessToken);
            }
        }
        return accessTokenBytes;
    }

    protected byte[] getAccessTokenByTokenValueFromMemory(String tokenValue) {
        return accessTokenValueToAccessTokenStore.get(tokenValue);
    }

    @Override
    public byte[] getAuthenticationByTokenValue(String tokenValue) {
        byte[] authenticationBytes;
        authenticationBytes = getAuthenticationByTokenValueFromMemory(tokenValue);
        if (authenticationBytes == null && tokenStoreSupport.isRestStoreTokensInDb()) {
            AccessToken accessToken = getAccessTokenByTokenValueFromDatabase(tokenValue);
            if (accessToken != null) {
                authenticationBytes = accessToken.getAuthenticationBytes();
                restoreAccessTokenIntoMemory(accessToken);
            }
        }
        return authenticationBytes;
    }

    protected byte[] getAuthenticationByTokenValueFromMemory(String tokenValue) {
        return accessTokenValueToAuthenticationStore.get(tokenValue);
    }

    @Nullable
    protected AccessToken getAccessTokenByTokenValueFromDatabase(String accessTokenValue) {
//        try (Transaction tx = persistence.createTransaction()) {
//            EntityManager em = persistence.getEntityManager();
//            accessToken = em.createQuery("select e from sys$AccessToken e where e.tokenValue = :tokenValue", AccessToken.class)
//                    .setParameter("tokenValue", accessTokenValue)
//                    .setViewName(View.LOCAL)
//                    .getFirstResult();
//            tx.commit();
//            return accessToken;
//        }
        return dataManager.load(AccessToken.class)
                .query("e.tokenValue = :tokenValue")
                .parameter("tokenValue", accessTokenValue)
                .fetchPlan(FetchPlan.LOCAL)
                .optional()
                .orElse(null);
    }

    @Nullable
    protected RefreshToken getRefreshTokenByTokenValueFromDatabase(String refreshTokenValue) {
//        RefreshToken refreshToken;
//        try (Transaction tx = persistence.createTransaction()) {
//            EntityManager em = persistence.getEntityManager();
//            refreshToken = em.createQuery("select e from sys$RefreshToken e where e.tokenValue = :tokenValue", RefreshToken.class)
//                    .setParameter("tokenValue", refreshTokenValue)
//                    .setViewName(View.LOCAL)
//                    .getFirstResult();
//            tx.commit();
//            return refreshToken;
//        }

        return dataManager.load(RefreshToken.class)
                .query("select e from sys$RefreshToken e where e.tokenValue = :tokenValue")
                .parameter("tokenValue", refreshTokenValue)
                .fetchPlan(FetchPlan.LOCAL)
                .one();
    }

    @Nullable
    protected AccessToken getAccessTokenByAuthenticationKeyFromDatabase(String authenticationKey) {
//        AccessToken accessToken;
//        try (Transaction tx = persistence.createTransaction()) {
//            EntityManager em = persistence.getEntityManager();
//            accessToken = em.createQuery("select e from sys$AccessToken e where e.authenticationKey = :authenticationKey", AccessToken.class)
//                    .setParameter("authenticationKey", authenticationKey)
//                    .setViewName(View.LOCAL)
//                    .getFirstResult();
//            tx.commit();
//            return accessToken;
//        }
        return dataManager.load(AccessToken.class)
                .query("select e from sys$AccessToken e where e.authenticationKey = :authenticationKey")
                .parameter("authenticationKey", authenticationKey)
                .fetchPlan(FetchPlan.LOCAL)
                .one();
    }

    @Override
    public byte[] getRefreshTokenByTokenValue(String tokenValue) {
        byte[] tokenBytes = getRefreshTokenByTokenValueFromMemory(tokenValue);
        if (tokenBytes == null && tokenStoreSupport.isRestStoreTokensInDb()) {
            RefreshToken refreshToken = getRefreshTokenByTokenValueFromDatabase(tokenValue);
            if (refreshToken != null) {
                tokenBytes = refreshToken.getTokenBytes();
                restoreRefreshTokenIntoMemory(refreshToken);
            }
        }
        return tokenBytes;
    }

    /**
     * Method fills in-memory maps from the {@link AccessToken} object got from the database
     */
    protected void restoreAccessTokenIntoMemory(AccessToken accessToken) {
        lock.writeLock().lock();
        try {
            accessTokenValueToAccessTokenStore.put(accessToken.getTokenValue(), accessToken.getTokenBytes());
            authenticationToAccessTokenStore.put(accessToken.getAuthenticationKey(), accessToken.getTokenBytes());
            accessTokenValueToAuthenticationStore.put(accessToken.getTokenValue(), accessToken.getAuthenticationBytes());
            accessTokenValueToAuthenticationKeyStore.put(accessToken.getTokenValue(), accessToken.getAuthenticationKey());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Method fills in-memory maps from the {@link RefreshToken} object got from the database
     */
    protected void restoreRefreshTokenIntoMemory(RefreshToken refreshToken) {
        lock.writeLock().lock();
        try {
            refreshTokenValueToRefreshTokenStore.put(refreshToken.getTokenValue(), refreshToken.getTokenBytes());
            refreshTokenValueToAuthenticationStore.put(refreshToken.getTokenValue(), refreshToken.getAuthenticationBytes());
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public RestUserSessionInfo getSessionInfoByTokenValue(String tokenValue) {
        RestUserSessionInfo sessionInfo = accessTokenValueToSessionInfoStore.get(tokenValue);
        if (sessionInfo == null && tokenStoreSupport.isRestStoreTokensInDb()) {
            AccessToken accessToken = getAccessTokenByTokenValueFromDatabase(tokenValue);
            if (accessToken != null) {
                String localeStr = accessToken.getLocale();
                if (!Strings.isNullOrEmpty(localeStr)) {
                    Locale locale = LocaleUtils.toLocale(localeStr);
                    return new RestUserSessionInfo(null, locale);
                }
            }
        }

        return sessionInfo;
    }

    @Override
    public RestUserSessionInfo putSessionInfo(String tokenValue, RestUserSessionInfo sessionInfo) {
        RestUserSessionInfo info = _putSessionInfo(tokenValue, sessionInfo);
        if (tokenStoreSupport.isRestSyncTokenReplication()) {
            clusterManagerAPI.sendSync(new TokenStorePutSessionInfoMsg(tokenValue, sessionInfo));
        } else {
            clusterManagerAPI.send(new TokenStorePutSessionInfoMsg(tokenValue, sessionInfo));
        }
        return info;
    }

    protected RestUserSessionInfo _putSessionInfo(String tokenValue, RestUserSessionInfo sessionInfo) {
        lock.writeLock().lock();
        try {
            return accessTokenValueToSessionInfoStore.put(tokenValue, sessionInfo);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void removeAccessToken(String tokenValue) {
        removeAccessTokenFromMemory(tokenValue);
        if (tokenStoreSupport.isRestStoreTokensInDb()) {
            removeAccessTokenFromDatabase(tokenValue);
        }
        if (tokenStoreSupport.isRestSyncTokenReplication()) {
            clusterManagerAPI.sendSync(new TokenStoreRemoveAccessTokenMsg(tokenValue));
        } else {
            clusterManagerAPI.send(new TokenStoreRemoveAccessTokenMsg(tokenValue));
        }
    }

    protected void removeAccessTokenFromMemory(String tokenValue) {
        RestUserSessionInfo sessionInfo;
        lock.writeLock().lock();
        try {
            accessTokenValueToAccessTokenStore.remove(tokenValue);
            accessTokenValueToAuthenticationStore.remove(tokenValue);
            accessTokenValueToUserLoginStore.remove(tokenValue);
            String authenticationKey = accessTokenValueToAuthenticationKeyStore.remove(tokenValue);
            if (authenticationKey != null) {
                authenticationToAccessTokenStore.remove(authenticationKey);
            }
            sessionInfo = accessTokenValueToSessionInfoStore.remove(tokenValue);
        } finally {
            lock.writeLock().unlock();
        }
        if (sessionInfo != null) {
            try {
                UserSession session = userSessions.get(sessionInfo.getId());
                if (session != null) {
                    // todo SecurityContext
//                    AppContext.setSecurityContext(new SecurityContext(session));
                    try {
                        //todo AuthenticationManager
//                        authenticationManager.logout();
                        //todo UserSessionLog
//                        userSessionLog.updateSessionLogRecord(session, SessionAction.LOGOUT);
                    } finally {
                        //todo SecurityContext
                        //AppContext.setSecurityContext(null);
                    }
                }
            } catch (NoUserSessionException ignored) {
            }
        }
    }

    protected void removeAccessTokenFromDatabase(String accessTokenValue) {
//        try (Transaction tx = persistence.getTransaction()) {
//            EntityManager em = persistence.getEntityManager();
//            em.createQuery("delete from sys$AccessToken t where t.tokenValue = :tokenValue")
//                    .setParameter("tokenValue", accessTokenValue)
//                    .executeUpdate();
//            tx.commit();
//        }
        List<AccessToken> accessTokenList = dataManager.load(AccessToken.class)
                .query("select sys$AccessToken t where t.tokenValue = :tokenValue")
                .parameter("tokenValue", accessTokenValue)
                .list();

        for (AccessToken accessToken : accessTokenList) {
            dataManager.remove(accessToken);
        }
    }

    @Override
    public void removeRefreshToken(String refreshTokenValue) {
        removeRefreshTokenFromMemory(refreshTokenValue);

        if (tokenStoreSupport.isRestStoreTokensInDb()) {
            removeRefreshTokenFromDatabase(refreshTokenValue);
        }
    }

    protected void removeRefreshTokenFromMemory(String refreshTokenValue) {
        lock.writeLock().lock();
        try {
            refreshTokenValueToRefreshTokenStore.remove(refreshTokenValue);
            refreshTokenValueToAuthenticationStore.remove(refreshTokenValue);
            refreshTokenValueToAccessTokenValueStore.remove(refreshTokenValue);
        } finally {
            lock.writeLock().unlock();
        }
    }

    protected void removeRefreshTokenFromDatabase(String refreshTokenValue) {
        List<RefreshToken> refreshTokenList = dataManager.load(RefreshToken.class)
                .query("select t from sys$RefreshToken t where t.tokenValue = :tokenValue")
                .parameter("tokenValue", refreshTokenValue)
                .list();
        for (RefreshToken refreshToken : refreshTokenList) {
            dataManager.remove(refreshToken);
        }
//        try (Transaction tx = persistence.getTransaction()) {
//            EntityManager em = persistence.getEntityManager();
//            em.createQuery("delete from sys$RefreshToken t where t.tokenValue = :tokenValue")
//                    .setParameter("tokenValue", refreshTokenValue)
//                    .executeUpdate();
//            tx.commit();
//        }
    }

    @Override
    public void deleteExpiredTokens() {
        deleteExpiredAccessTokensInMemory();
        deleteExpiredRefreshTokensInMemory();
        if (tokenStoreSupport.isRestStoreTokensInDb() && clusterManagerAPI.isMaster()) {
            deleteExpiredAccessTokensInDatabase();
            deleteExpiredRefreshTokensInDatabase();
        }
    }

    protected byte[] getRefreshTokenByTokenValueFromMemory(String tokenValue) {
        return refreshTokenValueToRefreshTokenStore.get(tokenValue);
    }

    @Override
    public byte[] getAuthenticationByRefreshTokenValue(String tokenValue) {
        return refreshTokenValueToAuthenticationStore.get(tokenValue);
    }

    @Override
    public void removeAccessTokenUsingRefreshToken(String refreshTokenValue) {
        String accessTokenValue = getAccessTokenValueByRefreshTokenValue(refreshTokenValue);
        if (accessTokenValue != null)
            removeAccessToken(accessTokenValue);
    }

    protected String getAccessTokenValueByRefreshTokenValue(String refreshTokenValue) {
        String accessTokenValue = refreshTokenValueToAccessTokenValueStore.get(refreshTokenValue);
        if (accessTokenValue == null && tokenStoreSupport.isRestStoreTokensInDb()) {
            accessTokenValue = getAccessTokenValueByRefreshTokenValueFromDatabase(refreshTokenValue);
        }
        return accessTokenValue;
    }

    @Nullable
    protected String getAccessTokenValueByRefreshTokenValueFromDatabase(String refreshTokenValue) {
//        String accessTokenValue;
//        try (Transaction tx = persistence.createTransaction()) {
//            EntityManager em = persistence.getEntityManager();
//            accessTokenValue = em.createQuery(
//                    "select e.tokenValue from sys$AccessToken e where e.refreshTokenValue = :refreshTokenValue", String.class)
//                    .setParameter("refreshTokenValue", refreshTokenValue)
//                    .getFirstResult();
//            tx.commit();
//            return accessTokenValue;
//        }
        return dataManager.loadValue("select e.tokenValue from sys$AccessToken e where e.refreshTokenValue = :refreshTokenValue", String.class)
                .parameter("refreshTokenValue", refreshTokenValue)
                .one();
    }

    protected void deleteExpiredAccessTokensInMemory() {
        TokenExpiry expiry = accessTokensExpiryQueue.poll();
        while (expiry != null) {
            removeAccessToken(expiry.getValue());
            expiry = accessTokensExpiryQueue.poll();
        }
    }

    protected void deleteExpiredRefreshTokensInMemory() {
        TokenExpiry expiry = refreshTokensExpiryQueue.poll();
        while (expiry != null) {
            removeRefreshToken(expiry.getValue());
            expiry = refreshTokensExpiryQueue.poll();
        }
    }

    protected void deleteExpiredAccessTokensInDatabase() {
//        try (Transaction tx = persistence.createTransaction()) {
//            EntityManager em = persistence.getEntityManager();
//            em.createQuery("delete from sys$AccessToken t where t.expiry < CURRENT_TIMESTAMP")
//                    .executeUpdate();
//            tx.commit();
//        }
        List<AccessToken> accessTokenList = dataManager.load(AccessToken.class)
                .query("select t from sys$AccessToken t where t.expiry < CURRENT_TIMESTAMP")
                .list();
        for (AccessToken accessToken : accessTokenList) {
            dataManager.remove(accessToken);
        }

    }

    protected void deleteExpiredRefreshTokensInDatabase() {
//        try (Transaction tx = persistence.createTransaction()) {
//            EntityManager em = persistence.getEntityManager();
//            em.createQuery("delete from sys$RefreshToken t where t.expiry < CURRENT_TIMESTAMP")
//                    .executeUpdate();
//            tx.commit();
//        }
        List<RefreshToken> refreshTokenList = dataManager.load(RefreshToken.class)
                .query("select t from sys$RefreshToken t where t.expiry < CURRENT_TIMESTAMP")
                .list();
        for (RefreshToken refreshToken : refreshTokenList) {
            dataManager.remove(refreshToken);
        }
    }


    protected static class TokenExpiry implements Delayed {

        protected final long expiry;

        protected final String value;

        public TokenExpiry(String value, Date date) {
            this.value = value;
            this.expiry = date.getTime();
        }

        @Override
        public int compareTo(Delayed other) {
            if (this == other) {
                return 0;
            }
            long diff = getDelay(TimeUnit.MILLISECONDS) - other.getDelay(TimeUnit.MILLISECONDS);
            return (diff == 0 ? 0 : ((diff < 0) ? -1 : 1));
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return expiry - System.currentTimeMillis();
        }

        public String getValue() {
            return value;
        }
    }
}
