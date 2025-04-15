package src.Util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

// utility class for logging application events and errors
public class Logger {
    // singleton instance of the logger
    private static java.util.logging.Logger logger = null;
    
    // log file path
    private static final String LOG_FOLDER = "logs";
    private static final String LOG_FILE = "storedb.log";
    
    // log levels
    public static final Level INFO = Level.INFO;
    public static final Level WARNING = Level.WARNING;
    public static final Level ERROR = Level.SEVERE;
    public static final Level DEBUG = Level.FINE;
    
    // initialize the logger
    public static void init() {
        if (logger == null) {
            try {
                // create logs directory if it doesn't exist
                File logDir = new File(LOG_FOLDER);
                if (!logDir.exists()) {
                    logDir.mkdir(); // create the directory
                }
                
                // configure the logger
                logger = java.util.logging.Logger.getLogger("StoreDatabase"); // get logger instance
                FileHandler fileHandler = new FileHandler(LOG_FOLDER + "/" + LOG_FILE, true); // true = append mode
                fileHandler.setFormatter(new SimpleFormatter()); // use simple text format
                logger.addHandler(fileHandler); // add file handler to logger
                
                // set the log level to capture all log levels
                logger.setLevel(Level.ALL);
                
                // log application start event
                log(INFO, "Application started");
            } catch (IOException e) {
                System.err.println("Failed to initialize logger: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    // log a message with the specified level
    // @param level the log level
    // @param message the message to log
    public static void log(Level level, String message) {
        if (logger == null) {
            init(); // initialize logger if not already done
        }
        
        logger.log(level, message); // log the message
        
        // also print to console for ERROR level for immediate visibility
        if (level == ERROR) {
            System.err.println("[ERROR] " + message);
        }
    }
    
    // log an exception with ERROR level
    // @param message the message to log
    // @param exception the exception to log
    public static void logException(String message, Exception exception) {
        if (logger == null) {
            init(); // initialize logger if not already done
        }
        
        logger.log(ERROR, message + ": " + exception.getMessage()); // log exception message
        exception.printStackTrace(); // print stack trace for debugging
    }
    
    // log a user action with INFO level
    // @param userId the user ID
    // @param action the action performed
    // @param details additional details about the action
    public static void logUserAction(int userId, String action, String details) {
        if (logger == null) {
            init(); // initialize logger if not already done
        }
        
        // format the log message with user ID, action, details, and timestamp
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String logMessage = String.format("[User %d] %s - %s at %s", userId, action, details, timestamp);
        
        logger.log(INFO, logMessage); // log the formatted message
    }
    
    // get the contents of the log file
    // @return the log file contents as a string
    public static String getLogContents() {
        try {
            // read all bytes from the log file and convert to string
            return new String(Files.readAllBytes(Paths.get(LOG_FOLDER + "/" + LOG_FILE)));
        } catch (IOException e) {
            System.err.println("Failed to read log file: " + e.getMessage());
            return "Failed to read log file: " + e.getMessage();
        }
    }
}
