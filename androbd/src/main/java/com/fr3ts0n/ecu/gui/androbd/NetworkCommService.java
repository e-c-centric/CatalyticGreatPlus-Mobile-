package com.fr3ts0n.ecu.gui.androbd;

import android.content.Context;
import android.os.Handler;

import com.fr3ts0n.prot.StreamHandler;

import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Network communication service to allow connection to WIFI OBD adapters
 */
public class NetworkCommService
	extends CommService
	implements Runnable
{
	private Socket mSocket;
	/** communication stream handler */
	private final StreamHandler ser = new StreamHandler();
	private Thread serThread;

	/** default constructor */
	public NetworkCommService()
	{
		super();
	}

	/**
	 * Constructor. Prepares a new Network Communication session.
	 *
	 * @param context The UI Activity Context
	 * @param handler A Handler to send messages back to the UI Activity
	 */
	public NetworkCommService(Context context, Handler handler)
	{
		super(context, handler);
		ser.setMessageHandler(elm);
	}

	@Override
	public void start()
	{
		log.fine("start");
		// set up protocol handlers
		elm.addTelegramWriter(ser);
		// create communication thread
		serThread = new Thread(this);
		serThread.start();
	}

	@Override
	public void stop()
	{
		log.fine("stop");
		elm.removeTelegramWriter(ser);
		// close socket
		try
		{
			mSocket.close();
		} catch (Exception e)
		{
			log.severe(e.getMessage());
		}
		setState(STATE.OFFLINE);
	}

	@Override
	public void write(byte[] out)
	{
		// forward message to stream handler
		ser.writeTelegram(new String(out).toCharArray());
	}

	@Override
	public void run()
	{
		// run communication loop
		ser.run();
		// loop was finished -> we have lost connection
		connectionLost();
	}

	/**
	 * Thread for connecting network device
	 * * required to eliminate android.os.NetworkOnMainThreadException
	 */
	class ConnectThread extends Thread
	{
		final CommService svc;
		final String device;
		int portNum;

		ConnectThread(CommService svc, String device, int portNum)
		{
			this.svc = svc;
			this.device = device;
			this.portNum  = portNum;
		}

		@Override
		public void run()
		{
			log.info(String.format("Connecting to %s port %d", device, portNum));
			setState(STATE.CONNECTING);
			try
			{
				// create socket connection
				mSocket = new Socket();
				InetSocketAddress addr = new InetSocketAddress(device, portNum);
				mSocket.connect(addr);
				// set streams for stream handler
				ser.setStreams(mSocket.getInputStream(), mSocket.getOutputStream());
				// we are connected -> signal connection established
				connectionEstablished(device);

				// start communication service thread
				svc.start();
			} catch (Exception e)
			{
				log.severe(e.getMessage());
				connectionFailed();
			}
		}
	}

	/**
	 * Open network connection to device using specified port
	 * @param device address of device
	 * @param portNum port to connect to
	 */
	public void connect(Object device, int portNum)
	{
		new ConnectThread(this, String.valueOf(device), portNum).start();
	}

	@Override
	public void connect(Object device, boolean secure)
	{
		connect(device, secure ? 23 : 22);
	}
}
