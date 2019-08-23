package org.iot.mqtt.test.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.slf4j.Logger;

public class ByteUtil {
	/**
	 * 
	 * 从指定数组的copy一个子数组并返回
	 *
	 * 
	 * 
	 * @param org
	 *            of type byte[] 原数组
	 * 
	 * @param append
	 *            合并一个byte[]
	 * 
	 * @return 合并的数据
	 * 
	 */

	public static byte[] appendBytes(byte[] org, byte[] append) {

		byte[] newByte = new byte[org.length + append.length];

		System.arraycopy(org, 0, newByte, 0, org.length);

		System.arraycopy(append, 0, newByte, org.length, append.length);

		return newByte;

	}

	/**
	 * 获取高四位
	 */
	public static int getHeight4(byte data) {// 获取高四位
		int height;
		height = ((data & 0xf0) >> 4);
		return height;
	}

	/**
	 * 获取低四位
	 */
	public static int getLow4(byte data) {// 获取低四位
		int low;
		low = (data & 0x0f);
		return low;
	}

	/**
	 * 从一个byte[]数组中截取一部分
	 * 
	 * @param src
	 * @param begin
	 * @param count
	 * @return
	 */
	public static byte[] subBytes(byte[] src, int begin, int count) {
		byte[] b = new byte[count];
		for (int i = begin; i < begin + count; i++){
			b[i - begin] = src[i];
		}
		return b;
	}
	
	public static byte[] fillFrontBytes(byte[] src, int size) {
		byte[] b = new byte[size];
		int j = 1;
		for (int i = src.length-1; i >= 0; i--){
			b[b.length-j] = src[i];
			j++;			
		}
		return b;
	}
	
	public static int fillFrontBytesToInt(byte[] src, int begin, int count) {
		return ByteUtil.bytesToInt(ByteUtil.fillFrontBytes(ByteUtil.subBytes(src, begin, count),4));
	}
	
	public static long fillFrontBytesToLong(byte[] src, int begin, int count) {
		return ByteUtil.bytesToLong(ByteUtil.fillFrontBytes(ByteUtil.subBytes(src, begin, count),8));
	}
	
	public static float fillFrontBytesToFloat(byte[] src, int begin, int count) {
		return ByteUtil.bytesToFloat(ByteUtil.fillFrontBytes(ByteUtil.subBytes(src, begin, count),4));
	}
	
	public static byte[] cutFrontBytes(byte[] src, int size) {
		byte[] b = new byte[size];
		int j = 1;
		for (int i = size-1; i >= 0; i--){
			b[i] = src[src.length-j];
			j++;
		}
		return b;
	}
	
	public static void printBytes(byte[] src) {
		for (int i = 0; i < src.length; i++){
			System.out.print(src[i]+(i<(src.length-1)?",":""));
		}
		System.out.println();
	}	
	
	public static void logBytes(byte[] src,Logger logger) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < src.length; i++){
			sb.append(src[i]+(i<(src.length-1)?",":""));
		}
		logger.info(sb.toString());
	}
	/**
	 * @功能: BCD码转为10进制串(阿拉伯数据)
	 * @参数: BCD码
	 * @结果: 10进制串
	 */
	public static String bcd2Str(byte[] bytes) {
		StringBuffer temp = new StringBuffer(bytes.length * 2);
		for (int i = 0; i < bytes.length; i++) {
			temp.append((byte) ((bytes[i] & 0xf0) >>> 4));
			temp.append((byte) (bytes[i] & 0x0f));
		}
		return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp.toString().substring(1) : temp.toString();
	}

	/**
	 * @功能: 10进制串转为BCD码
	 * @参数: 10进制串
	 * @结果: BCD码
	 */
	public static byte[] str2Bcd(String asc) {
		int len = asc.length();
		int mod = len % 2;
		if (mod != 0) {
			asc = "0" + asc;
			len = asc.length();
		}
		byte abt[] = new byte[len];
		if (len >= 2) {
			len = len / 2;
		}
		byte bbt[] = new byte[len];
		abt = asc.getBytes();
		int j, k;
		for (int p = 0; p < asc.length() / 2; p++) {
			if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
				j = abt[2 * p] - '0';
			} else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
				j = abt[2 * p] - 'a' + 0x0a;
			} else {
				j = abt[2 * p] - 'A' + 0x0a;
			}
			if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
				k = abt[2 * p + 1] - '0';
			} else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
				k = abt[2 * p + 1] - 'a' + 0x0a;
			} else {
				k = abt[2 * p + 1] - 'A' + 0x0a;
			}
			int a = (j << 4) + k;
			byte b = (byte) a;
			bbt[p] = b;
		}
		return bbt;
	}

	// byte 与 int 的相互转换
	public static byte intToByte(int x) {
		return (byte) x;
	}

	public static int byteToInt(byte b) {
		// Java 总是把 byte 当做有符处理；我们可以通过将其和 0xFF 进行二进制与得到它的无符值
		return b & 0xFF;
	}
	
	public static byte[] short2Bytes(short x) {
		byte high = (byte)(0x00FF & (x>>8));
		byte low = (byte)(0x00FF & x);
		return new byte[]{high,low};
	}
	
	public static short byte2short(byte[] bytes){
		byte high = bytes[0];
		byte low = bytes[1];
		return (short)(((high & 0x00FF) << 8) | (0x00FF & low));
	}

	// byte 数组与 int 的相互转换
	public static int bytesToInt(byte[] b) {
		return b[3] & 0xFF | (b[2] & 0xFF) << 8 | (b[1] & 0xFF) << 16 | (b[0] & 0xFF) << 24;
	}
	
	public static int bytesLEToInt(byte[] b) {
		return b[0] & 0xFF | (b[1] & 0xFF) << 8 | (b[2] & 0xFF) << 16 | (b[3] & 0xFF) << 24;
	}

	public static byte[] intTobytes(int a) {
		return new byte[] { (byte) ((a >> 24) & 0xFF), (byte) ((a >> 16) & 0xFF), (byte) ((a >> 8) & 0xFF),
				(byte) (a & 0xFF) };
	}

	// byte 数组与 long 的相互转换
	public static byte[] longTobytes(long x) {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.putLong(0, x);
		return buffer.array();
	}

	public static long bytesToLong(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.put(bytes, 0, bytes.length);
		buffer.flip();// need flip
		return buffer.getLong();
	}
	
	public static long bytesLEToLong(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.wrap(bytes,0,8);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getLong(); 
	}

	public static byte[] intToBytesLE(int i) {
		ByteBuffer buffer= ByteBuffer.allocate(4);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.asIntBuffer().put(i);
		return buffer.array();
	}
	public static byte[] longToBytesLE(long l) {
		ByteBuffer buffer= ByteBuffer.allocate(8);
	    buffer.order(ByteOrder.LITTLE_ENDIAN);
	    buffer.asLongBuffer().put(l);
	    return buffer.array();

	}
	
	public static float bytesToFloat(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.put(bytes, 0, bytes.length);
		buffer.flip();// need flip
		return buffer.getFloat();
	}
	
	public static byte[] floatTobytes(float f) {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.putFloat(0,f);
		return buffer.array();
	}

	/**
	 * 字符串转换成十六进制字符串
	 * 
	 * @param String
	 *            str 待转换的ASCII字符串
	 * @return String 每个Byte之间空格分隔，如: [61 6C 6B]
	 */
	public static String str2HexStr(String str) {

		char[] chars = "0123456789ABCDEF".toCharArray();
		StringBuilder sb = new StringBuilder("");
		byte[] bs = str.getBytes();
		int bit;

		for (int i = 0; i < bs.length; i++) {
			bit = (bs[i] & 0x0f0) >> 4;
			sb.append(chars[bit]);
			bit = bs[i] & 0x0f;
			sb.append(chars[bit]);
			sb.append(' ');
		}
		return sb.toString().trim();
	}

	/**
	 * 十六进制转换字符串
	 * 
	 * @param String
	 *            str Byte字符串(Byte之间无分隔符 如:[616C6B])
	 * @return String 对应的字符串
	 */
	public static String hexStr2Str(String hexStr) {
		String str = "0123456789ABCDEF";
		char[] hexs = hexStr.toCharArray();
		byte[] bytes = new byte[hexStr.length() / 2];
		int n;

		for (int i = 0; i < bytes.length; i++) {
			n = str.indexOf(hexs[2 * i]) * 16;
			n += str.indexOf(hexs[2 * i + 1]);
			bytes[i] = (byte) (n & 0xff);
		}
		return new String(bytes);
	}
	
	/**
	 * byte转换字符串
	 * 
	 * @param byte[]
	 * @return String 对应的字符串
	 */
	public static String byte2Str(byte[] b) {
		return ByteUtil.hexStr2Str(ByteUtil.byte2HexStr(b));
	}

	/**
	 * bytes转换成十六进制字符串
	 * 
	 * @param byte[]
	 *            b byte数组
	 * @return String 每个Byte值之间空格分隔
	 */
	public static String byte2HexStr(byte[] b) {
		String stmp = "";
		StringBuilder sb = new StringBuilder("");
		for (int n = 0; n < b.length; n++) {
			stmp = Integer.toHexString(b[n] & 0xFF);
			sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
			// sb.append(" ");
		}
		return sb.toString().toUpperCase().trim();
	}
	/**
	 * bytes转换成十六进制字符串
	 * 
	 * @param byte[]
	 *            b byte数组
	 * @return String 每个Byte值之间空格分隔
	 */
	public static String byte2SplitStr(byte[] b) {
		StringBuilder sb = new StringBuilder("");
		for (int n = 0; n < b.length; n++) {
			sb.append(b[n]);
			sb.append(",");
		}
		return sb.toString().substring(0, sb.length()-1);
	}
	/**
	 *  hexStr字符串转换为Byte值
	 * 
	 * @param String
	 *            src Byte字符串，每个Byte之间没有分隔符
	 * @return byte[]
	 */
	public static byte[] hexStr2Bytes(String src) {
		int len = src.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(src.substring(2 * i, 2 * i + 2),
                    16).byteValue();
        return result;
	}

	/**
	 * String的字符串转换成unicode的String
	 * 
	 * @param String
	 *            strText 全角字符串
	 * @return String 每个unicode之间无分隔符
	 * @throws Exception
	 */
	public static String strToUnicode(String strText) throws Exception {
		char c;
		StringBuilder str = new StringBuilder();
		int intAsc;
		String strHex;
		for (int i = 0; i < strText.length(); i++) {
			c = strText.charAt(i);
			intAsc = (int) c;
			strHex = Integer.toHexString(intAsc);
			if (intAsc > 128)
				str.append("\\u" + strHex);
			else
				// 低位在前面补00
				str.append("\\u00" + strHex);
		}
		return str.toString();
	}

	/**
	 * unicode的String转换成String的字符串
	 * 
	 * @param String
	 *            hex 16进制值字符串 （一个unicode为2byte）
	 * @return String 全角字符串
	 */
	public static String unicodeToString(String hex) {
		int t = hex.length() / 6;
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < t; i++) {
			String s = hex.substring(i * 6, (i + 1) * 6);
			// 高位需要补上00再转
			String s1 = s.substring(2, 4) + "00";
			// 低位直接转
			String s2 = s.substring(4);
			// 将16进制的string转为int
			int n = Integer.valueOf(s1, 16) + Integer.valueOf(s2, 16);
			// 将int转换为字符
			char[] chars = Character.toChars(n);
			str.append(new String(chars));
		}
		return str.toString();
	}
	
	public static byte[] toBigEndian(byte[] b) { 
		ByteBuffer buffer= ByteBuffer.allocate(b.length);
	    buffer.order(ByteOrder.LITTLE_ENDIAN);
	    buffer.put(b);
	    return buffer.array();
	}
	public static void main(String[] args) {
		byte[] b = new byte[] {1,2,4};
		cutFrontBytes(b, 2);
	}
}
