package eu.usrv.gtsu.helper;


import java.util.ArrayList;

import org.apache.logging.log4j.Level;

import cpw.mods.fml.common.FMLLog;

/**
 * Generic LogHelper to print stuff to the console
 * @author Namikon
 */
public final class LogHelper {
	private ArrayList<String> _mReportedCategories = new ArrayList<String>();
	private boolean doDebugLogs = false;
	private boolean doTraceLogs = false;
	private String _mModID = "";

	private final static String STR_NOCAT = "ihaznocathegory";
	private final static String STR_TOKEN_ONETIMEMESSAGE = " OTM";

	public LogHelper(String pModID)
	{
		_mModID = pModID;
	}

	/**
	 * Enable/Disable debug logs
	 * @param pEnabled
	 */
	public void setDebugOutput(boolean pEnabled)
	{
		doDebugLogs = pEnabled;
	}

	/**
	 * Enable/Disable trace logs
	 * @param pEnabled
	 */
	public void setTraceOutput(boolean pEnabled)
	{
		doTraceLogs = pEnabled;
	}	

	public void ResetCategories()
	{
		_mReportedCategories = new ArrayList<String>();
	}

	public void log(String pCategory, Level pLogLevel, String pMessage, Object... args)
	{
		if (pLogLevel == Level.DEBUG && !doDebugLogs)
			return;

		if (pLogLevel == Level.TRACE && !doTraceLogs)
			return;

		String tt = "";
		if (!pCategory.equals(STR_NOCAT))
		{
			tt = STR_TOKEN_ONETIMEMESSAGE;
			if (_mReportedCategories.contains(pCategory))
				return;
			else
			{
				_mReportedCategories.add(pCategory);
			}	
		}

		FMLLog.log(_mModID.toUpperCase() + tt, pLogLevel, pMessage, args);
	}


	public void ot_all(String pCategory, String object, Object... args)
	{
		log(pCategory, Level.ALL, object, args);
	}

	public void ot_debug(String pCategory, String object, Object... args)
	{
		log(pCategory, Level.DEBUG, object, args);
	}

	public void ot_error(String pCategory, String object, Object... args)
	{
		log(pCategory, Level.ERROR, object, args);
	}

	public void ot_fatal(String pCategory, String object, Object... args)
	{
		log(pCategory, Level.FATAL, object, args);
	}

	public void ot_info(String pCategory, String object, Object... args)
	{
		log(pCategory, Level.INFO, object, args);
	}

	public void ot_off(String pCategory, String object, Object... args)
	{
		log(pCategory, Level.OFF, object, args);
	}

	public void ot_trace(String pCategory, String object, Object... args)
	{
		log(pCategory, Level.TRACE, object, args);
	}

	public void ot_warn(String pCategory, String object, Object... args)
	{
		log(pCategory, Level.WARN, object, args);
	}

	public void all(String object, Object... args)
	{
		log(STR_NOCAT, Level.ALL, object, args);
	}

	public void debug(String object, Object... args)
	{
		log(STR_NOCAT, Level.DEBUG, object, args);
	}

	public void error(String object, Object... args)
	{
		log(STR_NOCAT, Level.ERROR, object, args);
	}

	public void fatal(String object, Object... args)
	{
		log(STR_NOCAT, Level.FATAL, object, args);
	}

	public void info(String object, Object... args)
	{
		log(STR_NOCAT, Level.INFO, object, args);
	}

	public void off(String object, Object... args)
	{
		log(STR_NOCAT, Level.OFF, object, args);
	}

	public void trace(String object, Object... args)
	{
		log(STR_NOCAT, Level.TRACE, object, args);
	}

	public void warn(String object, Object... args)
	{
		log(STR_NOCAT, Level.WARN, object, args);
	}
}

