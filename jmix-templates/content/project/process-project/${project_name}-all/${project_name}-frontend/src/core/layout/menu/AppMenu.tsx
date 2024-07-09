import {Link, useLocation} from "react-router-dom";
import {AppstoreOutlined, PartitionOutlined, SolutionOutlined} from "@ant-design/icons";
import {Menu} from "antd";
import {ItemType} from "antd/es/menu/interface";
import "./AppMenu.css";

const menuItems: ItemType[] = [
    {
        label: (
            <Link to="dashboard">
              Dashboard
            </Link>
        ),
        key: "dashboard",
        icon: <AppstoreOutlined style={{fontSize: "inherit"}}/>,
    },

    {
        label: (
            <Link to="tasks">
                Tasks
            </Link>
        ),
        key: "tasks",
        icon: <SolutionOutlined style={{fontSize: "inherit"}}/>
    },
    {
        label: (
            <Link to="processes">
                Processes
            </Link>
        ),
        key: "processes",
        icon: <PartitionOutlined style={{fontSize: "inherit"}}/>
    }
]

export const AppMenu = () => {
    const {pathname} = useLocation();
    const selectedKey = toSelectedKey(pathname);

    return (
        <>
            <Menu className="app-header__app-menu"
                  selectedKeys={[selectedKey]}
                  items={menuItems} mode="horizontal" theme="dark"/>
        </>
    );
};

const toSelectedKey = (pathname: string) => {
    if (pathname.length === 0) {
        return "dashboard";
    }
    return pathname.split("/", 2).join("");
}