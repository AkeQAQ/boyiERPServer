package com.boyi.common.fileFilter;

import java.io.File;
import java.io.FileFilter;

public class MaterialPicFileFilter implements FileFilter {
    private String picModule_id;

    public MaterialPicFileFilter(String module_id){
        this.picModule_id = module_id;
    }
    @Override
    public boolean accept(File file) {
        String craftId_timestamp = file.getName();
        String[] split = craftId_timestamp.split("_");
        return split[0].equals(picModule_id);
    }
}
