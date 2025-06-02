package exam_easv_belman.BLL.util;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Draft;
import com.google.api.services.gmail.model.Message;
import exam_easv_belman.BLL.exceptions.BelmanBLLException;
import org.apache.commons.codec.binary.Base64;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.security.auth.Subject;
import java.io.*;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import static com.google.api.services.gmail.GmailScopes.GMAIL_COMPOSE;
import static com.google.api.services.gmail.GmailScopes.GMAIL_SEND;

public class Gmailer {

    public static final String EMAIL = "M2CProject2025@gmail.com";

    private static final String APPLICATION_NAME = "Belman Photo Documentation";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final Gmail SERVICE;


    public Gmailer() throws GeneralSecurityException, IOException {
        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        SERVICE = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, jsonFactory)).setApplicationName(APPLICATION_NAME).build();

    }

    /**
     * Loads Google API credentials from the client_secrets.json file and handles the OAuth 2.0 authorization flow.
     * This method is used to authorize the application to access the Gmail API on behalf of the user.
     *
     * @param httpTransport The transport layer used for secure HTTP communication.
     * @param jsonFactory   The JSON factory used to parse client secrets.
     * @return A Credential object representing the authorized user's access token.
     * @throws IOException If the client_secrets.json file cannot be read or an error occurs during authorization.
     */
    private static Credential getCredentials(final NetHttpTransport httpTransport, GsonFactory jsonFactory)
            throws IOException {
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(Gmailer.class.getResourceAsStream("/client_secrets.json")));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets, Set.of(GMAIL_SEND, GMAIL_COMPOSE))
                .setDataStoreFactory(new FileDataStoreFactory(Paths.get("tokens").toFile()))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Sends an email with a subject, message body, and PDF attachment using the Gmail API.
     *
     * @param subject        The subject line of the email.
     * @param message        The plain text message body.
     * @param toEmailAddress The recipient's email address.
     * @param attachment     A PDF file to attach to the email.
     * @throws GeneralSecurityException If a security issue occurs during Gmail API access.
     * @throws IOException              If an IO error occurs during message construction or sending.
     * @throws MessagingException       If an error occurs when creating or sending the email message.
     */
    public void sendMail(String subject, String message, String toEmailAddress, File attachment) throws GeneralSecurityException, IOException, MessagingException {

        // Encode as MIME message
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(EMAIL));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(toEmailAddress));
        email.setSubject(subject);

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(message, "text/plain");
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);
        mimeBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(attachment);
        mimeBodyPart.setDataHandler(new DataHandler(source));
        mimeBodyPart.setFileName(subject + ".pdf");
        multipart.addBodyPart(mimeBodyPart);
        email.setContent(multipart);

        // Encode and wrap the MIME message into a gmail message
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] rawMessageBytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(rawMessageBytes);
        Message msg = new Message();
        msg.setRaw(encodedEmail);

        try {
            // Create and send the message
            // Draft creation is optional; you can send directly without it
            Message sentMessage = SERVICE.users().messages().send("me", msg).execute();  // Send the message directly
            System.out.println("Message sent: " + sentMessage.getId());
            System.out.println(sentMessage.toPrettyString());
        } catch (GoogleJsonResponseException e) {
            // Handle error appropriately
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 403) {
                System.err.println("Unable to send message: " + e.getDetails());
                throw new BelmanBLLException("Unable to send message: " + e.getDetails());
            } else {
                throw e;
            }
        }
    }

}

