import {Spin} from "antd";
import Title from "antd/es/typography/Title";
import "./DataLoading.css";

export const DataLoading = () => {
    return <div className="tasklist-data-loading__root">
        <div className="tasklist-data-loading__container">
            <Spin/>
            <Title level={3}>Loading...</Title>
        </div>
    </div>
}