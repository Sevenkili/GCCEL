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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
public class HtmlTest {
	public HashMap<String,String> extraHTMLText(String sb) {
	List<String>name=new ArrayList<String>();
    HashMap<String,String>hash=new HashMap<String,String>();//存储实体名，及其链接
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
		 if (sb.toString().length()> 0) {
					//将文件存为Document形式
					Document doc = Jsoup.parse(sb.toString());
					 String regex="[0-9]+?";//正则表示，过滤那些数字标签
					 Pattern p=Pattern.compile(regex);
		   /*
		    * 获取摘要部分的实体及其链接
		    */
		  Element abstracts=doc.getElementById("unifyprompt"); //获取摘要部分
		  Elements absentitylist = new Elements();
		   if(abstracts!=null){
			   absentitylist=abstracts.getElementsByTag("a");
			   
		   }
		    for(int i=0;i<absentitylist.size();i++){
		    	if(absentitylist.get(i).hasAttr("href")){
		    	   String entityname=absentitylist.get(i).text(); 
//		           String entityname=taglist.get(i).attributes().get("title"); 
     	           String link=absentitylist.get(i).attributes().get("href"); 
     	           if(!link.contains("http"))
     	           {
     	        	  link="http://www.baike.com/wiki/"+entityname;
     	           }
     	          //     !entityname.contains("[")&&
     	           Matcher m=p.matcher(entityname);
		    		if(!m.find()&&entityname.length()!=0&&!name.contains(entityname)){
		    		    name.add(entityname);
		    			hash.put(entityname, link);
		    		}//end if
		    	   }//end if
		    	 
		      } //end for
			
			/*
			 * 抽取html的正文内容的实体及其链接
			 * 通过jsoup API来解析
			 */
	      Element content = doc.getElementById("content");
	     // System.out.println(content.text());
	      Elements conentitylist = new Elements();
	      if(content!=null){
	    	  conentitylist=content.getElementsByTag("a");
	       }//end if
	      //  System.out.println(taglist.get(3).hasAttr("title"));
	      //System.out.println(taglist.get(3).attributes().get("title"));
	      //System.out.println(taglist.get(0).text().equals(null));
	       for(int i=0;i<conentitylist.size();i++){
	    	if(conentitylist.get(i).hasAttr("title")&&conentitylist.get(i).hasAttr("href")){
	    	   String entityname=conentitylist.get(i).text(); 
//	           String entityname=conentitylist.get(i).attributes().get("title"); 
	           String link=conentitylist.get(i).attributes().get("href"); 
	           if(!link.contains("http"))
 	           {
 	        	  link="http://www.baike.com/wiki/"+entityname;
 	           }
	      //     !entityname.contains("[")&&
	           Matcher m=p.matcher(entityname);
	    		if(!m.find()&&entityname.length()!=0&&!name.contains(entityname)){
	    			 name.add(entityname);
	    			hash.put(entityname, link);
	    		}//end if
	    	   }//end if
	    	 
	      } //end for
	       
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
				//System.out.println(lis.text());
			}//end for
	  			
			//提取人物关系
			for(int i=0;i<lis.size();i++){
				String entityname= lis.get(i).getElementsByTag("a").text();
				//System.out.println(entityname);
			   // String link=lis.get(i).getElementsByTag("a").attr("href");
				String link="http://www.baike.com/wiki/"+entityname;
				// Matcher m=p.matcher(entityname);
			    if(entityname.length()!=0&&!name.contains(entityname)){
			        name.add(entityname);
			    	hash.put(entityname, link);
		    	 }//end if
				
			}//end for
			
			/*
			 * 抽取相关词条
			 */
			
			 Element val = doc.getElementById("xgct");
			 Elements elment = new Elements();
			 if(val!= null){
				 elment= val.getElementsByTag("li");
				}
			
			//提取词条
				for(int i=0;i<elment.size();i++){
					String valname= elment.get(i).getElementsByTag("a").text();
					// System.out.println(valname);
				   // String link=lis.get(i).getElementsByTag("a").attr("href");
					String link="http://www.baike.com/wiki/"+valname;
					// Matcher m=p.matcher(valname);
				    if(valname.length()!=0&&!name.contains(valname)){
				        name.add(valname);
				    	hash.put(valname, link);
			    	 }//end if	
				}//end for
			
				}		
		}catch (Exception e) {
				e.printStackTrace();
	   }
//		System.out.println(name);
//		System.out.println(name.size());
	  return hash;
	}
	
 public static void main(String arg[]) throws IOException{
	 HashMap<String,String>result=new HashMap<String,String>();
	 HtmlTest ht=new HtmlTest();
	 NetSpider nsp=new NetSpider();
	 long startTime=System.currentTimeMillis();   //获取开始时间
	 String path="http://www.baike.com/wiki/何炅";
	 String str=nsp.spide(path);
	 int i=0;
	 //path=sp.spide("http://www.baike.com/wiki/"+name,name);
	 //result=ht.extraHTMLText(path);
	 result=ht.extraHTMLText(str);
//	 for(String entityname:result.keySet()){
//		 sp.spide(result.get(entityname), entityname);
//		 System.out.println(i);
//		 i++;
//	 }
	 System.out.println(result);
	// System.out.println(result.size());
	 long endTime=System.currentTimeMillis(); //获取结束时间
		double minute=(endTime-startTime)/1000.0;
		System.out.println("程序运行时间： "+minute+"s");
	 
 }

}
