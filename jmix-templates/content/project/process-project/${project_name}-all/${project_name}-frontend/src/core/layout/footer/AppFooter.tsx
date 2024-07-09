import {Flex, FlexProps} from "antd";
import Text from "antd/es/typography/Text";
import "./AppFooter.css";

export const AppFooter = (props: Omit<FlexProps, "children">) => {
    const {justify, ...rest} = props;
    return (
        <Flex justify={justify ? justify : "center"} {...rest}>
            <Text className="app-footer__text" type="secondary">Made with Jmee Plaftorm</Text>
        </Flex>
    );
};