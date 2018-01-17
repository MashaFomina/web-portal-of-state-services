package portal.services;

import java.io.FileNotFoundException;
import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.NamingException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Date;
import java.util.Hashtable;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

public class NotificationEmaiService {

    private InputStream inputStream;
    private String fromEmail;
    private String emailPassword;
    private String toEmail;
    private String notification;

    public NotificationEmaiService(String toEmail, String notification) {
        // Read configuration file for getting email account
        try {
            Properties prop = new Properties();
            String propFileName = "config.ini";

            inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }

            fromEmail = prop.getProperty("fromEmail");
            emailPassword = prop.getProperty("emailPassword");
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(NotificationEmaiService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        this.toEmail = toEmail;
        this.notification = notification;
    }

    public boolean sendNotification() {
        if (!isValidEmailAddress()) return false;
        return sendEmail();
    }

    // check if email address is valid or not
    private boolean isValidEmailAddress() {
        // check format
        try {
            new InternetAddress(toEmail).validate();
        } catch (AddressException ex) {
            return false;
        }
        String hostname = toEmail.split("@")[1];
        // request to DNS
        try {
            return doMailServerLookup(hostname);
        } catch (NamingException e) {
            return false;
        }
    }

    private boolean sendEmail() {

        Properties props = new Properties();
        // set secure connection to SMTP server
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        //create Authenticator object to pass in Session.getInstance argument
        Authenticator auth = new Authenticator() {
            //override the getPasswordAuthentication method
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, emailPassword);
            }
        };
        Session session = Session.getInstance(props, auth);

        StringBuilder sb = new StringBuilder();
        sb.append("You are registered at portal of state services.\n");
        sb.append("Important notification: " + notification + "\n");

        sendEmail(session, toEmail, "Important notification", sb.toString());
        return true;
    }

    /**
     * Utility method to send simple HTML email
     * @param session
     * @param toEmail
     * @param subject
     * @param body
     */
    public static void sendEmail(Session session, String toEmail, String subject, String body){
        try
        {
            MimeMessage msg = new MimeMessage(session);
            //set message headers
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
            msg.addHeader("format", "flowed");
            msg.addHeader("Content-Transfer-Encoding", "8bit");

            msg.setFrom(new InternetAddress("no_reply@journaldev.com", "NoReply"));

            msg.setReplyTo(InternetAddress.parse("no_reply@journaldev.com", false));

            msg.setSubject(subject, "UTF-8");

            msg.setText(body, "UTF-8");

            msg.setSentDate(new Date());

            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
            Transport.send(msg);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // return false if quantity of mail servers for domain name is equals zero or error occured while communication with DNS service
    public static boolean doMailServerLookup( String hostName ) throws NamingException {
        Hashtable env = new Hashtable();
        env.put("java.naming.factory.initial",
                "com.sun.jndi.dns.DnsContextFactory");
        DirContext ictx = new InitialDirContext( env );
        Attributes attrs =
                ictx.getAttributes( hostName, new String[] { "MX" });
        Attribute attr = attrs.get( "MX" );
        if( attr == null ) return false;
        return attr.size() > 0;
    }
}