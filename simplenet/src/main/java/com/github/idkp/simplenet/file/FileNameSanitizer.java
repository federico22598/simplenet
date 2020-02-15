package com.github.idkp.simplenet.file;

public final class FileNameSanitizer {
    private static final int MAX_FILE_NAME_LEN = 128;
    private static final String[] SPECIAL_SUBSTRS = {
            "..", "?", "[", "]" /*,"/", "\\"*/, "=", "<", ">",
            ":", ";", ",", "\"", "\"", "&", "$", "#", "*", "(", ")", "|", "~", "`", "!", "{", "}"
    };

    public static String sanitize(String name) {
        for (String specialSubstr : SPECIAL_SUBSTRS) {
            name = name.replace(specialSubstr, "-");
        }

        if (name.length() > MAX_FILE_NAME_LEN) {
            name = name.substring(0, MAX_FILE_NAME_LEN);
        }

        return name;
    }
}
