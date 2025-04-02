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
('David', 'Brown', 'david.b@email.com', '567-890-1234');

INSERT INTO Products (ProductID, ItemName, ItemPrice, ItemQuantity) VALUES
('P001', 'Laptop', 899.99, 10),
('P002', 'Smartphone', 499.99, 20),
('P003', 'Headphones', 79.99, 50),
('P004', 'Keyboard', 49.99, 30),
('P005', 'Mouse', 24.99, 40);

-- Call is for testing stored procedures (Running these calls will affect the database values)
-- And populates rows in purchase table 

-- Customer 1 buys 2 laptops
CALL MakePurchase(1, 'P001', 2);

-- Customer 2 buys 1 smartphone
CALL MakePurchase(2, 'P002', 1);

-- Customer 3 buys 3 headphones
CALL MakePurchase(3, 'P003', 3);

-- Customer 1 buys another laptop
CALL MakePurchase(1, 'P001', 1);

-- Customer 4 buys 2 keyboards
CALL MakePurchase(4, 'P004', 2);

-- Customer 5 buys 1 mouse
CALL MakePurchase(5, 'P005', 1);