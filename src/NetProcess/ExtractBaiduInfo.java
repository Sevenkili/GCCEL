package NetProcess;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import Similary.TextProcessing;

public class ExtractBaiduInfo {
	public List<String> extratinfo(String filePath) {

		/*
		 * 读取文件，将其以String形式存入
		 */
		File file = new File(filePath);
	    List<String>infolist=new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line = "";
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			if (sb.length() > 0) {// 如果string buffer 中有内容，进行处理
				Document doc = Jsoup.parse(sb.toString().replaceAll("&nbsp;","")); // 将文本内容存为doc
				/*
				 * infobox信息抽取函数：
				 * 输入： doc(Document)
				 * 输出： void
				 */
				Element infobox = doc.getElementById("baseInfoWrapDom"); // 获取包含infobox的DIV
				if (infobox != null) { // 如果存在Infobox
					Element left = infobox.getElementsByClass("baseInfoLeft").get(0); // 分别获取左右两个infobox
					Element right = infobox.getElementsByClass("baseInfoRight").get(0);
					ArrayList<Element> left_right = new ArrayList<Element>();// 将左右infobox存在一个list中
					left_right.add(left);
					left_right.add(right);

					for (Element info : left_right) {
						Elements iterm = info.getElementsByClass("biItem");// 获取每一条信息基本单位，一个class为"biIterm"的div

						for (int i = 0; i < iterm.size(); i++) {
							Elements inner = iterm.get(i).getElementsByClass(
									"biItemInner");// 获取class名为"biItemInner"的div
							String attr = inner.get(0)
									.getElementsByClass("biTitle").text(); //获取属性名称
							String value = inner.get(0)
									.getElementsByClass("biContent").text();//获取属性值
							//属性值长度大于5的进行切分
							if(value.length()>=5){
								 TextProcessing tp=new TextProcessing();
								 infolist.addAll(tp.SegmentProcess(value));
							}
							else{
								infolist.add(value);
							}
							
						}// end for
					}// end for
				}// end if
			} else {
				System.out.println("No  Content");
			}// end if else
		}// end try
		catch (Exception e) {
			e.printStackTrace();
		} // end catch

		return infolist;
	}
	public static void main(String arg[]){
		ExtractBaiduInfo info=new ExtractBaiduInfo();
		List<String>result=new ArrayList<String>();
		result=info.extratinfo("E:/graduate/hudong/5.txt");
		System.out.println(result);
		
	}
}
