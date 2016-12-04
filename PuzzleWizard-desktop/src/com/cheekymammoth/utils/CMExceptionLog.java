package com.cheekymammoth.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class CMExceptionLog {
	private static final String kExcFilePath = "Logs/pw_log.txt";

	private CMExceptionLog() { }

	public static void logException(String context, String msg) {
		try
		{
			{
				FileHandle file = Gdx.files.local(kExcFilePath);
				file.writeString(msg, false);
				file = null;
			}
			
			String txTitle = context;
			if (txTitle != null && txTitle.length() > 60)
				txTitle = txTitle.substring(0, 60);
			txLoggedExceptions(txTitle);
		} catch (Exception e) {
			// Give up.
		}
	}
	
	public static void clearLoggedExceptions() {
		try {
			if (isExceptionLogged())
				Gdx.files.local(kExcFilePath).delete();
		} catch (Exception e) {
			Gdx.app.log("Error", "CMExceptionLog: log deletion failed: "+ e.getMessage());
		}
	}
	
	private static boolean isExceptionLogged() {
		FileHandle file = Gdx.files.local(kExcFilePath);
		return file != null && file.exists();
	}
	
	public static void txLoggedExceptions(String txTitle) {
		try {
			if (isExceptionLogged()) {
				CMEmail.Send(
						"pw_debug",
						"OPf9+=diiIiLpwN.(as",
						"pw_debug" + "@" + "cheekymammoth." + "c" + "o" + "m",
						null,
						txTitle != null ? txTitle : "Puzzle Wizard debug log",
						"Puzzle Wizard: See attached unhandled exception log.",
						kExcFilePath);
			}
		} catch (Exception e) {
			Gdx.app.log("Error", "CMExceptionLog: log transmission failed: "+ e.getMessage());
		}
	}
}
