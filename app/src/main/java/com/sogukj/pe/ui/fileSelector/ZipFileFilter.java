package com.sogukj.pe.ui.fileSelector;

import com.sogukj.pe.util.FileUtil;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by admin on 2018/5/15.
 */

public class ZipFileFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
        FileUtil.FileType type = FileUtil.getFileType(pathname);
        return type == FileUtil.FileType.ZIP;
    }
}
