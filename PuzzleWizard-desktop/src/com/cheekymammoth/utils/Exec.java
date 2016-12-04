package com.cheekymammoth.utils;

import java.awt.Desktop;
import java.net.URI;

public final class Exec {

	private Exec() { }
	
	public static boolean openURL(String url) {
		boolean didOpen = false;
		Desktop desktop = Desktop.getDesktop();
		if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
			try {
                URI uri = new URI(url);
                desktop.browse(uri);
                didOpen = true;
            } catch (Exception e) {
            	// Do nothing
            }
		}
		return didOpen;
	}
}
