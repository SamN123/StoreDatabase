CREATE DATABASE IF NOT EXISTS StoreDB;
USE StoreDB;

CREATE TABLE Persons (
    PersonID INT AUTO_INCREMENT PRIMARY KEY,
    FName VARCHAR(50) NOT NULL,
    LName VARCHAR(50) NOT NULL,
    Email VARCHAR(100) NOT NULL UNIQUE,
    Phone VARCHAR(15) NOT NULL
);

-- Phone validation trigger 
DELIMITER //
CREATE TRIGGER validate_phone_format
BEFORE INSERT ON Persons
FOR EACH ROW
BEGIN
    IF NEW.Phone NOT REGEXP '^[0-9]{3}-[0-9]{3}-[0-9]{4}$' THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Invalid phone format (XXX-XXX-XXXX required)';
    END IF;
END //
DELIMITER ;

DELIMITER //
CREATE TRIGGER validate_phone_format_update
BEFORE UPDATE ON Persons
FOR EACH ROW
BEGIN
    IF NEW.Phone NOT REGEXP '^[0-9]{3}-[0-9]{3}-[0-9]{4}$' THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Invalid phone format (XXX-XXX-XXXX required)';
    END IF;
END //
DELIMITER ;

CREATE TABLE Products (
    ProductID VARCHAR(20) PRIMARY KEY,
    ItemName VARCHAR(100) NOT NULL,
    ItemPrice FLOAT(10,2) UNSIGNED NOT NULL ,
    ItemQuantity INT UNSIGNED NOT NULL,
    INDEX idx_itemname (ItemName)
);


CREATE TABLE Purchase (
    TransactionID INT AUTO_INCREMENT PRIMARY KEY,
    PersonID INT NOT NULL,
    ProductID VARCHAR(20) NOT NULL,
    Date DATETIME NOT NULL,
    QuantityPurchased INT NOT NULL CHECK (QuantityPurchased > 0),
    FOREIGN KEY (PersonID) REFERENCES Persons(PersonID),
    FOREIGN KEY (ProductID) REFERENCES Products(ProductID)
);

-- Inventory update trigger
DELIMITER //
CREATE TRIGGER update_inventory
AFTER INSERT ON Purchase
FOR EACH ROW
BEGIN
    UPDATE Products 
    SET ItemQuantity = ItemQuantity - NEW.QuantityPurchased
    WHERE ProductID = NEW.ProductID;
END //
DELIMITER ;
-- stored procedure for making purchases 
DELIMITER //
CREATE PROCEDURE MakePurchase(
    IN p_personID INT, 
    IN p_productID VARCHAR(20), 
    IN p_quantity INT
)
BEGIN
    DECLARE current_quantity INT;
    -- Check if sufficient stock is available 
    SELECT ItemQuantity INTO current_quantity
    FROM Products
    WHERE ProductID = p_productID;
    
    IF current_quantity >= p_quantity THEN
        INSERT INTO Purchase (PersonID, ProductID, Date, QuantityPurchased)
        VALUES (p_personID, p_productID, NOW(), p_quantity);
    ELSE
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Insufficient stock';
    END IF;
END //
DELIMITER ;
-- View to help retrieve most recent purchases made my customers 
CREATE VIEW RecentPurchases AS
SELECT 
    p.PersonID, 
    p.FName, 
    p.LName, 
    pr.ItemName, 
    pu.QuantityPurchased, 
    pu.Date
FROM Purchase pu
JOIN Persons p ON pu.PersonID = p.PersonID
JOIN Products pr ON pu.ProductID = pr.ProductID
ORDER BY pu.Date DESC;


-- Insert statements to check functionality
INSERT INTO Persons (FName, LName, Email, Phone) VALUES
('John', 'Doe', 'john.doe@email.com', '123-456-7890'),
('Jane', 'Smith', 'jane.smith@email.com', '234-567-8901'),
('Michael', 'Johnson', 'michael.j@email.com', '345-678-9012'),
('Emily', 'Williams', 'emily.w@email.com', '456-789-0123'),
('David', 'Brown', 'david.b@email.com', '567-890-1234'),
('Tom', 'Sanders', 't.sanders@email.com', '844-234-8432'),
('Bill', 'Thomas', 'bill.thomas@email.com', '836-832-1245'),
('Susie', 'Robins', 'susie.R@email.com', '742-535-2356'),
('Corie', 'Tanner', 'Corie.Tanner@email.com', '434-563-5686'),
('William', 'Karl', 'Will.Karl@email.com', '329-009-5476'),
('Fred', 'Jonas', 'F.Jonas@email.com', '245-733-0005'),
('Tyler', 'Mason', 'T.Mason@email.com', '888-385-9393'),
('Anna', 'Miller', 'A.Miller@email.com', '954-004-0490'),
('Jerome', 'Powers', 'J.Powers@email.com', '873-464-1121'),
('Jacob', 'Stevenson', 'Jacob.S@email.com', '937-976-2397'),
('Julie', 'Waters', 'Julie.Waters@email.com', '356-483-0040'),
('Tiara', 'Steele', 'Tiara.Steele@email.com', '543-764-9452'),
('Robert', 'Miller', 'robert.miller@email.com', '727-418-3921'),
('Linda', 'Garcia', 'linda.garcia@email.com', '310-563-7098'),
('James', 'Martinez', 'james.martinez@email.com', '615-329-8452'),
('Patricia', 'Lopez', 'patricia.lopez@email.com', '469-284-1176'),
('Charles', 'Gonzalez', 'charles.gonzalez@email.com', '978-405-6639'),
('Barbara', 'Wilson', 'barbara.wilson@email.com', '209-871-5627'),
('Paul', 'Anderson', 'paul.anderson@email.com', '845-376-2094'),
('Jennifer', 'Thomas', 'jennifer.thomas@email.com', '503-672-8841'),
('Mark', 'Taylor', 'mark.taylor@email.com', '312-485-2907'),
('Susan', 'Moore', 'susan.moore@email.com', '702-398-1203'),
('Steven', 'Jackson', 'steven.jackson@email.com', '415-739-2564'),
('Karen', 'Martin', 'karen.martin@email.com', '860-234-9071'),
('Brian', 'Lee', 'brian.lee@email.com', '225-918-3349'),
('Nancy', 'Perez', 'nancy.perez@email.com', '918-660-7342'),
('Edward', 'Thompson', 'edward.thompson@email.com', '512-307-4896'),
('Donna', 'White', 'donna.white@email.com', '337-691-2543'),
('Kevin', 'Harris', 'kevin.harris@email.com', '314-580-9325'),
('Sarah', 'Sanchez', 'sarah.sanchez@email.com', '781-603-4721'),
('Jason', 'Clark', 'jason.clark@email.com', '919-287-6403'),
('Betty', 'Ramirez', 'betty.ramirez@email.com', '303-229-7185'),
('Eric', 'Lewis', 'eric.lewis@email.com', '623-408-1529'),
('Donna', 'Roberts', 'donna.roberts@email.com', '504-367-9823'),
('Timothy', 'Walker', 'timothy.walker@email.com', '470-254-6130'),
('Cynthia', 'Young', 'cynthia.young@email.com', '602-319-4587'),
('Scott', 'Allen', 'scott.allen@email.com', '980-415-6630'),
('Angela', 'King', 'angela.king@email.com', '413-702-9184'),
('Gregory', 'Wright', 'gregory.wright@email.com', '210-645-7012'),
('Melissa', 'Scott', 'melissa.scott@email.com', '973-228-3311'),
('Patrick', 'Torres', 'patrick.torres@email.com', '718-209-9825'),
('Rebecca', 'Nguyen', 'rebecca.nguyen@email.com', '919-843-5207'),
('Joshua', 'Hill', 'joshua.hill@email.com', '651-912-4906'),
('Laura', 'Flores', 'laura.flores@email.com', '559-829-3741'),
('Daniel', 'Green', 'daniel.green@email.com', '808-473-2160');

-- Insert statement for products 
INSERT INTO Products (ProductID, ItemName, ItemPrice, ItemQuantity) VALUES
('P001', 'Laptop', 899.99, 50),
('P002', 'Smartphone', 499.99, 20),
('P003', 'Headphones', 79.99, 50),
('P004', 'Keyboard', 49.99, 40),
('P005', 'Mouse', 24.99, 40),
('P006', 'Backpack', 39.99, 25),
('P007', 'DeskLamp', 29.49, 18),
('P008', 'WaterBottle', 14.75, 60),
('P009', 'Notebook', 3.99, 100),
('P010', 'BluetoothSpeaker', 89.95, 15),
('P011', 'Smartwatch', 199.99, 12),
('P012', 'RunningShoes', 74.99, 20),
('P013', 'TravelMug', 11.95, 35),
('P014', 'PortableCharger', 45.00, 25),
('P015', 'Sunglasses', 19.95, 40),
('P016', 'DeskOrganizer', 22.50, 28),
('P017', 'OfficeChair', 149.99, 10),
('P018', 'Webcam', 59.99, 17),
('P019', 'YogaMat', 25.99, 22),
('P020', 'Toaster', 34.99, 14),
('P021', 'ElectricKettle', 49.99, 16),
('P022', 'HairDryer', 38.50, 19),
('P023', 'Blender', 65.00, 13),
('P024', 'Sneakers', 59.99, 30),
('P025', 'Basketball', 27.95, 24),
('P026', 'WaterFilter', 19.99, 50),
('P027', 'CoffeeGrinder', 44.50, 12),
('P028', 'IronBox', 36.99, 15),
('P029', 'RiceCooker', 54.90, 10),
('P030', 'WristBrace', 12.99, 33);

-- Call is for testing stored procedures (Running these calls will affect the database values)
-- And populates rows in purchase table 

CALL MakePurchase(6,'P017',1);
CALL MakePurchase(10,'P008',1);
CALL MakePurchase(13,'P011',1);
CALL MakePurchase(7,'P007',2);
CALL MakePurchase(4,'P004',2);
CALL MakePurchase(24,'P006',1);
CALL MakePurchase(12,'P021',1);
CALL MakePurchase(6,'P012',1);
CALL MakePurchase(11,'P026',1);
CALL MakePurchase(6,'P020',2);
CALL MakePurchase(19,'P004',1);
CALL MakePurchase(3,'P003',3);
CALL MakePurchase(15,'P002',1);
CALL MakePurchase(7,'P030',1);
CALL MakePurchase(23,'P014',1);
CALL MakePurchase(9,'P009',1);
CALL MakePurchase(18,'P018',1);
CALL MakePurchase(13,'P028',1);
CALL MakePurchase(6,'P022',1);
CALL MakePurchase(2,'P002',1);
CALL MakePurchase(1,'P001',2);
CALL MakePurchase(20,'P016',1);
CALL MakePurchase(6,'P027',1);
CALL MakePurchase(8,'P001',1);
CALL MakePurchase(7,'P019',2);
CALL MakePurchase(8,'P024',2);
CALL MakePurchase(7,'P013',1);
CALL MakePurchase(21,'P007',1);
CALL MakePurchase(25,'P012',1);
CALL MakePurchase(16,'P003',1);
CALL MakePurchase(1,'P001',1);
CALL MakePurchase(10,'P015',1);
CALL MakePurchase(22,'P009',1);
CALL MakePurchase(6,'P030',1);
CALL MakePurchase(6,'P018',1);
CALL MakePurchase(7,'P016',1);
CALL MakePurchase(14,'P023',1);
CALL MakePurchase(27,'P001',1);
CALL MakePurchase(10,'P014',1);
CALL MakePurchase(26,'P001',1);
CALL MakePurchase(17,'P020',1);
CALL MakePurchase(28,'P001',1);
CALL MakePurchase(29,'P001',1);
CALL MakePurchase(5,'P005',1);
CALL MakePurchase(7,'P029',1);
CALL MakePurchase(10,'P005',1);
CALL MakePurchase(10,'P006',1);
CALL MakePurchase(12,'P019',1);
CALL MakePurchase(7,'P014',1);
CALL MakePurchase(10,'P001',1);
