package com.cheekymammoth.utils;

public class Promo {
	public enum PromoDeviceType {
		Phone, Tablet_7, Tablet_10, iPad, iPhone_5_5, iPhone_4_7, iPhone_4_0, iPhone_3_5, Mac,
		Fraps_iPhone_4_7, Fraps_YouTube, Fraps_iPad, Fraps_iPhone_4_0, Fraps_iPhone_5_5
	};
	
	private static boolean isPromoEnabled = true;
	private static boolean isFrapsEnabled = true;
	private static boolean showPauseButton = true;
	
	// Mutators
	private static boolean slowAnimations = false;
	
	private Promo() { }
	
	public static PromoDeviceType getDeviceType() {
		return PromoDeviceType.Phone;
	}
	
	public static boolean isPromoEnabled() {
		return isPromoEnabled;
	}
	
	public static boolean isFrapsEnabled() {
		return isFrapsEnabled;
	}
	
	public static boolean isPlayerHudVisible() {
		return !isPromoEnabled;
	}
	
	public static boolean isPauseButtonVisible() {
		return showPauseButton || !isPromoEnabled;
	}
	
	public static boolean isTeleportAnimating() {
		return isFrapsEnabled || !isPromoEnabled;
	}
	
	public static boolean isKeyAnimating() {
		return isFrapsEnabled || !isPromoEnabled;
	}
	
	public static boolean isPromoDisplaySizeEnabled() {
		return isPromoEnabled;
	}
	
	public static boolean isDeviceScreenshotScaled(PromoDeviceType deviceType) {
		switch (deviceType) {
			case Phone: return false;
			case Tablet_7: return true;
			case Tablet_10: return true;
			case iPad: return true;
			case iPhone_5_5: return true;
			case iPhone_4_7: return false;
			case iPhone_4_0: return false;
			case iPhone_3_5: return false;
			case Mac: return true;
			default: return false;
		}
	}
	
	public static boolean isSlowAnimations() {
		return slowAnimations;
	}
	
	public static void setSlowAnimations(boolean value) {
		slowAnimations = value;
	}
	
	public static int getDisplayWidth() {
		PromoDeviceType deviceType = getDeviceType();
		
		switch (deviceType) {
			case Phone: return 1280;
			case Tablet_7: return 1280;
			case Tablet_10: return 1280;
			case iPad: return 1024;
			case iPhone_5_5: return 1104;
			case iPhone_4_7: return 1334;
			case iPhone_4_0: return 1136;
			case iPhone_3_5: return 960;
			case Mac: return 1440;
			case Fraps_iPhone_4_7: return 1336; // +2 due to Fraps bug.
			case Fraps_YouTube: return 1280;
			case Fraps_iPad: return 1200;
			case Fraps_iPhone_4_0: return 1136;
			case Fraps_iPhone_5_5: return 1600;
			default: return 1280;
		}
	}
	
	public static int getDisplayHeight() {
		PromoDeviceType deviceType = getDeviceType();
		
		switch (deviceType) {
			case Phone: return 720;
			case Tablet_7: return 720;
			case Tablet_10: return 720;
			case iPad: return 768;
			case iPhone_5_5: return 621;
			case iPhone_4_7: return 750;
			case iPhone_4_0: return 640;
			case iPhone_3_5: return 640;
			case Mac: return 900;
			case Fraps_iPhone_4_7: return 750;
			case Fraps_YouTube: return 720;
			case Fraps_iPad: return 900;
			case Fraps_iPhone_4_0: return 640;
			case Fraps_iPhone_5_5: return 900;
			default: return 720;
		}
	}
	
	public static float getRotatorDuration() {
		return isFrapsEnabled() || !slowAnimations ? 0.9f : 6f;
	}
	
	public static float getConveyorBeltDuration() {
		return isFrapsEnabled() || !slowAnimations ? 1.15f : 9f;
	}
}
