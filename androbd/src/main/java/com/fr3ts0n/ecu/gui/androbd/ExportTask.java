package com.fr3ts0n.ecu.gui.androbd;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Builds the CSV dump for sharing.
 *
 *  Scheuch-Heilig
 */
class ExportTask extends AsyncTask<XYMultipleSeriesDataset, Integer, String> {

	private final Activity activity;
	@SuppressLint("SimpleDateFormat")
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	private static final String OPT_FIELD_DELIM = "csv_field_delimiter";
	private static final String OPT_RECORD_DELIM = "csv_record_delimiter";
	private static final String OPT_TEXT_QUOTED = "csv_text_quoted";
	private static final String OPT_SEND_EXPORT = "send_after_export";

	private static String CSV_FIELD_DELIMITER = ",";
	private static String CSV_LINE_DELIMITER = "\n";
	private static boolean CSV_TEXT_QUOTED = false;

	private static final String TAG = ExportTask.class.getSimpleName();
	private static final Logger log = Logger.getLogger(TAG);

	private final SharedPreferences prefs;

	// file name to be saved
	private final String path;
	private final String fileName;

	public ExportTask(Activity activity) {
		this.activity = activity;
		path = FileHelper.getPath(activity).concat(File.separator + "csv");
		fileName = path.concat(File.separator + FileHelper.getFileName()
				.concat(".csv"));

		// get preferences
		prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		CSV_FIELD_DELIMITER = prefs.getString(OPT_FIELD_DELIM, ",");
		CSV_LINE_DELIMITER = prefs.getString(OPT_RECORD_DELIM, "\n");
		CSV_TEXT_QUOTED = prefs.getBoolean(OPT_TEXT_QUOTED, false);
	}

	private static String quoteStringIfNeeded(String string) {
		return String.format(CSV_TEXT_QUOTED ? "\"%s\"" : "%s", string);
	}

	@Override
	protected String doInBackground(XYMultipleSeriesDataset... params) {
		double currX;
		double currY;
		int maxCounts = 0;
		int highestResChannel = 0; // channel id with highest x-resolution

		XYSeries series[] = params[0].getSeries();

		// Find channel with highest x-resolution
		for (int i = 0; i < series.length; i++) {
			if (maxCounts < series[i].getItemCount()) {
				maxCounts = series[i].getItemCount();
				highestResChannel = i;
			}
		}

		// Ensure the directory exists
		new File(path).mkdirs();
		FileWriter writer;
		try {
			writer = new FileWriter(new File(fileName));

			// Create header line
			writer.append(quoteStringIfNeeded(activity.getString(R.string.time)));
			for (XYSeries sery : series) {
				writer.append(CSV_FIELD_DELIMITER); // Add delimiter between fields
				writer.append(quoteStringIfNeeded(sery.getTitle()));
			}
			writer.append(CSV_LINE_DELIMITER);

			// Generate data
			for (int i = 0; i < maxCounts; i++) {
				currX = series[highestResChannel].getX(i);
				writer.append(dateFormat.format(new Date((long) currX)));
				for (XYSeries sery : series) {
					writer.append(CSV_FIELD_DELIMITER); // Add delimiter between fields
					try {
						SortedMap<Double, Double> map = sery.getRange(currX, currX, true);
						currY = map.get(map.firstKey());
						writer.append(String.valueOf(currY));
					} catch (Exception ex) {
						// Do nothing, just catch the error
					}
				}
				writer.append(CSV_LINE_DELIMITER);
				publishProgress(10000 * i / maxCounts);
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileName;
	}

	@Override
	public void onPreExecute() {
		activity.setProgressBarVisibility(true);
	}

	@Override
	public void onProgressUpdate(Integer... values) {
		activity.setProgress(values[0]);
	}

	@Override
	public void onPostExecute(String result) {
		activity.setProgressBarVisibility(false);

		// show saved message
		String msg = String.format("CSV %s to %s",
				activity.getString(R.string.saved),
				fileName);
		log.log(Level.INFO, msg);
		Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();

		// if export file should be sent immediately ...
		if (prefs.getBoolean(OPT_SEND_EXPORT, false)) {
			// allow sending the generated file ...
			Intent sendIntent = new Intent(Intent.ACTION_SEND);
			sendIntent.setType("*/*");
			sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(fileName)));
			activity.startActivity(
					Intent.createChooser(sendIntent,
							activity.getResources().getText(R.string.send_to)));
		}
	}
}
