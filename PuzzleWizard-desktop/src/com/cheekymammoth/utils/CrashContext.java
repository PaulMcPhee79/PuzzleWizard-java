package com.cheekymammoth.utils;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Keys;

public class CrashContext {
	public static final String CONTEXT_ENABLED = "Enabled";
	public static final String CONTEXT_DISABLED = "Disabled";
	public static final String CONTEXT_GAME_STATE = "CRASH_CONTEXT_GAME_STATE";
	public static final String CONTEXT_LEVEL_NAME = "CRASH_CONTEXT_LEVEL_NAME";
	public static final String CONTEXT_PUZZLE_NAME = "CRASH_CONTEXT_PUZZLE_NAME";
	public static final String CONTEXT_LOCALE = "CRASH_CONTEXT_LOCALE";
	public static final String CONTEXT_MENU = "CRASH_CONTEXT_MENU";
	public static final String CONTEXT_SFX = "CRASH_CONTEXT_SFX";
	public static final String CONTEXT_MUSIC = "CRASH_CONTEXT_MUSIC";
	public static final String CONTEXT_COLOR_BLIND = "CRASH_CONTEXT_COLOR_BLIND";
	public static final String CONTEXT_FULL_SCREEN = "CRASH_CONTEXT_FULL_SCREEN";
	
	private static ObjectMap<String, String> gameContext = new ObjectMap<String, String>(16);
	
	private CrashContext() { }
	
	public static void setContext(String value, String context) {
		if (context != null)
			gameContext.put(context, value != null ? value : "NULL");
	}
	
	public static String context2String() {
		StringBuilder sb = new StringBuilder(256);
		Keys<String> keys = gameContext.keys();
		while (keys.hasNext) {
			String key = keys.next();
			sb.append("\n" + key + ": " + gameContext.get(key));
		}
		return sb.toString();
	}
}
