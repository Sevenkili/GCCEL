package Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/*
 * 测试函数以及计算节点的出入度函数
 */
public class Test {
	public static void main(String args[]) {  
        String result=null;  
        ComputerDegree cd=new ComputerDegree();
        HashMap<String,Double>hash=new HashMap<String,Double>();
//        List<Integer>outlist=new ArrayList<Integer>();
//        List<Integer>inlist=new ArrayList<Integer>();
       String obj[] = { "A", "B", "C", "D", "E", "F" }; 
        //Graph graph = new MatrixGraph(obj);  
        Graph graph = new MatrixGraph(obj);  
        //graph.addVex('F');  
        //行代表初度，列代表入度
        graph.addEdge("A","C",1);  
        graph.addEdge("B","A",1);  
        graph.addEdge("C","B",1);  
        graph.addEdge("E","D",1);  
        graph.addEdge("F","E",1);  
        graph.addEdge("A", "F", 1);  
        graph.addEdge("C", "F", 1); 
        result=graph.printGraph();
        System.out.println(result);  
        hash=cd.computer(obj, result);
        System.out.println();  
        System.out.println(hash);  
    } //end main
}
