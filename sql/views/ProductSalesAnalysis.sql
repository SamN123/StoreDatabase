-- ProductSalesAnalysis view
-- This view provides sales analysis data for products

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
