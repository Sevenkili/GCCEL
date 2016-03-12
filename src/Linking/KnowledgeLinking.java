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
 * 没有考虑增量挖掘的过程，只考虑知识库里面的知识情况
 */
public class KnowledgeLinking {
	 public static String docStr=null;
     /*
	 * 得到所有实体指称项的候选实体以及他们的分类
	 */
     public HashMap<String,List<HashMap<String,List<String>>>> getMention(List<String>list){
     HashMap<String,List<HashMap<String,List<String>>>>hm=new HashMap<String,List<HashMap<String,List<String>>>>();
    	 for(int i=0;i<list.size();i++){
    		 Neo4jOperate neo=new Neo4jOperate();
            List<HashMap<String,List<String>>>hash=new ArrayList<HashMap<String,List<String>>>();
             hash=neo.getMen(list.get(i));
//             System.out.println(list.get(i));
//             System.out.println(hash);
           //   hm.put(list.get(i),hash);
    		 //指称项在知识库中没有候选项
    		 if(hash.get(0).size()==0){
    			hm.put(list.get(i),hash);
    			System.out.println(list.get(i)+"在知识库中不存在候选，返回NIL");
    		 }//end if
    		 //指称项在知识库中有候选项
    		 if(hash.get(0).size()>0){
    		   hm.put(list.get(i),hash);
    		 }//end if
    		 neo.shutDown(); //注意关了之后要重新创建该类
    	 }
    	// System.out.println(hm); 
    	 return hm;
     }
  
     /*
      * 如果文本中只有一个实体，采用文本相似度比较的方法
      */
     public void oneEntity(List<String>list) throws IOException{
    	 TextSimilar ts=new TextSimilar();
 	     TextProcessing tp=new TextProcessing();
 	     NetSpider nsp=new NetSpider();//爬虫程序
 	     ExtractHtmlText ex=new ExtractHtmlText();
 	    HashMap<String,String>urlhm=new HashMap<String,String>();
 	     for(int i=0;i<list.size();i++)
 	     { 
 	       Neo4jOperate neo=new Neo4jOperate();
 	       urlhm=neo.getUrl(list.get(i));//得到url列表
 	            neo.shutDown(); //关闭知识库
 	      HashMap<String,Double>hs=new HashMap<String,Double>();
 	       String text=docStr;//得到文本中的内容
 	      //  System.out.println("该文本中只有一个实体");
 	     for(String key:urlhm.keySet()){
 	    	 List<String>onelist=tp.SegmentProcess(text);
 	    	 String str=nsp.spide(urlhm.get(key));
 	    	 String htmltext=ex.extraHTMLText(str);
 	    	 List<String>twolist=tp.SegmentProcess(htmltext);
 	    	 double sim=ts.compute(onelist,twolist);
 	    	 hs.put(key, sim);
 	     }//end for
 	     // System.out.println(hs);
 	      //选取最大相似度的
 	       double simmax=0.45;//相当于定义了一个阀值，这个值可以根据情况而改，低于阀值的相当于没找到
 	    	 for(String key:hs.keySet()){
 	    		 if(hs.get(key)>simmax){
 	    			 simmax=hs.get(key);
 	    		 }//end if
 	     }//end for
 	    //链接到知识库的候选实体上
 	    for(String key:hs.keySet()){
  	      if(simmax==hs.get(key))  System.out.println("该文本中的"+list.get(i)+"="+key);
 	       } //end for
 	  //  System.out.println(hs);
 	    if(simmax==0.45) System.out.println("该文本中的"+list.get(i)+"在知识库中没有相应信息,返回NIL");
 	     } //end for 
 	     
     }
     
   
     /*
      * 先用知识库的知识构造图，不行再利用外部的互动网页知识构造图
      * 找关系节点也很费时间
      * 
      */
     public HashMap<String,Double>Graph(List<String>list) throws IOException{
    	 HashMap<String,List<HashMap<String,List<String>>>>ha=getMention(list);
         List<String>graphnode=new ArrayList<String>();
         HtmlTest ht=new HtmlTest();
         Filter ft=new Filter();//根据流行度过滤
         ComputerDegree cd=new ComputerDegree();//计算矩阵的出入度
         ExtractProperty expro=new ExtractProperty(); //抽取网页属性
         NetSpider nsp=new NetSpider();//爬虫程序
         ComputerSimilarty cs=new ComputerSimilarty();//计算相似度
         String path=null;//网页url
         
         HashMap<String,String>result=new HashMap<String,String>();//存储网页链接
         List<String>result1=new ArrayList<String>();//存储实体指称项的属性
         List<String>result2=new ArrayList<String>();//存储候选实体的属性
         List<String>oneresult=new ArrayList<String>();//存储候选实体的属性
         List<String>classresult1=new ArrayList<String>();//存储候选实体的分类
         List<String>classresult2=new ArrayList<String>();//存储候选实体的分类
         HashMap<String,Double>degreehash=new HashMap<String,Double>();
         Boolean flag=false; //验证单个实体是否在知识库中
		 HashMap<String,Boolean>boolhash=new HashMap<String,Boolean>();
		 
         for(String key:ha.keySet()){
      	   //如果实体指称项在知识库子中没有候选实体或者只有一个候选实体，则该实体直接作为图节点
      	  if(ha.get(key).get(0).size()==0){
      		  graphnode.add(key);
      	  }//end if
      	  //如果实体在知识库中有一个的候选实体，则用该候选实体当做节点
      	  if(ha.get(key).get(0).size()==1){
      		   for(String key1:ha.get(key).get(0).keySet()){
      			   path="http://www.baike.com/wiki/"+key;
      		       String text=nsp.spide(path);
  	               oneresult=expro.extraInfobox(text).get(0);
      		       result1=ha.get(key).get(0).get(key1);
      		       double sim=cs.compute(oneresult, result1);//比较他们的相似度
      		      // System.out.println(sim);
      		       if(sim<0.4) {
      		    	   System.out.println(key+"在知识库中不存在相应信息");//0.35是一个阀值
      		    	    boolhash.put(key,flag);
      		       }
      		       else { System.out.println(key+"=知识库中的"+key1);
      		                 flag=true;//flag=true说明该实体在知识库中
							 boolhash.put(key,flag);
      		       }
      			   graphnode.add(key1);
      		   } //end for
      	  }//end if
      	
      	//如果实体在知识库中有多于一个的候选实体，则用该候选实体当做节点
      	  if(ha.get(key).get(0).size()>1){
      		   for(String key1:ha.get(key).get(0).keySet()){
      			   if(!key1.contains(" 　")&&!key1.contains("。")&&!key1.contains("}")&&!key1.contains(",")
      			  &&!key1.contains(")")&&!key1.contains(" ")&&!key1.contains("　　")&&!key1.contains(" ")){
      				 graphnode.add(key1);
      			   }
 //   
//      		 if(!key1.contains("　　")&&!key1.contains(" 　")&&!key1.contains(" ")&&ft.count(key1)>=10){
//      			   graphnode.add(key1);
//      			   }//end if 编辑次数大于10的才被选择
      			   
      		   } //end for
      	  }//end if
         }//end for
		 
         //为了更好的处理，将图节点存储两次
         String[] nodearray = (String[])graphnode.toArray(new String[0]);  
         String[] nodearray1 = (String[])graphnode.toArray(new String[0]);
         Graph graph = new MatrixGraph(nodearray);
         System.out.println(graphnode);
         HashMap<String,List<String>> global=new HashMap<String,List<String>>();//保留全局 
         for(int i=0;i<nodearray.length;i++){
        	 String subname=null;
        	 String sub=null;
        	 HashMap<String,List<String>> relhash=new HashMap<String,List<String>>();
        	 if(nodearray[i].contains("[")){
        		subname=nodearray[i].substring(0, nodearray[i].indexOf("["));
        	 }//end if 
        	 else if(nodearray[i].contains("》")){
        		 subname=nodearray[i].substring(1, nodearray[i].indexOf("》"));
        		// System.out.println(subname);
        	 }//书名歧义，比较特殊
        	 
        	 else{
        		 subname=nodearray[i]; 
        	 }//end else //简化节点信息
        	 
			 if(i>0){
				 if(nodearray[i-1].contains(subname)){
				    relhash=global;
		        	}
		    	else{
		    	 Neo4jOperate neo=new Neo4jOperate();
		         relhash=neo.getRelentity(subname);//得到与该节点有关系的节点
		         neo.shutDown(); //注意关了之后要重新创建该类
		         global=relhash;
		    	}
			 }//end if
			 
			 if(i==0){
			   Neo4jOperate neo=new Neo4jOperate();
	           relhash=neo.getRelentity(subname);//得到与该节点有关系的节点
	           neo.shutDown(); //注意关了之后要重新创建该类
	           global=relhash;
			 }//end if
    
        	 String temNode=null;//中间节点，防止同名实体之间也有关系
        	 HashMap<String, HashMap<String,Double>>hmm=new  HashMap<String, HashMap<String,Double>>();
        	 
        	 /*
        	  * 实体在知识库中，先判断他们在知识库中是否有关系，没有则去网上找
        	  */
        	 if(ha.get(subname).get(0).size()==1){
        	  //只有一个候选，但候选是正确的
        	   if(boolhash.get(subname)){
        		for(int j=0;j<nodearray1.length;j++){
        		 if(!nodearray[i].equals(nodearray1[j])){
        		    if(relhash.get(nodearray[i]).contains(nodearray1[j])){
         				graph.addEdge(nodearray[i],nodearray1[j],1);
         			//System.out.println(nodearray[i]+"和"+nodearray1[j]+"在库中有关系");
         			}//end if
               }//end if
            }//end for
         }//end if 
        }//end if
        	 
        /*
         * 实体在知识库中由多个候选，则也先判断他们在知识库中和其他实体之间是否有关系
         * 没有在去知识库中，歧义实体之间不进行比较
         */
          if(ha.get(subname).get(0).size()>1){
        	for(int j=0;j<nodearray1.length;j++){
        	     String subname1=null;
        		 if(nodearray1[j].contains("[")){
             	    subname1=nodearray1[j].substring(0, nodearray1[j].indexOf("["));
             	     } //end if
        		  else if(nodearray1[j].contains("》")){
        			  subname1=nodearray1[j].substring(1, nodearray1[j].indexOf("》"));
        		   }
             	  else {
             		  subname1=nodearray1[j];
             	  }//end else
        	//  if(!nodearray[i].equals(nodearray1[j])){
        	  if(!subname.equals(subname1)){ 
      		    if(relhash.get(nodearray[i]).contains(nodearray1[j])){
       				graph.addEdge(nodearray[i],nodearray1[j],1);
       			//System.out.println(nodearray[i]+"和"+nodearray1[j]+"在库中有关系");
       			}//end if
        	  }//end if
           }//end for
       	}//end if(第三种情况）
      }//end for
        String graphresult=graph.printGraph();
//        System.out.println();
//        System.out.println(graphresult);
        degreehash=cd.computer(nodearray, graphresult);
        return degreehash;
     }
     
    /*
     * 选择函数（判断文本中是一个实体还是两个实体）
     */
     public void Select(List<String>list) throws IOException{
    	 if(list.size()==1){
    		 oneEntity(list);
    	 }//end if
    	 
    	 if(list.size()>1){
    	  //HashMap<String,Integer>degreehash=buildGraph(list);
          HashMap<String,Double>degreehash=Graph(list);
    	  
         int sum=0;//用来统计所有节点的出入度和
         for(String key:degreehash.keySet()){
        	 sum+=degreehash.get(key);
         }//end for
         if(sum>0){
        	 for(int i=0;i<list.size();i++){
            	 HashMap<String,Double>canhash=new HashMap<String,Double>();
            	 String subkey;
            	 for(String key:degreehash.keySet()){
            		  if(key.contains("[")){
            			 subkey=key.substring(0, key.indexOf("["));
            		  } //end if
            		  else if(key.contains("》")){
            			  subkey=key.substring(1, key.indexOf("》"));
            		  }
            		  else{
            			subkey=key;
            		  }//end if
            		  
            		 if(subkey.equals(list.get(i))){
            			 canhash.put(key, degreehash.get(key));
            		 }//end if
            	 }//end for
            	 if(canhash.size()>1){
            		double max=0;
            		 for(String key:canhash.keySet()){
        	    		 if(canhash.get(key)>max){
        	    			 max=canhash.get(key);
        	    		 }//end if
        	           }//end for
            	   for(String key:canhash.keySet()){
                	      if(max==canhash.get(key)&&max>0) {
                	  System.out.println("文本中的"+list.get(i)+"="+key);
                	      } //end if  
               	       } //end for
           	    }//end if
              }//end for
         } //end if
        if(sum==0){
        	 oneEntity(list);
        }
       }//end if	 
     }
     
     /*
      * 主函数
      */
   public static void main(String args[]) throws IOException{
	    /*
		 * 从文本中读取内容
		 */
	    String text=null;//存储从文本中拿出来的字符串
	    List<String>entitylist=new ArrayList<String>();
	    KnowledgeLinking nel=new  KnowledgeLinking();
	    Entity en=new Entity();
	    long startTime=System.currentTimeMillis();  //获取开始时间
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
			  entitylist=en.getEntity(text);
		     nel.Select(entitylist);
		      System.out.println();  
			}
			fin.close();
		} catch (IOException e) {
			e.printStackTrace();
		}//end try
		// Date date=new Date();姚明
		 //System.out.println(date.getHours()+":"+date.getMinutes());
//	     entitylist=en.getEntity(text);
//	       nel.Select(entitylist);
	      //nel.Graph( entitylist);
	    // nel.oneEntity(entitylist);
	     long endTime=System.currentTimeMillis(); //获取结束时间
		 double minute=(endTime-startTime)/1000.0;
		System.out.println("程序运行时间： "+minute+"s");
	 }

}
