package NetProcess;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
public class Filter {
	/*
	 *统计某个实体被编辑的次数
	 *由此作为流行度的衡量标准 
	 */
	public int count(String name) throws IOException {
       NetSpider nsp=new NetSpider();
		String path="http://www.baike.com/wiki/"+name;
		String sb=nsp.spide(path);
        int c=0;
		try {
			//如果读取的文件存在内容，进行抽取
			if (sb.toString().length() > 0) {
				//将文件存为Document形式
				Document doc = Jsoup.parse(sb.toString());
				
				Elements List=doc.select("div[class*=\"rightdiv cooperation cooperation_t\"]");
					//处理编辑框
					if(!List.toString().equals("")){
						Element edit=List.get(0);
						Element lis =edit.getElementsByTag("li").get(1).getElementsByTag("span").get(0);
						String s=lis.text().substring(0, lis.text().indexOf("次"));
						c=Integer.parseInt(s);
					}	
			}	//end if		
		}catch (Exception e) {
			e.printStackTrace();
     } //end try
		return c;
  }
	public static void main(String arg[]) throws IOException{
		String str="林丹。";
		Filter ft=new Filter();
		System.out.println(ft.count(str));
	}
}
