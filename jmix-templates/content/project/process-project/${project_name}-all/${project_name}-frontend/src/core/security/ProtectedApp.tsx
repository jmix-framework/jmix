import {PropsWithChildren, useEffect, useState} from "react";
import {hasAuthParams, useAuth} from "react-oidc-context";
import {SplashScreen} from "../../page/splash/SplashScreen.tsx";

export const ProtectedApp = ({children}: PropsWithChildren) => {
    const auth = useAuth();
    const [hasTriedSignin, setHasTriedSignin] = useState(false);

    useEffect(() => {
        if (!hasAuthParams() &&
            !auth.isAuthenticated && !auth.activeNavigator && !auth.isLoading &&
            !hasTriedSignin
        ) {
            auth.signinRedirect();
            setHasTriedSignin(true);
        }
    }, [auth, hasTriedSignin]);

    if (auth.isLoading) {
        return <SplashScreen/>
    }

    if (!auth.isAuthenticated) {
        return <div>Unable to log in</div>;
    }

    return (
        <>
            {children}
        </>
    );
};