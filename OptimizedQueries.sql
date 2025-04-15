-- Optimized Queries and Indexing for StoreDB

-- Add additional indexes to improve query performance
-- Index on Purchase.PersonID to speed up customer history queries
CREATE INDEX IF NOT EXISTS idx_purchase_personid ON Purchase(PersonID);

-- Index on Purchase.Date to speed up date-based queries and sorting
CREATE INDEX IF NOT EXISTS idx_purchase_date ON Purchase(Date);

-- Composite index on Purchase for common query patterns
CREATE INDEX IF NOT EXISTS idx_purchase_person_date ON Purchase(PersonID, Date);

-- Index on Products.ItemPrice for price-based queries and sorting
CREATE INDEX IF NOT EXISTS idx_products_price ON Products(ItemPrice);

-- Index on Persons.Email for faster customer lookup by email
CREATE INDEX IF NOT EXISTS idx_persons_email ON Persons(Email);

-- Optimized view for recent purchases with limit
CREATE OR REPLACE VIEW RecentPurchasesLimited AS
SELECT 
    p.PersonID, 
    p.FName, 
    p.LName, 
    pr.ItemName, 
    pu.QuantityPurchased, 
    pu.Date,
    pr.ItemPrice,
    (pr.ItemPrice * pu.QuantityPurchased) AS TotalPrice
FROM Purchase pu
JOIN Persons p ON pu.PersonID = p.PersonID
JOIN Products pr ON pu.ProductID = pr.ProductID
ORDER BY pu.Date DESC
LIMIT 100;

-- Optimized view for customer purchase summary (aggregated data)
CREATE OR REPLACE VIEW CustomerPurchaseSummary AS
SELECT 
    p.PersonID,
    p.FName,
    p.LName,
    p.Email,
    COUNT(pu.TransactionID) AS TotalTransactions,
    SUM(pu.QuantityPurchased) AS TotalItemsPurchased,
    SUM(pu.QuantityPurchased * pr.ItemPrice) AS TotalSpent,
    MAX(pu.Date) AS LastPurchaseDate
FROM Persons p
LEFT JOIN Purchase pu ON p.PersonID = pu.PersonID
LEFT JOIN Products pr ON pu.ProductID = pr.ProductID
GROUP BY p.PersonID, p.FName, p.LName, p.Email;

-- Optimized view for product sales analysis
CREATE OR REPLACE VIEW ProductSalesAnalysis AS
SELECT 
    pr.ProductID,
    pr.ItemName,
    pr.ItemPrice,
    pr.ItemQuantity AS CurrentStock,
    COUNT(pu.TransactionID) AS TimesSold,
    SUM(pu.QuantityPurchased) AS TotalQuantitySold,
    SUM(pu.QuantityPurchased * pr.ItemPrice) AS TotalRevenue
FROM Products pr
LEFT JOIN Purchase pu ON pr.ProductID = pu.ProductID
GROUP BY pr.ProductID, pr.ItemName, pr.ItemPrice, pr.ItemQuantity
ORDER BY TotalRevenue DESC;

-- Stored procedure for paginated product listing
DELIMITER //
CREATE PROCEDURE GetPaginatedProducts(
    IN p_page INT,
    IN p_pageSize INT,
    IN p_sortColumn VARCHAR(50),
    IN p_sortDirection VARCHAR(4)
)
BEGIN
    DECLARE v_offset INT;
    DECLARE v_query VARCHAR(1000);
    
    -- Calculate offset
    SET v_offset = (p_page - 1) * p_pageSize;
    
    -- Validate sort column
    IF p_sortColumn NOT IN ('ProductID', 'ItemName', 'ItemPrice', 'ItemQuantity') THEN
        SET p_sortColumn = 'ProductID';
    END IF;
    
    -- Validate sort direction
    IF p_sortDirection NOT IN ('ASC', 'DESC') THEN
        SET p_sortDirection = 'ASC';
    END IF;
    
    -- Build and execute dynamic query
    SET @sql = CONCAT('SELECT ProductID, ItemName, ItemPrice, ItemQuantity 
                      FROM Products 
                      ORDER BY ', p_sortColumn, ' ', p_sortDirection, ' 
                      LIMIT ', p_pageSize, ' OFFSET ', v_offset);
    
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
    
    -- Return total count for pagination
    SELECT COUNT(*) AS TotalProducts FROM Products;
END //
DELIMITER ;

-- Stored procedure for customer purchase history with pagination
DELIMITER //
CREATE PROCEDURE GetCustomerPurchaseHistory(
    IN p_personID INT,
    IN p_page INT,
    IN p_pageSize INT
)
BEGIN
    DECLARE v_offset INT;
    
    -- Calculate offset
    SET v_offset = (p_page - 1) * p_pageSize;
    
    -- Get paginated purchase history
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
    WHERE pu.PersonID = p_personID
    ORDER BY pu.Date DESC
    LIMIT p_pageSize OFFSET v_offset;
    
    -- Return total count for pagination
    SELECT COUNT(*) AS TotalPurchases 
    FROM Purchase 
    WHERE PersonID = p_personID;
END //
DELIMITER ;

-- Stored procedure for searching products with multiple criteria
DELIMITER //
CREATE PROCEDURE SearchProducts(
    IN p_nameSearch VARCHAR(100),
    IN p_minPrice FLOAT,
    IN p_maxPrice FLOAT,
    IN p_inStockOnly BOOLEAN
)
BEGIN
    SELECT 
        ProductID, 
        ItemName, 
        ItemPrice, 
        ItemQuantity
    FROM Products
    WHERE 
        (p_nameSearch IS NULL OR ItemName LIKE CONCAT('%', p_nameSearch, '%'))
        AND (p_minPrice IS NULL OR ItemPrice >= p_minPrice)
        AND (p_maxPrice IS NULL OR ItemPrice <= p_maxPrice)
        AND (p_inStockOnly = FALSE OR ItemQuantity > 0)
    ORDER BY 
        CASE WHEN p_nameSearch IS NOT NULL 
            THEN LOCATE(p_nameSearch, ItemName) 
            ELSE 999 
        END,
        ItemName;
END //
DELIMITER ;
