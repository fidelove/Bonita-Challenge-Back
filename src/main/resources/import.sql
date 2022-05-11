INSERT INTO roles (role_id, role_name) VALUES (1, 'ADMIN');
INSERT INTO roles (role_id, role_name) VALUES (2, 'CHEF');
INSERT INTO roles (role_id, role_name) VALUES (3, 'USER');

INSERT INTO users (user_id, user_email, user_name, user_password, role_id) VALUES (1, 'admin@bonita.com', 'admin', 'password', 1);
