public class Person {
    // attributes created with proper datatypes to match/validate data accessed
    // from persons table from database
    private int PersonID;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    // Constructor to initialize persons object and validate data from the database
    public Person(int PersonID, String firstName, String lastName, String phone, String email) {
        this.PersonID = PersonID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
    }

    // Accessors
    public int getPersonID() { return PersonID; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhone() { return phone; }
    public String getEmail() {return email;}


    // Mutators
    public void setPersonID(int personID) { this.PersonID = personID; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) {this.email = email;}
    // toString method to display data
    @Override
    public String toString() {
        return "ID: " + PersonID +
                ", Name: " + firstName + " " + lastName +
                ", Phone: " + phone + " " + ", Email: " + email;
    }
}
