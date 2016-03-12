package NetProcess;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ICTCLAS.I3S.AC.SegmentProcess;

public class ClassProcess {
	/**
	 * 分词并删除停用词
	*/
	public ArrayList<String> SegmentProcess(String zhaiYao){
		
		String line=null;
		HashSet<String> StopWord=new HashSet();
		ArrayList<String> WordList=new ArrayList();//用于存储特征词
		try {
			InputStream in = new FileInputStream("Data/StopWord.txt");//读取文件上的数据
			//指定编码方式，将字节流向字符流的转换
			InputStreamReader isr = new InputStreamReader(in,"UTF-16");
			//创建字符流缓冲区
			BufferedReader bufr = new BufferedReader(isr);

			while((line = bufr.readLine())!=null){
//				 System.out.println(line);
				StopWord.add(line);
			}
	        isr.close(); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//分词并删除停用词
		SegmentProcess sp= new SegmentProcess();
		sp.init();
		String str =sp.paragraphProcess(zhaiYao, 0);
		
		String word[] =str.split("\\s+");
		for(int i=0;i<word.length;i++){
			if(StopWord.contains(word[i])){
//				System.out.println("删除停用词:"+word[i]);
//				word[i].replaceAll(word[i], "");
			}
			else{
				WordList.add(word[i]);
			}
		}
		return WordList;
		
	}
	/**
	 * 删除List中重复元素
	*/
	public List removeDuplicateWithOrder(List list) {
	     Set set = new HashSet();
	      List newList = new ArrayList();
	   for (Iterator iter = list.iterator(); iter.hasNext();) {
	          Object element = iter.next();
	          if (set.add(element))
	             newList.add(element);
	       } 
	      list.clear();
	      list.addAll(newList);
		return list;
	}
	
	


}
