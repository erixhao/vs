package com.vs.dao.utility;

import java.io.*;
import java.time.LocalDateTime;
import java.util.List;

import com.google.common.collect.Lists;
import com.vs.common.domain.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

@Configuration
@PropertySource(value = "classpath:config.properties")
public class FileUtil {
    @Autowired
    private Environment env;
    private static String path;

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

    private static String getDataFileName(String fileName) {
        String dataFilePath = getPath() + File.separator + fileName + ".dat";

        File file = new File(dataFilePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        return dataFilePath;
    }

    public static <T extends Entity> void writeFile(String fileName, List<T> entities) throws Exception {
        writeFile(fileName, entities, true);
    }

    public static <T extends Entity> void writeFile(String fileName, List<T> entities, boolean isBackup) throws Exception {
        if (isBackup) {
            backUpFile(fileName);
        }
        try (FileOutputStream out = new FileOutputStream(getDataFileName(fileName));
             OutputStreamWriter outWriter = new OutputStreamWriter(out, "gb2312");
             BufferedWriter bufWriter = new BufferedWriter(outWriter);) {
            for (T entity : entities) {
                bufWriter.write(entity.to() + "\r\n");
            }
        }
    }

    private static void backUpFile(String fileName) throws IOException {
        String oldFileName = getDataFileName(fileName);
        String backUpFileName = getDataFileName(fileName + "_" + System.currentTimeMillis());
        File backUpFile = new File(backUpFileName.replace(".dat", ".bkp"));
        File oldFile = new File(oldFileName);
        FileCopyUtils.copy(oldFile, backUpFile);
    }

    public static <T extends Entity> List<T> readFile(String fileName, Class<T> clazz) throws Exception {
        List<T> entities = Lists.newArrayList();
        try (FileInputStream in = new FileInputStream(getDataFileName(fileName));
             InputStreamReader inReader = new InputStreamReader(in, "gb2312");
             BufferedReader bufReader = new BufferedReader(inReader);) {
            String line = null;
            while ((line = bufReader.readLine()) != null) {
                T entity = clazz.newInstance();
                entities.add(entity.from(line));
            }
        }
        return entities;
    }
}
