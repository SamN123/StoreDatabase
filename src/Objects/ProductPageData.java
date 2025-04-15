package src.Objects;

import java.util.ArrayList;
import java.util.List;

// class to hold product page data for multi-threaded retrieval
public class ProductPageData {
    private List<ProductData> products;
    private int totalProducts;
    
    // constructor
    public ProductPageData() {
        this.products = new ArrayList<>();
        this.totalProducts = 0;
    }
    
    // add a product to the page data
    // @param id product id
    // @param name product name
    // @param price product price
    // @param quantity product quantity
    public void addProduct(String id, String name, double price, int quantity) {
        products.add(new ProductData(id, name, price, quantity));
    }
    
    // get the list of products
    // @return list of products
    public List<ProductData> getProducts() {
        return products;
    }
    
    // get the number of products in this page
    // @return product count
    public int getProductCount() {
        return products.size();
    }
    
    // set the total number of products in the database
    // @param totalProducts total number of products
    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }
    
    // get the total number of products in the database
    // @return total number of products
    public int getTotalProducts() {
        return totalProducts;
    }
}
