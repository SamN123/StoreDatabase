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
