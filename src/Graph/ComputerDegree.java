package Graph;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ComputerDegree {
   public HashMap<String,Double> computer(String[]obj,String Matrix){
	   List<Integer>outlist=new ArrayList<Integer>();
       List<Integer>inlist=new ArrayList<Integer>();
       HashMap<String,Double>hash=new HashMap<String,Double>();
	   String []row=Matrix.split("\n");
       int len=row.length;
       String juzheng[][]=new String[len][];
      //将前面返回的字符串存成数组的形式
      for(int i=0;i<len;i++){
   	   juzheng[i]=row[i].split(" ");
   	  // System.out.println(row[i]);     
      }
      //System.out.println(juzheng[0][0]);   
     //计算顶点的出度 
      for(int i=0;i<len;i++)
      { int out=0;
     	for(int j=0;j<len;j++)
         { if(juzheng[i][j].equals("1.0")){
       	  out++;
         }
   	   //System.out.print("  "+juzheng[i][j]);  
          }
           outlist.add(out);
          // System.out.println(obj[i]+"的出度:"+out); 
      } //end 出度
      
     // System.out.println();
     //计算顶点的入度
      for(int j=0;j<len;j++)
      { int in=0;
   	   for(int i=0;i<len;i++)
         { if(juzheng[i][j].equals("1.0")){
       	  in++;
         }
   	   //System.out.print("  "+juzheng[i][j]);  
          }
   	       inlist.add(in);
          // System.out.println(obj[j]+"的入度:"+in); 
      } //end 入度
    //  System.out.println();
      //计算节点的度（出度+入度）
      for(int i=0;i<obj.length;i++)
      {  
   	   double account=outlist.get(i)+inlist.get(i);
   	    hash.put(obj[i], account);
     	//System.out.println(obj[i]+"的度:"+account);  
     	
      }
	   return hash;
   }
   
   public void main(String arg[]){
	   
   }
}
