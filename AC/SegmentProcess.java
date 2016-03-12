package ICTCLAS.I3S.AC;
/*
 对输入语句进行分词处理
 */
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class SegmentProcess {
   // static String[] str2=new String[]{};
	//static String str=null;
	private ICTCLAS50 ictclas = new ICTCLAS50();
	public boolean init(){
		String argu=".";
		try{
			if (  ictclas.ICTCLAS_Init(argu.getBytes("utf-8")  )==false){
				System.out.println("Init Fail!");
				System.out.println("----------");
				return false;
			}else{
//				
				int nCount=0;
				String usrdir="userdict.txt";
				byte[] usrdirb = usrdir.getBytes();
				nCount = ictclas.ICTCLAS_ImportUserDictFile(usrdirb, 3);

				if (ictclas.ICTCLAS_SetPOSmap(0)==1){
					return true;
				}else{
					System.out.println("设置失败");
					return false;
				}
			}
		}catch(Exception ex){
			return false;
		}
	}
	
	
	public String fileProcess(String src, int istag, String ecodeName){
		return "";
	}
	//没有代词的分词
//	public String paragraphProcess1(String src, int istag){
//	    	byte[] rt = null;
//		   String rtStr = null;
//		try{
//			rt=ictclas.ICTCLAS_ParagraphProcess(src.getBytes("UTF-8"), 3, istag);//设置格式为utf-8
//			rtStr = new String(rt,0,rt.length,"UTF-8");
//		}catch(UnsupportedEncodingException e1){
//			System.out.println("编码异常，分词失败");
//			e1.printStackTrace();
//		}
//		//去掉两个实体左右两边符号部分包括逗号，顿号，分号（现在只考虑逗号，逗号情况占绝大部分）
//				String regex1=".*，.*/n.*/n.*，.*";
//				String regex11=".*，.*/n.*/n.*";
//				String regex111=".*/n.*/n.*，.*";
//				
//////				String regex1="(.*，.*/n.*/n.*，.*)|(.*；.*/n.*/n.*；.*)|(.*、.*/n.*/n.*、.*)"
//////						+ "|(.*，.*/n.*/n.*；.*)|(.*；.*/n.*/n.*，.*)|(.*、.*/n.*/n.*，.*)"
//////						+ "|(.*，.*/n.*/n.*、.*)|(.*；.*/n.*/n.*、.*)|(.*、.*/n.*/n.*；.*)";
////				   //str1=str1.replaceAll("[\\pP，；、]", "");//去掉所有标点符
//				Pattern p1=Pattern.compile(regex1);
//     			Matcher m1=p1.matcher(rtStr);
//				boolean b1=m1.matches();
//				//System.out.println(b1);
//				if(b1){
//				     String str[]=rtStr.split("，");
//				     rtStr=str[1];
//					
//				}
//				Pattern p2=Pattern.compile(regex11);
//				Matcher m2=p2.matcher(rtStr);
//				boolean b2=m2.matches();
//				//System.out.println(b2);
//				if(b2){
//					rtStr=rtStr.replaceAll(".*，",""); 
//					
//				}
//				Pattern p3=Pattern.compile(regex111);
//				Matcher m3=p3.matcher(rtStr);
//				boolean b3=m3.matches();
//				//System.out.println(b3);
//				if(b3){
//				     
//					rtStr=rtStr.replaceAll("，.*",""); 
//				}
//				
//				//去掉两个实体之间的有逗号间隔的部分
//				String regex=".*/n.*，.*，.*/n.*";
//				Pattern p=Pattern.compile(regex);
//				Matcher m=p.matcher(rtStr);
//				boolean b=m.matches();
//			//System.out.println(b);
//				if(b){
//					rtStr=rtStr.replaceAll("，.*，","");
//				}
//				
//				//去掉括号部分
//				String regex2=" (\\（.*\\）)| (\\【.*\\】)";
//				rtStr=rtStr.replaceAll(regex2, "");
//				
//				
//				//去掉句子中的特殊符号分号和破折号、引号以及分词出来的结果
//				String regex3="(；)|(—)|(“.*”)|(/\\w+)";
//				rtStr=rtStr.replaceAll(regex3, "");
//		         return rtStr.trim();
//		
//		
//	}
	
	public String paragraphProcess(String src, int istag){
		byte[] rt = null;
		String rtStr = null;
		try{
			rt=ictclas.ICTCLAS_ParagraphProcess(src.getBytes("UTF-8"), 3, istag);//设置格式为utf-8
			rtStr = new String(rt,0,rt.length,"UTF-8");
		}catch(UnsupportedEncodingException e1){
			System.out.println("编码异常，分词失败");
			e1.printStackTrace();
		}
		//去掉两个实体左右两边符号部分包括逗号，顿号，分号（现在只考虑逗号，逗号情况占绝大部分）
//				String regex1=".*，.*/n.*/n.*，.*";
//				String regex11=".*，.*/n.*/n.*";
//				String regex111=".*/n.*/n.*，.*";
//				
////				String regex1="(.*，.*/n.*/n.*，.*)|(.*；.*/n.*/n.*；.*)|(.*、.*/n.*/n.*、.*)"
////						+ "|(.*，.*/n.*/n.*；.*)|(.*；.*/n.*/n.*，.*)|(.*、.*/n.*/n.*，.*)"
////						+ "|(.*，.*/n.*/n.*、.*)|(.*；.*/n.*/n.*、.*)|(.*、.*/n.*/n.*；.*)";
//				   //str1=str1.replaceAll("[\\pP，；、]", "");//去掉所有标点符
//				Pattern p1=Pattern.compile(regex1);
//				Matcher m1=p1.matcher(rtStr);
//				boolean b1=m1.matches();
//				//System.out.println(b1);
//				if(b1){
//				     String str[]=rtStr.split("，");
//				     rtStr=str[1];
//					
//				}
//				Pattern p2=Pattern.compile(regex11);
//				Matcher m2=p2.matcher(rtStr);
//				boolean b2=m2.matches();
//				//System.out.println(b2);
//				if(b2){
//					rtStr=rtStr.replaceAll(".*，",""); 
//					
//				}
//				Pattern p3=Pattern.compile(regex111);
//				Matcher m3=p3.matcher(rtStr);
//				boolean b3=m3.matches();
//				//System.out.println(b3);
//				if(b3){
//				     
//					rtStr=rtStr.replaceAll("，.*",""); 
//				}
				
//				//去掉两个实体之间的有逗号间隔的部分
//				String regex=".*/n.*，.*，.*/n.*";
//				Pattern p=Pattern.compile(regex);
//				Matcher m=p.matcher(rtStr);
//				boolean b=m.matches();
//				//System.out.println(b);
//				if(b){
//					rtStr=rtStr.replaceAll("，.*，","");
//				}
				
				//去掉括号部分
				String regex2=" (\\（.*\\）)| (\\【.*\\】)";
				rtStr=rtStr.replaceAll(regex2, "");
				
				
//				//去掉句子中的特殊符号分号和破折号、引号以及分词出来的结果
//				String regex3="(；)|(—)|(“.*”)|(/\\w+)";
				
			  rtStr=rtStr.replaceAll("/\\w+", "");
		         return rtStr.trim();
	}
	
	public boolean exit(){
		return ictclas.ICTCLAS_Exit();
	}
	
	
	
	
//	public static void main(String[] args) throws UnsupportedEncodingException{
//		String str="毛泽东视为之一。";
//		SegmentProcess sp = new SegmentProcess();
//		sp.init();
//		//String str =sp.paragraphProcess("他经常和我一起在学校打台球",0);
//		String str1 = sp.paragraphProcess(str,1);
//		//System.out.println(str);
//		
//		 
//		System.out.println(str1);
//			
//	}
}