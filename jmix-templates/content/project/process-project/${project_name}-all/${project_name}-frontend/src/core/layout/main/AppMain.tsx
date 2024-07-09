import {Layout} from "antd";
import {AppHeader} from "../header/AppHeader.tsx";
import "./AppMain.css";
import {useAuth} from "react-oidc-context";
import {AppContent} from "../content/AppContent.tsx";

const {Header, Content} = Layout;

export const AppMain = () => {
    const {isAuthenticated} = useAuth();

    return (
        <>
            <Layout className="main-layout">
                {isAuthenticated && <Header className="main-layout__header">
                    <AppHeader/>
                </Header>}
                <Layout>
                    <Content className="main-layout__content">
                        <AppContent/>
                    </Content>
                </Layout>
            </Layout>
        </>
    );
};