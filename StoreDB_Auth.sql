-- authentication and role-based access control additions to storedb

-- add password and role columns to the persons table
ALTER TABLE Persons
ADD COLUMN password VARCHAR(64) NOT NULL DEFAULT '',
ADD COLUMN salt VARCHAR(32) NOT NULL DEFAULT '',
ADD COLUMN role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER';

-- create a default admin user (password will be hashed in the application)
INSERT INTO Persons (FName, LName, Email, Phone, password, salt, role)
VALUES ('Admin', 'User', 'admin@store.com', '000-000-0000', '', '', 'ADMIN')
ON DUPLICATE KEY UPDATE role = 'ADMIN';

-- update existing users to have the user role
UPDATE Persons SET role = 'USER' WHERE Email != 'admin@store.com';
