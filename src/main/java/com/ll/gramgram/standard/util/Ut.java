package com.ll.gramgram.standard.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Ut {
    public static class url {
        public static String encode(String str) {
            try {
                return URLEncoder.encode(str, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return str;
            }
        }
    }
}