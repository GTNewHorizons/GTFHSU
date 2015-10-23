package eu.usrv.gtsu.helper;

public class EnergySystemConverter
{
	public static enum PowerSystem
	{ // Todo: make this dynamic, so we can add more systems later on
		GT5(5),
		RF(20),
		IC2(5),
		Joule(50);
		
		private int _mRatio;
		private PowerSystem(int pConversionRatio)
		{
			_mRatio = pConversionRatio;
		}
		
		public int getRatio()
		{
			return _mRatio;
		}
		
		/**
		 * Get the current Value of our own EnergyUnits of given type
		 * @param pValue
		 * @return
		 */
		public long getEnergyUnits(long pValue)
		{
			return new Double(Math.ceil((double)pValue / (double)_mRatio)).longValue();
		}
		
		/** 
		 * Convert PowerUnits of *this* type to pTarget type 
		 * @param pTarget
		 * @param pAmount
		 * @return
		 */
		public long convertTo(PowerSystem pTarget, long pAmount)
		{
			double tBaseEnergy = (double)pAmount / (double)_mRatio;
			tBaseEnergy = Math.ceil(tBaseEnergy);
			return new Double(tBaseEnergy * pTarget.getRatio()).longValue();
		}
	}

}
