package src.Objects;

public class Purchase {
    private int id;
    private int perID;
    private String prodID;
    private String date;
    private int quantity;

    public Purchase (int purchaseId, int personID, String productID, String purchaseDate, int purchaseQuantity) {
        this.id = purchaseId;
        this.perID = personID;
        this.prodID = productID;
        this.date = purchaseDate;
        this.quantity = purchaseQuantity;
    }

    public int getId() { return id; }
    public String getDate() { return date; }
    public int getQuantity() { return quantity; }

    // Setters (Add as project needs demand)

    @Override
    public String toString() {
        return String.format("PurchaseID: %d | CustomerID: %d | ProductID: %s | Quantity: %d | Date: %s", id, perID, prodID, quantity, date);
    }
}
