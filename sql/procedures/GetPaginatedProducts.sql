-- GetPaginatedProducts stored procedure
-- This procedure retrieves a paginated list of products with sorting options

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
