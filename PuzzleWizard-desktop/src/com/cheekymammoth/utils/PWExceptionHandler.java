package com.cheekymammoth.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import com.badlogic.gdx.Gdx;

public class PWExceptionHandler implements UncaughtExceptionHandler {

	public PWExceptionHandler() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void uncaughtException(Thread arg0, Throwable arg1) {
		handle(arg1);
	}
	
	public void handle(Throwable throwable) {
		try {
			// System info
			String sysInfoString = "\nBEGIN SYSTEM INFO:";
			try {
				StringBuilder sb = new StringBuilder(512);
				sb.append(sysInfoString);
				sb.append("\njava.vm.version: " + System.getProperty("java.vm.version"));
				sb.append("\njava.vm.vendor: " + System.getProperty("java.vm.vendor"));
				sb.append("\njava.vm.name: " + System.getProperty("java.vm.name"));
				sb.append("\nos.name: " + System.getProperty("os.name"));
				sb.append("\nos.arch: " + System.getProperty("os.arch"));
				sb.append("\nos.version: " + System.getProperty("os.version"));
				sysInfoString = sb.toString();
			} catch (Exception e) {
				sysInfoString = "\nBEGIN SYSTEM INFO: Failed to poll system info.";
			}
			
			// Runtime state
			String runtimeInfoString = "\nBEGIN RUNTIME INFO:";
			try {
				StringBuilder sb = new StringBuilder(128);
				sb.append(runtimeInfoString);
				sb.append("\nFree memory: " + Runtime.getRuntime().freeMemory());
				sb.append("\nTotal memory: " + Runtime.getRuntime().totalMemory());
				runtimeInfoString = sb.toString();
			} catch (Exception e) {
				runtimeInfoString = "\nBEGIN RUNTIME INFO: Failed to poll runtime info.";
			}
			
			// Game state
			String gameContextString = "\nBEGIN GAME CONTEXT:";
			try {
				StringBuilder sb = new StringBuilder(128);
				sb.append(gameContextString);
				sb.append("\n" + "Game Version: v" + Version.kVersionNoString + " b" + Version.kBuildNo);
				sb.append(CrashContext.context2String());
				gameContextString = sb.toString();
			} catch (Exception e) {
				gameContextString = "\nBEGIN GAME CONTEXT: Failed to retrieve game context.";
			}
			
			// Log
			
			StringWriter sw = new StringWriter();
			throwable.printStackTrace(new PrintWriter(sw));
			String exceptionAsString = sw.toString();
			CMExceptionLog.logException(throwable.toString(),
					exceptionAsString
					+ "\n" + sysInfoString
					+ "\n" + runtimeInfoString
					+ "\n" + gameContextString);
			Gdx.app.log("Error", "Uncaught Exception detected: "+ exceptionAsString);
		} catch (Throwable t) {
			// Give up
		}
	}
}
