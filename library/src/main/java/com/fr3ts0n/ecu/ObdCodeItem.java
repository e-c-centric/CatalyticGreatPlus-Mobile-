

package com.fr3ts0n.ecu;

import com.fr3ts0n.prot.ProtoHeader;

/**
 * Definition of a single OBD failure code
 *
 * 
 */
public class ObdCodeItem extends EcuCodeItem
{
	/**
	 *
	 */
	private static final long serialVersionUID = -3976920283943009811L;

	/** code types */
	private static final String codeTypes = "PCBU";

	private static final int ID_CODE_TYPE = 0;
	private static final int ID_CODE_VALUE = 1;

	/**
	 * List of telegram parameters in order of appearance
	 */
	private static final int[][] TC_PARAMETERS =
	/*  START,  LEN,     PARAM-TYPE     // REMARKS */
    /* ------------------------------------------- */
		{{0, 1, ProtoHeader.PT_ALPHA},   // ID_CODE_TYPE
			{1, 4, ProtoHeader.PT_HEX},     // ID_CODE_VALUE
		};

	/** Creates a new instance of ObdCodeItem */
	public ObdCodeItem()
	{
		setKeyAttribute(FIELDS[0]);
	}

	/**
	 * Creates a new instance of ObdCodeItem
	 *
	 * @param code        String representation of DFC
	 * @param description descriptive text of DFC
	 */
	public ObdCodeItem(String code, String description)
	{
		setKeyAttribute(FIELDS[0]);
		put(FID_CODE, code);
		put(FID_DESCRIPT, description);
	}

	/**
	 * Creates a new instance of ObdCodeItem
	 *
	 * @param numericCode numeric code ID
	 * @param description descriptive text of DFC
	 */
	public ObdCodeItem(int numericCode, String description)
	{
		setKeyAttribute(FIELDS[0]);
		put(FID_CODE, getPCode(numericCode));
		put(FID_DESCRIPT, description);
	}

	/**
	 * Return numeric code representation from P/C/B/U-Code String
	 *
	 * @param pCode P/C/B/U-Code String
	 * @return numeric code value for corresponding code
	 */
	protected static int getNumericCode(String pCode)
	{
		// get code number
		int numCode = Integer.valueOf(pCode.substring(1), 16).intValue();
		int typIdx = codeTypes.indexOf(pCode.charAt(0));
		numCode |= (typIdx << 14);

		return (numCode);
	}

	/**
	 * Return P/C/B/U-Code String representation from numeric code
	 *
	 * @param numericCode numeric code value for corresponding code
	 * @return P/C/B/U-Code String
	 */
	static String getPCode(int numericCode)
	{
		char[] buffer = new char[5];
		int codeType = numericCode >> 14;
		int codeVal = numericCode & 0x3FFF;
		ProtoHeader.setParamValue(ID_CODE_TYPE, TC_PARAMETERS, buffer, codeTypes.substring(codeType, codeType + 1));
		ProtoHeader.setParamValue(ID_CODE_VALUE, TC_PARAMETERS, buffer, Integer.valueOf(codeVal));
		return (new String(buffer));
	}
}
