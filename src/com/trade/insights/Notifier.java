package com.trade.insights;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class Notifier {
	public static String to = "helloworld0424@gmail.com";
	private static final String username = "ubuntuslave0424@gmail.com";
	private static final String password = "Ub3rL3$tPass";
     
	//this class has utils to notify via email + sms
	public static void main(String[] args) {
		sendSMS("pen");

	}
	
	public static void sendEmail(String emailTo, String subject, String content) {
        try {
            String host = "smtp.gmail.com";

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", "587");

            // Get the Session object.
            Session session = Session.getInstance(props,
            new javax.mail.Authenticator() {
               protected PasswordAuthentication getPasswordAuthentication() {
                  return new PasswordAuthentication(username, password);
               }
            });
    		
    		MimeMessage message = new MimeMessage(session);  
			message.setFrom(new InternetAddress(username));
			message.addRecipient(Message.RecipientType.TO,new InternetAddress(emailTo));  
	        message.setSubject(subject);  
	        message.setText(content);
	        Transport.send(message);
	        
	        System.out.println("Email message sent successfully: " + message.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void sendSMS(String message) {
		String emailTo = "6313779060@vtext.com";	//my phone number
		sendEmail(emailTo, null, message);
	}

}
