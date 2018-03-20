package com.sogukj.pe.util;


import com.sogukj.pe.bean.CityArea;

import java.util.Comparator;

public class PinyinComparator implements Comparator<CityArea> {
    @Override
    public int compare(CityArea province, CityArea province2) {
        if (province.getSortLetters().equals("@")
                || province2.getSortLetters().equals("#")) {
            return -1;
        } else if (province.getSortLetters().equals("#")
                || province2.getSortLetters().equals("@")) {
            return 1;
        } else {
            return province.getSortLetters().compareTo(province2.getSortLetters());
        }
    }
}
