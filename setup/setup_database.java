// simple script to compile and run the databasesetup utility
// 
// this script will:
// 1. compile the databasesetup.java file
// 2. run the databasesetup utility
// 
// usage:
// java -cp .;lib/mysql-connector-j-9.1.0.jar setup.setup_database
public class setup_database {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  Store Database Setup Utility Launcher");
        System.out.println("=================================================");
        System.out.println();
        
        try {
            // load the jdbc driver
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                System.out.println("mysql jdbc driver loaded successfully");
            } catch (ClassNotFoundException e) {
                System.err.println("error: mysql jdbc driver not found!");
                System.err.println("make sure mysql-connector-j-9.1.0.jar is in your classpath");
                e.printStackTrace();
                return;
            }
            
            // run the databasesetup utility
            System.out.println("launching databasesetup utility...");
            System.out.println();
            
            src.Util.DatabaseSetup.main(args);
        } catch (Exception e) {
            System.err.println("Error launching DatabaseSetup utility: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
