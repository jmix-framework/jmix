import {UserManager, WebStorageStateStore} from "oidc-client-ts";

export const userManager = new UserManager({
    authority: import.meta.env.VITE_OIDC_REALM_URL ?? "http://localhost:8180/realms/realm-name",
    client_id: import.meta.env.VITE_OIDC_CLIENT_ID ?? "client-id",
    redirect_uri: import.meta.env.VITE_OIDC_REDIRECT_URI ?? "http://localhost:3000/",
    post_logout_redirect_uri: window.location.origin,
    userStore: new WebStorageStateStore({store: window.sessionStorage}),
})

export const onSigninCallback = () => {
    window.history.replaceState({}, document.title, window.location.pathname);
};