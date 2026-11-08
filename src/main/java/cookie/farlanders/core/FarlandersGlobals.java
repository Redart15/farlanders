package cookie.farlanders.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FarlandersGlobals {
	public static final String MOD_ID = "farlanders";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	static String formatTranslationKey(String key) {
		return String.format("%s.%s", MOD_ID, key);
	}

	static String formatName(String name, String type) {
		return String.format("%s:%s/%s", MOD_ID, type, name);
	}
}
