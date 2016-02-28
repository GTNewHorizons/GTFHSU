package eu.usrv.gtsu;


public class TierHelper
{
	public static int[] V = {128, 512, 2048, 8192, 32768, 131072, 524288, 2097152};
	public static String[] VS = {"MV", "HV", "EV", "IV", "LuV", "ZPM", "UV", "MAX"};
	
	public static long getMaxStorageForTier(int pTier)
	{
		long result = 0;
		if (pTier >= V.length - 1)
			result = Long.MAX_VALUE-1;
		else
			result = (long)((long)V[pTier] * (long)((long)V[pTier] * (long)V[pTier]));

		return result;
	}
}
