package com.cheekymammoth.locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.cheekymammoth.sceneControllers.SceneController;
import com.cheekymammoth.ui.TextUtils;

public class Localizer {
	public enum LocaleType { EN, CN, DE, ES, FR, IT, JP, KR, INVALID_LOCALE };
	public static final int kNumLocales = LocaleType.INVALID_LOCALE.ordinal();
	private static final ObjectMap<String, LocaleType> kISOLocaleMap = new ObjectMap<String, LocaleType>(kNumLocales);
	public static final LocaleType[] kLocales = new LocaleType[] {
		LocaleType.EN,
		LocaleType.CN,
		LocaleType.DE,
		LocaleType.ES,
		LocaleType.FR,
		LocaleType.IT,
		LocaleType.JP,
		LocaleType.KR
	};
	
	private static final String kFontKeyPrefix = ""; //"fonts/";
	private static final String kFontFileSuffix = ".fnt";
	private static final String kFontImageSuffix = ".png"; //"_0.png";
	
	private static LocaleType s_Locale = LocaleType.INVALID_LOCALE;
	@SuppressWarnings("unchecked")
	private static ObjectMap<String, String>[] s_LocaleMaps = (ObjectMap<String, String>[])new ObjectMap[kNumLocales];
	private static Array<LocaleType> s_ContentMap = new Array<LocaleType>(true, kNumLocales, LocaleType.class);
	//private static ObjectMap<String, String> s_LocaleStrings = null;
	private static SceneController s_Scene = null;
	
	static {
		String[] isoCodes = new String[] { "en", "zh", "de", "es", "fr", "it", "ja", "ko" };
		for (int i = 0, n = isoCodes.length; i < n; i++)
			kISOLocaleMap.put(isoCodes[i], kLocales[i]);
	}
	
	private Localizer() { }

	public static void setScene(SceneController owner, SceneController setting) {
		// Don't allow previous owners to null new owners
		if (s_Scene == null || s_Scene == owner || setting != null)
			s_Scene = setting;
	}
	
	public static void initLocalizationStrings(LocaleType srcLocale, LocaleType destLocale) {
		ObjectMap<String, String> srcLocaleStrings = getStringsForLocale(srcLocale);
		ObjectMap<String, String> destLocaleStrings = getStringsForLocale(destLocale);
		
		// Don't init null and don't init more than once
		if (srcLocaleStrings == null || destLocaleStrings == null || destLocaleStrings.size != 0)
			return;
		
		FileHandle handle = Gdx.files.internal(pathForLocaleStrings(destLocale));
		String contents = handle.readString("UTF-8");
		
		String[] lines = contents.split("\n");
		
		for (int i = 0, n = lines.length; i < n; i++) {
			String line = lines[i].replace('^', '\n');
			int delimIndex = line.indexOf(',', 0);
			String[] tokens = new String[] {
					line.substring(0, delimIndex),
					line.substring(delimIndex+1, line.length())
			};
			assert(tokens.length == 2) : "Localizer - invalid Locale strings file: " + line;
			srcLocaleStrings.put(tokens[1], tokens[0]);
			destLocaleStrings.put(tokens[0], tokens[1]);
		}
	}
	
	public static void preloadFontsForLocale(LocaleType locale, boolean async) {
		if (locale != null && locale == LocaleType.INVALID_LOCALE)
			return;
		
		String fontKey = fontKeyForLocale(locale);
		BitmapFont font = s_Scene.getFont(fontKey + kFontFileSuffix);
		if (font == null) {
			String texName = fontKey + kFontImageSuffix;
			TextureParameter texParam = new TextureParameter();
			texParam.genMipMaps = true;
			texParam.minFilter = TextureFilter.MipMapLinearLinear; // Cleans it up at smaller scales.
			texParam.magFilter = TextureFilter.Linear;
			texParam.wrapU = TextureWrap.ClampToEdge;
			texParam.wrapV = TextureWrap.ClampToEdge;
			s_Scene.getFM().loadFont(fontKey + kFontFileSuffix, texName, texParam, async);
		}
		
		String FXFontKey = FXFontKeyForLocale(locale);
		BitmapFont FXFont = s_Scene.getFont(FXFontKey + kFontFileSuffix);
		if (FXFont == null) {
			String FXTexName = FXFontKey + kFontImageSuffix;
			TextureParameter FXTexParam = new TextureParameter();
			FXTexParam.genMipMaps = true;
			FXTexParam.minFilter = TextureFilter.MipMapLinearNearest; // MipMapLinearLinear;
			FXTexParam.magFilter = TextureFilter.Linear;
			FXTexParam.wrapU = TextureWrap.ClampToEdge;
			FXTexParam.wrapV = TextureWrap.ClampToEdge;
			s_Scene.getFM().loadFont(FXFontKey + kFontFileSuffix, FXTexName, FXTexParam, async);
		}
	}
	
	public static void initLocalizationContent(LocaleType locale) {
		if (locale != null && locale == LocaleType.INVALID_LOCALE ||
				s_ContentMap.contains(locale2ContentKey(locale), false))
			return;
		
		preloadFontsForLocale(locale, false);
		
		String fontKey = fontKeyForLocale(locale);
		BitmapFont font = s_Scene.getFont(fontKey + kFontFileSuffix);
		assert(font != null) : "Localizer - failed to load Locale font for " + locale.toString();
		s_Scene.addFontName(fontKey + kFontFileSuffix, TextUtils.kBaseFontSize);
		
		String FXFontKey = FXFontKeyForLocale(locale);
		BitmapFont FXFont = s_Scene.getFont(FXFontKey + kFontFileSuffix);
		assert(FXFont != null) : "Localizer - failed to load Locale FX font for " + locale.toString();
		s_Scene.addFontName(FXFontKey + kFontFileSuffix, TextUtils.kBaseFXFontSize);
		
		s_ContentMap.add(locale2ContentKey(locale));
	}
	
	public static void destroyLocalizationContent() {
		for (int i = 0, n = kLocales.length; i < n; i++) {
			purgeLocalizationStrings(kLocales[i]);
			purgeLocalizationContent(kLocales[i]);
		}
		s_ContentMap.clear();
	}
	
	public static void purgeLocale(LocaleType fromLocale, LocaleType toLocale) {
		if (fromLocale == null || toLocale == null || fromLocale == LocaleType.INVALID_LOCALE || fromLocale == toLocale)
			return;
		
		// Don't remove toLocale's font names in case this is called after a locale transition. If it's
		// not, then the new locale will overwrite the old ones anyway.
		if (toLocale == LocaleType.INVALID_LOCALE) {
			s_Scene.removeFontName(TextUtils.kBaseFontSize);
			s_Scene.removeFontName(TextUtils.kBaseFXFontSize);
		}
		
		// All EU langs share the same content
		//if (!fontKeyForLocale(fromLocale).equalsIgnoreCase(fontKeyForLocale(toLocale)))
		if (locale2ContentKey(fromLocale) != locale2ContentKey(toLocale))
			purgeLocalizationContent(fromLocale);
		
		if (fromLocale != LocaleType.EN)
			purgeLocalizationStrings(fromLocale);
	}
	
	private static void purgeLocalizationStrings(LocaleType locale) {
		if (locale == LocaleType.INVALID_LOCALE) {
			for (int i = 0, n = s_LocaleMaps.length; i < n; i++)
				s_LocaleMaps[i] = null;
		} else
			s_LocaleMaps[locale.ordinal()] = null;
	}
	
	private static void purgeLocalizationContent(LocaleType locale) {
		if (locale == LocaleType.INVALID_LOCALE)
			return;
		
		String fontKey = fontKeyForLocale(locale);
		String FXFontKey = FXFontKeyForLocale(locale);
		s_Scene.getFM().unloadFont(fontKey + kFontFileSuffix);
		s_Scene.getFM().unloadFont(FXFontKey + kFontFileSuffix);
		s_ContentMap.removeValue(locale2ContentKey(locale), true);
	}
	
	public static String pathForLocaleStrings(LocaleType locale) {
		return "locales/" + locale2String(locale) + ".txt";
	}
	
	public static LocaleType getLocale() {
		return s_Locale;
	}
	
	public static int getLocaleIndex() {
		LocaleType locale = getLocale();
		return locale != null && locale != LocaleType.INVALID_LOCALE ? locale.ordinal() : LocaleType.EN.ordinal();
	}
	
	public static void setLocale(LocaleType locale) {
		if (locale == null || locale == s_Locale || locale == LocaleType.INVALID_LOCALE)
			return;
		
		assert(s_LocaleMaps[locale.ordinal()] != null) : "Locale must be initialized before being set: " + locale.toString();
		
		//s_LocaleStrings = s_LocaleMaps[locale.ordinal()];
		s_Locale = locale;
	}
	
	public static String localize(String text) {
		return localize(text, getLocale());
	}
	
	public static String localize(String value, LocaleType locale) {
		ObjectMap<String, String> localeStrings = getStringsForLocale(locale);
		if (value == null || localeStrings == null)
			return value;
		
		String localizedString = localeStrings.get(value);
		if (localizedString != null)
			return localizedString;
		else {
			ObjectMap<String, String> enStrings = getStringsForLocale(LocaleType.EN);
			if (enStrings != null) {
				localizedString = enStrings.get(value);
				if (localizedString != null) {
					localizedString = localeStrings.get(localizedString);
					if (localizedString != null)
						return localizedString;
				}
			}
		}
		
		return value;
	}
	
	public static String fontKeyPrefixForLocale(LocaleType locale) {
		return kFontKeyPrefix + locale2FontPrefix(locale); // + "-";
	}
	
	public static String fontKeyForLocale(LocaleType locale) {
		return fontKeyPrefixForLocale(locale); // + TextUtils.kBaseFontSize;
	}
	
	public static String FXFontKeyPrefixForLocale(LocaleType locale) {
		return kFontKeyPrefix + locale2FontPrefix(locale) + "-FX"; // "-FX-";
	}
	
	public static String FXFontKeyForLocale(LocaleType locale) {
		return FXFontKeyPrefixForLocale(locale); // + TextUtils.kBaseFXFontSize;
	}
	
	public static String iconTextureNameForLocale(LocaleType locale) {
		return fontKeyForLocale(locale) + kFontImageSuffix;
	}
	
	public static String FXIconTextureNameForLocale(LocaleType locale) {
		return FXFontKeyForLocale(locale) + kFontImageSuffix;
	}

	private static ObjectMap<String, String> getStringsForLocale(LocaleType locale) {
		assert(locale != LocaleType.INVALID_LOCALE) :
			"Invalid locale in Localizer::getStringsForLocale: " + locale.toString();
		
		if (s_LocaleMaps[locale.ordinal()] == null)
			s_LocaleMaps[locale.ordinal()] = new ObjectMap<String, String>(200);
		
		return s_LocaleMaps[locale.ordinal()];
	}
	
//	private static void setStringsForLocale(LocaleType locale, ObjectMap<String, String> localeStrings) {
//		if (localeStrings != null && locale != LocaleType.INVALID_LOCALE) {
//			assert(s_LocaleMaps[locale.ordinal()] == null) :
//				"Localizer::setStringsForLocale called when locale strings are already present: " + locale.toString();
//			s_LocaleMaps[locale.ordinal()] = localeStrings;
//		}
//	}
	
	private static LocaleType locale2ContentKey(LocaleType locale) {
		switch (locale) {
			case EN:
			case DE:
			case ES:
			case FR:
			case IT:
				return LocaleType.EN;
			default:
				return locale;
		}
	}
	
	public static String locale2String(LocaleType locale) {
		return locale.toString();
	}
	
	public static String locale2StringLower(LocaleType locale) {
		return locale.toString().toLowerCase();
	}

	private static String locale2FontPrefix(LocaleType locale) {
		switch (locale) {
			case EN:
			case DE:
			case ES:
			case FR:
			case IT:
				return "EU";
			default:
				return locale.toString();
		}
	}
	
	public static LocaleType getLocaleTypeFromCurrentUICulture() {
		String isoCode = System.getProperty("user.language").toLowerCase();
		LocaleType locale = kISOLocaleMap.get(isoCode, null);
		return locale != null ? locale : LocaleType.EN;
	}
	
	public static LocaleType ordinal2Locale(int localeOrdinal) {
		switch (localeOrdinal) {
			case 1: return LocaleType.CN;
			case 2: return LocaleType.DE;
			case 3: return LocaleType.ES;
			case 4: return LocaleType.FR;
			case 5: return LocaleType.IT;
			case 6: return LocaleType.JP;
			case 7: return LocaleType.KR;
			default: return LocaleType.EN;
		}
	}
}
