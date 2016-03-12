package NetProcess;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

public class Spider {
	
	public String spide(String urlPath,String name) throws IOException{
		URL url = new URL(urlPath); 
		String path="E:/graduate/hudong/"+name+".txt";
		//设置字符的编码形式
		Charset cn=Charset.forName("UTF-8");
		  //打开到此 URL 的连接并返回一个用于从该连接读入的 InputStream。 
		//reader只能读取单个字符
		Reader reader = new InputStreamReader(new BufferedInputStream(url.openStream())); 
		FileOutputStream out=new FileOutputStream(path);
		String str=" ";
		int c; 
	    while ((c=reader.read())!=-1) 
	    { // System.out.print((char)c); 
	    	str+=(char)c;
	    }
	    	out.write(str.getBytes(cn));
	      // out.write((char)c);
	         reader.close(); 
	         out.close();	
	         return path;
		}

	public static void main(String[]args) throws IOException{
		Spider tu=new Spider();
		tu.spide("http://www.baike.com/wiki/李娜[跳水运动员]","李娜[跳水运动员]");
		System.out.println("数据已爬完");

}
}
