package pl.egalit.vocab.model;

import java.util.HashMap;
import java.util.Map;

import pl.egalit.vocab.R;

public enum Language {
	ENGLISH("en"), GERMAN("de"), FRENCH("fr"), SPANISH("es"), ITALIAN("it"), POLISH(
			"pl"), PORTUGUESE("po"), RUSSIAN("po"), CHINESE("cn"), UNKNOWN(
			"unknown");

	private String code;

	private Language(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static Language getLanguageForCode(String code) {
		for (Language l : Language.values()) {
			if (l.getCode().equals(code)) {
				return l;
			}
		}
		return UNKNOWN;
	}

	private static final Map<Language, Integer> languageToFlagRes = new HashMap<Language, Integer>();

	static {
		languageToFlagRes.put(Language.CHINESE, R.drawable.vokabes_china);
		languageToFlagRes.put(Language.ENGLISH, R.drawable.vokabes_uk);
		languageToFlagRes.put(Language.FRENCH, R.drawable.vokabes_france);
		languageToFlagRes.put(Language.GERMAN, R.drawable.vokabes_germany);
		languageToFlagRes.put(Language.ITALIAN, R.drawable.vokabes_italy);
		languageToFlagRes.put(Language.POLISH, R.drawable.vokabes_poland);
		languageToFlagRes.put(Language.RUSSIAN, R.drawable.vokabes_russia);
		languageToFlagRes.put(Language.SPANISH, R.drawable.vokabes_spain);
		languageToFlagRes.put(Language.UNKNOWN, R.drawable.vokabes_unknown);
	}

	public static int getFlagResForLanguage(Language language) {
		return languageToFlagRes.get(language);
	}

}