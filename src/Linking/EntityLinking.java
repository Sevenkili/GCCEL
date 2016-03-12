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
public class EntityLinking {
	 /*
	  * 全局变量
	  */
	 public static String docStr=null;
	 List<String>outlist=new ArrayList<String>();
	// HashMap<String,String>global=new HashMap<String,String>();	
	 // int account=0;
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
 	    //	 global.put(key, str);
 	    	 String htmltext=ex.extraHTMLText(str);
 	    	 List<String>twolist=tp.SegmentProcess(htmltext.replace(list.get(i),""));
 	    	  double sim=ts.compute(onelist,twolist);
 	    	//  System.out.println(key+sim);
 	    	  hs.put(key, sim);
 	     	 }//end else
 	     }//end for
 	    // System.out.println(hs);
 	      //选取最大相似度的
 	       double simmax=0.5;//相当于定义了一个阀值，这个值可以根据情况而改，低于阀值的相当于没找到
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
 	      if(simmax==0.5) {
 	    	  System.out.println("该文本中的"+list.get(i)+"在知识库中没有相应信息,返回NIL");
 	    	   outlist.add(list.get(i));
 	    	  //  account++;
 	      }
 	     } //end for 
 	     return flag;
     }   
     
     /*
     * 计算实体的先验概率（文本和候选实体页面的相似度，即单个实体的相似度） 
     */
     public HashMap<String,Double> Prior(List<String>list) throws IOException{
    	 HashMap<String,List<HashMap<String,List<String>>>>ha=new HashMap<String,List<HashMap<String,List<String>>>>();
    	 List<String>mullist=new ArrayList<String>();
    	 for(int k=0;k<list.size();k++){
    		  Neo4jOperate neo=new Neo4jOperate();
              List<HashMap<String,List<String>>>hash=new ArrayList<HashMap<String,List<String>>>();
              hash=neo.getMen(list.get(k));
              if(hash.get(0).size()>1){
               mullist.add(list.get(k));
       		   ha.put(list.get(k),hash);
       		 }//end if
    	 }
    	 
    	 TextSimilar ts=new TextSimilar();
 	     TextProcessing tp=new TextProcessing();
 	     ExtractHtmlText ex=new ExtractHtmlText();
 	     NetSpider nsp=new NetSpider();//爬虫程序
 	     String path=null;
 	     HashMap<String,Double>hs=new HashMap<String,Double>();
 	     for(int i=0;i<mullist.size();i++)
 	     {  
 	      String mention=mullist.get(i);
 	      if(ha.get(mention).get(0).size()>1)
 	      {  
 	    	 for(String key:ha.get(mention).get(0).keySet()){
 	    	  if(!key.contains(" 　")&&!key.contains("。")&&!key.contains("}")&&!key.contains(",")
 	    	   &&!key.contains(":")&&!key.contains(")")&&!key.contains(" ")&&!key.contains("　　")&&!key.contains(" ")
 	    	   &&!key.contains("{")&&!key.contains("　")&&!key.contains("，")&&!key.contains("：")){
 	    		  
 	    		 path="http://www.baike.com/wiki/"+key;
    	         String text=nsp.spide(path);  
    	       //  global.put(key, text);
    	         String puttext=docStr.replaceAll(mention, "");//得到文本中的内容,除去该实体的   
    	         List<String>onelist=tp.SegmentProcess(puttext);
  	    	     String htmltext=ex.extraHTMLText(text);
  	    	     List<String>twolist=tp.SegmentProcess(htmltext.replace(mention,""));
  	    	     double priorsim=ts.compute(onelist,twolist);   
  	    	     hs.put(key, priorsim); 
 	    		}	 
 	    	 }//end for     
 	       }//end if
 	     } //end for 
 	     return hs;
     }   
     
     
     /*
      * 先用知识库的知识构造图，不行再利用外部的互动网页知识构造图
      * 找关系节点也很费时间
      * 
      */
     public HashMap<String, Double> Graph(List<String>list) throws IOException{
    	 HashMap<String,List<HashMap<String,List<String>>>>ha=getMention(list);
    //	 HashMap<String,Double>priorhash=Prior(list);
         List<String>graphnode=new ArrayList<String>();
         HtmlTest ht=new HtmlTest();
         Filter ft=new Filter();//根据流行度过滤
         ComputerDegree cd=new ComputerDegree();//计算矩阵的出入度
         ExtractProperty expro=new ExtractProperty(); //抽取网页属性
         NetSpider nsp=new NetSpider();//爬虫程序
         ComputerSimilarty cs=new ComputerSimilarty();//计算相似度
         String path=null;//网页url
         HashMap<String,Double>degreehash=new HashMap<String,Double>();
         Boolean flag=false; //验证单个实体是否在知识库中
		 HashMap<String,Boolean>boolhash=new HashMap<String,Boolean>();     
         for(String key:ha.keySet()){
      	   //如果实体指称项在知识库子中没有候选实体或者只有一个候选实体，则该实体直接作为图节点
      	  if(ha.get(key).get(0).size()==0){
      		  graphnode.add(key);
      	  }//end if
      	  /*
      	   * 如果实体在知识库中有一个的候选实体，则用该候选实体当做节点
      	   * 实体在知识库中只有一个实体的情况该如何去判断文本中的实体就是知识库中的实体
      	   * 文本相似度？？？新实体？？？？
      	   */
      	  if(ha.get(key).get(0).size()==1){
      		    for(String key1:ha.get(key).get(0).keySet()){
      		    	 /*
      		    	  * 歧义实体在知识库中暂时只有一个候选实体
      		    	  */
      		    	if(key1.contains("[")||key1.contains("《")){
      		    	  List<String>keylist=new ArrayList<String>();
      		    	    keylist.add(key);
      		    	   flag=oneEntity(keylist);
      		    	   boolhash.put(key,flag);
      		    	   if(flag) {
      		    	   graphnode.add(key1);
      		    	   }
      		    	   else{
      		    	   graphnode.add(key); 
      		    	   }
      		    	 }
      		    	
      		    	/*
      		    	 * 不是歧义实体且只有一个实体的,防止新出现的实体
      		    	 */
      		    	else{
      		    		   List<String>result1=new ArrayList<String>();//存储实体指称项的属性
      	      			   List<String>oneresult=new ArrayList<String>();//存储候选实体的属性
      	      			   path="http://www.baike.com/wiki/"+key;
      	      		       String text=nsp.spide(path);
      	      	//	       global.put(key, text);
      	  	               oneresult=expro.extraInfobox(text).get(0);
      	      		       result1=ha.get(key).get(0).get(key1);
      	      		       double sim=cs.compute(oneresult, result1);//比较他们的相似度
      		    	
      		      // System.out.println(sim);
      		         if(sim<0.5) {
      		    	   System.out.println(key+"在知识库中不存在相应信息，返回NIL");//0.4是一个阀值
      		    	    boolhash.put(key,flag);
      		    	    outlist.add(key);
      		    	   // account++;
      		       }
      		       else { System.out.println(key+"=知识库中的"+key1);
      		                 flag=true;//flag=true说明该实体在知识库中
							 boolhash.put(key,flag);
							  outlist.add(key);
						//	 account++;
      		       }
      			   graphnode.add(key1);
      		    	} //end else
      		   } //end for
      	  }//end if
      
      	//如果实体在知识库中有多于一个的候选实体，则用该候选实体当做节点
      	  if(ha.get(key).get(0).size()>1){
      		   for(String key1:ha.get(key).get(0).keySet()){
      			   if(!key1.contains(" 　")&&!key1.contains("。")&&!key1.contains("}")&&!key1.contains(",")
      		  &&!key1.contains(":")&&!key1.contains(")")&&!key1.contains(" ")&&!key1.contains("　　")&&!key1.contains(" ")
      		 &&!key1.contains("{")&&!key1.contains("　")&&!key1.contains("，")&&!key1.contains("：")){  //知识库数据的原因，所以才要加这么多限制条件
      				   graphnode.add(key1);
//      				   if(priorhash.get(key1)>0.2){
//      					 graphnode.add(key1);
//      				  }
      				   
      			   }
 //   :
//      		 if(!key1.contains("　　")&&!key1.contains(" 　")&&!key1.contains(" ")&&ft.count(key1)>=10){
//      			   graphnode.add(key1);
//      			   }//end if 编辑次数大于10的才被选择
      			   
      		   } //end for
      	  }//end if
         }//end for
		 
         //为了更好的处理，将图节点存储两次
         String[] nodearray = (String[])graphnode.toArray(new String[0]);  
   //      String[] nodearray1 = (String[])graphnode.toArray(new String[0]);
         Graph graph = new MatrixGraph(nodearray);
         System.out.println("实体指示图的节点:"+graphnode);
         System.out.println("实体指示图的节点个数:"+graphnode.size());
         /*
          * 建立边的过程
          */
         for(int i=0;i<list.size();i++) 
         {
        	 HashMap<String, HashMap<String,Double>>hmm=new  HashMap<String, HashMap<String,Double>>();
        	 HashMap<String,String>result=new HashMap<String,String>();//存储网页链接
        	 String mention=list.get(i); 
        	 /*
        	  * 实体指称项在知识库中不存在候选的情况
        	  */
        	 if(ha.get(mention).get(0).size()==0){
        		 HashMap<String,Double>hs=new HashMap<String,Double>();//储存候选实体和他们与实体指称项的相似度 
            	 /*
            	  * 得到该实体的网页页面的链接
            	  */
        		 path="http://www.baike.com/wiki/"+mention;
            	  String text=nsp.spide(path);
                  result=ht.extraHTMLText(text);
                  
        		 for(int j=0;j<list.size();j++){
        		   String mention1=list.get(j);
        		    if(ha.get(mention1).get(0).size()>1){
        		    	HashMap<String,List<String>> hash=ha.get(mention1).get(0);
        		        for(String key:result.keySet()){ //网页链接花费大量的时间
        		          if((key.equals(mention1)||key.contains(mention1+"[")||key.contains("《"+mention1))&&!key.contains(" 　"))
        		        	  {
        		        	 List<String>result1=new ArrayList<String>();//存储实体指称项的属性
                     	     List<String>classresult1=new ArrayList<String>();//存储候选实体的分类
         	                 String urlvalue=result.get(key);
         	                 String textvalue=nsp.spide(urlvalue);      	    	     
	  	     	         	 result1=expro.extraInfobox(textvalue).get(0);//得到实体指称项的属性
         	    	         classresult1=expro.extraInfobox(textvalue).get(1);
        		             for(String key1:hash.keySet()){
        		              if(graphnode.contains(key1)){ 
        		               List<String>result2=new ArrayList<String>();//存储候选实体的属性
           	             	   List<String>classresult2=new ArrayList<String>();//存储候选实体的分类
           	             	   result2=ha.get(mention1).get(0).get(key1);
           	             	  classresult2=ha.get(mention1).get(1).get(key1);
                           	  double sim=cs.compute(result1, result2);//比较他们的属性相似度
                           	  double sim1=cs.compute(classresult1,classresult2);//比较他们的分类相似度
                            //   double priorsim=priorhash.get(key1);  //先验概率
                           	   // double sumsim=sim;
                           	  double sumsim=0.6*sim+0.4*sim1;
                           	  //double sumsim=0.4*sim+0.2*sim1+0.4*priorsim; //算总的相似度 属性相似度占0.6 分类相似度占0.4，这个有待多次训练
                           	//   System.out.println(mention+key1+sumsim);
                           	    if(sumsim>0.5){
                           	    hs.put(key1,sumsim);
                           	    hmm.put(mention1, hs);
                           	     }//end if 
           		    	      }//end if  graphnode
        		            }//end for     
        		      }//end if 
        		    }//end for
        		 }//end if
        	  }  //end for 第二个结点循环
        		    /*
                     * 比较候选实体与指称项的相似度大小，选取最大的
                     */
                  for(String men:hmm.keySet()){
                     //只有一个候选的情况
                     if(hmm.get(men).size()==1){
                   	    for(String keycan:hmm.get(men).keySet())
                    	   {
                    	        graph.addEdge(mention,keycan,1);  
                    	        } 
                  	      }//end if
                   	    //有多个候选实体的情况
                   	    if(hmm.get(men).size()>1){
                   	      HashMap<String,Double>haa=hmm.get(men);
                   	      //选取最大相似度的
                   	       double simmax=0.5;//相当于定义了一个阀值，这个值可以根据情况而改，低于阀值的相当于没找到
                   	    	 for(String keycan:haa.keySet()){
                   	    		 if(haa.get(keycan)>simmax){
                   	    			 simmax=haa.get(keycan);
                   	    		 }//end if
                   	     }//end for
  
                   	    //链接到知识库的候选实体上
                   	     for(String keyca:haa.keySet()){
                    	      if(simmax==haa.get(keyca)) {
                    	    	 graph.addEdge(mention,keyca,1);  
                    	      } //end if  
                   	       } //end for
                   	    }//end if
        	 } // end for 相似度的for循环
           }//end if 第2种情况
               	    
        	 /*
        	  * 实体指称项在知识库中只存在一个候选的情况
        	  */
        	 if(ha.get(mention).get(0).size()==1){
//        		System.out.println(mention);
                HashMap<String,Double>hs=new HashMap<String,Double>();//储存候选实体和他们与实体指称项的相似度
                HashMap<String,List<String>> relhash=new HashMap<String,List<String>>();
                 Neo4jOperate neo=new Neo4jOperate();
		         relhash=neo.getRelentity(mention);//得到与该节点有关系的节点
		         String smen=mention;
		         
		         /*
		          * 得到该实体的网页页面的链接
		          */
		         path="http://www.baike.com/wiki/"+smen;
                 String text=nsp.spide(path);
//		         String text=global.get(smen);
                result=ht.extraHTMLText(text);  
		         /*
		          * 该实体在知识库中
		          */
		     //  System.out.println(boolhash.get(mention));
        		 if(boolhash.get(mention)){  
        		 for(String sigle:ha.get(mention).get(0).keySet()){
    					 smen=sigle;
    				  }
//        		     String text=global.get(smen);
//                     result=ht.extraHTMLText(text); 
                     
        		   for(int j=0;j<list.size();j++) {
        			  String mention1=list.get(j);
        			  if(ha.get(mention1).get(0).size()>1){
        				  HashMap<String,List<String>> hash=ha.get(mention1).get(0);
        				  for(String key:hash.keySet()){ //歧义节点循环
        					 if(graphnode.contains(key)){
//        					   System.out.println(smen+"->关系 "+relhash);
//        					   System.out.println(relhash.get(smen).contains(key));
        					   if(!relhash.isEmpty()&&relhash.get(smen).contains(key)){
        	         				graph.addEdge(smen,key,1);
        	         				break;  
        	         			}//end if 关系判断
        					   
        					   else{ //知识库中不存在关系
        		                   for(String key1:result.keySet()){
        		                      if(key1.equals(mention1)||key1.equals(key)||key1.contains(mention1+"[")||key1.contains("《"+mention1)){ //包含全名或者实体指称项的名称
        		                    	 List<String>result1=new ArrayList<String>();//存储实体指称项的属性
        	                         	 List<String>classresult1=new ArrayList<String>();//存储候选实体的分类
        	             	             String urlvalue=result.get(key1);
        	             	             String textvalue=nsp.spide(urlvalue);    	 
            	   	      	    	     result1=expro.extraInfobox(textvalue).get(0);//得到实体指称项的属性
        	             	    	     classresult1=expro.extraInfobox(textvalue).get(1);
        	            		          List<String>result2=new ArrayList<String>();//存储候选实体的属性
        	               	              List<String>classresult2=new ArrayList<String>();//存储候选实体的分类
        	               	              result2=ha.get(mention1).get(0).get(key);
        	               	              classresult2=ha.get(mention1).get(1).get(key);
        	                              double sim=cs.compute(result1, result2);//比较他们的属性相似度
        	                              double sim1=cs.compute(classresult1,classresult2);//比较他们的分类相似度
        	                            //  double priorsim=priorhash.get(key);
        	                              // double sumsim=sim;
        	                               double sumsim=0.6*sim+0.4*sim1;
        	                            //   double sumsim=0.4*sim+0.2*sim1+0.4*priorsim; //算总的相似度 属性相似度占0.6 分类相似度占0.4，这个有待多次训练
        	                             // System.out.println(mention+key+sumsim);
        	                              
        	                              if(sumsim>0.5){
        	                               	    hs.put(key,sumsim);
        	                               	    hmm.put(mention1, hs);
        	                                 }//end if 
        		                    	}//end if 链接包含
        		                     }//end if 链接 
        					   }//end else
        					 }//end if graphnode
        				  }//end for
        			  } //end if 大于1个实体的情况
        		   }//end for
        		}//end if 该实体在知识库中 
        		 
        		/*
        		 * 该实体不在知识库中的情况
        		 */
        		 if(!boolhash.get(mention)){
        			 
//        			 String text=global.get(smen);
//                     result=ht.extraHTMLText(text); 
                     
            		 for(int j=0;j<list.size();j++)
            		 {
            		   String mention1=list.get(j);
            		    if(ha.get(mention1).get(0).size()>1){
            		    	HashMap<String,List<String>> hash=ha.get(mention1).get(0);
            		        for(String key:result.keySet()){ //网页链接花费大量的时间
            		        	if(key.equals(mention1)||key.contains(mention1+"[")||key.contains("《"+mention1))
            		        	{
            		        	 List<String>result1=new ArrayList<String>();//存储实体指称项的属性
                         	     List<String>classresult1=new ArrayList<String>();//存储候选实体的分类
             	                 String urlvalue=result.get(key);
             	                 String textvalue=nsp.spide(urlvalue); 
             	    	         result1=expro.extraInfobox(textvalue).get(0);//得到实体指称项的属性
             	    	         classresult1=expro.extraInfobox(textvalue).get(1);
            		             for(String key1:hash.keySet()){
            		              if(graphnode.contains(key1)){ 
            		               List<String>result2=new ArrayList<String>();//存储候选实体的属性
               	             	   List<String>classresult2=new ArrayList<String>();//存储候选实体的分类
               	             	   result2=ha.get(mention1).get(0).get(key1);
               	             	   classresult2=ha.get(mention1).get(1).get(key1);
                               	    double sim=cs.compute(result1, result2);//比较他们的属性相似度
                               	    double sim1=cs.compute(classresult1,classresult2);//比较他们的分类相似度
                               	 //   double priorsim=priorhash.get(key1);
                               	    //    double sumsim=sim;
                                   double sumsim=0.6*sim+0.4*sim1;
                               	 //  double sumsim=0.4*sim+0.2*sim1+0.4*priorsim; //算总的相似度 属性相似度占0.6 分类相似度占0.4，这个有待多次训练
                                 // System.out.println(mention+key+sumsim);
                               	    if(sumsim>0.5){
                               	    hs.put(key1,sumsim);
                               	    hmm.put(mention1, hs);
                               	     }//end if  
               		    	      }//end if graphnode
            		            }//end for    
            		        }//end for
            		      }//end if
            		 }//end if
            	  }  //end for 第二个结点循环
        	  }//end if该实体不在知识库中
         	//System.out.println(hmm);
                /*
                 * 比较候选实体与指称项的相似度大小，选取最大的
                 */	   
                      for(String men:hmm.keySet()){
                         //只有一个候选的情况
                         if(hmm.get(men).size()==1){
                       	    for(String keycan:hmm.get(men).keySet())
                        	   {
                        	        graph.addEdge(smen,keycan,1);  
                        	        } 
                      	      }//end if
                       	    //有多个候选实体的情况
                       	    if(hmm.get(men).size()>1){
                       	      HashMap<String,Double>haa=hmm.get(men);
                       	      //选取最大相似度的
                       	       double simmax=0.5;//相当于定义了一个阀值，这个值可以根据情况而改，低于阀值的相当于没找到
                       	    	 for(String keycan:haa.keySet()){
                       	    		 if(haa.get(keycan)>simmax){
                       	    			 simmax=haa.get(keycan);
                       	    		 }//end if
                       	     }//end for
      
                       	    //链接到知识库的候选实体上
                       	     for(String keyca:haa.keySet()){
                        	      if(simmax==haa.get(keyca)) {
                        	    	 graph.addEdge(smen,keyca,1);  
                        	      } //end if  
                       	       } //end for
                       	    }//end if
            	     } // end for 相似度的for循环
        	 }//end if 第3种情况
        	 /*
        	  * 实体指称在知识库中存在多个候选的情况
        	  */
             if(ha.get(mention).get(0).size()>1){
            	//  System.out.println(mention);
                 HashMap<String,List<String>> relhash=new HashMap<String,List<String>>();
                  Neo4jOperate neo=new Neo4jOperate();
 		          relhash=neo.getRelentity(mention);//得到与该节点有关系的节点
            	  for(String key:ha.get(mention).get(0).keySet()){
            		HashMap<String, HashMap<String,Double>>hmm1=new  HashMap<String, HashMap<String,Double>>();
            		 HashMap<String,Double>hs=new HashMap<String,Double>();//储存候选实体和他们与实体指称项的相似度
            		 if(graphnode.contains(key)){ //候选实体节点必须在图节点中
            		  /*
            		   * 得到该实体的网页链接页面
            		   */
            		    path="http://www.baike.com/wiki/"+key;
	       	        	String text=nsp.spide(path);
            	//		String text=global.get(key);
	       	            result=ht.extraHTMLText(text);  
	       	            
            		//  System.out.println(key);
            		 for(int j=0;j<list.size();j++){
            			 String mention1=list.get(j); //得到实体指称项
            			 if(!mention.equals(mention1))
            			 {    
            				 if(ha.get(mention1).get(0).size()==0){
            				  if(!relhash.isEmpty()&&relhash.get(key).contains(mention1)){
           	         			 graph.addEdge(key,mention1,1);
           	         	    	  }//end if 判断在知识库中是否存在关系
           					   else
           					    {
           	                     for(String key1:result.keySet()){ 
           	                    	if(key1.equals(mention1)||key1.contains("《"+mention1)){
           	                    		graph.addEdge(key,mention1,1);  	
           	                    	}//end if 链接包含
           	                     } //end for 链接包含
           					   }//end else 不存在关系  
            				 }//与没有候选的实体的关系判断
            				 
            	
            			     if(ha.get(mention1).get(0).size()==1){
            			       String smen=mention1;
            			      if(boolhash.get(mention1)){
            			    	 for(String sigle:ha.get(mention1).get(0).keySet()){
             						  smen=sigle;
             					  }
             			      // System.out.println(smen);
           					   if(!relhash.isEmpty()&&relhash.get(key).contains(smen)){
           	         				graph.addEdge(key,smen,1);
           	         			}//end if 判断在知识库中是否存在关系
           					   else
           					   {
           	                     for(String key1:result.keySet()){ 
           	                    	if(key1.equals(smen)||key1.contains("《"+mention1)){
           	                    		graph.addEdge(key,smen,1);  	
           	                    	}//end if 链接包含
           	                     } //end for 链接包含
           					   }//end else不存在关系 
           					   
            			    } //在知识库中
            			      
            			     if(!boolhash.get(mention1)) {
            			    	 for(String key2:result.keySet()){ 
            	                    	if(key2.equals(mention1)||key2.contains(mention1+"[")||key2.contains("《"+mention1)){
            			    		    graph.addEdge(key,mention1,1);  	
            	                    	}//end if 链接包含
            	                     } //end for 链接包含
            			     }//end if
            			       
            			     }// 判断与只有一个候选实体的实体指称项之间的关系
            			  
            			     
            			   if(ha.get(mention1).get(0).size()>1)
            			   { 
            				  //System.out.println(mention1);     
            				 for(String key2:ha.get(mention1).get(0).keySet())
            				 { 
            				  if(graphnode.contains(key2)){
            					  if(!relhash.isEmpty()&&relhash.get(key).contains(key2))
                				  {
           	         			    	graph.addEdge(key,key2,1);
           	         			    	break;
           	         			    	
           	         			    }//end if 判断在知识库中是否存在关系
     					  
                				 else{
                	                    for(String urlkey:result.keySet()){ //判断链接花了大量的时间
                					   //   System.out.println(key2+(urlkey.equals(key2)||urlkey.contains(mention1)));
                	                    	if(urlkey.equals(key2)||urlkey.equals(mention1)||urlkey.equals(mention1+"[")){//包括全名或者实体指称项名
                					    	 List<String>result1=new ArrayList<String>();//存储实体指称项的属性
                	                         List<String>classresult1=new ArrayList<String>();//存储候选实体的分类
                	                         List<String>result2=new ArrayList<String>();//存储实体指称项的属性
                                    	     List<String>classresult2=new ArrayList<String>();//存储候选实体的分类
                	                         String urlvalue=result.get(urlkey);   
                	                         String textvalue=nsp.spide(urlvalue);     
                	                         result1=expro.extraInfobox(textvalue).get(0);//得到实体指称项的属性   
                	                         classresult1=expro.extraInfobox(textvalue).get(1); 
                      	             	     result2=ha.get(mention1).get(0).get(key2);
                      	             	     classresult2=ha.get(mention1).get(1).get(key2);
                      	             	  
                      			    	    // result2=ha.get(key).get(nodearray1[j]);//得到候选实体的属性
                                      	     double sim=cs.compute(result1, result2);//比较他们的属性相似度
                                      	     double sim1=cs.compute(classresult1,classresult2);//比较他们的分类相似度
                                      	//    double priorsim=priorhash.get(key2);
                                      	  //    double sumsim=sim;
                                      	    double sumsim=0.6*sim+0.4*sim1; //没有考虑先验概率的
                                      	   //  double sumsim=0.4*sim+0.2*sim1+0.4*priorsim; //算总的相似度 属性相似度占0.6 分类相似度占0.4，这个有待多次训练
                                        	// System.out.println(key+key2+sumsim);
                                      	     if(sumsim>=0.5){ //由于知识库的知识不全，阀值不好确定大小
                                      	        hs.put(key2, sumsim);
                                      	        hmm1.put(mention1, hs);
                                      	       }//end if
            				              }//end if urlkey
            					        }//end for urlkey
            			        	 }//else
            				        }//end graphnode
            			     	 }//end for 得到歧义项的候选实体
            			      }//end if判断与歧义候选实体之间的关系
            			   } //end if判断与候选实体的关系
            		    }//end for
                // System.out.println(hmm1); 
            		 /*
                      * 比较候选实体与指称项的相似度大小，选取最大的
                      */
                       for(String men:hmm1.keySet()){
                    	    //只有一个候选实体
                    	    if(hmm1.get(men).size()==1)
                    	    {  
                    	     for(String keycan:hmm1.get(men).keySet())
                    	      {
                    	         graph.addEdge(key,keycan,1);  
                    	       }
                    	      }
                    	
                    	    //有多个候选实体的情况
                    	    if(hmm1.get(men).size()>1){
                    	      HashMap<String,Double>haa=hmm1.get(men);
                    	      //选取最大相似度的
                    	       double simmax=0.5;//相当于定义了一个阀值，这个值可以根据情况而改，低于阀值的相当于没找到
                    	    	 for(String keycan:haa.keySet()){
                    	    		 if(haa.get(keycan)>simmax){
                    	    			 simmax=haa.get(keycan);
                    	    		 }//end if
                    	     }//end for
                    	    //链接到知识库的候选实体上
                    	     for(String keyca:haa.keySet()){
                     	      if(simmax==haa.get(keyca)) {
                     	    	 graph.addEdge(key,keyca,1);  
                     	      if(simmax==0.5){
                     	    	  System.out.println(men+"在知识库中不存在相应信息,返回NIL");
                     	    	  outlist.add(men);
                     	    	// account++;
                     	      }//end if
                     	     } //end if  
                    	    } //end for
                    	} //end if 
                    } //end for(比较结束)    
            	} //end if graphnode
             }  //end for 歧义实体的循环
         }//end if 第3种情况	 
      }//end for 第一层节点循环
         String graphresult=graph.printGraph();
//         System.out.println();
   //    System.out.println(graphresult);
         degreehash=cd.computer(nodearray, graphresult);
    //    System.out.println( degreehash);
         return degreehash; 
      }//end graph 函数
    
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
     	 
//           HashMap<String,Boolean>judge=new HashMap<String,Boolean>();
//           for(int i=0;i<list.size();i++){
//         	   judge.put(list.get(i), true);
//             }
           
         	 for(int i=0;i<list.size();i++){
             	 HashMap<String,Double>canhash=new HashMap<String,Double>();
             	 List<String>prior=new ArrayList<String>();//把最初的文本相似度也考虑进来，防止度相同的时候没法选择
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
             		double sum=0;
             		double max=0;
             		 for(String key:canhash.keySet()){
             			 sum+=canhash.get(key);
         	    		 if(canhash.get(key)>max){
         	    			 max=canhash.get(key);
         	    		 }//end if
         	           }//end for
             		// account=sum;
             	  if(sum>0){
//             		   Boolean flag=false;
//             		   judge.put(list.get(i), flag);
             	   for(String key:canhash.keySet()){
                 	      if(max==canhash.get(key)&&max>0) {
                 	  System.out.println("文本中的"+list.get(i)+"="+key);
                 	      outlist.add(list.get(i));
                 	      break;  //找到了就跳出循环
                 	//  account++;
                 	      } //end if  
                	       } //end for
             	    }//end sum
             	  if(sum==0){
             		 System.out.println("文本中的"+list.get(i)+"在知识库中不存在相应信息");  
             		    outlist.add(list.get(i));
             	   }
            	    }//end if 
               }//end for
 
         	for(int i=0;i<list.size();i++)
         	{
         		if(!outlist.contains(list.get(i))){
         			System.out.println("文本中的"+list.get(i)+"在知识库中不存在相应信息");
         		}
         	 }//end for

        }//end if	 
      }//end select 函数
     
     /*
      * 主函数
      */
    public static void main(String args[]) throws IOException{
	    /*
		 * 从文本中读取内容
		 */
	    String text=null;//存储从文本中拿出来的字符串
       // String text="伦敦奥运会男子110米栏预赛中，刘翔打栏意外摔倒在地，最终单腿跳过终点无缘晋级。2012年8月10日，刘翔手术成功。";
	    List<String>entitylist=new ArrayList<String>();
	    EntityLinking nel=new  EntityLinking();
	 //   Entity en=new Entity();
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
			//   entitylist=en.getEntity(text);
		      nel.Select(entitylist);  
			// System.out.println(nel.Prior(entitylist));
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
