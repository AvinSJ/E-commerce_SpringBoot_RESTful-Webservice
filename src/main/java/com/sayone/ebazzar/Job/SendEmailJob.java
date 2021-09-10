package com.sayone.ebazzar.Job;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;

@Component
public class SendEmailJob implements Job {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MailProperties mailProperties;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
            String mail = jobDataMap.getString("email");
            String body = String.format("%d \n %s ",jobDataMap.get("review_Id"),jobDataMap.getString("vote"));

            sendMail("avin.springdemo@gmail.com", mail, "Test", body);
            sendMail("avin.springboot@gmail.com","avin@sayonetech.com","TEST","FAKE MAIL");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void sendMail(String fromEmail, String toEmail, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper messageHelper = new MimeMessageHelper(message, StandardCharsets.UTF_8.toString());
        messageHelper.setSubject(subject);
        messageHelper.setText(body, true);
        messageHelper.setFrom(fromEmail);
        messageHelper.setTo(toEmail);

        mailSender.send(message);
    }
}



