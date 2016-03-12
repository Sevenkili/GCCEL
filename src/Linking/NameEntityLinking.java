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

public class NameEntityLinking {
	 public static String docStr=null;
	 double account=0; //统计输出次数
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
    			account++;
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
     public void oneEntity(List<String>list) throws IOException{
    	 TextSimilar ts=new TextSimilar();
 	     TextProcessing tp=new TextProcessing();
 	     NetSpider nsp=new NetSpider();//爬虫程序
 	     ExtractHtmlText ex=new ExtractHtmlText();
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
 	      	    ||key.contains(")")||key.contains(" ")||key.contains("　　")||key.contains(" ")
 	        ||key.contains("　")||key.contains("，"))
 	    	 {
 	    		// do nothing 格式有问题
 	    	   }
 	    	else {
 	         List<String>onelist=tp.SegmentProcess(text);
 	    	 String str=nsp.spide(urlhm.get(key));
 	    	 String htmltext=ex.extraHTMLText(str);
 	    	 List<String>twolist=tp.SegmentProcess(htmltext.replace(list.get(i),""));
 	    	  double sim=ts.compute(onelist,twolist);
 	    	 // System.out.println(key+sim);
 	    	  hs.put(key, sim);
 	     	 }//end else
 	     }//end for
 	    // System.out.println(hs);
 	      //选取最大相似度的
 	       double simmax=0.40;//相当于定义了一个阀值，这个值可以根据情况而改，低于阀值的相当于没找到
 	    	 for(String key:hs.keySet()){
 	    		 if(hs.get(key)>simmax){
 	    			 simmax=hs.get(key);
 	    		 }//end if
 	     }//end for
 	    //链接到知识库的候选实体上
 	    for(String key:hs.keySet()){
  	      if(simmax==hs.get(key))  System.out.println("该文本中的"+list.get(i)+"="+key);
  	        account++;
 	       } //end for
 	  //  System.out.println(hs);
 	    if(simmax==0.40){
 	    	System.out.println("该文本中的"+list.get(i)+"在知识库中没有相应信息,返回NIL");
 	    	account++;
 	      }
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
      			 List<String>result1=new ArrayList<String>();//存储实体指称项的属性
      			 List<String>oneresult=new ArrayList<String>();//存储候选实体的属性
      			   path="http://www.baike.com/wiki/"+key;
      		       String text=nsp.spide(path);
  	               oneresult=expro.extraInfobox(text).get(0);
      		       result1=ha.get(key).get(0).get(key1);
      		       double sim=cs.compute(oneresult, result1);//比较他们的相似度
      		      // System.out.println(sim);
      		       if(sim<0.4) {
      		    	   System.out.println(key+"在知识库中不存在相应信息，返回NIL");//0.4是一个阀值
      		    	    boolhash.put(key,flag);
      		    	    account++;
      		       }
      		       else { System.out.println(key+"=知识库中的"+key1);
      		                 flag=true;//flag=true说明该实体在知识库中
							 boolhash.put(key,flag);
							 account++;
      		       }
      			   graphnode.add(key1);
      		   } //end for
      	  }//end if
      	
      	//如果实体在知识库中有多于一个的候选实体，则用该候选实体当做节点
      	  if(ha.get(key).get(0).size()>1){
      		   for(String key1:ha.get(key).get(0).keySet()){
      			   if(!key1.contains(" 　")&&!key1.contains("。")&&!key1.contains("}")&&!key1.contains(",")
      			  &&!key1.contains(")")&&!key1.contains(" ")&&!key1.contains("　　")&&!key1.contains(" ")
      		&&!key1.contains("　")&&!key1.contains("，")){  //知识库数据的原因，所以才要加这么多限制条件
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
        	 HashMap<String, HashMap<String,Double>>hmm=new  HashMap<String, HashMap<String,Double>>();
        	 HashMap<String,String>result=new HashMap<String,String>();//存储网页链接
        	 String subname=null;
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
				 String prior=null;
				 if(nodearray[i-1].contains("[")){
					 prior=nodearray[i-1].substring(0, nodearray[i-1].indexOf("["));
				  }
				 else {
					 prior=nodearray[i-1];
				 } //if-else
				 
				 if(prior.equals(subname)){
				    relhash=global;
		        	}
		    	else{
		    	 Neo4jOperate neo=new Neo4jOperate();
		         relhash=neo.getRelentity(subname);//得到与该节点有关系的节点
//		         neo.shutDown(); //注意关了之后要重新创建该类
		         global=relhash;
		    	} //end if-else
			 }//end if
			 
			 if(i==0){
			   Neo4jOperate neo=new Neo4jOperate();
	           relhash=neo.getRelentity(subname);//得到与该节点有关系的节点
//	           neo.shutDown(); //注意关了之后要重新创建该类
	           global=relhash;
			 }//end if
             // System.out.println(relhash);
     
        	 /*
        	  * 实体不在知识库的情况，直接去网页上找证据
        	  */
        	 if(ha.get(subname).get(0).size()==0){
        		 HashMap<String,Double>hs=new HashMap<String,Double>();//储存候选实体和他们与实体指称项的相似度 
            	 path="http://www.baike.com/wiki/"+nodearray[i];
            	  String text=nsp.spide(path);
                  result=ht.extraHTMLText(text);
            	   for(int j=0;j<nodearray1.length;j++){
            		
            		   if(!nodearray[i].equals(nodearray1[j]))
            		   {  String subname1=null;
                         for(String key:result.keySet()){
                        	  if(nodearray1[j].contains("[")){
                        	    subname1=nodearray1[j].substring(0, nodearray1[j].indexOf("["));
                        	     } //end if
                        	  else if(nodearray1[j].contains("》")){
                         		 subname1=nodearray1[j].substring(1, nodearray1[j].indexOf("》"));
                         	  }//书名的处理比较特殊
                        	  else {
                        		  subname1=nodearray1[j];
                        	  }//end else
                        	   //为了扩大查找范围，只要那链接包含实体指称项的部分{歧义}key<---->subname
            			    //   if(subname.equals(key)){
                        	    if(key.contains(subname1)){ 
                        	    List<String>result1=new ArrayList<String>();//存储实体指称项的属性
                        	    List<String>classresult1=new ArrayList<String>();//存储候选实体的分类
                        	    //String urlvalue=result.get(key);//得到包含在网页和文本中实体实体指称的链接
            	                 String urlvalue=result.get(key);
            	                 String textvalue=nsp.spide(urlvalue);
            	    	         result1=expro.extraInfobox(textvalue).get(0);//得到实体指称项的属性
            	    	         classresult1=expro.extraInfobox(textvalue).get(1);
            	             	 // if(ha.get(key).size()>0)
            	             	  if(ha.get(subname1).get(0).size()>1)
            			    	  {
            	             	  List<String>result2=new ArrayList<String>();//存储候选实体的属性
            	             	  List<String>classresult2=new ArrayList<String>();//存储候选实体的分类
            	             	   result2=ha.get(subname1).get(0).get(nodearray1[j]);
            	             	   classresult2=ha.get(subname1).get(1).get(nodearray1[j]);
            			    	    //result2=ha.get(key).get(nodearray1[j]);//得到候选实体的属性
                            	    double sim=cs.compute(result1, result2);//比较他们的属性相似度
                            	    double sim1=cs.compute(classresult1,classresult2);//比较他们的分类相似度
                            	    double sumsim=0.6*sim+0.4*sim1; //算总的相似度 属性相似度占0.6 分类相似度占0.4，这个有待多次训练
                            	  //  System.out.println(nodearray[i]+nodearray1[j]+sumsim);
                            	    if(sumsim>0.4){
                            	    hs.put(nodearray1[j], sumsim);
                            	    hmm.put(subname1, hs);
                            	   // hmm.put(key, hs);
                            	    }//end if
            			    	  }// end if
                        	}//end if
                         }//end for
            		   }//end if  
            		   
            	   } //end for 第二层节点循环
            	//System.out.println(hmm);
              /*
              * 比较候选实体与指称项的相似度大小，选取最大的
              */
               for(String men:hmm.keySet()){
            	   if(hmm.get(men).size()==1){
            		   for(String keycan:hmm.get(men).keySet())
             	         {
             	        graph.addEdge(nodearray[i],keycan,1);  
             	        } 
           	      }
            	    //有多个候选实体的情况
            	    if(hmm.get(men).size()>1){
            	      HashMap<String,Double>haa=hmm.get(men);
            	      //选取最大相似度的
            	       double simmax=0.40;//相当于定义了一个阀值，这个值可以根据情况而改，低于阀值的相当于没找到
            	    	 for(String keycan:haa.keySet()){
            	    		 if(haa.get(keycan)>simmax){
            	    			 simmax=haa.get(keycan);
            	    		 }//end if
            	     }//end for
            	    //链接到知识库的候选实体上
            	     for(String keyca:haa.keySet()){
             	      if(simmax==haa.get(keyca)) {
             	    	 graph.addEdge(nodearray[i],keyca,1);  
//             	      if(simmax==0.40){
//             	    	//  System.out.println(men+"在知识库中不存在相应信息,返回NIL");
//             	      }//end if
             	      } //end if  
            	       } //end for
            	    }//end if
               }//end for
        	 }//end if (第一种情况)
        	 
        	 /*
        	  * 实体在知识库中，先判断他们在知识库中是否有关系，没有则去网上找
        	  */
        	 if(ha.get(subname).get(0).size()==1){
        	  HashMap<String,Double>hs=new HashMap<String,Double>();//储存候选实体和他们与实体指称项的相似度
        	  //只有一个候选，但候选是正确的
        	   if(boolhash.get(subname)){
        		for(int j=0;j<nodearray1.length;j++){
        		//	System.out.println(nodearray[i]+nodearray1[j]);
        		 if(!nodearray[i].equals(nodearray1[j])){
        		    if(!relhash.isEmpty()&&relhash.get(nodearray[i]).contains(nodearray1[j])){
         				graph.addEdge(nodearray[i],nodearray1[j],1);
         			//System.out.println(nodearray[i]+"和"+nodearray1[j]+"在库中有关系");
         			}//end if
        		    
         			else{
//         			   HashMap<String,Double>hs=new HashMap<String,Double>();//储存候选实体和他们与实体指称项的相似度
         	           path="http://www.baike.com/wiki/"+nodearray[i];
         	        	String text=nsp.spide(path);
         	            result=ht.extraHTMLText(text);
         			    String subname1=null;
                       for(String key:result.keySet()){
                      	  if(nodearray1[j].contains("[")){
                      	    subname1=nodearray1[j].substring(0, nodearray1[j].indexOf("["));
                      	     } //end if
                      	else if(nodearray1[j].contains("》")){
                      		subname1=nodearray1[j].substring(1, nodearray1[j].indexOf("》"));
                       	}
                      	  else {
                      		  subname1=nodearray1[j];
                      	  }//end else
                      	  
                      	   //为了扩大查找范围，只要那链接包含实体指称项的部分{歧义}key<---->subname
          			    //   if(subname.equals(key)){
                      	    if(key.contains(subname1)){ 
                      	     List<String>result1=new ArrayList<String>();//存储实体指称项的属性
                         	 List<String>classresult1=new ArrayList<String>();//存储候选实体的分类
                      	    //String urlvalue=result.get(key);//得到包含在网页和文本中实体实体指称的链接
          	                 String urlvalue=result.get(key);
          	                 String textvalue=nsp.spide(urlvalue);
          	    	         result1=expro.extraInfobox(textvalue).get(0);//得到实体指称项的属性
          	    	         classresult1=expro.extraInfobox(textvalue).get(1);
          	             	 // if(ha.get(key).size()>0)
          	             	  if(ha.get(subname1).get(0).size()>1)
          	             	  {
              	    	       List<String>result2=new ArrayList<String>();//存储候选实体的属性
             	               List<String>classresult2=new ArrayList<String>();//存储候选实体的分类
          	             	   result2=ha.get(subname1).get(0).get(nodearray1[j]);
          	             	   classresult2=ha.get(subname1).get(1).get(nodearray1[j]);
          			    	    //result2=ha.get(key).get(nodearray1[j]);//得到候选实体的属性
                          	    double sim=cs.compute(result1, result2);//比较他们的属性相似度
                          	    double sim1=cs.compute(classresult1,classresult2);//比较他们的分类相似度
                          	    double sumsim=0.6*sim+0.4*sim1; //算总的相似度 属性相似度占0.6 分类相似度占0.4，这个有待多次训练
                          	//  System.out.println(nodearray[i]+nodearray1[j]+sumsim);
                          	    // System.out.println(hs);
                          	    if(sumsim>0.4){
                          	    hs.put(nodearray1[j], sumsim);
                          	   // hmm.put(key, hs);
                          	     hmm.put(subname1, hs);
                          	    }//end if
                          	    
          			    	  }// end if
          		           }//end if (key.contain) 
          	           } //end for 
         	       }//end else
               }//end if
            }//end for
         }//end if
        	   
        //有候选，且候选就是是错误的
       //  System.out.println(boolhash.get(subname));
          if(!boolhash.get(subname)){
        	 path="http://www.baike.com/wiki/"+nodearray[i];
        	  String text=nsp.spide(path);
              result=ht.extraHTMLText(text);
        	   for(int j=0;j<nodearray1.length;j++){
        		   if(!nodearray[i].equals(nodearray1[j]))
        		   {  String subname1=null;
        		     
                    for(String key:result.keySet()){
                    	  if(nodearray1[j].contains("[")){
                    	    subname1=nodearray1[j].substring(0, nodearray1[j].indexOf("["));
                    	     } //end if
                    	  
                    	  else if(nodearray1[j].contains("》")){
                    		 subname1=nodearray1[j].substring(1, nodearray1[j].indexOf("》"));
                    	  }
                    	  
                    	  else {
                    		  subname1=nodearray1[j];
                    	  }//end else
                    	   //为了扩大查找范围，只要那链接包含实体指称项的部分{歧义}key<---->subname
        			    //   if(subname.equals(key)){
                    	     if(key.contains(subname1)){ 
                    	     List<String>result1=new ArrayList<String>();//存储实体指称项的属性
                          	 List<String>classresult1=new ArrayList<String>();//存储候选实体的分类
                    	    //String urlvalue=result.get(key);//得到包含在网页和文本中实体实体指称的链接
        	                 String urlvalue=result.get(key);
        	                 String textvalue=nsp.spide(urlvalue);
        	    	         result1=expro.extraInfobox(textvalue).get(0);//得到实体指称项的属性
        	    	         classresult1=expro.extraInfobox(textvalue).get(1);
//        	    	         System.out.println(temNode);
//        	    	         System.out.println(result1);
//        	    	         System.out.println(classresult1);
        	             	 // if(ha.get(key).size()>0)
        	             	  if(ha.get(subname1).get(0).size()>1)
        			    	  {
        	             	   List<String>result2=new ArrayList<String>();//存储实体指称项的属性
                      	       List<String>classresult2=new ArrayList<String>();//存储候选实体的分类
        	             	   result2=ha.get(subname1).get(0).get(nodearray1[j]);
        	             	   classresult2=ha.get(subname1).get(1).get(nodearray1[j]);
        			    	    //result2=ha.get(key).get(nodearray1[j]);//得到候选实体的属性
                        	    double sim=cs.compute(result1, result2);//比较他们的属性相似度
                        	    double sim1=cs.compute(classresult1,classresult2);//比较他们的分类相似度
                        	    double sumsim=0.6*sim+0.4*sim1; //算总的相似度 属性相似度占0.6 分类相似度占0.4，这个有待多次训练
                        	   // System.out.println(nodearray[i]+nodearray1[j]+sumsim);
                        	    if(sumsim>0.4){
                        	    hs.put(nodearray1[j], sumsim);
                        	   // hmm.put(key, hs);
                        	   hmm.put(subname1, hs);
                        	    }
        			    	  }// end if
                    	}//end if
                     }//end for
        		   }//end if  
        	   } //end for 
          }//end if
     //    System.out.println(hmm);
            /*
            * 比较候选实体与指称项的相似度大小，选取最大的
            */
               for(String men:hmm.keySet()){
            	if(hmm.get(men).size()==1){
            		for(String keycan:hmm.get(men).keySet())
          	       {
          	        graph.addEdge(nodearray[i],keycan,1);  
          	        }
           	    }
          	    //有多个候选实体的情况
          	    if(hmm.get(men).size()>1){
          	      HashMap<String,Double>haa=hmm.get(men);
          	      //选取最大相似度的
          	       double simmax=0.40;//相当于定义了一个阀值，这个值可以根据情况而改，低于阀值的相当于没找到
          	    	 for(String keycan:haa.keySet()){
          	    		 if(haa.get(keycan)>simmax){
          	    			 simmax=haa.get(keycan);
          	    		 }//end if
          	     }//end for
          	    //链接到知识库的候选实体上
          	     for(String keyca:haa.keySet()){
           	      if(simmax==haa.get(keyca)) {
           	    	 graph.addEdge(nodearray[i],keyca,1);  
//           	      if(simmax==0.40){
//           	    	//  System.out.println(men+"在知识库中不存在相应信息,返回NIL");
//           	      }//end if
           	     } //end if  
          	    } //end for
          	  }//end if
          }//end for(比较结束)	 
        }//end if（第二种种情况）
        	 
        /*
         * 实体在知识库中由多个候选，则也先判断他们在知识库中和其他实体之间是否有关系
         * 没有在去知识库中，歧义实体之间不进行比较
         */
//          System.out.println(subname);	  
//          System.out.println(ha.get(subname).get(0).size()>1);	 
          if(ha.get(subname).get(0).size()>1){
        	HashMap<String,Double>hs=new HashMap<String,Double>();//储存候选实体和他们与实体指称项的相似度
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
        		 // System.out.println(relhash.get(nodearray[i]));
      		    if(!relhash.isEmpty()&&relhash.get(nodearray[i]).contains(nodearray1[j]))
      		    {
      		     //System.out.println(relhash.get(nodearray[i]).contains(nodearray1[j]));
       				graph.addEdge(nodearray[i],nodearray1[j],1);
       			//System.out.println(nodearray[i]+"和"+nodearray1[j]+"在库中有关系");
       		     }//end if
        	    
      		     else {
       	           path="http://www.baike.com/wiki/"+nodearray[i];
       	        	String text=nsp.spide(path);
       	            result=ht.extraHTMLText(text);
                   for(String key:result.keySet()){
                    	   //为了扩大查找范围，只要那链接包含实体指称项的部分{歧义}key<---->subname
        			    //   if(subname.equals(key)){
       	                //    if(result.keySet().contains(subname1)){
                    	   if(key.contains(subname1)){ 
                    	     List<String>result1=new ArrayList<String>();//存储实体指称项的属性
                         	 List<String>classresult1=new ArrayList<String>();//存储候选实体的分类
                    	   //  String urlvalue=result.get(subname1);//得到包含在网页和文本中实体实体指称的链接
        	                 String urlvalue=result.get(key);
        	                 String textvalue=nsp.spide(urlvalue);
        	    	         result1=expro.extraInfobox(textvalue).get(0);//得到实体指称项的属性
        	    	         classresult1=expro.extraInfobox(textvalue).get(1);
//        	    	         System.out.println(temNode);
//        	    	         System.out.println(result1);
//        	    	         System.out.println(classresult1);
        	             	 // if(ha.get(key).size()>0)
        	             	  if(ha.get(subname1).get(0).size()>1)
        			    	  {   
        	             	   List<String>result2=new ArrayList<String>();//存储实体指称项的属性
                      	       List<String>classresult2=new ArrayList<String>();//存储候选实体的分类
        	             	   result2=ha.get(subname1).get(0).get(nodearray1[j]);
        	             	   classresult2=ha.get(subname1).get(1).get(nodearray1[j]);
        			    	    //result2=ha.get(key).get(nodearray1[j]);//得到候选实体的属性
                        	    double sim=cs.compute(result1, result2);//比较他们的属性相似度
                        	    double sim1=cs.compute(classresult1,classresult2);//比较他们的分类相似度
                        	    double sumsim=0.6*sim+0.4*sim1; //算总的相似度 属性相似度占0.6 分类相似度占0.4，这个有待多次训练
                        	//   System.out.println(nodearray[i]+nodearray1[j]+sumsim);
                        	    if(sumsim>=0.4){ //由于知识库的知识不全，阀值不好确定大小
                        	     hs.put(nodearray1[j], sumsim);
                        	     hmm.put(subname1, hs);
                        	   // hmm.put(key, hs);
                        	      }//end if
        			    	  }// end if
        	             	  
        	                if(ha.get(subname1).get(0).size()==1)
       			    	    {  
        	            	  graph.addEdge(nodearray[i],nodearray1[j],1);  
        	            	  
       			    	     }// end if
        	                
        			    	  //if(ha.get(key).size()==0){
        	                 if(ha.get(subname1).get(0).size()==0){
        	             		//  hmm.put(key,new HashMap<String,Double>());
        			    		  hmm.put(subname1,new HashMap<String,Double>());
        			    		  //graph.addEdge(nodearray[i],key,1);
        			    		  graph.addEdge(nodearray[i],subname1,1);
        			    	  }//end    
        		           }//end if (key.contain) 
        	           } //end for 	
      		     }//end else
        	  }//end if
           }//end for
        //	 System.out.println(hmm);
        	/*
             * 比较候选实体与指称项的相似度大小，选取最大的
             */
              for(String men:hmm.keySet()){
           	    //只有一个候选实体
           	    if(hmm.get(men).size()==1)
           	    {  
           	     for(String keycan:hmm.get(men).keySet())
           	      {
           	        graph.addEdge(nodearray[i],keycan,1);  
           	        }
           	      }
           	
           	    //有多个候选实体的情况
           	    if(hmm.get(men).size()>1){
           	      HashMap<String,Double>haa=hmm.get(men);
           	      //选取最大相似度的
           	       double simmax=0.40;//相当于定义了一个阀值，这个值可以根据情况而改，低于阀值的相当于没找到
           	    	 for(String keycan:haa.keySet()){
           	    		 if(haa.get(keycan)>simmax){
           	    			 simmax=haa.get(keycan);
           	    		 }//end if
           	     }//end for
           	    //链接到知识库的候选实体上
           	     for(String keyca:haa.keySet()){
            	      if(simmax==haa.get(keyca)) {
            	    	 graph.addEdge(nodearray[i],keyca,1);  
            	      if(simmax==0.40){
            	    	  System.out.println(men+"在知识库中不存在相应信息,返回NIL");
            	    	    account++;
            	      }//end if
            	     } //end if  
           	    } //end for
           	} //end if 
          } //end for(比较结束)
       	}//end if(第三种情况）
          
      }//end for
        String graphresult=graph.printGraph();
       System.out.println();
       System.out.println(graphresult);
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
    	 
          HashMap<String,Boolean>judge=new HashMap<String,Boolean>();
          for(int i=0;i<list.size();i++){
        	   judge.put(list.get(i), true);
            }
          
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
            		double sum=0;
            		double max=0;
            		 for(String key:canhash.keySet()){
            			 sum+=canhash.get(key);
        	    		 if(canhash.get(key)>max){
        	    			 max=canhash.get(key);
        	    		 }//end if
        	           }//end for
            		 account=sum;
            	  if(sum>0){
            		   Boolean flag=false;
            		   judge.put(list.get(i), flag);
            	   for(String key:canhash.keySet()){
                	      if(max==canhash.get(key)&&max>0) {
                	  System.out.println("文本中的"+list.get(i)+"="+key);
                	  account++;
                	      } //end if  
               	       } //end for
            	    }//end sum
           	    }//end if 
              }//end for
        	  
      if(account<list.size()){
           List<String>canlist=new ArrayList<String>();
        	for(int i=0;i<list.size();i++)
        	{
              Neo4jOperate neo=new Neo4jOperate();
              if(neo.getMen(list.get(i)).get(0).size()>1&&judge.get(list.get(i)))
              {
            	  canlist.add(list.get(i));
                }//end if 
        	 }//end for
        	  oneEntity(canlist);
           }
       }//end if	 
     }//end select 函数
     
     /*
      * 主函数
      */
   public static void main(String args[]) throws IOException{
	    /*
		 * 从文本中读取内容
		 */
	  //  String text=null;//存储从文本中拿出来的字符串
	    String text="20岁时经人介绍认识了妻子，经过两年的相处两人1978年走入婚姻殿堂。1980年，大女儿王佟艺出生，对于24岁做父亲的王晶是一个很大的惊喜。1980年小女儿王子涵出生，王晶才感到做父亲的责任和父爱的重要。";
	    List<String>entitylist=new ArrayList<String>();
	    NameEntityLinking nel=new  NameEntityLinking();
	    Entity en=new Entity();
	    Neo4jOperate neo=new Neo4jOperate();
	    long startTime=System.currentTimeMillis();  //获取开始时间
	    neo.createDb(); //打开知识库
	    System.out.println("文本为："+text);
//		try {
//			FileInputStream fin=new FileInputStream("Text/text.txt");
//			BufferedReader innet = new BufferedReader(new InputStreamReader(fin));
//			while((text=innet.readLine())!=null)
//			{ 
//		     System.out.println("文本为："+text);
////			 byte[] buf=new byte[1000000];//缓存的大小决定文本的大小
////			 int len=fin.read(buf);//从text.txt中读出内容
////			 text=new String(buf,0,len);
//			  docStr=text;//存储一个全局的值
//			  entitylist=en.getEntity(text);
//		     nel.Select(entitylist);
//		      System.out.println();
//		     
//			}
//			fin.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}//end try
		// Date date=new Date();姚明
		 //System.out.println(date.getHours()+":"+date.getMinutes());
	     docStr=text;//存储一个全局的值 
	     entitylist=en.getEntity(text);
         nel.Select(entitylist);
	      //nel.Graph( entitylist);
	    // nel.oneEntity(entitylist);
		    neo.shutDown();//关闭知识库
	     long endTime=System.currentTimeMillis(); //获取结束时间
		 double minute=(endTime-startTime)/1000.0;
		System.out.println("程序运行时间： "+minute+"s");
	 }
}
