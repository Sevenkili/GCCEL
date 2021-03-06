package Graph;


import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import com.sun.jmx.remote.internal.ArrayQueue;

/** 
 * 邻接矩阵法表示图 
 * @author  
 * 
 */  
public class MatrixGraph implements Graph {  
    private static final int defaultSize = 100;  
    private int maxLen;  //矩阵的最大长度  
    private int edgeNum; //边的条数   
    private List vertexs;  //顶点列表
    private Edge edges[][]; //边的数组（表示顶点间的关系）
      
    private enum Visit{unvisited, visited};  
      
    /** 
     * 构造函数 
     */  
    public MatrixGraph() {  
        maxLen = defaultSize;  
        vertexs  = new ArrayList();  
        edges = new MatrixEdge[maxLen][maxLen];  
    }  
    /** 
     * 构造函数 
     * @param vexs 顶点的数组 
     */  
    public MatrixGraph(Object vexs[]) {  
        maxLen = vexs.length;  
        vertexs  = new ArrayList();  
        edges = new MatrixEdge[maxLen][maxLen];  
        for(int i=0; i<maxLen; i++) {  
            vertexs.add(vexs[i]);  
        }  
    }  
    @Override  
    public void addEdge(Object v1, Object v2, double weight) {  
        int i1 = vertexs.indexOf(v1);  
        int i2 = vertexs.indexOf(v2);  
        //System.out.println("i1: " + i1 + "  i2:" + i2);  
        if(i1>=0 && i1<vertexs.size() && i2 >=0 && i2<vertexs.size()) {  
            edges[i1][i2] = new MatrixEdge(v1, v2,null, weight);  
            edgeNum ++;  
        } else {  
            throw new ArrayIndexOutOfBoundsException("顶点越界或对应的边不合法！");  
        }  
    }  
    @Override  
    public void addEdge(Object v1, Object v2, Object info, double weight) {  
        int i1 = vertexs.indexOf(v1);  
        int i2 = vertexs.indexOf(v2);  
        if(i1>=0 && i1<vertexs.size() && i2 >=0 && i2<vertexs.size()) {  
            edges[i1][i2] = new MatrixEdge( v1, v2, info, weight);  
            edgeNum ++;  
        } else {  
            throw new ArrayIndexOutOfBoundsException("顶点越界或对应的边不合法！");  
        }  
    }  
    //加入新的节点之后，处理图的方法
    @Override  
    public void addVex(Object v) {  
        vertexs.add(v);  
        if(vertexs.size() > maxLen) {  
            expand();  
        }  
    }  
    private void expand() {  
        MatrixEdge newEdges[][] = new MatrixEdge[2*maxLen][2*maxLen];  
        for(int i=0; i<maxLen; i++) {  
            for(int j=0; j<maxLen; j++) {  
                newEdges[i][j] = (MatrixEdge) edges[i][j];  
            }  
        }  
        edges = newEdges;  
    }  
      
    @Override  
    public int getEdgeSize() {  
        return edgeNum;  
    }  
    @Override  
    public int getVertexSize() {  
        return vertexs.size();  
    }  
    @Override  
    public void removeEdge(Object v1, Object v2) {  
        int i1 = vertexs.indexOf(v1);  
        int i2 = vertexs.indexOf(v2);  
        if(i1>=0 && i1<vertexs.size() && i2 >=0 && i2<vertexs.size()) {  
            if(edges[i1][i2] == null) {  
                try {  
                    throw new Exception("该边不存在！");  
                } catch (Exception e) {  
                    e.printStackTrace();  
                }  
            } else {  
                edges[i1][i2] = null;  
                edgeNum --;  
            }  
        } else {  
            throw new ArrayIndexOutOfBoundsException("顶点越界或对应的边不合法！");  
        }  
    }  
    @Override  
    public void removeVex(Object v) {  
        int index = vertexs.indexOf(v);  
        int n = vertexs.size();  
        vertexs.remove(index);  
        for(int i=0; i<n; i++){  
            edges[i][n-1] = null;  
            edges[n-1][i] = null;  
        }  
    }  
    @Override  
    public String printGraph() {  
        StringBuilder sb = new StringBuilder();  
        int n = getVertexSize();  
        for (int i = 0; i < n; i++) {  
            for(int j=0; j<n; j++) {  
            	if(edges[i][j]==null) 
            	edges[i][j]=new MatrixEdge(null,null ,null,0.0); ;
                sb.append(edges[i][j]+" ");  
            }  
            sb.append("\n");  
        }  
         return sb.toString();  
      // return edges.toString();
    }  
    @Override  
    public void clear() {  
        maxLen = defaultSize;  
        vertexs.clear();  
        edges = null;  
    }  
    @Override  
    public String dfs(Object o) {
//        Visit visit[] = new Visit[vertexs.size()];  
//        for(int i=0; i<vertexs.size(); i++)  
//            visit[i] = Visit.unvisited;  
//        StringBuilder sb = new StringBuilder();  
//        dfs(o, visit, sb);  
//        return sb.toString();
    	  return null;  
    }  
    private void dfs(Object o, Visit[] visit, StringBuilder sb) {  
//        int n = vertexs.indexOf(o);  
//        sb.append(o + "\t");  
//        visit[n] = Visit.visited;  
//          
//        Object v = getFirstVertex(o);  
//        while(null != v) {  
//            if(Visit.unvisited == visit[vertexs.indexOf(v)])  
//                dfs(v, visit, sb);  
//            v = getNextVertex(o, v);  
//        }  
    }  
    @Override  
    public Object getFirstVertex(Object v) {  
        int i = vertexs.indexOf(v);  
        if(i<0)  
            throw new ArrayIndexOutOfBoundsException("顶点v不存在！");  
        for(int col=0; col<vertexs.size(); col++)  
            if(edges[i][col] != null)  
                return vertexs.get(col);  
        return null;  
    }  
    @Override  
    public Object getNextVertex(Object v1, Object v2) {  
        int i1 = vertexs.indexOf(v1);  
        int i2 = vertexs.indexOf(v2);  
        if(i1<0 || i2<0)  
            throw new ArrayIndexOutOfBoundsException("顶点v不存在！");  
        for(int col=i2+1; col<vertexs.size(); col++)  
            if(edges[i1][col] != null)  
                return vertexs.get(col);  
        return null;  
    }
	@Override
	public String bfs(Object o) {
		// TODO Auto-generated method stub
		return null;
	}  
}  
