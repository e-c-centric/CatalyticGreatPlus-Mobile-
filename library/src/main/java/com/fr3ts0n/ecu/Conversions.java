

package com.fr3ts0n.ecu;

import com.fr3ts0n.pvs.PvLimits;

import java.text.DecimalFormat;


/**
 * Collection of all known OBD data conversions.
 * This collection implements conversions to metric and imperial system
 *
 * 
 */
public class Conversions
{

	private static final int SYSTEM_METRIC = 0;
	public static final int SYSTEM_IMPERIAL = 1;
	public static final int SYSTEM_TYPES = 2;

	// current conversion system
	private static final int cnvSystem = SYSTEM_METRIC;
	/**
	 * ID's of Conversions
	 * These ID's are used as index into table below
	 * Please take care of the order
	 */
	public static final int CNV_ID_ONETOONE = 0;
	public static final int CNV_ID_PERCENT = 1;
	public static final int CNV_ID_PERCENT_REL = 2;
	public static final int CNV_ID_PERCENT7 = 3;
	public static final int CNV_ID_PERCENT7_REL = 4;
	public static final int CNV_ID_RPM = 5;
	public static final int CNV_ID_VEHSPEED = 6;
	public static final int CNV_ID_TEMPERATURE = 7;
	public static final int CNV_ID_TEMP_WIDERANGE = 8;
	public static final int CNV_ID_AIRFLOW = 9;
	public static final int CNV_ID_PRESS = 10;
	public static final int CNV_ID_PRESS_AIR = 11;
	public static final int CNV_ID_PRESS_REL = 12;
	public static final int CNV_ID_PRESS_WIDERANGE = 13;
	public static final int CNV_ID_PRESS_VAPOR = 14;
	public static final int CNV_ID_ANGLE = 15;
	public static final int CNV_ID_VOLTAGE = 16;
	public static final int CNV_ID_VOLTAGE_HIGHRES = 17;
	public static final int CNV_ID_RATIO = 18;
	public static final int CNV_ID_RATIO_WIDERANGE = 19;
	public static final int CNV_ID_DISTANCE = 20;
	public static final int CNV_ID_HOURS = 21;
	public static final int CNV_ID_SPEED_HIGHRES = 22;
	public static final int CNV_ID_TORQUE = 23;
	public static final int CNV_ID_RATIO_RELATIVE = 24;
	public static final int CNV_ID_OBD_TYPE = 25;
	public static final int CNV_ID_OBD_CODELIST = 26;
	public static final int CNV_ID_MAX = 27;// This needs to be last entry

	private static final ObdCodeList obdCodeList = new ObdCodeList();

	private static final HashConversion cnvObdType = new HashConversion(new String[]{
		"1=OBD II",
		"2=OBD Federal EPA",
		"3=OBD and OBD II",
		"4=OBD I",
		"5=Not OBD compliant",
		"6=EOBD",
		"7=EOBD and OBD II",
		"8=EOBD and OBD",
		"9=EOBD, OBD and OBD II",
		"10=JOBD",
		"11=JOBD and OBD II",
		"12=JOBD and EOBD",
		"13=JOBD, EOBD and OBD II"
	});

	/** limits for RPM display */
	private static final PvLimits rpmLimits = new PvLimits(0.0f, 6000.0f);

	private static final Conversion[][] cnvFactors =
		{
			//                     METRIC                            ,                      IMPERIAL
			//                     FACT,  DIV, OFFS, PhOf,  UNIT     ,                      FACT,  DIV, OFFS, PhOf,  UNIT
			{new LinearConversion(1, 1, 0, 0, "-"), new LinearConversion(1, 1, 0, 0, "-")}, // OneToOne
			{new LinearConversion(100, 255, 0, 0, "%"), new LinearConversion(100, 255, 0, 0, "%")}, // Percent
			{new LinearConversion(100, 255, -128, 0, "%"), new LinearConversion(100, 255, -128, 0, "%")}, // Percent relative
			{new LinearConversion(100, 128, 0, 0, "%"), new LinearConversion(100, 128, 0, 0, "%")}, // Percent 7Bit (Fuel Trim)
			{new LinearConversion(100, 128, -128, 0, "%"), new LinearConversion(100, 128, -128, 0, "%")}, // Percent 7Bit relative)
			{new LinearConversion(1, 4, 0, 0, "/min", rpmLimits), new LinearConversion(1, 4, 0, 0, "/min", rpmLimits)}, // RPM
			{new LinearConversion(1, 1, 0, 0, "km/h"), new LinearConversion(1000, 1609, 0, 0, "mph")}, // vehicle speed
			{new LinearConversion(1, 1, -40, 0, "°C"), new LinearConversion(9, 5, -40, 32, "°F")}, // Temperature
			{new LinearConversion(1, 10, -40, 0, "°C"), new LinearConversion(9, 50, -40, 32, "°F")}, // Temperature (wide range)
			{new LinearConversion(1, 100, 0, 0, "g/s"), new LinearConversion(1, 756, 0, 0, "lb/min")}, // Air Flow
			{new LinearConversion(3, 1, 0, 0, "kPa"), new LinearConversion(4351, 10000, 0, 0, "PSI")}, // Pressure
			{new LinearConversion(1, 1, 0, 0, "kPa"), new LinearConversion(2953, 10000, 0, 0, "inHg")}, // Pressure (intake)
			{new LinearConversion(79, 1000, 0, 0, "kPa"), new LinearConversion(100, 8727, 0, 0, "PSI")}, // Pressure (relative)
			{new LinearConversion(10, 1, 0, 0, "kPa"), new LinearConversion(14504, 10000, 0, 0, "PSI")}, // Pressure (wide range)
			{new LinearConversion(1, 4, 0, 0, "Pa"), new LinearConversion(100, 99635, 0, 0, "in H2O")}, // Pressure (Vapor)
			{new LinearConversion(1, 2, -128, 0, "°"), new LinearConversion(1, 2, -128, 0, "°")}, // Angle (Timing adv)
			{new LinearConversion(1, 1000, 0, 0, "V"), new LinearConversion(1, 1000, 0, 0, "V")}, // Voltage
			{new LinearConversion(10, 81967, 0, 0, "V"), new LinearConversion(10, 81967, 0, 0, "V")}, // Voltage (high resolution)
			{new LinearConversion(100, 32768, 0, 0, "%"), new LinearConversion(100, 32768, 0, 0, "%")}, // Ratio
			{new LinearConversion(100, 256, -32768, 0, "%"), new LinearConversion(100, 256, -32768, 0, "%")}, // Ratio (wide range)
			{new LinearConversion(1, 1, 0, 0, "km"), new LinearConversion(1000, 1609, 0, 0, "miles")}, // Distance
			{new LinearConversion(1, 3600, 0, 0, "h"), new LinearConversion(1, 3600, 0, 0, "h")}, // Time (hours)
			{new LinearConversion(1, 128, 0, 0, "km/h"), new LinearConversion(100, 20595, 0, 0, "mph")}, // vehicle speed
			{new LinearConversion(1, 1, 0, 0, "Nm"), new LinearConversion(1, 1, 0, 0, "Nm")}, // Torque
			{new LinearConversion(100, 65535, 0, 0, "%"), new LinearConversion(100, 65535, 0, 0, "%")}, // Ratio relative
			{cnvObdType, cnvObdType},
			{obdCodeList, obdCodeList},
		};

	/**
	 * Creates a new instance of Conversions
	 */
	public Conversions()
	{
	}

	/**
	 * convert measurement item from storage format to physical value
	 */
	public static float memToPhys(long value, int cnvID)
	{
		return (cnvFactors[cnvID][cnvSystem].memToPhys(value).floatValue());
	}

	/**
	 * convert measurement item from storage format to physical value
	 */
	public static long physToMem(float value, int cnvID)
	{
		return (cnvFactors[cnvID][cnvSystem].physToMem(value).longValue());
	}

	/**
	 * convert measurement item from storage format to physical value
	 */
	public static String getUnits(int cnvID)
	{
		return (cnvFactors[cnvID][cnvSystem].getUnits());
	}

	/**
	 * convert measurement item from storage format to physical value
	 */
	private static String memToString(long value, int cnvID, int decimals)
	{
		return (cnvFactors[cnvID][cnvSystem].memToString(value, decimals));
	}

	/** object to be used for parsing and formatting decimal numbers */
	private static DecimalFormat decimalFormat;
	private static final DecimalFormat[] formats =
		{
			new DecimalFormat("0;-#"),
			new DecimalFormat("0.0;-#"),
			new DecimalFormat("0.00;-#"),
			new DecimalFormat("0.000;-#"),
			new DecimalFormat("0.0000;-#")
		};

	/**
	 * Format physical value to physical value string
	 *
	 * @param physVal  physical value
	 * @param cnvId    ID of conversion to be used
	 * @param decimals number of decimals for formatting
	 * @return physical value as formatted string
	 */
	public static String physToPhysFmtString(Float physVal, int cnvId, int decimals)
	{
		String result;
		if (decimals >= 0)
		{
			decimalFormat = formats[decimals];
			result = decimalFormat.format(physVal);
		} else
		{
			result = Conversions.memToString(physVal.longValue(), cnvId, decimals);
		}
		return (result);
	}
}
