package com.sogukj.pe.util;

import com.bumptech.glide.load.Key;

import java.security.MessageDigest;

/**
 *
 * @author admin
 * @date 2018/3/12
 */

public class HeaderImgKey implements Key {
    private String headUrl;


    public HeaderImgKey(String headUrl) {
        this.headUrl = headUrl;
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {

    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof String) {
            if (headUrl.contains("?")) {
                String s = (String) o;
                return s.substring(0, s.indexOf("?")).equals(headUrl.substring(0, headUrl.indexOf("?")));
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return headUrl.hashCode();
    }
}
