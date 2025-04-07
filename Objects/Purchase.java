public class Purchase {
    private int id;
    private String name;
    private int price;
    private String date;
    private int quantity;

    public Purchase (int purchaseId, String personID, int productID, String purchaseDate, int purchaseQuantity) {
        this.id = purchaseId;
        this.name = personID;
        this.price = productID;
        this.date = purchaseDate;
        this.quantity = purchaseQuantity;
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public int getPrice() { return price; }
    public String getDate() { return date; }
    public int getQuantity() { return quantity; }

    @Override
    public String toString() { return "Insert string here depending on project needs."; }
}
