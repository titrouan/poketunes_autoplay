package fr.titrouan.poketunesautoplay.config;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class LangHelper {
    private static final ResourceBundle bundle;

    static {
        Locale locale = Locale.getDefault();
        Locale effectiveLocale = locale.getLanguage().equals("fr") ? Locale.FRENCH : Locale.ENGLISH;

        ResourceBundle temp;
        try {
            temp = ResourceBundle.getBundle("poketunes_logs", effectiveLocale);
        } catch (MissingResourceException e) {
            temp = ResourceBundle.getBundle("poketunes_logs", Locale.ENGLISH);
        }
        bundle = temp;
    }

    public static String get(String key) {
        return bundle.getString(key);
    }

    public static String get(String key, Object... args) {
        return MessageFormat.format(bundle.getString(key), args);
    }
}