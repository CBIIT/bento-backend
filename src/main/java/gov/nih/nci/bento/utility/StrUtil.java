package gov.nih.nci.bento.utility;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrUtil {

    public static String getBoolFromText(String text) {
        String str = Optional.ofNullable(text).orElse("");
        String strPattern = "(?i)([ ]?\\bfalse\\b|\\btrue\\b[ ]?){1}";
        Pattern pattern = Pattern.compile(strPattern);
        Matcher matcher = pattern.matcher(str);
        String result = "";
        if (matcher.find()) result = matcher.group(1).toLowerCase();
        return result.replaceAll(" ", "");
    }
}