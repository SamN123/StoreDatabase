package src.Util;

// exception thrown when input validation fails
public class ValidationException extends Exception {
    private String field; // the field that failed validation
    
    // create a new validation exception
    // @param message the error message
    // @param field the field that failed validation
    public ValidationException(String message, String field) {
        super(message); // pass message to parent Exception class
        this.field = field; // store the field name
    }
    
    // get the field that failed validation
    // @return the field name
    public String getField() {
        return field; // return the stored field name
    }
}
