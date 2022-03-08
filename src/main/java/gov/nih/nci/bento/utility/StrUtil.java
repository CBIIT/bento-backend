package gov.nih.nci.bento.utility;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrUtil {

    public static String getBoolText(String text) {
        String strPattern = "(?i)(\\bfalse\\b|\\btrue\\b)";
        return getStr(strPattern, text).toLowerCase();
    }

    public static String getIntText(String text) {
        String strPattern = "(\\b[0-9]+\\b)";
        return getStr(strPattern, text);
    }

    private static String getStr(String strPattern, String text) {
        String str = Optional.ofNullable(text).orElse("");
        Pattern pattern = Pattern.compile(strPattern);
        Matcher matcher = pattern.matcher(str);
        String result = "";
        if (matcher.find()) result = matcher.group(1);
        return result;
    }
}