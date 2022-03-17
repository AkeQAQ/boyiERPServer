package com.boyi.common.fileFilter;

import java.io.File;
import java.io.FileFilter;

public class CraftPicFileFilter implements FileFilter {
    private String craftPicPrefixId;

    public CraftPicFileFilter(String id){
        this.craftPicPrefixId = id;
    }
    @Override
    public boolean accept(File file) {
        String craftId_timestamp = file.getName();
        String[] split = craftId_timestamp.split("_");
        return split[0].equals(craftPicPrefixId);
    }
}
