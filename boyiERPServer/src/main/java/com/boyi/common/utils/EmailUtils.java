package com.boyi.common.utils;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class EmailUtils {
    public static final String MODULE_NAME="【博艺ERP】【物料低预警线】";
    public static final String MODULE_ADDNEW_MATERIAL_NAME="【博艺ERP】【补充物料】";

    public static final String MODULE_ORDER_BEFORE_PRODUCTION_PROGRESS_SURECUSTOMERREQUIRED_NAME="【博艺ERP】【产前沟通确认进度表模块】【客户要求确认】";
    public static final String MODULE_ORDER_BEFORE_PRODUCTION_PROGRESS_SURECUSTOMER_NAME="【博艺ERP】【产前沟通确认进度表模块】【确认鞋确认】";

    private static final String EMAIL_OWNER_ADDR_HOST = "smtp.aliyun.com"; //smtp.163.com  smtp.aliyun.com  smtp.qq.com
    private static final String EMAIL_OWNER_ADDR = "akeqaq@aliyun.com";
    private static final String EMAIL_OWNER_ADDR_PASS = "sk1447647127";

    public static void sendMail(String title, String email,String[] csEmails, String content) throws MessagingException {
        Properties prop = new Properties();
        prop.put("mail.host", EMAIL_OWNER_ADDR_HOST);
        prop.put("mail.transport.protocol", "smtp");
        prop.put("mail.smtp.auth", "true");
        //如果不加下面的这行代码 windows下正常，linux环境下发送失败，解决：http://www.cnblogs.com/Harold-Hua/p/7029117.html
        prop.setProperty("mail.smtp.ssl.enable", "true");
        //使用java发送邮件5步骤
        //1.创建sesssion
        Session session = Session.getInstance(prop);
        //开启session的调试模式，可以查看当前邮件发送状态
        //session.setDebug(true);

        //2.通过session获取Transport对象（发送邮件的核心API）
        Transport ts = session.getTransport();
        //3.通过邮件用户名密码链接，阿里云默认是开启个人邮箱pop3、smtp协议的，所以无需在阿里云邮箱里设置
        ts.connect(EMAIL_OWNER_ADDR, EMAIL_OWNER_ADDR_PASS);

        //4.创建邮件
        //创建邮件对象
        MimeMessage mm = new MimeMessage(session);
        //设置发件人
        mm.setFrom(new InternetAddress(EMAIL_OWNER_ADDR));
        //设置收件人
        mm.setRecipient(Message.RecipientType.TO, new InternetAddress(email));

        if(csEmails.length> 0){
            //设置抄送人
            InternetAddress[] csaddress = new InternetAddress[csEmails.length];
            for (int i = 0; i <csEmails.length; i++) {
                csaddress[i] = new InternetAddress(csEmails[i]);
            }
            mm.setRecipients(Message.RecipientType.CC, csaddress);
        }

        mm.setSubject(title);

        //mm.setContent("您的注册验证码为:<b style=\"color:blue;\">0123</b>", "text/html;charset=utf-8");
        mm.setContent(content, "text/html;charset=utf-8");

        // true表示开始附件模式 -----------------------------------------------------------------------
        /*MimeMessageHelper messageHelper = new MimeMessageHelper(mm, true, "utf-8");
        // 设置收件人，寄件人
        messageHelper.setTo(email);
        messageHelper.setFrom(EMAIL_OWNER_ADDR);
        messageHelper.setSubject(title);
        // true 表示启动HTML格式的邮件
        messageHelper.setText(content, true);

        FileSystemResource file1 = new FileSystemResource(new File("d:/rongke.log"));
        FileSystemResource file2 = new FileSystemResource(new File("d:/新建文本文档.txt"));
        // 添加2个附件
        messageHelper.addAttachment("rongke.log", file1);
        try {
            //附件名有中文可能出现乱码
            messageHelper.addAttachment(MimeUtility.encodeWord("新建文本文档.txt"), file2);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new MessagingException();
        }*/
        //-------------------------------------------------------------------------------------------
        //5.发送电子邮件

        ts.sendMessage(mm, mm.getAllRecipients());
    }

    public static void main(String[] args) throws MessagingException {
        sendMail("博艺ERP系统", "244454526@qq.com",new String[]{"1447647127@qq.com"}, "boyi 邮件测试2");
    }
}