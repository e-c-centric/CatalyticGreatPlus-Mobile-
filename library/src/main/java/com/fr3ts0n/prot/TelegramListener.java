

package com.fr3ts0n.prot;

import java.util.EventListener;

/**
 * TelegramListener
 * Interface to handle incoming protocol Telegrams
 */
public interface TelegramListener extends EventListener
{
	/**
	 * handle incoming protocol telegram
	 *
	 * @param buffer - telegram buffer
	 * @return number of listeners notified
	 */
	int handleTelegram(char[] buffer);
}
