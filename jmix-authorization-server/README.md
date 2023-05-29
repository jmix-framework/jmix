# Jmix Authorization Server

**CAUTION: This add-on is now in the incubating state and its API and behavior may be modified in the future minor and patch releases.**

## Add-On Overview

In a few words, Jmix Authorization Server add-on allows you to issue access and refresh tokens and protect API resources (REST API, custom controllers) with these tokens.  The add-on is built on top of [Spring Authorization Server](https://spring.io/projects/spring-authorization-server). 

Jmix Authorization Server is a replacement for Jmix Security OAuth2 module that depends on outdated [Spring Security OAuth](https://spring.io/projects/spring-security-oauth) project that has reached end of life.

The Jmix Authorization Server add-on features:

* Contains predefined Spring configurations for working as participant with "authorization server" and "resource server" roles described in OAuth 2.1 protocol flows. This means that your Jmix application may issue access and refresh tokens and protect API resources with these tokens.
* Supports authorization code grant for web clients and mobile devices.
* Supports client credentials grant for server-to-server interaction.
* Only **opaque** tokens are supported out of the box.

## Adding Add-on to the Application

1. If there is a `jmix-security-oauth2-starter` dependency in the `build.gradle` (it is added automatically when you add the REST API add-on) then you need to **remove** it:
```groovy
implementation 'io.jmix.security:jmix-security-oauth2-starter'
```

2. Add the `jmix-authorization-server-starter` dependency
```groovy
implementation 'io.jmix.authorizationserver:jmix-authorization-server-starter'
```

## Auto-Configuration

When the add-on is included to the application the auto-configuration does initial setup:

* `SecurityFilterChain` is added for OAuth2 protocol endpoints (token endpoint, authorization endpoint etc.). 
* `SecurityFilterChain` is added for login form.
* `InMemoryClientRepository` is registered.
* Default `RegisteredClientProvider` is registered that creates a RegisteredClient based on application properties (read below).
* `SecurityFilterChain` for resource server configuration (URLs that must be protected using access tokens).

If you want to completely disable the default auto-configuration and provide your own one, set the following application property:

```properties
jmix.authorization-server.use-default-configuration=false
```

## RegisteredClientProvider

The interface is used to provide a list of `RegisteredClient` that must be added to the clients repository. You may define your own implementations of the interface in the project. The add-on provides the default implementation `DefaultRegisteredClientProvider` that registers a single client that may be configured using application properties.

```properties
jmix.authorization-server.default-client.client-id=someclient
jmix.authorization-server.default-client.client-secret={noop}somesecret
jmix.authorization-server.default-client.access-token-time-to-live=60m
jmix.authorization-server.default-client.refresh-token-time-to-live=10d
```

Custom implementation of the interface may look as follows:

```java
@Component
public class MyRegisteredClientProvider implements RegisteredClientProvider {
    @Override
    public List<RegisteredClient> getRegisteredClients() {
        RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("client1")
                .clientSecret("{noop}secret1")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .redirectUris(uris -> {
                    uris.add("http://localhost:8080/authorized");
                    uris.add("https://oauth.pstmn.io/v1/callback");
                })
                .tokenSettings(TokenSettings.builder()
                        .accessTokenFormat(OAuth2TokenFormat.REFERENCE)
                        .build())
                .build();
        return List.of(client);
    }
}
```

## Obtaining Access Token

### Authorization Code Grant Type

When obtaining the token from web or mobile application, the client must first request the authorization code:

```
GET /oauth2/authorize?response_type=code&client_id=<client_id>&redirect_uri=<redirect_uri>
```

A special login page will be displayed where the user must enter their credentials. If credentials are valid, a request to
the redirect_uri will be performed with authorization code in the request parameter.

To exchange the authorization code to the access token the client application must make a request to the following URL:

```
POST /oauth2/token
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code&code=<authorization_code>
&redirect_uri=<redirect_uri>
```

Access and refresh tokens will be returned in the response.

### Client Credentials Grant Type

When it is not possible to enter user credentials in the browser login window, e.g. in case of some integration between 
two applications, the client credentials flow may be used. There must be Basic authentication on behalf of one of registered clients. 

```
   POST /oauth2/token
   Authorization: Basic czZCaGRSa3F0MzpnWDFmQmF0M2JW
   Content-Type: application/x-www-form-urlencoded

   grant_type=client_credentials
```

When access token is used for accessing protected API the user with the username equal to the client id will be searched. If the user is found the user will be put as authentication principal to the security context and all operations will be executed with permissions of that user. If no such user exists an exception will be thrown. 

## Protecting API Endpoints 

The authorization server add-on by default validates the bearer access token when accessing the following URLs:

* URLs of REST API add-on (/rest/**)
* URLs defined in the `jmix.rest.authenticated-url-patterns` application property
* URLs returned by implementations of the `io.jmix.core.security.AuthorizedUrlsProvider` interface

Token introspection is performed by checking that the token from request header exists in the `OAuth2AuthorizationService`.