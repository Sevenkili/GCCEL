package NetProcess;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/*
 * 主要是获取网页的纯文本内容，去停用词（的，地，得）
 */
public class ExtractHtmlText {
	public String extraHTMLText(String sb) {
		List<String>name=new ArrayList<String>();
		String text=null;
	       //读取网页文件
//			File file = new File(filePath);
//			BufferedReader br = null;
			try {
//				br = new BufferedReader(new InputStreamReader(new FileInputStream(
//						file), "UTF-8"));
//				StringBuilder sb = new StringBuilder();
//				int ch = 0;
//
//				while ((ch = br.read()) != -1) {
//					sb.append((char) ch);
//				}			
				//如果读取的文件存在内容，进行抽取
				if (sb.toString().length() > 0) {
					//将文件存为Document形式
		 Document doc = Jsoup.parse(sb.toString());
          
		  Element openclass = doc.getElementById("openCatp");//获取开放分类
		  Element abstracts=doc.getElementById("unifyprompt"); //获取摘要部分			
	      Element content = doc.getElementById("content"); //获取正文部分
	      Element baikedangan = doc.getElementById("bkda"); //获取百科档案部分
	      String fenlei=null;
	      String dangan=null;
	      String abs=null;
	      String con=null;
	      if(openclass!=null){
	    	  fenlei=openclass.text().replace("开放分类：","");
	      }
	      if(abstracts!=null)
	      {  abs=abstracts.text().replace("编辑摘要","");}
	      if(content!=null)
	      {con=content.text();}
	      if(baikedangan!=null)
	      {dangan=baikedangan.text().replace("百科档案","");}
	      text=fenlei+abs+dangan+con;
	      
//	      System.out.println(abs);
//	      System.out.println(dangan);
//	      System.out.println(con);
	     // System.out.println( text);
	    	
				}
		}catch (Exception e) {
				e.printStackTrace();
				// logger.error	(e.getMessage());
	   }
    return text;
}
	public static void main(String arg[]) throws IOException{
		ExtractHtmlText ex=new ExtractHtmlText();
		String text=null;
		NetSpider nsp=new NetSpider();
		 String name="王晶[导演]";
		 String path="http://www.baike.com/wiki/"+name;
		 long startTime=System.currentTimeMillis();   //获取开始时间
		 String str=nsp.spide(path);
		 //ex.extraHTMLText(path);
		 text=ex.extraHTMLText(str);
		 System.out.println(text);
		 //将提取得到的纯文本保存到本地
//		 FileOutputStream out=new FileOutputStream("E:/graduate/hudong/text/"+name+".txt");
//		 out.write(text.getBytes());
//		 out.close();
		 long endTime=System.currentTimeMillis(); //获取结束时间
		 double minute=(endTime-startTime)/1000.0;
		System.out.println("程序运行时间： "+minute+"s");
	}
}
