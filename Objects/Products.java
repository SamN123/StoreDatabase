public class Product {
    private int id;
    private String name;
    private int price;
    private int quantity;

    public Product (int productId, String productName, int productPrice, int productQuantity) {
        this.id = productId;
        this.name = productName;
        this.price = productPrice;
        this.quantity = productQuantity;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name;}
    public int getPrice() { return price; }
    public int getQuantity() { return quantity; }

    // Setters (Insert as needed as project progresses)
    
    @Override
    public String toString() { return "Insert string here depending on project needs."; }
}
