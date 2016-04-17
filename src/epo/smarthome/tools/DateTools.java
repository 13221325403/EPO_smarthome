package epo.smarthome.tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;

/**
 * ���ڹ�����
 * 
 * @author Administrator
 * 
 */
public class DateTools {
	/**
	 * �õ���ǰʱ�� "yyyy-MM-dd HH:mm:ss"
	 * 
	 * @return
	 */
	public static String getNowTimeString() {
		// String result = "";
		// Date now = new Date();
		// Calendar cal = Calendar.getInstance();
		//
		// DateFormat d1 = DateFormat.getDateInstance();
		// //Ĭ�����ԣ�����µ�Ĭ�Ϸ��MEDIUM��񣬱��磺2008-6-16 20:54:53��
		// result = d1.format(now);
		// SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	}

	/**
	 * ��ǰʱ�䵽strTime��ʱ��� ����
	 * 
	 * @param strTime
	 *            2004-03-26 13:31:40
	 * @return
	 */
	public static long getNowTimeByLastTimeDifference(String strTime) {
		long timeVal = -1;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		try {
			Date d1 = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
			Date d2 = df.parse(strTime);
			timeVal = d1.getTime() - d2.getTime();
			timeVal /= 1000;
			System.out.println("newTime:" + d1.toString() + "  d2:"
					+ d2.toString() + "   strTime:" + strTime + "    val:"
					+ timeVal);
		} catch (Exception e) {
		}

		return timeVal;
	}

}
