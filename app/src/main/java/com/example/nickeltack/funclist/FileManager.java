package com.example.nickeltack.funclist;

import android.util.Log;

import java.io.*;
import java.nio.file.*;

public class FileManager {

    private File directory;

    private static FileManager instance;


    public static FileManager getInstance(String directoryPath) {
        if (instance == null) {
            instance = new FileManager(directoryPath);
        }
        return instance;
    }


    private FileManager(String directoryPath) {
        this.directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs(); // 如果目录不存在，则创建目录
        }
    }

    // 设置存储目录
    public void setDirectory(String directoryPath) {
        this.directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs(); // 如果目录不存在，则创建目录
        }
    }

    // 序列化方法，保存对象到文件
    public <T> void saveObject(String fileName, T object) {
        File file = new File(directory, fileName + ".ntr");
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(object);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 反序列化方法，从文件加载对象
    public <T> T loadObject(String fileName) {
        File file = new File(directory, fileName + ".ntr");
        if (!file.exists()) {
            return null; // 文件不存在时返回null
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (T) in.readObject();
        } catch (Exception e) {
            Log.d("TAG_0", e.toString());
        }
        return null;
    }

    public boolean removeFile(String fileName){
        File file = new File(directory, fileName + ".ntr");
        if (file.exists()) {
            return file.delete();}
        return false;
    }
}
