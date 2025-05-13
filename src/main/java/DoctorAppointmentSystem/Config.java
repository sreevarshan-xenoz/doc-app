package DoctorAppointmentSystem;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Config {
    // Default values
    public static String SUPABASE_URL = "https://your-supabase-project-url.supabase.co";
    public static String SUPABASE_API_KEY = "your-supabase-api-key";
    
    // Email configuration
    public static String EMAIL_HOST = "smtp.gmail.com";
    public static int EMAIL_PORT = 587;
    public static String EMAIL_USERNAME = "your-email@gmail.com";
    public static String EMAIL_PASSWORD = "your-app-password";
    
    // Current logged in user
    private static User currentUser;
    
    static {
        loadEnvVariables();
    }
    
    private static void loadEnvVariables() {
        try {
            // First check if .env file exists
            Path envPath = Paths.get(".env");
            System.out.println("Looking for .env file at: " + envPath.toAbsolutePath());
            
            if (Files.exists(envPath)) {
                Properties envProps = new Properties();
                try (FileInputStream input = new FileInputStream(".env")) {
                    envProps.load(input);
                    
                    // Get Supabase credentials from .env file
                    if (envProps.containsKey("SUPABASE_URL")) {
                        SUPABASE_URL = envProps.getProperty("SUPABASE_URL").trim();
                    }
                    
                    if (envProps.containsKey("SUPABASE_API_KEY")) {
                        SUPABASE_API_KEY = envProps.getProperty("SUPABASE_API_KEY").trim();
                    }
                    
                    // Get Email configuration from .env file
                    if (envProps.containsKey("EMAIL_HOST")) {
                        EMAIL_HOST = envProps.getProperty("EMAIL_HOST").trim();
                    }
                    
                    if (envProps.containsKey("EMAIL_PORT")) {
                        try {
                            EMAIL_PORT = Integer.parseInt(envProps.getProperty("EMAIL_PORT").trim());
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid EMAIL_PORT value: " + e.getMessage());
                        }
                    }
                    
                    if (envProps.containsKey("EMAIL_USERNAME")) {
                        EMAIL_USERNAME = envProps.getProperty("EMAIL_USERNAME").trim();
                    }
                    
                    if (envProps.containsKey("EMAIL_PASSWORD")) {
                        EMAIL_PASSWORD = envProps.getProperty("EMAIL_PASSWORD").trim();
                    }
                    
                    System.out.println("Loaded Supabase credentials from .env file");
                    System.out.println("URL: " + SUPABASE_URL);
                    System.out.println("API key length: " + SUPABASE_API_KEY.length());
                    System.out.println("Email host: " + EMAIL_HOST);
                    System.out.println("Email port: " + EMAIL_PORT);
                    System.out.println("Email username: " + EMAIL_USERNAME);
                }
            } else {
                // If .env file doesn't exist, try to load from environment variables
                String envUrl = System.getenv("SUPABASE_URL");
                String envKey = System.getenv("SUPABASE_API_KEY");
                String envEmailHost = System.getenv("EMAIL_HOST");
                String envEmailPort = System.getenv("EMAIL_PORT");
                String envEmailUsername = System.getenv("EMAIL_USERNAME");
                String envEmailPassword = System.getenv("EMAIL_PASSWORD");
                
                if (envUrl != null && !envUrl.isEmpty()) {
                    SUPABASE_URL = envUrl.trim();
                }
                
                if (envKey != null && !envKey.isEmpty()) {
                    SUPABASE_API_KEY = envKey.trim();
                }
                
                if (envEmailHost != null && !envEmailHost.isEmpty()) {
                    EMAIL_HOST = envEmailHost.trim();
                }
                
                if (envEmailPort != null && !envEmailPort.isEmpty()) {
                    try {
                        EMAIL_PORT = Integer.parseInt(envEmailPort.trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid EMAIL_PORT environment variable: " + e.getMessage());
                    }
                }
                
                if (envEmailUsername != null && !envEmailUsername.isEmpty()) {
                    EMAIL_USERNAME = envEmailUsername.trim();
                }
                
                if (envEmailPassword != null && !envEmailPassword.isEmpty()) {
                    EMAIL_PASSWORD = envEmailPassword.trim();
                }
                
                System.out.println("Tried loading configuration from environment variables");
            }
            
            // Validate credentials
            if (SUPABASE_URL.equals("https://your-supabase-project-url.supabase.co") ||
                SUPABASE_API_KEY.equals("your-supabase-api-key")) {
                System.err.println("Warning: Using default Supabase credentials. Please update your .env file.");
            }
            
            // Validate email configuration
            if (EMAIL_USERNAME.equals("your-email@gmail.com") ||
                EMAIL_PASSWORD.equals("your-app-password")) {
                System.err.println("Warning: Using default email credentials. Please update your .env file.");
            }
        } catch (IOException e) {
            System.err.println("Error loading .env file: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error loading configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Methods to handle current user
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public static boolean isPatient() {
        return currentUser != null && currentUser.isPatient();
    }
} 