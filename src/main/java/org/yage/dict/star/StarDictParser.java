package org.yage.dict.star;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;

/**
 * StarDict dictionary file parser
 * usage as follows:<br/>
 * <code>StarDictParser rdw=new StarDictParser();</code><br/>
 * <code>rdw.loadIndexFile(f);</code><br/>
 * <code>rdw.loadContentFile(fcnt);</code><br/>
 * @author beethoven99@126.com
 */
public class StarDictParser {
	
	//全局读文件缓冲区
	private byte buf[]=new byte[1024];
	
	//字符串的开始标记
	private int smark;
	
	//缓冲区的处理标记
	private int mark;
	
	//装载读到的单词和位置、长度
	private Map<String,WordPosition> words=new HashMap<String,WordPosition>();

	//最大返回结果数
	public static final int MAX_RESULT=40;
	
	//随机读取字典内容
	private RandomAccessFile randomAccessFile;
	
	public List<Map.Entry<String,WordPosition>> searchWord(String term){
		//直接开头的结果
		List<Entry<String, WordPosition>> resa=new ArrayList<Map.Entry<String,WordPosition>>();
		//间接开头的结果
		List<Entry<String, WordPosition>> resb=new ArrayList<Map.Entry<String,WordPosition>>();
		
		int i=-1;
		for(Map.Entry<String, WordPosition> en:words.entrySet()){
			if(en.getKey()==null){
				System.out.println("oh no null");
			}
			i=en.getKey().toLowerCase().indexOf(term);
			if(i==0){
				resa.add(en);
			}else if(i>0 && resb.size()<MAX_RESULT){
				resb.add(en);
			}
			if(resa.size()>MAX_RESULT){
				break;
			}
		}
		
		Collections.sort(resa,WordComparator);
		Collections.sort(resb,WordComparator);
		
		if(resa.size()<MAX_RESULT){
			int need=MAX_RESULT-resa.size();
			if(need>resb.size()){
				need=resb.size();
			}
			resa.addAll(resb.subList(0, need));
		}
		return resa;
	}
	
	/**
	 * 加载内容文件
	 * @param f
	 */
	public boolean loadContentFile(String f){
		try {
			this.randomAccessFile=new java.io.RandomAccessFile(f, "r");
			System.out.println("is file open valid: "+this.randomAccessFile.getFD().valid());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * 得到字典内容中的内容片断，即为释义
	 * @param start offset point
	 * @param len length to get
	 * @return
	 */
	public String getWordExplanation(int start,int len){
		String res="";
		byte[] buf=new byte[len];
		try {
			this.randomAccessFile.seek(start);
			int ir=this.randomAccessFile.read(buf);
			if(ir!=len){
				System.out.println("Error occurred, not enought bytes read, wanting:"+len+",got:"+ir);
			}
			res=new String(buf,"utf-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	/**
	 * 加载索引文件
	 * @param f
	 */
	public void loadIndexFile(String f){
		FileInputStream fis=null;
		try {
			fis=new FileInputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			//第一次读
			int res=fis.read(buf);
			while(res>0){
				mark=0;
				smark=0;
				parseByteArray(buf,1024);
				if(mark==res){
					//上一个刚好完整，可能性几乎不存在，不过还要考虑
					res=fis.read(buf);
				}else{
					//有上一轮未处理完的，应该从mark+1开始
					//System.out.println("写 buf from: "+(buf.length-smark)+", len:"+(smark));
					res=fis.read(buf,buf.length-smark, smark);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * parse a block of bytes
	 * @param buf 使用全局的，之后可以去掉它
	 * @param len cbuf最多只有这么长
	 * @throws UnsupportedEncodingException
	 */
	private void parseByteArray(byte buf[],int len) throws UnsupportedEncodingException{
		for(;mark<len;){
			if(buf[mark]!=0){
				//在字符串中
				if(mark==len-1){
					//字符串被截断了
					//System.out.println("字符串被截断：mark:"+mark+", smark: "+smark+", left:"+(len-mark));
					System.arraycopy(buf, smark, buf, 0, len-smark);
					break;
				}else{
					mark++;
				}
			}else{
				//等于0，说明一个单词完结
				String tword=null;
				if(mark!=0){
					byte[] bs=ArrayUtils.subarray(buf, smark, mark);
					tword=new String(bs,"utf-8");
				}
				
				if(len-mark>8){
					//如果后面至少还有8个
					smark=mark+9;
					byte[] bstartpos = ArrayUtils.subarray(buf, mark+1,mark+5);
					byte[] blen=ArrayUtils.subarray(buf, mark+5, mark+9);
					int startpos=ByteArrayHelper.toIntAsBig(bstartpos);
					int strlen=ByteArrayHelper.toIntAsBig(blen);
					//System.out.println(" start: "+startpos+" , len:"+strlen);
					//现在是一个完整的单词了
					if(tword!=null && tword.trim().length()>0 && strlen<10000){
						words.put(tword, new WordPosition(startpos,strlen));
					}
					mark+=8;
				}else{
					//后面少于8个
					//System.out.println("后面少于8个:mark="+mark+", smark: "+smark+", left:"+(len-mark));
					//加完之后已跳过0，指向两个8字节数字
					System.arraycopy(buf, smark, buf, 0, len-smark);
					break;
				}
			}
		}
	}
	
	public Map<String, WordPosition> getWords() {
		return words;
	}
	
	public void setWords(Map<String, WordPosition> words) {
		this.words = words;
	}
	
	/**
	 * customer comparator
	 */
	private  static Comparator<Map.Entry<String,WordPosition>> WordComparator=new Comparator<Map.Entry<String,WordPosition>>(){
		public int compare(Map.Entry<String,WordPosition> ea,Map.Entry<String,WordPosition> eb){
			return ea.getKey().compareToIgnoreCase(eb.getKey());
		}
	};
	
	/**
	 * 主程序，测试
	 * @param args
	 */
	public static void main(String[] args) {
		String f="H:\\programs\\dics\\dics\\stardict-oxford-gb-formated-2.4.2\\oxford-gb-formated.idx";
		f="F:\\softs\\stardic.dic\\stardic.牛津高阶.oald\\牛津高阶英汉双解.idx";
		String fcnt="H:\\programs\\dics\\dics\\stardict-oxford-gb-formated-2.4.2\\oxford-gb-formated.dict.dz";
		fcnt="F:\\softs\\stardic.dic\\stardic.牛津高阶.oald\\牛津高阶英汉双解.dict";
		StarDictParser rdw=new StarDictParser();
		rdw.loadIndexFile(f);
		rdw.loadContentFile(fcnt);
		rdw.showWords();
		rdw.testByConsole();
	}
	
	/**
	 * test only
	 */
	public void testByConsole(){
		java.io.BufferedReader br=new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
		String s=null;
		
		try {
			s=br.readLine();
			while(s.length()>0){
				System.out.println(s);
				List<Map.Entry<String,WordPosition>> res=this.searchWord(s);
				int i=0;
				for(Map.Entry<String,WordPosition> en:res){
					System.out.println(i+++" : "+en.getKey());
				}
				s=br.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 测试，显示这些单词
	 */
	public void showWords(){
		int i=0;
		for(Map.Entry<String, WordPosition> en:words.entrySet()){
			System.out.println(en.getKey()+" :"+en.getValue().getStartPos()+" - "+en.getValue().getLength());
			if(i++%25==0){
				System.out.println(this.getWordExplanation(en.getValue().getStartPos(), en.getValue().getLength()));
			}
		}
	}
}
