package com.fr3ts0n.ecu.gui.androbd;

//import bufferedReader.BufferedReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.fr3ts0n.ecu.prot.obd.ElmProt;
import com.fr3ts0n.ecu.prot.obd.ObdProt;
import com.fr3ts0n.pvs.PvList;
import com.fr3ts0n.ecu.gui.androbd.SessionManager;

import com.fr3ts0n.ecu.EcuDataPv; // Ensure this class is in your project

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.FileWriter;
import java.io.FileInputStream;
import com.fr3ts0n.ecu.ObdVidItem;
import com.fr3ts0n.ecu.gui.androbd.MainActivity;

/**
 * Task to save measurements
 *
 */
class FileHelper {
	/** Date Formatter used to generate file name */
	@SuppressLint("SimpleDateFormat")
	private static final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss");
	private static ProgressDialog progress;

	private static final Logger log = Logger.getLogger(FileHelper.class.getName());

	private final Context context;
	private final ElmProt elm;

	/**
	 * Initialize static data for static calls
	 * 
	 * @param context APP context
	 *
	 */
	FileHelper(Context context) {
		this.context = context;
		this.elm = CommService.elm;
	}

	/**
	 * get default path for load/store operation
	 * * path is based on configured <user data location>/<package name>
	 *
	 * @return default path for current app context
	 */
	static String getPath(Context context) {
		// generate file name
		return Environment.getExternalStorageDirectory()
				+ File.separator
				+ context.getPackageName();
	}

	/**
	 * get filename (w/o extension) based on current date & time
	 *
	 * @return file name
	 */
	static String getFileName() {
		return dateFmt.format(System.currentTimeMillis());
	}

	/**
	 * Save all data in a independent thread
	 */
	void saveDataThreaded() {
		// Generate file paths for .obd and .csv
		final String mPath = getPath(context);
		final String obdFileName = mPath + File.separator + getFileName() + ".obd";
		final String csvFileName = mPath + File.separator + getFileName() + ".csv";
		final String remoteUrl = "http://137.184.18.27/upload.php"; // Replace with your actual URL

		// Create progress dialog
		progress = ProgressDialog.show(context,
				context.getString(R.string.saving_data),
				obdFileName + " and " + csvFileName,
				true);

		Thread saveTask = new Thread() {
			public void run() {
				Looper.prepare();
				saveData(mPath, obdFileName); // Save as .obd
				saveDataAsCsv(mPath, csvFileName); // Save as .csv
				sendDataToRemoteUrl(csvFileName, remoteUrl); // Send to remote URL
				progress.dismiss();
				Looper.loop();
			}
		};
		saveTask.start();
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	private synchronized void saveDataAsCsv(String mPath, String mFileName) {
		File outFile;

		// Ensure the path is created
		new File(mPath).mkdirs();
		outFile = new File(mFileName);

		try (FileWriter writer = new FileWriter(outFile)) {
			// Write CSV header
			writer.append("PID,Description,Value,Units\n");

			// Write data for PIDs
			for (Object key : ObdProt.PidPvs.keySet()) {
				EcuDataPv pv = (EcuDataPv) ObdProt.PidPvs.get(key);
				writer.append(String.format("%s,%s,%s,%s\n",
						pv.get(EcuDataPv.FID_PID),
						pv.get(EcuDataPv.FID_DESCRIPT),
						pv.get(EcuDataPv.FID_VALUE),
						pv.get(EcuDataPv.FID_UNITS)));
			}

			// Get VIN/license plate from preferences
			SessionManager sessionManager = new SessionManager(context);
			String vinValue = context.getSharedPreferences("AppSession", Context.MODE_PRIVATE)
					.getString("CurrentVehicleVin", "");

			// Write the VIN to the CSV
			writer.append(String.format("VIN,Vehicle identification number,%s,\n", vinValue));

			// Add User ID as its own row
			int userId = sessionManager.getUserId();
			writer.append(String.format("USER_ID,Logged in user ID,%d,\n", userId));

			// Show success message
			String msg = String.format("%s %d Bytes to %s",
					context.getString(R.string.saved),
					outFile.length(),
					mPath);
			log.info(msg);
			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	/**
	 * Save all data
	 */
	@SuppressWarnings("ResultOfMethodCallIgnored")
	private synchronized void saveData(String mPath, String mFileName) {
		File outFile;

		// ensure the path is created
		// noinspection ResultOfMethodCallIgnored
		new File(mPath).mkdirs();
		outFile = new File(mFileName);

		// prevent data updates for saving period
		ObdItemAdapter.allowDataUpdates = false;

		try {
			outFile.createNewFile();
			FileOutputStream fStr = new FileOutputStream(outFile);
			ObjectOutputStream oStr = new ObjectOutputStream(fStr);
			oStr.writeInt(elm.getService());
			oStr.writeObject(ObdProt.PidPvs);
			oStr.writeObject(ObdProt.VidPvs);
			oStr.writeObject(ObdProt.tCodes);
			oStr.writeObject(MainActivity.mPluginPvs);

			oStr.close();
			fStr.close();

			@SuppressLint("DefaultLocale")
			String msg = String.format("%s %d Bytes to %s",
					context.getString(R.string.saved),
					outFile.length(),
					mPath);
			log.info(msg);
			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

		// we are done saving, allow data updates again
		ObdItemAdapter.allowDataUpdates = true;
	}

	private void sendDataToRemoteUrl(String csvFileName, String remoteUrl) {
		Thread sendTask = new Thread(() -> {
			try {
				File csvFile = new File(csvFileName);
				if (!csvFile.exists()) {
					log.warning("CSV file does not exist: " + csvFileName);
					return;
				}

				// Prepare HTTP connection
				HttpURLConnection connection = (HttpURLConnection) new URL(remoteUrl).openConnection();
				connection.setRequestMethod("POST");
				connection.setDoOutput(true);
				connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundary");

				String boundary = "----WebKitFormBoundary";
				String lineEnd = "\r\n";
				String twoHyphens = "--";

				try (OutputStream os = connection.getOutputStream()) {
					// Write form data headers
					os.write((twoHyphens + boundary + lineEnd).getBytes());
					os.write(("Content-Disposition: form-data; name=\"csv_file\"; filename=\"" + csvFile.getName()
							+ "\"" + lineEnd).getBytes());
					os.write(("Content-Type: text/csv" + lineEnd).getBytes());
					os.write(lineEnd.getBytes());

					// Write file content
					try (FileInputStream fis = new FileInputStream(csvFile)) {
						byte[] buffer = new byte[1024];
						int bytesRead;
						while ((bytesRead = fis.read(buffer)) != -1) {
							os.write(buffer, 0, bytesRead);
						}
					}

					// Write form data footer
					os.write(lineEnd.getBytes());
					os.write((twoHyphens + boundary + twoHyphens + lineEnd).getBytes());
				}

				// Get response
				int responseCode = connection.getResponseCode();
				if (responseCode == HttpURLConnection.HTTP_OK) {
					try (InputStream is = connection.getInputStream()) {
						String response = new String(is.readAllBytes());
						log.info("Server response: " + response);
					}
				} else {
					log.warning("Failed to send data. Response code: " + responseCode);
				}
			} catch (Exception e) {
				log.log(Level.SEVERE, "Error sending data to remote URL", e);
			}
		});
		sendTask.start();
	}

	/**
	 * Load all data in a independent thread
	 * 
	 * @param uri Uri of ile to be loaded
	 */
	synchronized void loadDataThreaded(final Uri uri,
			final Handler reportTo) {
		// create progress dialog
		progress = ProgressDialog.show(context,
				context.getString(R.string.loading_data),
				uri.getPath(),
				true);

		Thread loadTask = new Thread() {
			public void run() {
				Looper.prepare();
				loadData(uri);
				progress.dismiss();
				reportTo.sendMessage(reportTo.obtainMessage(MainActivity.MESSAGE_FILE_READ));
				Looper.loop();
			}
		};
		loadTask.start();
	}

	/**
	 * Load data from file into data structures
	 *
	 * @param uri URI of file to be loaded
	 */
	@SuppressLint("DefaultLocale")
	@SuppressWarnings("UnusedReturnValue")
	private synchronized int loadData(final Uri uri) {
		int numBytesLoaded = 0;
		String msg;
		InputStream inStr;

		try {
			inStr = context.getContentResolver().openInputStream(uri);
			numBytesLoaded = inStr != null ? inStr.available() : 0;
			msg = context.getString(R.string.loaded).concat(String.format(" %d Bytes", numBytesLoaded));
			ObjectInputStream oIn = new ObjectInputStream(inStr);
			/*
			 * ensure that measurement page is activated
			 * to avoid deletion of loaded data afterwards
			 */
			int currService = oIn.readInt();
			/* if data was saved in mode 0, keep current mode */
			if (currService != 0)
				elm.setService(currService, false);
			/* read in the data */
			ObdProt.PidPvs = (PvList) oIn.readObject();
			ObdProt.VidPvs = (PvList) oIn.readObject();
			ObdProt.tCodes = (PvList) oIn.readObject();
			MainActivity.mPluginPvs = (PvList) oIn.readObject();

			oIn.close();

			log.log(Level.INFO, msg);
			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
		} catch (Exception ex) {
			Toast.makeText(context, ex.toString(), Toast.LENGTH_SHORT).show();
			log.log(Level.SEVERE, uri.toString(), ex);
		}
		return numBytesLoaded;
	}
}
