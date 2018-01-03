package com.vs.dao.utility;

import com.google.common.collect.Lists;
import com.vs.common.domain.Entity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.util.List;

public class FileUtil {
    private static String path;
    private static boolean isBackup;

    private static String getPath() {
        InputStream in = FileUtil.class.getResourceAsStream("/config.properties");

        Reader f = new InputStreamReader(in);
        BufferedReader fb = new BufferedReader(f);
        String line = null;
        try {
            line = fb.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line.split("=")[1];
    }

    private static boolean getBackupFlag() {
        InputStream in = FileUtil.class.getResourceAsStream("/config.properties");

        Reader f = new InputStreamReader(in);
        BufferedReader fb = new BufferedReader(f);
        String line = null;
        try {
            line = fb.readLine();
            line = fb.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Boolean.valueOf(line.split("=")[1]).booleanValue();
    }

    private static String getDataFileName(String fileName) {
        String dataFilePath = getPath() + File.separator + fileName + ".dat";

        File file = new File(dataFilePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return dataFilePath;
    }

    public static <T extends Entity> void writeFile(String fileName, List<T> entities) {
        writeFile(fileName, entities, getBackupFlag());
    }

    public static <T extends Entity> void writeFile(String fileName, List<T> entities, boolean isBackup) {
        if (isBackup) {
            backUpFile(fileName);
        }
        try (FileOutputStream out = new FileOutputStream(getDataFileName(fileName));
             OutputStreamWriter outWriter = new OutputStreamWriter(out, "gb2312");
             BufferedWriter bufWriter = new BufferedWriter(outWriter);) {
            for (T entity : entities) {
                bufWriter.write(entity.to() + "\r\n");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void backUpFile(String fileName) {
        try {
            String oldFileName = getDataFileName(fileName);
            String backUpFileName = getDataFileName(fileName + "_" + System.currentTimeMillis());
            File backUpFile = new File(backUpFileName.replace(".dat", ".bkp"));
            File oldFile = new File(oldFileName);
            FileCopyUtils.copy(oldFile, backUpFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends Entity> List<T> readFile(String fileName, Class<T> clazz) {
        List<T> entities = Lists.newArrayList();
        try (FileInputStream in = new FileInputStream(getDataFileName(fileName));
             InputStreamReader inReader = new InputStreamReader(in, "gb2312");
             BufferedReader bufReader = new BufferedReader(inReader);) {
            String line = null;
            while ((line = bufReader.readLine()) != null) {
                if (StringUtils.isNotBlank(line)) {
                    T entity = clazz.newInstance();
                    entities.add(entity.from(line));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return entities;
    }
}
