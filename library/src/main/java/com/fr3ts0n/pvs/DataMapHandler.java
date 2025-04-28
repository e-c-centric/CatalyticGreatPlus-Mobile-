

package com.fr3ts0n.pvs;

import java.util.Map;

/**
 * generic Handler interface for mapped Data
 *
 * @author $Author: erwin $
 */
interface DataMapHandler
{
	/**
	 * handle a set/map of data attributes
	 *
	 * @param data Map of new data attributes to handle
	 * @return previous value of corresponding data item
	 */
	@SuppressWarnings("rawtypes")
	Object handleData(Map data);
}
