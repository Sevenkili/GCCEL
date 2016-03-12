package NetProcess;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

public class NetSpider {
	public String spide(String urlPath) throws IOException{
		URL url= new URL(urlPath); 
		//System.out.println(url);
		//设置字符的编码形式
		Charset cn=Charset.forName("UTF-8");
		  //打开到此 URL 的连接并返回一个用于从该连接读入的 InputStream。 
		//reader只能读取单个字符
		Reader reader= new InputStreamReader(new BufferedInputStream(url.openStream()));
		//FileOutputStream out=new FileOutputStream(path);
		String str="";
		int c; 
	    while ((c=reader.read())!=-1) 
	    { // System.out.print((char)c); 
	    	str+=(char)c;
	    } 
	         reader.close(); 	
	         return str;
		}
	
	public static void main(String arg[]) throws IOException{
		NetSpider nsp=new NetSpider();
		String[]str={"中国农业大学"};
		long startTime=System.currentTimeMillis();   //获取开始时间
		for(int i=0;i<str.length;i++){
			String url="http://www.baike.com/wiki/"+str[i];
			 System.out.println(nsp.spide(url));	
		}
		long endTime=System.currentTimeMillis(); //获取结束时间
		double minute=(endTime-startTime)/1000.0;
		System.out.println("程序运行时间： "+minute+"s");
	}
}
