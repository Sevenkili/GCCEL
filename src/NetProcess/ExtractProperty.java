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
public class ExtractProperty {
	public List<List<String>>extraInfobox(String sb) {
		/*
		 * 用于存储一个网页的属性hash表
		 * 读取网页文件
		 * <title>李盛鹏_搜索_互动百科</title>
		 */
	ClassProcess cp=new ClassProcess();
	List<String>properlist=new ArrayList<String>();//存储属性和关系
	List<String>properlist1=new ArrayList<String>();//存储分类信息
	List<List<String>>list=new ArrayList<List<String>>();
//		File file = new File(filePath);
//		BufferedReader br = null;
		try {
//			br=new BufferedReader(new InputStreamReader(new FileInputStream(
//					file), "UTF-8"));
//			StringBuilder sb = new StringBuilder();
//			int ch = 0;
//
//			while ((ch = br.read()) != -1) {
//				sb.append((char) ch);
//			}			
			//如果读取的文件存在内容，进行抽取
			if (sb.toString().length() > 0) {
				//将文件存为Document形式
				Document doc = Jsoup.parse(sb.toString());
				/*
				 * 抽取infobox内容
				 * 通过jsoup API获取包含infobox的div
				 */
				Elements List = doc.select("div[class*=\"module zoom\"]");
				if(!List.toString().equals("")){
					Element div = List.get(0);
					//通过jsoup API进一步分离Html标签，获取infobox的承载形式---table
					Elements tables = div.getElementsByTag("table");
				   if(!tables.toString().equals("")){
				   //if(tables!=null){
					Element table = tables.get(0);	
					//通过jsoup API进一步分离Html标签，获取每一条td
					Elements tds = table.getElementsByTag("td");	
					for (int i=0;i<tds.size();i++) {		
						//对于每一条td，如果其text不为空，则可抽取
						if(!tds.get(i).text().equals("")){	
						Element td = tds.get(i);
					//HashMap hm = new HashMap(); //存储该条td的属性对
						//String key=td.getElementsByTag("strong").text().replace("：", "");
						String value =td.select(" td>span").text().replace(" ", ",");
					//	properlist.add(value);
						if(value.length()<10){
							properlist.add(value);
						   } //end if
						}//end if
					 }//end for
				 }//end if table
			}//end if
			 else properlist.add("null");
				/*抽取人物关系部分的实体及其链接
			  	  * 
			  	  */
			   Element relation = doc.getElementById("fi_opposite");
			   Element relation1 = doc.getElementById("holder1");
			   Elements lis = new Elements();
			   Elements lisecend = new Elements();
					//处理人物关系
					if(relation != null){
						lis = relation.getElementsByTag("li");
					}
					if(relation1 != null){
						lisecend = relation1.getElementsByTag("li");
					}
					//合并两个集合
					for(int i=0;i<lisecend.size();i++){
						Element li = lisecend.get(i);
                    	 lis.add(li);
					}//end for
			  			
					//提取人物关系部分的关系值
					for(int i=0;i<lis.size();i++){
						String entityname= lis.get(i).getElementsByTag("a").text();
					    if(!properlist.contains(entityname)){
					    	properlist.add(entityname);
				    	 }//end if
					}//end for
					
					
					/*
					 * 抽取开放分类
					 */
					Element openclass = doc.getElementById("openCatp");//获取包含完整开放分类的内容块
					Elements fenlei=new Elements();
					if(openclass != null){
						 fenlei = openclass.getElementsByTag("a");//分离<dd>标签
					}//end if
					if(!fenlei.toString().equals("")){
						if(fenlei.size()<10)
						{
						 for(int i=0;i<fenlei.size();i++){
							String strclass=fenlei.get(i).text();
							List<String> WordList=cp.SegmentProcess(strclass);
							properlist1.addAll(WordList);
							properlist1=cp.removeDuplicateWithOrder(properlist1);
						 }//end for
						  }//end if
						 else{
						  for(int i=0;i<10;i++){
							 String strclass=fenlei.get(i).text();
							 List<String> WordList=cp.SegmentProcess(strclass);
							 properlist1.addAll(WordList);
							 properlist1=cp.removeDuplicateWithOrder(properlist1);
						 }//end for
					}//end if
					}//end if
					else properlist1.add("null");
     	 }//end if (读取文件的部分)
		}catch (Exception e) {
			e.printStackTrace();
			// logger.error	(e.getMessage());
  } //end try
		list.add(properlist);
		list.add(properlist1);
		return list;
	}
	
public static void main(String args[]) throws IOException{
	ExtractProperty expro=new ExtractProperty();
	 NetSpider nsp=new NetSpider();
	String path="http://www.baike.com/wiki/李娜[网球运动员]";
	String str=nsp.spide(path);
	System.out.println(expro.extraInfobox(str));
	
	//System.out.println(array[0]);
}
}
			
