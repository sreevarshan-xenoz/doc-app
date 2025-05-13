import javax.mail.Session;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import java.util.Properties;

/**
 * A simple standalone test to verify email sending works
 */
public class simpleEmailTest {
    public static void main(String[] args) {
        System.out.println("Simple Email Test");
        System.out.println("================");
        
        String emailHost = "smtp.gmail.com";
        int emailPort = 587;
        final String emailUsername = "sreevarshan1511@gmail.com"; // From your .env file
        final String emailPassword = "ggqc iysy dhdg xbhx"; // From your .env file
        
        try {
            // Set up mail server properties
            Properties properties = new Properties();
            properties.put("mail.smtp.host", emailHost);
            properties.put("mail.smtp.port", emailPort);
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            
            // Create a session with authenticator
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(emailUsername, emailPassword);
                }
            });
            
            // Enable debugging
            session.setDebug(true);
            
            // Create a new message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailUsername, "Test Email"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("test@example.com"));
            message.setSubject("Test Email");
            message.setText("This is a test email from the standalone test application.");
            
            // Send the message
            System.out.println("Sending email...");
            Transport.send(message);
            System.out.println("Email sent successfully!");
            
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 