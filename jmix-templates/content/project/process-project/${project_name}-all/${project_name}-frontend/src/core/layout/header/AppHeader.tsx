import {UserPanel} from "../../security/UserPanel.tsx";
import "./AppHeader.css";
import {AppMenu} from "../menu/AppMenu.tsx";
import Title from "antd/es/typography/Title";
import {Flex, theme} from "antd";

export const AppHeader = () => {
    const {
        token: {colorBgContainer},
    } = theme.useToken();
    return (
        <>
            <Flex className="app-header__app-logo" gap={5}>
                <img src="/logo-dark.svg" className="app-header__icon" alt=""/>
                <Title level={4} style={{color: colorBgContainer, margin: "auto"}}>Tasklist</Title>
            </Flex>
            <AppMenu/>
            <div className="app-header__controls">
                <UserPanel/>
            </div>
        </>
    );
};