package com.boyi;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class Test {
    /*public static void main (String[] args) throws Exception{
        String filePath="C:\\Users\\Ake\\Desktop\\test5.png";
        BufferedImage img = ImageIO.read(new File(filePath));

        // 这里对图片黑白处理,增强识别率.这里先通过截图,截取图片中需要识别的部分
        img = ImageHelper.convertImageToGrayscale(img);

        // 图片锐化,自己使用中影响识别率的主要因素是针式打印机字迹不连贯,所以锐化反而降低识别率
        img = ImageHelper.convertImageToBinary(img);

        // 图片放大5倍,增强识别率(很多图片本身无法识别,放大7倍时就可以轻易识,但是考滤到客户电脑配置低,针式打印机打印不连贯的问题,这里就放大7倍)
//        img = ImageHelper.getScaledInstance(img, img.getWidth() * 7, img.getHeight() * 7);

        //识别图片的文字
        String result = doOCR(img);
        System.out.println(result);
        //再结合敏感词过滤算法，审核图片中的文字是否包含敏感词
//        boolean isSensitive = sensitiveScan(result);
    }
    public static String doOCR(BufferedImage image) throws TesseractException {
        //创建Tesseract对象
        ITesseract tesseract = new Tesseract();
        //设置中文字体库路径
        tesseract.setDatapath("D:\\BaiduNetdiskDownload\\tessdata");
        //中文识别
        tesseract.setLanguage("chi_sim");
        //执行ocr识别
        String result = tesseract.doOCR(image);
        //替换回车和tal键  使结果为一行
//        result = result.replaceAll("\\r|\\n", "-").replaceAll(" ", "");
        return result;
    }
*/

}
