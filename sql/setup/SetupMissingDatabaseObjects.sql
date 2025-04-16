-- Setup Missing Database Objects for Store Database
USE StoreDB;

-- Create the SearchProducts stored procedure
DELIMITER //
CREATE PROCEDURE SearchProducts(
    IN p_nameSearch VARCHAR(100),
    IN p_minPrice FLOAT,
    IN p_maxPrice FLOAT,
    IN p_inStockOnly BOOLEAN
)
BEGIN
    -- Build the query dynamically based on the parameters
    SET @sql = 'SELECT ProductID, ItemName, ItemPrice, ItemQuantity FROM Products WHERE 1=1';
    
    -- Add name search condition if provided
    IF p_nameSearch IS NOT NULL THEN
        SET @sql = CONCAT(@sql, ' AND ItemName LIKE ?');
    END IF;
    
    -- Add minimum price condition if provided
    IF p_minPrice IS NOT NULL THEN
        SET @sql = CONCAT(@sql, ' AND ItemPrice >= ?');
    END IF;
    
    -- Add maximum price condition if provided
    IF p_maxPrice IS NOT NULL THEN
        SET @sql = CONCAT(@sql, ' AND ItemPrice <= ?');
    END IF;
    
    -- Add in-stock only condition if requested
    IF p_inStockOnly = TRUE THEN
        SET @sql = CONCAT(@sql, ' AND ItemQuantity > 0');
    END IF;
    
    -- Add order by clause
    SET @sql = CONCAT(@sql, ' ORDER BY ItemName');
    
    -- Prepare and execute the statement
    PREPARE stmt FROM @sql;
    
    -- Set parameters based on which ones were provided
    SET @param_pos = 0;
    
    IF p_nameSearch IS NOT NULL THEN
        SET @param_pos = @param_pos + 1;
        SET @p1 = CONCAT('%', p_nameSearch, '%');
        
        IF @param_pos = 1 THEN
            EXECUTE stmt USING @p1;
        END IF;
    END IF;
    
    IF p_minPrice IS NOT NULL THEN
        SET @param_pos = @param_pos + 1;
        SET @p2 = p_minPrice;
        
        IF @param_pos = 1 THEN
            EXECUTE stmt USING @p2;
        ELSEIF @param_pos = 2 THEN
            EXECUTE stmt USING @p1, @p2;
        END IF;
    END IF;
    
    IF p_maxPrice IS NOT NULL THEN
        SET @param_pos = @param_pos + 1;
        SET @p3 = p_maxPrice;
        
        IF @param_pos = 1 THEN
            EXECUTE stmt USING @p3;
        ELSEIF @param_pos = 2 THEN
            EXECUTE stmt USING @p1, @p3;
        ELSEIF @param_pos = 3 THEN
            EXECUTE stmt USING @p1, @p2, @p3;
        END IF;
    END IF;
    
    -- If no parameters were provided, execute without parameters
    IF @param_pos = 0 THEN
        EXECUTE stmt;
    END IF;
    
    -- Clean up
    DEALLOCATE PREPARE stmt;
END //
DELIMITER ;

-- Create the GetCustomerPurchaseHistory stored procedure
DELIMITER //
CREATE PROCEDURE GetCustomerPurchaseHistory(
    IN p_customerId INT,
    IN p_page INT,
    IN p_pageSize INT
)
BEGIN
    DECLARE v_offset INT;
    
    -- Calculate offset for pagination
    SET v_offset = (p_page - 1) * p_pageSize;
    
    -- Get paginated purchase history for the customer
    SELECT 
        pu.TransactionID,
        pu.Date,
        pr.ProductID,
        pr.ItemName,
        pu.QuantityPurchased,
        pr.ItemPrice,
        (pu.QuantityPurchased * pr.ItemPrice) AS TotalPrice
    FROM Purchase pu
    JOIN Products pr ON pu.ProductID = pr.ProductID
    WHERE pu.PersonID = p_customerId
    ORDER BY pu.Date DESC
    LIMIT p_pageSize OFFSET v_offset;
    
    -- Get total count for pagination
    SELECT COUNT(*) AS TotalPurchases
    FROM Purchase
    WHERE PersonID = p_customerId;
END //
DELIMITER ;

-- Create the CustomerPurchaseSummary view
CREATE OR REPLACE VIEW CustomerPurchaseSummary AS
SELECT 
    p.PersonID,
    p.FName,
    p.LName,
    p.Email,
    COUNT(DISTINCT pu.TransactionID) AS TotalTransactions,
    SUM(pu.QuantityPurchased) AS TotalItemsPurchased,
    SUM(pu.QuantityPurchased * pr.ItemPrice) AS TotalSpent,
    MAX(pu.Date) AS LastPurchaseDate
FROM Persons p
LEFT JOIN Purchase pu ON p.PersonID = pu.PersonID
LEFT JOIN Products pr ON pu.ProductID = pr.ProductID
GROUP BY p.PersonID, p.FName, p.LName, p.Email;

-- Create the ProductSalesAnalysis view
CREATE OR REPLACE VIEW ProductSalesAnalysis AS
SELECT 
    p.ProductID,
    p.ItemName,
    p.ItemPrice,
    p.ItemQuantity AS CurrentStock,
    COUNT(pu.TransactionID) AS TimesSold,
    COALESCE(SUM(pu.QuantityPurchased), 0) AS TotalQuantitySold,
    COALESCE(SUM(pu.QuantityPurchased * p.ItemPrice), 0) AS TotalRevenue,
    COUNT(DISTINCT pu.PersonID) AS UniqueCustomers,
    MAX(pu.Date) AS LastPurchaseDate
FROM 
    Products p
LEFT JOIN 
    Purchase pu ON p.ProductID = pu.ProductID
GROUP BY 
    p.ProductID, p.ItemName, p.ItemPrice, p.ItemQuantity
ORDER BY 
    TotalRevenue DESC;

-- Create a stored procedure to find a customer ID by email
DELIMITER //
CREATE PROCEDURE FindCustomerIDByEmail(
    IN p_email VARCHAR(100)
)
BEGIN
    SELECT PersonID, FName, LName, Email, Phone
    FROM Persons
    WHERE Email = p_email;
END //
DELIMITER ;

-- Create a utility procedure to list all customers (for admin use)
DELIMITER //
CREATE PROCEDURE ListAllCustomers()
BEGIN
    SELECT PersonID, FName, LName, Email, Phone, role
    FROM Persons
    ORDER BY PersonID;
END //
DELIMITER ;

-- Add a menu option to find customer ID by email
DELIMITER //
CREATE PROCEDURE FindMyCustomerID(
    IN p_email VARCHAR(100)
)
BEGIN
    DECLARE v_count INT;
    
    -- Check if email exists
    SELECT COUNT(*) INTO v_count
    FROM Persons
    WHERE Email = p_email;
    
    IF v_count > 0 THEN
        -- Email exists, return customer info
        SELECT PersonID, FName, LName, Email, Phone
        FROM Persons
        WHERE Email = p_email;
    ELSE
        -- Email doesn't exist
        SELECT 'No customer found with this email address.' AS Message;
    END IF;
END //
DELIMITER ;

-- Create the GetPaginatedProducts stored procedure
DELIMITER //
CREATE PROCEDURE GetPaginatedProducts(
    IN p_page INT,
    IN p_pageSize INT,
    IN p_sortColumn VARCHAR(50),
    IN p_sortDirection VARCHAR(4)
)
BEGIN
    DECLARE v_offset INT;
    DECLARE v_orderBy VARCHAR(100);
    
    -- Calculate offset for pagination
    SET v_offset = (p_page - 1) * p_pageSize;
    
    -- Set default sort column if not provided
    IF p_sortColumn IS NULL OR p_sortColumn = '' THEN
        SET p_sortColumn = 'ProductID';
    END IF;
    
    -- Set default sort direction if not provided
    IF p_sortDirection IS NULL OR p_sortDirection = '' THEN
        SET p_sortDirection = 'ASC';
    END IF;
    
    -- Validate sort column to prevent SQL injection
    IF p_sortColumn NOT IN ('ProductID', 'ItemName', 'ItemPrice', 'ItemQuantity') THEN
        SET p_sortColumn = 'ProductID';
    END IF;
    
    -- Validate sort direction to prevent SQL injection
    IF p_sortDirection NOT IN ('ASC', 'DESC') THEN
        SET p_sortDirection = 'ASC';
    END IF;
    
    -- Build the ORDER BY clause
    SET v_orderBy = CONCAT(p_sortColumn, ' ', p_sortDirection);
    
    -- Get paginated products
    SET @sql = CONCAT('SELECT ProductID, ItemName, ItemPrice, ItemQuantity FROM Products ORDER BY ', v_orderBy, ' LIMIT ?, ?');
    PREPARE stmt FROM @sql;
    SET @offset = v_offset;
    SET @limit = p_pageSize;
    EXECUTE stmt USING @offset, @limit;
    DEALLOCATE PREPARE stmt;
    
    -- Get total count for pagination
    SELECT COUNT(*) AS TotalProducts FROM Products;
END //
DELIMITER ;

-- Print completion message
SELECT 'Database objects created successfully!' AS Message;
