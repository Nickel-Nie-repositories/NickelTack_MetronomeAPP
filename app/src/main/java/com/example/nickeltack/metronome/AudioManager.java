package com.example.nickeltack.metronome;

import android.content.Context;
import android.graphics.Color;
import android.media.SoundPool;
import android.util.Log;
import android.util.SparseIntArray;

import androidx.annotation.NonNull;

import com.example.nickeltack.R;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AudioManager {
    private static AudioManager instance;
    private SoundPool soundPool;

    private Context context;
    private final SparseIntArray soundMap;  // 存储音效ID


    final Map<String, Integer> soundsDict; // 记录音频名称到 soundId 的转换。
    private final int maxStreams = 10;  // 最大并发播放数

    // 单例模式
    private AudioManager(Context context) {
        soundPool = new SoundPool.Builder()
                .setMaxStreams(maxStreams)
                .build();
        soundMap = new SparseIntArray();

        soundsDict = new LinkedHashMap<String, Integer>();

        initSounds(context);
    }

    public static AudioManager getInstance(Context context) {
        if (instance == null) {
            synchronized (AudioManager.class) {
                if (instance == null) {
                    instance = new AudioManager(context.getApplicationContext());
                    //instance.context = context.getApplicationContext();
                    instance.context = context;
                }
            }
        }
        return instance;
    }

    //初始化，加载应该加载的所有音效
    private void initSounds(Context context)
    {

        soundsDict.put("【None】", -1);
        loadSound(context, R.raw.arcade_game_jump_coin);
        loadSound(context, R.raw.classic_click);
        loadSound(context, R.raw.modern_technology_select);
        loadSound(context, R.raw.select_click);
    }


    // 加载音效
    public void loadSound(Context context, int soundResId) {
        if (soundMap.get(soundResId) == 0) {
            int soundId = soundPool.load(context, soundResId, 1);
            String soundName = ResourceUtils.getResourceName(context,soundResId);
            soundMap.put(soundResId, soundId);
            soundsDict.put(soundName, soundId);
        }
    }

    public void loadExternalSound(String filePath) {
        File soundFile = new File(filePath);

        if (soundFile.exists()) {
            try {
                // 使用 FileDescriptor 加载外部文件
                int soundId = soundPool.load(soundFile.getAbsolutePath(), 1);
                soundsDict.put(filePath, soundId);
            } catch (Exception e) {
                Log.e("AudioManager", "Failed to load external sound", e);
            }
        }
    }

    public String[] getAllSoundsName()
    {
        return soundsDict.keySet().toArray(new String[0]);
    }

    public int getSoundIdByName(String name)
    {
        return soundsDict.getOrDefault(name,-1);
    }


    public int getOrderBySoundId(int soundId)
    {
        int order = 0;
        for(Map.Entry<String, Integer> entry : soundsDict.entrySet())
        {
            if(soundId == entry.getValue())
            {
                return order;
            }
            order++;
        }
        return 0;
    }

    public int getColorBySoundId(int soundId)
    {
        return (soundId == -1)? Color.WHITE:ColorManager.colorArray[(getOrderBySoundId(soundId)-1)%ColorManager.colorArrayLength];
    }


    // 播放音效 参数为ResId
    public void playSoundByResId(int soundResId) {
        int soundId = soundMap.get(soundResId, -1);
        if (soundId != -1) {
            soundPool.play(soundId, 1f, 1f, 0, 0, 1f);
        }
    }

    // 播放音频 参数为SoundId
    public void playSound(int soundId)
    {
        soundPool.play(soundId, 1f, 1f, 0, 0, 1f);
    }

    public void playSound(int soundId, float volume)
    {
        soundPool.play(soundId, volume, volume, 0, 0, 1f);
    }


    // 停止所有音效
    public void stopAllSounds() {
        soundPool.autoPause();  // 暂停所有音效的播放
    }

    // 释放资源
    public void release() {
        soundPool.release();
        soundPool = null;
        soundMap.clear();
        instance = null;
    }
}
