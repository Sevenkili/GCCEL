package KnowledgeBase;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.kernel.Traversal;

import NetProcess.ClassProcess;
public class Neo4jOperate {
	   private static final String DB_PATH = "E:/graduate/all_ontology1.0.db";//知识库名称和地址路径
		public String greeting;
		static GraphDatabaseService graphDb;
		Relationship relationship;
		
		//List<String>urllist=new ArrayList<String>();//存储候选实体组的url
		
		private static enum RelTypes implements RelationshipType {
			ISA
		}
		
		/**
		 * 通过tag索引找到同名实体节点
		 * 输入：tag[String]
		 * 输出：同名(tag)节点列表[ArrayList<Node>]
		 * 注明必须在建立数据库的时候建立索引才有有效
		 */
		
		public List<Node> getNodesByTag(String tag){
			List<Node> nodesList = new ArrayList<Node>();
			Transaction tx = graphDb.beginTx();
			try {

				IndexManager indexManager = graphDb.index();
				Index<Node>  entity_index = indexManager.forNodes( "entity_index" );
			    //通过索引查找
		        IndexHits<Node> hits = entity_index.get("tag",tag);
		        while(hits.hasNext())
		        {
		         Node entity = hits.next();
		         nodesList.add(entity);
		        }
			    tx.success();
			} finally {
				tx.finish();
			}
			return nodesList;
		 }
		
		/*
		 * 得到数据库中的候选实体列表
		 */
		public HashMap<String,List<String>> getCandidate(String mention){
		//	graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);//启动数据库
			Transaction tx = graphDb.beginTx();//连接知识库
			 List<Node> candidatenode=getNodesByTag(mention);
			 HashMap<String,List<String>>hm=new HashMap<String,List<String>>();//用来存储实体名和属性
			for(int i=0;i<candidatenode.size();i++){
				List<String>prolist=new ArrayList<String>();//存储实体属性的列表
				String nodename=null;
				Node candidate= candidatenode.get(i);
				String abs=null;
				if(candidate.hasProperty("name")){
				   nodename=candidate.getProperty("name").toString();//存储节点名字
				  // System.out.println(nodename);
				}//end if
				if(candidate.hasProperty("ABSTRACT")){
					abs=candidate.getProperty("ABSTRACT").toString();
					//System.out.println(abs);
				}
			   Iterator<String>ProIt=candidate.getPropertyKeys().iterator();
			   while(ProIt.hasNext()){
				   String pronode=ProIt.next(); 
				   String value=candidate.getProperty(pronode).toString();
				   if(!value.equals(abs)&&!prolist.contains(value)&&value.length()<10){
						  prolist.add(value);
						hm.put(nodename, prolist); 
					  }//end if
			   } //end while
			
			 Iterator<Relationship>RelIt=candidate.getRelationships(Direction.OUTGOING).iterator();
			    while(RelIt.hasNext()){
			    	Relationship re=RelIt.next();
			    	String Relname=re.getType().name();
			    	//System.out.println(Relname);
			     if(!Relname.equals(RelTypes.ISA.toString())){
			    	Node neibo=re.getEndNode();
			    	String value="";
			    	if(neibo.hasProperty("name")){
			    	 value=neibo.getProperty("name").toString();
			    	}
			    	if(!prolist.contains(value)&&value.length()<10){
						  prolist.add(value);
						hm.put(nodename, prolist); 
					  }//end if
			    }//end if
			  }//end while
			}// end for
		  // System.out.println(hm);
			return hm;
		}
        /*
        * 若文本中只有一个实体，则拿到候选实体的url
        */
		public HashMap<String,String> getUrl(String mention){
		   	HashMap<String,String>urlhm=new HashMap<String,String>();
			//createDb();
			Transaction tx = graphDb.beginTx();//连接知识库
            List<Node> candidatenode=getNodesByTag(mention);
			for(int i=0;i<candidatenode.size();i++){
				String url=null;//用来存储实体的url(若没有url则自己构建url,http://www.baike.com/wiki/实体名字（name属性）)
				String nodename=null;//用来存放节点的名字
				Node candidate= candidatenode.get(i);
				if(candidate.hasProperty("name")){
				   nodename=candidate.getProperty("name").toString();//存储节点名字
				  // System.out.println(nodename);
				}//end if
		       if(candidate.hasProperty("URL")){
		    	   url=candidate.getProperty("URL").toString();
		       }//end if
		       if(!candidate.hasProperty("URL")){
		    	   url="http://www.baike.com/wiki/"+nodename;
		       }//end if
		        urlhm.put(nodename,url);
		      //  urllist.add(url); 
			   }//end for
			//System.out.println(urlhm);
			return urlhm;
		
		 }
		
	 /*
	  * 得到与实体有关系的实体
	  */
	  public HashMap<String,List<String>>getRelentity(String mention ){
		//  graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);//启动数据库
		  Transaction tx = graphDb.beginTx();//连接知识库
          List<Node> candidatenode=getNodesByTag(mention);
          HashMap<String,List<String>>hm=new HashMap<String,List<String>>();
          List<HashMap<String,List<String>>>en=new ArrayList<HashMap<String,List<String>>>();
          for(int i=0;i<candidatenode.size();i++){
          List<String> entitylist=new ArrayList<String>();
           Node candidate= candidatenode.get(i);
           String nodename=null;
           if(candidate.hasProperty("name")){
        	   nodename=candidate.getProperty("name").toString();
           }
           Iterator<Relationship>RelIt=candidate.getRelationships(Direction.OUTGOING).iterator();
			    while(RelIt.hasNext()){
			    	Relationship re=RelIt.next();
			    	String Relname=re.getType().name();
			    	//System.out.println(Relname);
			     if(!Relname.equals(RelTypes.ISA.toString())){//&&!Relname.equals("国籍")){
			    	Node neibo=re.getEndNode();
			    	String value="";
			    	if(neibo.hasProperty("name")){
			    	 value=neibo.getProperty("name").toString();
			    	}
			    	entitylist.add(value);	
			    }//end if
			  }//end while
			   hm.put(nodename, entitylist);
           }//end for
	      
		  return hm;
	  }  
	  
	  /*
		  * 得到与实体的分类
		  */
		 public List<HashMap<String,List<String>>>getMen(String mention ){
				Transaction tx = graphDb.beginTx();//连接知识库
				 List<Node> candidatenode=getNodesByTag(mention);
				 List<HashMap<String,List<String>>>list=new ArrayList<HashMap<String,List<String>>>();
				 HashMap<String,List<String>>hm=new HashMap<String,List<String>>();//用来存储实体名和属性
				 HashMap<String,List<String>>hm1=new HashMap<String,List<String>>();//用来存储实体的分类
				 ClassProcess cp=new ClassProcess();
				 for(int i=0;i<candidatenode.size();i++){
					List<String>prolist=new ArrayList<String>();//存储实体属性的列表
					List<String>prolist1=new ArrayList<String>();//存储实体分类列表
					String nodename=null;
					Node candidate= candidatenode.get(i);
					String abs=null;
					if(candidate.hasProperty("name")){
					   nodename=candidate.getProperty("name").toString();//存储节点名字
					  // System.out.println(nodename);
					}//end if
				 if(candidate.hasProperty("ABSTRACT")){
						abs=candidate.getProperty("ABSTRACT").toString();
						//System.out.println(abs);
					}
				   Iterator<String>ProIt=candidate.getPropertyKeys().iterator();
				   while(ProIt.hasNext()){
					   String pronode=ProIt.next(); 
					   String value=candidate.getProperty(pronode).toString();
					   if(!value.equals(abs)&&!prolist.contains(value)&&value.length()<10){
							  prolist.add(value);
							hm.put(nodename, prolist); 
						  }//end if
				   } //end while
				 Iterator<Relationship>RelIt=candidate.getRelationships(Direction.OUTGOING).iterator();
				    while(RelIt.hasNext()){
				    	Relationship re=RelIt.next();
				    	String Relname=re.getType().name();
				    	//System.out.println(Relname);
				     if(!Relname.equals(RelTypes.ISA.toString())){
				    	Node neibo=re.getEndNode();
				    	String value="";
				    	if(neibo.hasProperty("name")){
				    	 value=neibo.getProperty("name").toString();
				    	}//end if
				    	if(!prolist.contains(value)&&value.length()<10){
							  prolist.add(value);
							hm.put(nodename, prolist); 
						  }//end if
				    }//end if
				    if(Relname.equals(RelTypes.ISA.toString())){
				 //   else{
				    	Node neibo=re.getEndNode();
				    	String value="";
				    	if(neibo.hasProperty("name")){
				    	 value=neibo.getProperty("name").toString();
				    	}//end if
				    	if(!prolist1.contains(value)&&value.length()<10){
				    		List<String> WordList=cp.SegmentProcess(value);
				    		 prolist1.addAll(WordList);	
				    		 prolist1=cp.removeDuplicateWithOrder(prolist1);
							 hm1.put(nodename, prolist1); 
						  }//end if
				    }//end if (else)
				  }//end while
				}// end for
			  // System.out.println(hm);
				list.add(hm);
				list.add(hm1);
				return list;
		  }  
		 
		    /**
			 * 删除无效的节点,也就是那些没有任何信息的点，没有name属性，没有relation
			 * 输入：无
			 * 输出：无
			 */
			public void deleteInvalidNodes(String mention) {
				graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);//启动数据库
				Transaction tx = graphDb.beginTx();//连接知识库
			    List<Node> candidatenode=getNodesByTag(mention);
			    Node node=null;
			    for(int i=0;i<candidatenode.size();i++){
			    	String nodename=null;
			    	Node candidate= candidatenode.get(i);
				
					if(candidate.hasProperty("name")){
				    nodename=candidate.getProperty("name").toString();//存储节点名字
				    if(nodename.equals(mention)){
				    	node=candidate;
				     }
				 }
			    }
			   //System.out.println(node.getProperty("name").toString());
				for(int i=0;i<candidatenode.size();i++){
					String nodename=null;
					Node candidate= candidatenode.get(i);
					String abs=null;
					if(candidate.hasProperty("name")){
					  nodename=candidate.getProperty("name").toString();//存储节点名字
					   if(nodename.contains("　　")||nodename.contains("。")){
				    Iterator<Relationship>RelIt=candidate.getRelationships().iterator(); 
				   while(RelIt.hasNext()){
				    	Relationship re=RelIt.next();
				    	String Relname=re.getType().name();
				    	//System.out.println(Relname);
				    	Node[] neibo=re.getNodes();
				    	for(int j=0;j<neibo.length;j++)
				    	{	String value="";
				    	if(neibo[j].hasProperty("name")){
				     	 value=neibo[j].getProperty("name").toString();
				     	         }//end if 
				    	 relationship = neibo[j].createRelationshipTo(node,
 							DynamicRelationshipType.withName(Relname));
				          }
				          }//end while
				    candidate.delete();
				       }//end if 
				  }//end if
		    }//end for	
				System.out.println("非法节点删除完成");
		}
		
				
	  /*
	   * 主函数
	   */
       public static void main(String[]args){
    	 graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);//启动数据库
    	   long startTime=System.currentTimeMillis();   //获取开始时间
    	   Neo4jOperate neo=new Neo4jOperate();
    	   String mention="李丽";
    	   String mention1="";
    	 //  neo.getCandidate(mention);
    	  // neo.getUrl(mention);
    	 //neo.deleteInvalidNodes(mention);
    	// System.out.println(neo.getRelentity(mention));
    	 System.out.println(neo.getMen(mention));
    	  // System.out.println(neo.getUrl("王晶"));
    	    neo.shutDown();
    	 long endTime=System.currentTimeMillis(); //获取结束时间
  		 double minute=(endTime-startTime)/1000.0;
  		 System.out.println("程序运行时间： "+minute+"s");
       }
       /*
        * 打开数据库中国
        */
       public void createDb() {
   		// deleteFileOrDirectory(new File(DB_PATH));
   		// START SNIPPET: startDb
   		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);//打开数据库
   		// GraphDatabaseService graphDb=new
   		// RestGraphDatabase("http://localhost:7474/db/data");
   		registerShutdownHook(graphDb);
   		
     	}
       /*
        * 断开数据库
        */
		public void shutDown() {
			//System.out.println("Shutting down database ...");
			// START SNIPPET: shutdownServer
			graphDb.shutdown();
			// END SNIPPET: shutdownServer
		}
		// START SNIPPET: shutdownHook
		static void registerShutdownHook(final GraphDatabaseService graphDb) {
			// Registers a shutdown hook for the Neo4j instance so that it
			// shuts down nicely when the VM exits (even if you "Ctrl-C" the
			// running application).
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
				graphDb.shutdown();
				}
			});
			}
}
