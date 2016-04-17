package epo.smarthome.device;

public class LedLight extends Dev {
	
	private int lampRVal;				//  ��ɫ����
	private int lampGVal;
	private int lampBVal;
	
	private double temp = 0;  			//   ů�����
	private boolean lightState = false;  //  �ƹ⿪��ָʾ

	public int getLampRVal() {
		return lampRVal;
	}

	public void setLampRVal(int lampRVal) {
		if (lampRVal < 0) {
			lampRVal = 0;
		}
		if (lampRVal > 99) {
			lampRVal = 99;
		}
		this.lampRVal = lampRVal;
		isDataChange(true);
	}

	public int getLampGVal() {
		return lampGVal;
	}

	public void setLampGVal(int lampGVal) {
		if (lampGVal < 0) {
			lampGVal = 0;
		}
		if (lampGVal > 99) {
			lampGVal = 99;
		}
		this.lampGVal = lampGVal;
		isDataChange(true);
	}

	public int getLampBVal() {
		return lampBVal;
	}

	public void setLampBVal(int lampBVal) {
		if (lampBVal < 0) {
			lampBVal = 0;
		}
		if (lampBVal > 99) {
			lampBVal = 99;
		}
		this.lampBVal = lampBVal;
		isDataChange(true);
	}


	public double getTemp() {
		return temp;
	}

	public void setTemp(double temp) {
		this.temp = temp;
		isDataChange(true);
	}

	public boolean isLightState() {
		return lightState;
	}

	public void setLightState(boolean lightState) {
		this.lightState = lightState;
		isDataChange(true);
	}



}
