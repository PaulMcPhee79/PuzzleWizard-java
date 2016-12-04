package com.cheekymammoth.locale;

public interface ILocalizable {
	public static final int kNonLocalizableTag = 123456789;
	public static final String kNonLocalizableName = "123456789"; 
	void localeDidChange(String fontKey, String FXFontKey);
}
