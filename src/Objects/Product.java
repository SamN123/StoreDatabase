package src.Objects;

public class Product {
    private String id;
    private String name;
    private double price;
    private int quantity;

    public Product (String productId, String productName, double productPrice, int productQuantity) {
        this.id = productId;
        this.name = productName;
        this.price = productPrice;
        this.quantity = productQuantity;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name;}
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }

    // Setters (Insert as needed as project progresses)

    @Override
    public String toString() {
        return String.format("ID: %s | Name: %s | Price: $%.2f | Quantity: %d", id, name, price, quantity);
    }
}
