package src.Objects;

// class to hold product data for display
public class ProductData {
    public String id;
    public String name;
    public double price;
    public int quantity;
    
    // constructor
    // @param id product id
    // @param name product name
    // @param price product price
    // @param quantity product quantity
    public ProductData(String id, String name, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }
}
