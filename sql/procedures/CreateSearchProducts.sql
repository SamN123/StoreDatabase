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
