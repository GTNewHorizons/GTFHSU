package eu.usrv.gtsu;

import java.text.DecimalFormat;

public class NumberPrettifier {
	public static String getPrettifiedNumber(long pNumber)
	{
		if (pNumber < 1)
			return "0 eU";
		
		String[] tUnits = {"eU", "kilo eU", "mega eU", "giga eU", "tera eU", "peta eU", "exa eU"};
		String tResultStr = "";
		float tResult = 0;
		DecimalFormat df = new DecimalFormat("0.00");
		
		int tPowerVal = 0;
		int tArrayRef = 6;
		for (int i = 18; i >= 3; i-=3)
		{
			if (pNumber > Math.pow(10, i))
			{
				tPowerVal = i;
				break;
			}
			else
				tArrayRef--;
		}
		
		if (tPowerVal > 0)
			tResultStr = String.format("%s %s", df.format((float) (pNumber / Math.pow(10, tPowerVal))), tUnits[tArrayRef]);
		else
			tResultStr = String.format("%s %s", pNumber, tUnits[tArrayRef]);
		
		return tResultStr;
	}
}
