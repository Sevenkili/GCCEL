package NetProcess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ExtractClass {
	public List<String> extraClass(String sb) {
		/*
		 * 用于存储一个网页的属性hash表
		 * 读取网页文件
		 * 
		 */
	ClassProcess cp=new ClassProcess();
	List<String>properlist=new ArrayList<String>();
//		File file = new File(filePath);
//		BufferedReader br = null;
		try {
//			br=new BufferedReader(new InputStreamReader(new FileInputStream(
//					file), "UTF-8"));
//			StringBuilder sb = new StringBuilder();
//			int ch = 0;
//
//			while ((ch = br.read()) != -1) {
//				sb.append((char) ch);
//			}			
			//如果读取的文件存在内容，进行抽取
			if (sb.toString().length() > 0) {
				//将文件存为Document形式
				Document doc = Jsoup.parse(sb.toString());
					/*
					 * 抽取开放分类
					 */
					Element openclass = doc.getElementById("openCatp");//获取包含完整开放分类的内容块
					Elements fenlei=new Elements();
					if(openclass != null){
						 fenlei = openclass.getElementsByTag("a");//分离<dd>标签
					}//end if
					if(!fenlei.toString().equals("")){
						if(fenlei.size()<6)
						{
						 for(int i=0;i<fenlei.size();i++){
							String strclass=fenlei.get(i).text();
							List<String> WordList=cp.SegmentProcess(strclass);
							properlist.addAll(WordList);	
						 }
						  }//end if
						 else{
						  for(int i=0;i<5;i++){
							 String strclass=fenlei.get(i).text();
							 List<String> WordList=cp.SegmentProcess(strclass);
							 properlist.addAll(WordList);
						 }
					}//end if
					}//end if
     	 }//end if (读取文件的部分)
		}catch (Exception e) {
			e.printStackTrace();
			// logger.error	(e.getMessage());
  } //end try
		properlist=cp.removeDuplicateWithOrder(properlist);
		return properlist;
	}
	
public static void main(String args[]) throws IOException{
	ExtractClass expro=new ExtractClass();
	 NetSpider nsp=new NetSpider();
	String path="http://www.baike.com/wiki/李娜[网球运动员]";
	String str=nsp.spide(path);
	List<String>result=expro.extraClass(str);
	System.out.println(result);
	System.out.println(result.size());
	//System.out.println(array[0]);
}

}
