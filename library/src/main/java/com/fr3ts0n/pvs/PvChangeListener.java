

package com.fr3ts0n.pvs;

import java.util.EventListener;

/**
 * Listener interface for notification of process variable changes
 *
 * @author $Author: erwin $
 */
public interface PvChangeListener extends EventListener
{

	/**
	 * handler for process variable changes
	 */
	void pvChanged(PvChangeEvent event);

}
