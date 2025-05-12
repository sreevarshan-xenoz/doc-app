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
    
    // Default admin credentials (for demo purposes)
    public static final String ADMIN_USERNAME = "admin";
    public static final String ADMIN_PASSWORD = "admin";
    
    static {
        loadEnvVariables();
    }
    
    private static void loadEnvVariables() {
        try {
            // First check if .env file exists
            Path envPath = Paths.get(".env");
            if (Files.exists(envPath)) {
                Properties envProps = new Properties();
                FileInputStream input = new FileInputStream(".env");
                envProps.load(input);
                input.close();
                
                // Get Supabase credentials from .env file
                if (envProps.containsKey("SUPABASE_URL")) {
                    SUPABASE_URL = envProps.getProperty("SUPABASE_URL");
                }
                
                if (envProps.containsKey("SUPABASE_API_KEY")) {
                    SUPABASE_API_KEY = envProps.getProperty("SUPABASE_API_KEY");
                }
                
                System.out.println("Loaded Supabase credentials from .env file");
            } else {
                // If .env file doesn't exist, try to load from environment variables
                String envUrl = System.getenv("SUPABASE_URL");
                String envKey = System.getenv("SUPABASE_API_KEY");
                
                if (envUrl != null && !envUrl.isEmpty()) {
                    SUPABASE_URL = envUrl;
                }
                
                if (envKey != null && !envKey.isEmpty()) {
                    SUPABASE_API_KEY = envKey;
                }
                
                System.out.println("Tried loading Supabase credentials from environment variables");
            }
        } catch (IOException e) {
            System.err.println("Error loading .env file: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 