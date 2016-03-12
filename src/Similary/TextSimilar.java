package Similary;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import NetProcess.ExtractHtmlText;
import NetProcess.NetSpider;

public class TextSimilar {
	 public double compute(List<String>list1,List<String>list2){
		 List<String>list=new ArrayList<String>();
		 TextProcessing Tp=new TextProcessing();
		 int k=0;
		String[] array1 = (String[])list1.toArray(new String[0]);
		String[] array2 = (String[])list2.toArray(new String[0]);
	//	String[] array2 = (String[])Tp.selectKey(list2).toArray(new String[0]);
//    	System.out.println(list1);
//	    System.out.println(list2);
//		list.addAll(list1);
//		list.addAll(list2);
		//选择网页中的关键字
	//  list=Tp.selectKey(list2);
      list=(ArrayList<String>)removeDuplicateWithOrder(list1);
	  //  System.out.println(list);
		//遍历合并后的list1
		int[] wordNum1 = new int[list.size()];//初始化里面的值全是0
		int[] wordNum2 = new int[list.size()];
		ListIterator<String> It=list.listIterator();
		while(It.hasNext()){
			String word = It.next();
			for(int m=0;m<array1.length;m++){
				if(word.equals(array1[m]))
					wordNum1[k]++;
			}
			for(int m=0;m<array2.length;m++){
				if(word.equals(array2[m]))
					wordNum2[k]++;
			}
			k++;
		}
		int numerator = 0;
		int denominator1 = 0;
		int denominator2 = 0;
		for(int m=0;m<list.size();m++){
			
			numerator+=wordNum1[m]*wordNum2[m];
			denominator1+=Math.pow(wordNum1[m],2);
			denominator2+=Math.pow(wordNum2[m],2);
			}
		double sim = numerator/(Math.sqrt(denominator1)*Math.sqrt(denominator2)); //余弦相似度
		//double sim = numerator/(Math.sqrt(denominator1)+Math.sqrt(denominator2)-numerator);//jacard相似度
		//System.out.println(sim);	
		return sim;
	}
	/**
	 * 删除List中重复元素
	*/
	public List removeDuplicateWithOrder(List list) {
	     Set set = new HashSet();  //集合具有唯一性，利用这一点特点我们可以确保元素的唯一性
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
	/*
	 * 找最大数
	 */
	 public double findmax(Double array[]){
		 double max=array[0];
		  for(int i=0;i<array.length;i++){
			 if(array[i]>max) max=array[i];
		  }
		 return max;
	 }
	 
    public static void main(String args[]) throws IOException{
    	String text=null;
		try {
			FileInputStream fin=new FileInputStream("text.txt");
			byte[] buf=new byte[1000000];//缓存的大小决定文本的大小
			int len=fin.read(buf);//从text.txt中读出内容
			//System.out.println(new String(buf,0,len));
			text=new String(buf,0,len);
			//System.out.println(text);
			fin.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    TextProcessing Tp=new TextProcessing();
	ExtractHtmlText ex=new ExtractHtmlText();
	TextSimilar Ts=new TextSimilar();
	NetSpider nsp=new NetSpider();
	String name="李娜[歌手]";
	String path="http://www.baike.com/wiki/"+name;//url地址
	String str=nsp.spide(path);
    //String name="李娜[网球运动员]";
//	path=sp.spide("http://www.baike.com/wiki/"+name,name);
 //String result=ex.extraHTMLText(path);
    String result=ex.extraHTMLText(str);
    //System.out.println(str);
    List<String>one=Tp.SegmentProcess(text.replaceAll("李娜",""));
    List<String>two=Tp.SegmentProcess(result.replaceAll("李娜",""));
     System.out.println(Ts.compute(one,two));
    
   }
}
