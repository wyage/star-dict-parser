package org.yage.dict.star;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * byte[] functionality
 * @author olaf@merkert.de
 */
public class ByteArrayHelper {

	public static long toLong(byte[] in) {
		long out = 0;
		for (int i = in.length - 1; i > 0; i--) {
			out |= in[i] & 0xff;
			out <<= 8;
		}
		out |= in[0] & 0xff;
		return out;
	}

	/**
	 * 按JAVA的小端顺序来
	 * @param in
	 * @return
	 */
	public static int toInt(byte[] in) {
		int out = 0;
		for (int i = in.length - 1; i > 0; i--) {
			out |= in[i] & 0xff;
			out <<= 8;
		}
		out |= in[0] & 0xff;
		return out;
	}

	/**
	 * 转成int，按大端序号来
	 * @param in
	 * @return
	 */
	public static int toIntAsBig(byte[] in) {
		int out = 0;
		for (int i = 0; i < in.length - 1; i++) {
			out |= in[i] & 0xff;
			out <<= 8;
		}
		out |= in[in.length - 1] & 0xff;
		return out;
	}
	
	/**
	 * 转成int，按大端序号来
	 * @param in
	 * @return
	 */
	public static int toIntAsBig2(byte[] buf){
		int out=Integer.MAX_VALUE;
		out&=0x000000ff&buf[3];
		out|=0x0000ff00&(int)(buf[2])<<8;
		out|=0x00ff0000&(int)(buf[1])<<16;
		out|=0xff000000&(int)(buf[0])<<24;
		return out;
	}
	
	/**
	 * 转成int，按大端序号来
	 * @param in
	 * @return
	 */
	public static int toIntAsBig3(byte[] buf){
		int out=0;
		out|=0x000000ff&buf[3];
		out|=0x0000ff00&(int)(buf[2])<<8;
		out|=0x00ff0000&(int)(buf[1])<<16;
		out|=0xff000000&(int)(buf[0])<<24;
		return out;
	}

	public static short toShort(byte[] in) {
		short out = 0;
		for (int i = in.length - 1; i > 0; i--) {
			out |= in[i] & 0xff;
			out <<= 8;
		}
		out |= in[0] & 0xff;
		return out;
	}

	public static byte[] toByteArray(int in) {
		byte[] out = new byte[4];

		out[0] = (byte) in;
		out[1] = (byte) (in >> 8);
		out[2] = (byte) (in >> 16);
		out[3] = (byte) (in >> 24);

		return out;
	}

	public static byte[] toByteArray(int in, int outSize) {
		byte[] out = new byte[outSize];
		byte[] intArray = toByteArray(in);
		for (int i = 0; i < intArray.length && i < outSize; i++) {
			out[i] = intArray[i];
		}
		return out;
	}

	public static String toString(byte[] theByteArray) {
		StringBuffer out = new StringBuffer();
		for (int i = 0; i < theByteArray.length; i++) {
			String s = Integer.toHexString(theByteArray[i] & 0xff);
			if (s.length() < 2) {
				out.append('0');
			}
			out.append(s).append(' ');
		}
		return out.toString();
	}

	public static boolean isEqual(byte[] first, byte[] second) {
		boolean out = first != null && second != null
				&& first.length == second.length;
		for (int i = 0; out && i < first.length; i++) {
			if (first[i] != second[i]) {
				out = false;
			}
		}
		return out;
	}

	public static int toInt(char[] cs) {
		return toInt(toByteArray(cs));
	}

	public static byte[] toByteArray(char[] cs) {
		byte[] res = new byte[cs.length];
		int i = 0;
		for (char c : cs) {
			res[i++] = (byte) c;
		}
		return res;
	}

	public static char[] toCharArray(byte[] cs) {
		char[] res = new char[cs.length];
		int i = 0;
		for (byte c : cs) {
			res[i++] = (char) c;
		}
		return res;
	}

	/**
	 * test only
	 * @param args
	 */
	public static void main(String args[]) {
		testa();
		testb();
	}
	
	private static void testb() {
		byte[] bs = toByteArray(Integer.MAX_VALUE);
		for (byte b:bs) {
			System.out.print(b + ",");
		}
	}
	
	private static void testa() {
		try {
			FileOutputStream fos=new FileOutputStream("a.txt");
			fos.write(toByteArray(4577623),0, 4);
			fos.close();
			
			byte res[] = new byte[4];
			FileInputStream fis=new FileInputStream("a.txt");
			fis.read(res);
			fis.close();
			System.out.println(toInt(res));
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
