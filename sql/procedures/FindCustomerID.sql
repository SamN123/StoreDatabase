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
