import Button from "antd/es/button";
import {LogoutOutlined, UserOutlined} from "@ant-design/icons";
import {Modal, Space} from "antd";
import {useCallback} from "react";
import Text from "antd/es/typography/Text";
import {useAuth} from "react-oidc-context";
import "./UserPanel.css";

export const UserPanel = () => {
    const auth = useAuth();
    const username = auth.user?.profile.preferred_username;
    const user = username || "unknown user";

    const showLogoutConfirm = useCallback(() => {
        Modal.confirm({
            content: "Are you sure you want to logout?",
            okText: "OK",
            cancelText: "Cancel",
            onOk: async () => {
                await auth.signoutRedirect();
            },
        });
    }, [auth]);

    return (
        <>
            <Space>
                <UserOutlined className="app-header__user-icon"/>
                <Text className="app-header__user-label">{user}</Text>
                <Button shape="circle"
                        className="app-header__icon-btn"
                        type="text"
                        icon={<LogoutOutlined/>}
                        onClick={showLogoutConfirm}
                />
            </Space>
        </>
    );
};