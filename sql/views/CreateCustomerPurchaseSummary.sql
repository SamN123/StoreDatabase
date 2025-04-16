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
