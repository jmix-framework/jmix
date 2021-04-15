
CREATE TABLE scr_association_m2m_test_entity (
    id uuid NOT NULL,
    name character varying(255)
);

CREATE TABLE scr_association_m2o_test_entity (
    id uuid NOT NULL,
    name character varying(255)
);

CREATE TABLE scr_association_o2m_test_entity (
    id uuid NOT NULL,
    datatypes_test_entity_id uuid,
    name character varying(255)
);

CREATE TABLE scr_association_o2o_test_entity (
    id uuid NOT NULL,
    name character varying(255)
);

CREATE TABLE scr_boring_string_id_test_entity (
    id character varying(10) NOT NULL,
    description character varying(255),
    create_ts timestamp without time zone,
    created_by character varying(50),
    delete_ts timestamp without time zone,
    deleted_by character varying(50),
    update_ts timestamp without time zone,
    updated_by character varying(50),
    version integer NOT NULL,
    uuid uuid
);

CREATE TABLE scr_car (
    id uuid NOT NULL,
    manufacturer character varying(255) NOT NULL,
    model character varying(255),
    reg_number character varying(5),
    purchase_date date,
    manufacture_date timestamp without time zone,
    wheel_on_right boolean,
    car_type character varying(255) NOT NULL,
    eco_rank integer,
    garage_id uuid,
    max_passengers integer,
    price numeric,
    mileage double precision,
    technical_certificate_id uuid,
    created_by character varying(255),
    created_date date,
    last_modified_by character varying(255),
    last_modified_date date,
    version integer NOT NULL,
    photo character varying(255)
);

CREATE TABLE scr_car_rent (
    id uuid NOT NULL,
    car_id uuid NOT NULL,
    from_date date,
    from_time time without time zone,
    from_date_time timestamp without time zone
);

CREATE TABLE scr_composition_o2m_test_entity (
    id uuid NOT NULL,
    datatypes_test_entity_id uuid,
    quantity integer,
    name character varying(255)
);

CREATE TABLE scr_composition_o2o_test_entity (
    id uuid NOT NULL,
    name character varying(255),
    quantity integer,
    nested_composition_id uuid
);

CREATE TABLE scr_customer (
    id uuid NOT NULL,
    name character varying(255),
    email character varying(255)
);

CREATE TABLE scr_datatypes_test_entity (
    id uuid NOT NULL,
    big_decimal_attr numeric,
    boolean_attr boolean,
    date_attr date,
    date_time_attr timestamp without time zone,
    double_attr double precision,
    integer_attr integer,
    long_attr bigint,
    string_attr character varying(255),
    time_attr time without time zone,
    uuid_attr uuid,
    local_date_time_attr timestamp without time zone,
    offset_date_time_attr timestamp with time zone,
    local_date_attr date,
    local_time_attr time without time zone,
    offset_time_attr time with time zone,
    enum_attr character varying(255),
    association_o2_oattr_id uuid,
    association_m2_oattr_id uuid,
    composition_o2_oattr_id uuid,
    name character varying(255),
    int_identity_id_test_entity_association_o2o_attr_id integer,
    datatypes_test_entity3_id uuid,
    string_id_test_entity_association_o2o_identifier character varying(10),
    string_id_test_entity_association_m2o_id character varying(10),
    read_only_string_attr character varying(255)
);

CREATE TABLE scr_datatypes_test_entity2 (
    id uuid NOT NULL,
    datatypes_test_entity_attr_id uuid,
    int_identity_id_test_entity_attr_id integer,
    integer_id_test_entity_attr_id integer,
    string_id_test_entity_attr_identifier character varying(10),
    weird_string_id_test_entity_attr_identifier character varying(10)
);

CREATE TABLE scr_datatypes_test_entity3 (
    id uuid NOT NULL,
    name character varying(255)
);

CREATE TABLE scr_datatypes_test_entity_association_m2m_test_entity_link (
    association_m2_m_test_entity_id uuid NOT NULL,
    datatypes_test_entity_id uuid NOT NULL
);

CREATE TABLE scr_datatypes_test_entity_integer_id_test_entity_link (
    datatypes_test_entity_id uuid NOT NULL,
    integer_id_test_entity_id integer NOT NULL
);

CREATE TABLE scr_deeply_nested_test_entity (
    id uuid NOT NULL,
    name character varying(255),
    association_o2_oattr_id uuid
);

CREATE TABLE scr_favorite_car (
    id uuid NOT NULL,
    car_id uuid NOT NULL,
    user_id uuid NOT NULL,
    notes character varying(255)
);

CREATE TABLE scr_garage (
    id uuid NOT NULL,
    name character varying(255) NOT NULL,
    address character varying(255),
    capacity integer,
    van_entry boolean,
    working_hours_from time without time zone,
    working_hours_to time without time zone
);

CREATE TABLE scr_garage_user_link (
    garage_id uuid NOT NULL,
    user_id uuid NOT NULL
);

CREATE TABLE scr_int_identity_id_test_entity (
    id integer NOT NULL,
    description character varying(255),
    update_ts timestamp without time zone,
    updated_by character varying(50),
    delete_ts timestamp without time zone,
    deleted_by character varying(50),
    create_ts timestamp without time zone,
    created_by character varying(50),
    version integer NOT NULL,
    datatypes_test_entity3_id uuid
);

CREATE TABLE scr_integer_id_test_entity (
    id integer NOT NULL,
    description character varying(255),
    create_ts timestamp without time zone,
    created_by character varying(50),
    update_ts timestamp without time zone,
    updated_by character varying(50),
    delete_ts timestamp without time zone,
    deleted_by character varying(50),
    version integer NOT NULL,
    datatypes_test_entity3_id uuid
);

CREATE TABLE scr_order (
    id uuid NOT NULL,
    date_ timestamp without time zone,
    amount numeric(19,2),
    customer_id uuid
);

CREATE TABLE scr_order_line (
    id uuid NOT NULL,
    product_id uuid NOT NULL,
    quantity integer,
    order_id uuid
);

CREATE TABLE scr_product (
    id uuid NOT NULL,
    car_id uuid NOT NULL,
    price numeric(19,2),
    special boolean
);

CREATE TABLE scr_spare_part (
    id uuid NOT NULL,
    name character varying(255),
    spare_parts_id uuid
);

CREATE TABLE scr_string_id_test_entity (
    identifier character varying(10) NOT NULL,
    description character varying(255),
    product_code character varying(10),
    create_ts timestamp without time zone,
    created_by character varying(50),
    update_ts timestamp without time zone,
    updated_by character varying(50),
    delete_ts timestamp without time zone,
    deleted_by character varying(50),
    version integer NOT NULL,
    datatypes_test_entity3_id uuid
);

CREATE TABLE scr_technical_certificate (
    id uuid NOT NULL,
    cert_number character varying(255)
);

CREATE TABLE scr_tricky_id_test_entity (
    tricky_id bigint NOT NULL,
    other_attr character varying(255)
);

CREATE TABLE scr_user (
    id uuid NOT NULL,
    version integer NOT NULL,
    username character varying(255) NOT NULL,
    password character varying(255),
    first_name character varying(255),
    last_name character varying(255),
    email character varying(255),
    enabled boolean,
    phone character varying(255)
);

CREATE TABLE scr_weird_string_id_test_entity (
    identifier character varying(10) NOT NULL,
    description character varying(255),
    id character varying(255),
    create_ts timestamp without time zone,
    created_by character varying(50),
    update_ts timestamp without time zone,
    updated_by character varying(50),
    delete_ts timestamp without time zone,
    deleted_by character varying(50),
    version integer NOT NULL,
    datatypes_test_entity3_id uuid
);

CREATE TABLE sec_resource_policy (
    id uuid NOT NULL,
    version integer DEFAULT 1 NOT NULL,
    create_ts timestamp without time zone,
    created_by character varying(50),
    update_ts timestamp without time zone,
    updated_by character varying(50),
    delete_ts timestamp without time zone,
    deleted_by character varying(50),
    type_ character varying(255) NOT NULL,
    policy_group character varying(255),
    resource_ character varying(1000) NOT NULL,
    action_ character varying(255) NOT NULL,
    effect character varying(255) NOT NULL,
    role_id uuid NOT NULL
);

CREATE TABLE sec_resource_role (
    id uuid NOT NULL,
    version integer DEFAULT 1 NOT NULL,
    create_ts timestamp without time zone,
    created_by character varying(50),
    update_ts timestamp without time zone,
    updated_by character varying(50),
    delete_ts timestamp without time zone,
    deleted_by character varying(50),
    name character varying(255) NOT NULL,
    code character varying(255) NOT NULL,
    child_roles character ,
    sys_tenant_id character varying(255),
    description character
);

CREATE TABLE sec_role_assignment (
    id uuid NOT NULL,
    version integer DEFAULT 1 NOT NULL,
    create_ts timestamp without time zone,
    created_by character varying(50),
    update_ts timestamp without time zone,
    updated_by character varying(50),
    delete_ts timestamp without time zone,
    deleted_by character varying(50),
    username character varying(255) NOT NULL,
    role_code character varying(255) NOT NULL,
    role_type character varying(255) NOT NULL
);

CREATE TABLE sec_row_level_policy (
    id uuid NOT NULL,
    version integer DEFAULT 1 NOT NULL,
    create_ts timestamp without time zone,
    created_by character varying(50),
    update_ts timestamp without time zone,
    updated_by character varying(50),
    delete_ts timestamp without time zone,
    deleted_by character varying(50),
    type_ character varying(255) NOT NULL,
    action_ character varying(255) NOT NULL,
    entity_name character varying(255) NOT NULL,
    where_clause character ,
    join_clause character ,
    script_ character ,
    role_id uuid NOT NULL
);

CREATE TABLE sec_row_level_role (
    id uuid NOT NULL,
    version integer DEFAULT 1 NOT NULL,
    create_ts timestamp without time zone,
    created_by character varying(50),
    update_ts timestamp without time zone,
    updated_by character varying(50),
    delete_ts timestamp without time zone,
    deleted_by character varying(50),
    name character varying(255) NOT NULL,
    code character varying(255) NOT NULL,
    child_roles character,
    sys_tenant_id character varying(255),
    description character
);

CREATE TABLE ui_filter_configuration (
    id uuid NOT NULL,
    component_id character varying(255) NOT NULL,
    configuration_id character varying(255) NOT NULL,
    username character varying(255),
    root_condition character ,
    sys_tenant_id character varying(255),
    name character varying(255),
    default_for_all boolean
);

CREATE TABLE ui_setting (
    id uuid NOT NULL,
    create_ts timestamp without time zone,
    created_by character varying(50),
    username character varying(255),
    name character varying(255),
    value_ character
);

CREATE TABLE ui_table_presentation (
    id uuid NOT NULL,
    create_ts timestamp without time zone,
    created_by character varying(50),
    component character varying(255),
    name character varying(255),
    settings character varying(4000),
    username character varying(255),
    is_auto_save boolean,
    update_ts timestamp without time zone,
    updated_by character varying(50),
    sys_tenant_id character varying(255)
);

ALTER TABLE scr_association_m2m_test_entity
    ADD CONSTRAINT pk_scr_association_m2m_test_entity PRIMARY KEY (id);

ALTER TABLE scr_association_m2o_test_entity
    ADD CONSTRAINT pk_scr_association_m2o_test_entity PRIMARY KEY (id);

ALTER TABLE scr_association_o2m_test_entity
    ADD CONSTRAINT pk_scr_association_o2m_test_entity PRIMARY KEY (id);

ALTER TABLE scr_association_o2o_test_entity
    ADD CONSTRAINT pk_scr_association_o2o_test_entity PRIMARY KEY (id);

ALTER TABLE scr_boring_string_id_test_entity
    ADD CONSTRAINT pk_scr_boring_string_id_test_entity PRIMARY KEY (id);

ALTER TABLE scr_car
    ADD CONSTRAINT pk_scr_car PRIMARY KEY (id);

ALTER TABLE scr_car_rent
    ADD CONSTRAINT pk_scr_car_rent PRIMARY KEY (id);

ALTER TABLE scr_composition_o2m_test_entity
    ADD CONSTRAINT pk_scr_composition_o2m_test_entity PRIMARY KEY (id);

ALTER TABLE scr_composition_o2o_test_entity
    ADD CONSTRAINT pk_scr_composition_o2o_test_entity PRIMARY KEY (id);

ALTER TABLE scr_customer
    ADD CONSTRAINT pk_scr_customer PRIMARY KEY (id);

ALTER TABLE scr_datatypes_test_entity
    ADD CONSTRAINT pk_scr_datatypes_test_entity PRIMARY KEY (id);

ALTER TABLE scr_datatypes_test_entity2
    ADD CONSTRAINT pk_scr_datatypes_test_entity2 PRIMARY KEY (id);

ALTER TABLE scr_datatypes_test_entity3
    ADD CONSTRAINT pk_scr_datatypes_test_entity3 PRIMARY KEY (id);

ALTER TABLE scr_datatypes_test_entity_association_m2m_test_entity_link
    ADD CONSTRAINT pk_scr_datatypes_test_entity_association_m2m_test_entity_link PRIMARY KEY (association_m2_m_test_entity_id, datatypes_test_entity_id);

ALTER TABLE scr_datatypes_test_entity_integer_id_test_entity_link
    ADD CONSTRAINT pk_scr_datatypes_test_entity_integer_id_test_entity_link PRIMARY KEY (datatypes_test_entity_id, integer_id_test_entity_id);

ALTER TABLE scr_deeply_nested_test_entity
    ADD CONSTRAINT pk_scr_deeply_nested_test_entity PRIMARY KEY (id);

ALTER TABLE scr_favorite_car
    ADD CONSTRAINT pk_scr_favorite_car PRIMARY KEY (id);

ALTER TABLE scr_garage
    ADD CONSTRAINT pk_scr_garage PRIMARY KEY (id);

ALTER TABLE scr_garage_user_link
    ADD CONSTRAINT pk_scr_garage_user_link PRIMARY KEY (garage_id, user_id);

ALTER TABLE scr_int_identity_id_test_entity
    ADD CONSTRAINT pk_scr_int_identity_id_test_entity PRIMARY KEY (id);

ALTER TABLE scr_integer_id_test_entity
    ADD CONSTRAINT pk_scr_integer_id_test_entity PRIMARY KEY (id);

ALTER TABLE scr_order
    ADD CONSTRAINT pk_scr_order PRIMARY KEY (id);

ALTER TABLE scr_order_line
    ADD CONSTRAINT pk_scr_order_line PRIMARY KEY (id);

ALTER TABLE scr_product
    ADD CONSTRAINT pk_scr_product PRIMARY KEY (id);

ALTER TABLE scr_spare_part
    ADD CONSTRAINT pk_scr_spare_part PRIMARY KEY (id);

ALTER TABLE scr_string_id_test_entity
    ADD CONSTRAINT pk_scr_string_id_test_entity PRIMARY KEY (identifier);

ALTER TABLE scr_technical_certificate
    ADD CONSTRAINT pk_scr_technical_certificate PRIMARY KEY (id);

ALTER TABLE scr_tricky_id_test_entity
    ADD CONSTRAINT pk_scr_tricky_id_test_entity PRIMARY KEY (tricky_id);

ALTER TABLE scr_user
    ADD CONSTRAINT pk_scr_user PRIMARY KEY (id);

ALTER TABLE scr_weird_string_id_test_entity
    ADD CONSTRAINT pk_scr_weird_string_id_test_entity PRIMARY KEY (identifier);

ALTER TABLE sec_resource_policy
    ADD CONSTRAINT sec_resource_policy_pkey PRIMARY KEY (id);

ALTER TABLE sec_resource_role
    ADD CONSTRAINT sec_resource_role_pkey PRIMARY KEY (id);

ALTER TABLE sec_role_assignment
    ADD CONSTRAINT sec_role_assignment_pkey PRIMARY KEY (id);

ALTER TABLE sec_row_level_policy
    ADD CONSTRAINT sec_row_level_policy_pkey PRIMARY KEY (id);

ALTER TABLE sec_row_level_role
    ADD CONSTRAINT sec_row_level_role_pkey PRIMARY KEY (id);

ALTER TABLE ui_filter_configuration
    ADD CONSTRAINT ui_filter_configuration_pkey PRIMARY KEY (id);

ALTER TABLE ui_setting
    ADD CONSTRAINT ui_setting_pkey PRIMARY KEY (id);

ALTER TABLE ui_table_presentation
    ADD CONSTRAINT ui_table_presentation_pkey PRIMARY KEY (id);

CREATE INDEX idx_resource_role_un_c ON sec_resource_role (code);

CREATE INDEX idx_row_level_role_un_c ON sec_row_level_role (code);

CREATE UNIQUE INDEX idx_scr_user_on_username ON scr_user (username);

ALTER TABLE sec_resource_policy
    ADD CONSTRAINT fk_res_policy_role FOREIGN KEY (role_id) REFERENCES sec_resource_role(id);

ALTER TABLE sec_row_level_policy
    ADD CONSTRAINT fk_row_level_policy_role FOREIGN KEY (role_id) REFERENCES sec_row_level_role(id);

ALTER TABLE scr_association_o2m_test_entity
    ADD CONSTRAINT fk_scr_association_o2m_test_entity_on_datatypes_test_entity FOREIGN KEY (datatypes_test_entity_id) REFERENCES scr_datatypes_test_entity(id);

ALTER TABLE scr_car
    ADD CONSTRAINT fk_scr_car_on_garage FOREIGN KEY (garage_id) REFERENCES scr_garage(id);

ALTER TABLE scr_car
    ADD CONSTRAINT fk_scr_car_on_technical_certificate FOREIGN KEY (technical_certificate_id) REFERENCES scr_technical_certificate(id);

ALTER TABLE scr_car_rent
    ADD CONSTRAINT fk_scr_car_rent_on_car FOREIGN KEY (car_id) REFERENCES scr_car(id);

ALTER TABLE scr_composition_o2m_test_entity
    ADD CONSTRAINT fk_scr_composition_o2m_test_entity_on_datatypes_test_entity FOREIGN KEY (datatypes_test_entity_id) REFERENCES scr_datatypes_test_entity(id);

ALTER TABLE scr_composition_o2o_test_entity
    ADD CONSTRAINT fk_scr_composition_o2o_test_entity_on_nested_composition FOREIGN KEY (nested_composition_id) REFERENCES scr_deeply_nested_test_entity(id);

ALTER TABLE scr_datatypes_test_entity2
    ADD CONSTRAINT fk_scr_datatypes_test_entity2_on_integer_id_test_entity_attr FOREIGN KEY (integer_id_test_entity_attr_id) REFERENCES scr_integer_id_test_entity(id);

ALTER TABLE scr_datatypes_test_entity
    ADD CONSTRAINT fk_scr_datatypes_test_entity_on_association_o2_oattr FOREIGN KEY (association_o2_oattr_id) REFERENCES scr_association_o2o_test_entity(id);

ALTER TABLE scr_datatypes_test_entity
    ADD CONSTRAINT fk_scr_datatypes_test_entity_on_composition_o2_oattr FOREIGN KEY (composition_o2_oattr_id) REFERENCES scr_composition_o2o_test_entity(id);

ALTER TABLE scr_datatypes_test_entity
    ADD CONSTRAINT fk_scr_datatypes_test_entity_on_datatypes_test_entity3 FOREIGN KEY (datatypes_test_entity3_id) REFERENCES scr_datatypes_test_entity3(id);

ALTER TABLE scr_deeply_nested_test_entity
    ADD CONSTRAINT fk_scr_deeply_nested_test_entity_on_association_o2_oattr FOREIGN KEY (association_o2_oattr_id) REFERENCES scr_association_o2o_test_entity(id);

ALTER TABLE scr_favorite_car
    ADD CONSTRAINT fk_scr_favorite_car_on_car FOREIGN KEY (car_id) REFERENCES scr_car(id);

ALTER TABLE scr_favorite_car
    ADD CONSTRAINT fk_scr_favorite_car_on_user FOREIGN KEY (user_id) REFERENCES scr_user(id);

ALTER TABLE scr_int_identity_id_test_entity
    ADD CONSTRAINT fk_scr_int_identity_id_test_entity_on_datatypes_test_entity3 FOREIGN KEY (datatypes_test_entity3_id) REFERENCES scr_datatypes_test_entity3(id);

ALTER TABLE scr_integer_id_test_entity
    ADD CONSTRAINT fk_scr_integer_id_test_entity_on_datatypes_test_entity3 FOREIGN KEY (datatypes_test_entity3_id) REFERENCES scr_datatypes_test_entity3(id);

ALTER TABLE scr_order_line
    ADD CONSTRAINT fk_scr_order_line_on_order FOREIGN KEY (order_id) REFERENCES scr_order(id);

ALTER TABLE scr_order_line
    ADD CONSTRAINT fk_scr_order_line_on_product FOREIGN KEY (product_id) REFERENCES scr_product(id);

ALTER TABLE scr_order
    ADD CONSTRAINT fk_scr_order_on_customer FOREIGN KEY (customer_id) REFERENCES scr_customer(id);

ALTER TABLE scr_product
    ADD CONSTRAINT fk_scr_product_on_car FOREIGN KEY (car_id) REFERENCES scr_car(id);

ALTER TABLE scr_spare_part
    ADD CONSTRAINT fk_scr_spare_part_on_spare_parts FOREIGN KEY (spare_parts_id) REFERENCES scr_spare_part(id);

ALTER TABLE scr_string_id_test_entity
    ADD CONSTRAINT fk_scr_string_id_test_entity_on_datatypes_test_entity3 FOREIGN KEY (datatypes_test_entity3_id) REFERENCES scr_datatypes_test_entity3(id);

ALTER TABLE scr_weird_string_id_test_entity
    ADD CONSTRAINT fk_scr_weird_string_id_test_entity_on_datatypes_test_entity3 FOREIGN KEY (datatypes_test_entity3_id) REFERENCES scr_datatypes_test_entity3(id);

ALTER TABLE scr_datatypes_test_entity2
    ADD CONSTRAINT fk_scrdatatypestestentity2_on_weirdstringidtestentityattridenti FOREIGN KEY (weird_string_id_test_entity_attr_identifier) REFERENCES scr_weird_string_id_test_entity(identifier);

ALTER TABLE scr_datatypes_test_entity
    ADD CONSTRAINT fk_scrdatatypestestentity_on_intidentityidtestentityassociation FOREIGN KEY (int_identity_id_test_entity_association_o2o_attr_id) REFERENCES scr_int_identity_id_test_entity(id);

ALTER TABLE scr_datatypes_test_entity
    ADD CONSTRAINT fk_scrdatatypestestentity_on_stringidtestentityassociationo2oid FOREIGN KEY (string_id_test_entity_association_o2o_identifier) REFERENCES scr_string_id_test_entity(identifier);

ALTER TABLE scr_datatypes_test_entity_association_m2m_test_entity_link
    ADD CONSTRAINT fk_scrdattesentassm2mtesent_on_association_m2_m_test_entity FOREIGN KEY (association_m2_m_test_entity_id) REFERENCES scr_association_m2m_test_entity(id);

ALTER TABLE scr_datatypes_test_entity_association_m2m_test_entity_link
    ADD CONSTRAINT fk_scrdattesentassm2mtesent_on_datatypes_test_entity FOREIGN KEY (datatypes_test_entity_id) REFERENCES scr_datatypes_test_entity(id);

ALTER TABLE scr_datatypes_test_entity_integer_id_test_entity_link
    ADD CONSTRAINT fk_scrdattesentintidtesent_on_integer_id_test_entity FOREIGN KEY (integer_id_test_entity_id) REFERENCES scr_integer_id_test_entity(id);

ALTER TABLE scr_garage_user_link
    ADD CONSTRAINT fk_scrgaruse_on_garage FOREIGN KEY (garage_id) REFERENCES scr_garage(id);

ALTER TABLE scr_garage_user_link
    ADD CONSTRAINT fk_scrgaruse_on_user FOREIGN KEY (user_id) REFERENCES scr_user(id);