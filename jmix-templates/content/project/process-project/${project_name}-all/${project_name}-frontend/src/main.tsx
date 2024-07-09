import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App.tsx'
import './index.css'
import {DevSupport} from "@react-buddy/ide-toolbox";
import {ComponentPreviews, useInitial} from "./dev";
import {BrowserRouter} from "react-router-dom";
import {ProtectedApp} from "./core/security/ProtectedApp.tsx";
import {ConfigProvider} from "antd";
import {AuthProvider} from "react-oidc-context";
import {onSigninCallback, userManager} from "./core/security/config.ts";


ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        <DevSupport ComponentPreviews={ComponentPreviews}
                    useInitialHook={useInitial}>
            <BrowserRouter>
                <AuthProvider userManager={userManager} onSigninCallback={onSigninCallback}>
                    <ProtectedApp>
                        <ConfigProvider theme={{
                            cssVar: {key: 'jmix-bpm-tasklist'}, components: {
                                Table: {
                                    headerBg: "#2281fa",
                                    headerColor: "#fff",
                                    headerSortHoverBg: "rgba(64,150,255,0.8)",
                                    bodySortBg: "#fff",
                                    headerSortActiveBg: "#4196fd",
                                    borderRadius: 0,
                                }
                            }
                        }}>
                            <App/>
                        </ConfigProvider>
                    </ProtectedApp>
                </AuthProvider>
            </BrowserRouter>
        </DevSupport>
    </React.StrictMode>
)
