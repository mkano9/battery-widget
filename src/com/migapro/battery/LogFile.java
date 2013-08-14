package com.migapro.battery;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.os.Environment;

public class LogFile {

	public static void log(String msg) {
		File logFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "battery_log.txt");
		
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			Date date = Calendar.getInstance().getTime();
			SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss:SSS", Locale.US);

			BufferedWriter buffer = new BufferedWriter(new FileWriter(logFile, true));
			buffer.append(dateFormat.format(date) + " " + msg);
			buffer.newLine();
			buffer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
