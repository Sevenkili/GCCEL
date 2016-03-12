package Linking;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import EntityRecognize.Entity;
import EntityRecognize.EntityRecogtion;
import Graph.ComputerDegree;
import Graph.Graph;
import Graph.MatrixGraph;
import KnowledgeBase.Neo4jOperate;
import NetProcess.ExtractHtmlText;
import NetProcess.ExtractProperty;
import NetProcess.Filter;
import NetProcess.HtmlTest;
import NetProcess.NetSpider;
import Similary.ComputerSimilarty;
import Similary.TextProcessing;
import Similary.TextSimilar;
 /*
  * 目标提高消岐的速度，速度较前期的有所提升
  * 最新修改加了break；
  */
public class PriorEntityLinking {
	 /*
	  * 全局变量
	  */
	 public static String docStr=null;
	 List<String>outlist=new ArrayList<String>();
     /*
	 * 得到所有实体指称项的候选实体以及他们的分类
	 * 最新版本
	 */
     public HashMap<String,List<HashMap<String,List<String>>>> getMention(List<String>list){
     HashMap<String,List<HashMap<String,List<String>>>>hm=new HashMap<String,List<HashMap<String,List<String>>>>();
        for(int i=0;i<list.size();i++){  
    		Neo4jOperate neo=new Neo4jOperate();
            List<HashMap<String,List<String>>>hash=new ArrayList<HashMap<String,List<String>>>();
             hash=neo.getMen(list.get(i));
             
    		 //指称项在知识库中没有候选项
    		 if(hash.get(0).size()==0){
    			hm.put(list.get(i),hash);
    			System.out.println(list.get(i)+"在知识库中不存在候选，返回NIL");
    			outlist.add(list.get(i));
    		//	account++;
    		 }//end if
    		 //指称项在知识库中有候选项
    		 if(hash.get(0).size()>0){
    		   hm.put(list.get(i),hash);
    		 }//end if
    	 }
    	// System.out.println(hm); 
    	 return hm;
     }
  
     /*
      * 如果文本中只有一个实体，采用文本相似度比较的方法
      */
     public Boolean oneEntity(List<String>list) throws IOException{
    	 TextSimilar ts=new TextSimilar();
 	     TextProcessing tp=new TextProcessing();
 	     ExtractHtmlText ex=new ExtractHtmlText();
 	     NetSpider nsp=new NetSpider();//爬虫程序
 	     Boolean flag=false;
 	    HashMap<String,String>urlhm=new HashMap<String,String>();
 	  
 	     for(int i=0;i<list.size();i++)
 	      {   
 	       Neo4jOperate neo=new Neo4jOperate();
 	       urlhm=neo.getUrl(list.get(i));//得到hurl列表
 	       String text=docStr.replaceAll(list.get(i), "");//得到文本中的内容,除去该实体的
 	       HashMap<String,Double>hs=new HashMap<String,Double>();
 	      //  System.out.println("该文本中只有一个实体");
 	     for(String key:urlhm.keySet()){
 	    	if(key.contains(" 　")||key.contains("。")||key.contains("}")||key.contains(",")
 	     ||key.contains(":")||key.contains(")")||key.contains(" ")||key.contains("　　")||key.contains(" ")
 	      ||key.contains("：")||key.contains("{")||key.contains("　")||key.contains("，"))
 	    	 {
 	    		// do nothing 格式有问题
 	    	   }
 	    	else {
 	         List<String>onelist=tp.SegmentProcess(text);
 	    	 String str=nsp.spide(urlhm.get(key));
 	    	
 	    	 String htmltext=ex.extraHTMLText(str);
 	    	 List<String>twolist=tp.SegmentProcess(htmltext.replace(list.get(i),""));
 	    	  double sim=ts.compute(onelist,twolist);
 	    	//  System.out.println(key+sim);
 	    	  hs.put(key, sim);
 	     	 }//end else
 	     }//end for
 	    // System.out.println(hs);
 	      //选取最大相似度的
 	       double simmax=0.35;//相当于定义了一个阀值，这个值可以根据情况而改，低于阀值的相当于没找到
 	    	 for(String key:hs.keySet()){
 	    		 if(hs.get(key)>simmax){
 	    			 simmax=hs.get(key);
 	    		 }//end if
 	     }//end for
 	    //链接到知识库的候选实体上
 	    for(String key:hs.keySet()){
  	      if(simmax==hs.get(key)) 
  	     {System.out.println("该文本中的"+list.get(i)+"="+key);
  	           outlist.add(list.get(i));
  	           flag=true;
  	           break;
  	        // account++;
  	           }
  	     
 	       } //end for
 	 //=   System.out.println(hs);
 	      if(simmax==0.35) {
 	    	  System.out.println("该文本中的"+list.get(i)+"在知识库中没有相应信息,返回NIL");
 	    	   outlist.add(list.get(i));
 	    	  //  account++;
 	      }
 	     } //end for 
 	     return flag;
     }   
    
     public static void main(String arg[]){
    	 String text=null;//存储从文本中拿出来的字符串
       List<String>entitylist=new ArrayList<String>();
       PriorEntityLinking nel=new PriorEntityLinking();
       EntityRecogtion er=new EntityRecogtion();
	    Neo4jOperate neo=new Neo4jOperate();
	    long startTime=System.currentTimeMillis();  //获取开始时间
	    neo.createDb(); //打开知识库
	 //   System.out.println("文本为："+text);
		try {
			FileInputStream fin=new FileInputStream("Text/text.txt");
			BufferedReader innet = new BufferedReader(new InputStreamReader(fin));
			while((text=innet.readLine())!=null)
			{ 
		     System.out.println("文本为："+text);
//			 byte[] buf=new byte[1000000];//缓存的大小决定文本的大小
//			 int len=fin.read(buf);//从text.txt中读出内容
//			 text=new String(buf,0,len);
			    docStr=text;//存储一个全局的值
			    entitylist=er.entityrecognize(text);
			    HashMap<String,List<HashMap<String,List<String>>>>ha=nel.getMention(entitylist);
			    for(int i=0;i<entitylist.size();i++){
			    	String mention=entitylist.get(i);
			    	List<String>linshi=new ArrayList<String>();
			    	if(ha.get(mention).get(0).size()>1){
			    		linshi.add(mention);
			    		nel.oneEntity(linshi);
			    	}
			    }
		        System.out.println();
			}
			fin.close();
		} catch (IOException e) {
			e.printStackTrace();
		}//end try
		 neo.shutDown();//关闭知识库
	     long endTime=System.currentTimeMillis(); //获取结束时间
		 double minute=(endTime-startTime)/1000.0;
		System.out.println("程序运行时间： "+minute+"s");
	 }
      
    	 
     }
     
