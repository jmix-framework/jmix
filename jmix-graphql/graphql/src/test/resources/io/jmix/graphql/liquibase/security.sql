-- users
INSERT INTO public.scr_user (id, version, username, password, first_name, last_name, email, enabled, phone)
VALUES
('af89f9b9-5e64-bdf9-2466-5da9c91cf3d4', 1, 'mechanic', '{bcrypt}$2a$10$9beORT6c61zvR1fbpcaqd.pUnIIKK0EKGT3IbssNpuHxK5WJq1VYe', 'John', 'Doe', 'jd@example.com', true, NULL),
('62864fc8-0273-7c57-890d-314c1fd2fde3', 1, 'manager', '{bcrypt}$2a$10$pO.Edb8qWb3VTP0RSjRXXefIzLAVpXLvhZobCdpCYODx5FP8.4qWG', 'Ivan', 'Petrov', 'ivanp@example.com', true, NULL),
('62864fc8-0273-7c57-890d-314c1fd2fde5', 1, 'perm', '{noop}admin', 'Ivan', 'Petrov', 'ivanp@example.com', true, NULL);

-- roles
INSERT INTO public.sec_resource_role (id, create_ts, created_by, name, code, scopes)
VALUES
('23548523-3f0f-f96a-07ff-0d60b9cb5c1b', '2021-02-19 15:59:57.976', 'admin', 'Mechanics', 'mechanics',  '"API", "UI"'),
('91099ca3-194e-6ba5-7aa6-15b03bcef05a', '2021-02-19 16:00:16.377', 'admin', 'Managers',  'managers',   '"API", "UI"'),
('91099ca3-194e-6ba5-7aa6-25b03bcef05a', '2021-02-19 16:00:16.377', 'admin', 'Low permissions',  'low-permissions',   '"API", "UI"');

-- users to roles
INSERT INTO public.sec_role_assignment (id, create_ts, created_by, username, role_code, role_type)
VALUES
('7c2d9b0b-0d11-ade7-5005-39748c488373', now(), 'admin', 'mechanic', 'mechanics', 'resource'),
('51e0b9a4-0437-ad73-eefe-b34438b27389', now(), 'admin', 'manager', 'managers', 'resource'),
('51e0b9a4-0437-ad73-eefe-b34438b27349', now(), 'admin', 'perm', 'low-permissions', 'resource');

-- manager permissions
INSERT INTO public.sec_resource_policy (id, create_ts, created_by, type_, policy_group, resource_, action_, effect, role_id)
VALUES
('1fe89195-6624-99a8-19eb-46868d280706', '2021-02-19 16:24:59.947', 'admin', 'specific',  NULL,    'rest.enabled',    'access', 'allow', '91099ca3-194e-6ba5-7aa6-15b03bcef05a'),
('1fe89195-6614-99a8-19eb-46868d280706', '2021-02-19 16:24:59.947', 'admin', 'specific',  NULL,    'graphql.enabled', 'access', 'allow', '91099ca3-194e-6ba5-7aa6-15b03bcef05a'),
('7f1cd8e8-7af2-43c8-9a76-0fc8c2e01947', '2021-02-19 16:24:59.947', 'admin', 'screen',    NULL,    '*',               'access', 'allow', '91099ca3-194e-6ba5-7aa6-15b03bcef05a'),
('07583742-266a-4084-ab8d-d221d7891902', '2021-02-19 16:24:59.947', 'admin', 'menu',      NULL,    '*',               'access', 'allow', '91099ca3-194e-6ba5-7aa6-15b03bcef05a'),

('92511121-22bd-b09b-3a87-51c9d70212b4', '2021-02-19 16:24:59.947', 'admin', 'entity', 'scr$Car', 'scr$Car',      'create', 'allow', '91099ca3-194e-6ba5-7aa6-15b03bcef05a'),
('ca8200b7-bb43-e9bb-e93b-5f2707b6a8f6', '2021-02-19 16:24:59.947', 'admin', 'entity', 'scr$Car', 'scr$Car',      'read',   'allow', '91099ca3-194e-6ba5-7aa6-15b03bcef05a'),
('876d9a74-3bea-c5fd-0885-334bd8816cfd', '2021-02-19 16:24:59.948', 'admin', 'entity', 'scr$Car', 'scr$Car',      'update', 'allow', '91099ca3-194e-6ba5-7aa6-15b03bcef05a'),
('c9d5bf36-2e50-c587-4170-103bb55d9350', '2021-02-19 16:24:59.947', 'admin', 'entity', 'scr$Car', 'scr$Car',      'delete', 'allow', '91099ca3-194e-6ba5-7aa6-15b03bcef05a'),
('d8b4fea5-02e7-7f76-05bc-5057c5f052d7', '2021-02-19 16:24:59.947', 'admin', 'entityAttribute', 'scr$Car', 'scr$Car.carType',       'modify', 'allow', '91099ca3-194e-6ba5-7aa6-15b03bcef05a'),
('c59fd408-ba94-d6a2-23bb-42a1567f4656', '2021-02-19 16:24:59.948', 'admin', 'entityAttribute', 'scr$Car', 'scr$Car.manufacturer',  'modify', 'allow', '91099ca3-194e-6ba5-7aa6-15b03bcef05a'),
('17741978-75cb-f14f-3513-f3c5c1a78016', '2021-02-19 16:24:59.947', 'admin', 'entityAttribute', 'scr$Car', 'scr$Car.model',         'modify', 'allow', '91099ca3-194e-6ba5-7aa6-15b03bcef05a'),
('5927ab8e-aa5e-0da7-3f58-aabdc77f49f3', '2021-02-19 16:24:59.948', 'admin', 'entityAttribute', 'scr$Car', 'scr$Car.regNumber',     'modify', 'allow', '91099ca3-194e-6ba5-7aa6-15b03bcef05a');

-- mechanic permissions
INSERT INTO public.sec_resource_policy (id, create_ts, created_by, type_, policy_group, resource_, action_, effect, role_id)
VALUES
('35010e42-f9c1-dde3-655d-98c2956225a8', '2021-02-19 16:24:59.947', 'admin', 'specific',  NULL,       'rest.enabled',    'access', 'allow', '23548523-3f0f-f96a-07ff-0d60b9cb5c1b'),
('35010e42-f9c1-dde1-655d-98c2956225a8', '2021-02-19 16:24:59.947', 'admin', 'specific',  NULL,       'graphql.enabled', 'access', 'allow', '23548523-3f0f-f96a-07ff-0d60b9cb5c1b'),
('35010e42-f9c1-dde2-655d-98c2956225a8', '2021-02-19 16:24:59.947', 'admin', 'graphQL',  NULL,       'userInfo', 'access', 'allow', '23548523-3f0f-f96a-07ff-0d60b9cb5c1b'),
('bb818629-a6d6-4aaa-ad65-b431e054fd9e', '2021-02-19 16:24:59.947', 'admin', 'screen',    NULL,       '*',               'access', 'allow', '23548523-3f0f-f96a-07ff-0d60b9cb5c1b'),
('173308a1-1ff4-4edb-a559-872c8b239283', '2021-02-19 16:24:59.947', 'admin', 'menu',      NULL,       '*',               'access', 'allow', '23548523-3f0f-f96a-07ff-0d60b9cb5c1b'),

('b12f7355-3358-02ac-f54d-e9b27291c2b2', '2021-02-19 16:24:59.947', 'admin', 'entity',    'scr$Car',  'scr$Car',      'create', 'allow', '23548523-3f0f-f96a-07ff-0d60b9cb5c1b'),
('4a34b7db-381d-b55d-126b-9bac1c644d1e', '2021-02-19 16:24:59.947', 'admin', 'entity',    'scr$Car',  'scr$Car',      'read',   'allow', '23548523-3f0f-f96a-07ff-0d60b9cb5c1b'),
('2866648a-e6f5-42e4-bf0d-1cb565b27971', '2021-02-19 16:24:59.948', 'admin', 'entity',    'scr$Car',  'scr$Car',      'update', 'allow', '23548523-3f0f-f96a-07ff-0d60b9cb5c1b'),
('868403ca-5475-7e3e-a5a7-1e700d25824b', '2021-02-19 16:24:59.947', 'admin', 'entity',    'scr$Car',  'scr$Car',      'delete', 'allow', '23548523-3f0f-f96a-07ff-0d60b9cb5c1b'),
('e45b39c6-f251-4507-987a-598d954dbec2', '2021-02-19 16:24:59.947', 'admin', 'entityAttribute', 'scr$Car', 'scr$Car.carType',       'modify', 'allow', '23548523-3f0f-f96a-07ff-0d60b9cb5c1b'),
('fd08d779-5515-4035-b4db-ed58531d8f14', '2021-02-19 16:24:59.948', 'admin', 'entityAttribute', 'scr$Car', 'scr$Car.manufacturer',  'modify', 'allow', '23548523-3f0f-f96a-07ff-0d60b9cb5c1b'),
('a5d5adb1-2bc5-4599-b308-427892c54e92', '2021-02-19 16:24:59.947', 'admin', 'entityAttribute', 'scr$Car', 'scr$Car.model',         'modify', 'allow', '23548523-3f0f-f96a-07ff-0d60b9cb5c1b'),
('44c2e708-64d8-0045-6f5b-e33d6140f287', '2021-02-19 16:24:59.948', 'admin', 'entityAttribute', 'scr$Car', 'scr$Car.mileage',       'view',   'allow', '23548523-3f0f-f96a-07ff-0d60b9cb5c1b');

-- low permissions
INSERT INTO public.sec_resource_policy (id, create_ts, created_by, type_, policy_group, resource_, action_, effect, role_id)
VALUES
('35010e42-f9c1-dde3-655d-98c2956215a8', '2021-02-19 16:24:59.947', 'admin', 'specific',  NULL,       'rest.enabled',    'access', 'allow', '91099ca3-194e-6ba5-7aa6-25b03bcef05a'),
('bb818629-a6d6-4aaa-ad65-b431e0545d9e', '2021-02-19 16:24:59.947', 'admin', 'screen',    NULL,       '*',               'access', 'allow', '91099ca3-194e-6ba5-7aa6-25b03bcef05a'),
('173308a1-1ff4-4edb-a559-872c8b237283', '2021-02-19 16:24:59.947', 'admin', 'menu',      NULL,       '*',               'access', 'allow', '91099ca3-194e-6ba5-7aa6-25b03bcef05a'),

('4a34b7db-381d-b55d-126b-9bac1c644d0e', '2021-02-19 16:24:59.947', 'admin', 'entity',    'scr$Car',  'scr$Car',      'read',   'allow', '91099ca3-194e-6ba5-7aa6-25b03bcef05a'),
('44c2e703-64d8-0045-6f5b-e33d6140f237', '2021-02-19 16:24:59.948', 'admin', 'entityAttribute', 'scr$Car', 'scr$Car.mileage',       'view',   'allow', '91099ca3-194e-6ba5-7aa6-25b03bcef05a');
