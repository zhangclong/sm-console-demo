package com.uh.console.utils;



import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.uh.common.utils.StringUtils;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailSenderUtils {

    // 构造器或setter用于设置邮件配置
    public static boolean sendEmail(String to, String subject, String text,String sendMial){
        JSONObject jsonObject = JSON.parseObject(sendMial);
        String host = jsonObject.getString("host");
        String username = jsonObject.getString("username");
        String password = jsonObject.getString("password");
        if(StringUtils.isBlank(host) || StringUtils.isBlank(username)  || StringUtils.isBlank(password) ){
            return false;
        }

        Properties props = new Properties();
        props.put("mail.host", host);
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props);
        session.setDebug(true);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(text);
            Transport transport = session.getTransport();
            transport.connect(host, username, password);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
