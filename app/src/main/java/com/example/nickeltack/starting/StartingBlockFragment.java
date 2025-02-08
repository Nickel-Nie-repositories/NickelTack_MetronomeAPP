package com.example.nickeltack.starting;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.nickeltack.R;
import com.example.nickeltack.funclist.FileManager;
import com.example.nickeltack.metronome.AudioManager;
import com.example.nickeltack.metronome.Fraction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class StartingBlockFragment extends Fragment {

    private Button startButton;
    private Button playPauseButton;
    private Button settingsButton;
    private boolean isStarted = false;
    private boolean isPlaying = false;

    private Handler handler;
    private Runnable vibrationRunnable;

    private MultiVibratingDotView multiVibratingDotView;
    private WaveformCircleView waveformCircle;

    private AverageAlgorithm AverageMode = AverageAlgorithm.ARITHMETIC_MEAN; // 平均模式，用于节拍间隔的计算
    private int interval = 200;
    private long[] accentTimes;

    private int startingAccentCount  = 4;
    private int detectedAccentNum = 0;
    private AudioManager audioManager;

    private List<ItemData> soundsList = new ArrayList<>();

    private int threshold = 12000;
    private int risingEdgeThreshold = 7800;
    private int refractoryPeriod = 200;
    private int fallingEdgeThreshold = 6800;

    private String fileName;

    public StartingBlockFragment() {
        // Required empty public constructor
    }

    public StartingBlockFragment(String fileName)
    {
        this.fileName = fileName;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_starting_block, container, false);

        audioManager = AudioManager.getInstance(getContext());

        // Get references to the views
        startButton = view.findViewById(R.id.startButton);
        playPauseButton = view.findViewById(R.id.playPauseButton);
        settingsButton = view.findViewById(R.id.settingsButton);

        multiVibratingDotView = view.findViewById(R.id.multiVibratingDot);
        waveformCircle = view.findViewById(R.id.waveformCircle);

        // Set the start button logic
        startButton.setOnClickListener(v -> onStartButtonClicked());

        // Set the play/pause button logic
        playPauseButton.setOnClickListener(v -> onPlayPauseButtonClicked());

        // Set the settings button logic
        settingsButton.setOnClickListener(v -> onSettingsButtonClicked());

        // set the runnable
        vibrationRunnable = new Runnable() {
            @Override
            public void run() {
                multiVibratingDotView.vibrate();
                waveformCircle.vibrate(1f);
                handler.postDelayed(this,interval);
            }
        };
        handler = new Handler(Looper.getMainLooper());

        // init vibrating Dot sounds
        soundsList.add(new ItemData("【None】", 50));
        setVibratingDotSounds();

        // init accent listener parameters
        waveformCircle.SetListenerArguments(this.threshold,this.risingEdgeThreshold,this.fallingEdgeThreshold,this.refractoryPeriod);

        // set vibrating Dot LongPress Listener
        multiVibratingDotView.setLongPressListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(isPlaying)
                {
                    onPlayPauseButtonClicked();
                }
                showVibratingDotSettingDialog();
                return true;
            }
        });

        if(!Objects.equals(fileName, "")) {load();}

        // Disable start button if the game is paused
        updateStartButtonState();

        return view;
    }

    private void setVibratingDotSounds() {
        int[] soundIds =  new int[soundsList.size()];
        float[] soundVolumes = new float[soundsList.size()];
        for (int i= 0 ; i<soundsList.size(); i++)
        {
            soundIds[i] = audioManager.getSoundIdByName(soundsList.get(i).getSoundName());
            soundVolumes[i] = soundsList.get(i).getSeekBarValue() / 100f;
        }
        multiVibratingDotView.setSounds(soundIds, soundVolumes);
    }

    private void onStartButtonClicked() {
        // Handle start logic here
        if (!isPlaying) {
            // Do the initialization or start logic
            //isStarted = true;
            showStartingDialog(getContext());

        }
    }

    private void onPlayPauseButtonClicked() {
        // Toggle between play and pause
        if (isPlaying) {
            isPlaying = false;
            playPauseButton.setText("开始");
            stopVibration();

        } else {
            isPlaying = true;
            playPauseButton.setText("暂停");
            startVibration();

        }
        updateStartButtonState();
    }

    private void onSettingsButtonClicked() {
        showParameterSettingDialog();
    }

    private void updateStartButtonState() {
        playPauseButton.post(new Runnable() {
            @Override
            public void run() {
                playPauseButton.setEnabled(isStarted);
            }
        });
        startButton.post(new Runnable() {
            @Override
            public void run() {
                startButton.setEnabled(!isPlaying);
            }
        });
        settingsButton.post(new Runnable() {
            @Override
            public void run() {
                settingsButton.setEnabled(!isPlaying);
            }
        });

        // playPauseButton.setEnabled(isStarted);
        // startButton.setEnabled(!isPlaying);
    }

    // 计算并启动节拍，根据已经记录的重音的间隔计算节拍的间隔，
    private void calculateAndStartBeats()
    {
        isStarted = true;
        long[] differences = CalculateUtils.calculateDifferences(accentTimes);
        //Log.d("TAG_0","get differences.");
        interval = Math.toIntExact(Objects.requireNonNull(AverageAlgorithm.AverageAlgorithms.get(AverageMode)).apply(differences));
        //Log.d("TAG_0","get interval.");
        save();

        onPlayPauseButtonClicked();
    }

    private void startVibration() {
        handler.postDelayed(vibrationRunnable,interval);
    }

    private void stopVibration(){
        handler.removeCallbacks(vibrationRunnable);
    }


    public void showStartingDialog(Context context) {
        // 创建一个布局容器
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 30, 30, 30);
        layout.setGravity(Gravity.CENTER);

        // 创建圆圈细线控件
        WaveformCircleView circleLineView = new WaveformCircleView(context);
        circleLineView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400));
        circleLineView.startRecording();

        // 创建可勾选的同心圆控件
        CheckableCirclesBar checkableCircleView = new CheckableCirclesBar(context);
        checkableCircleView.setNumber(startingAccentCount);
        checkableCircleView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 300));

        // 将控件添加到布局中
        layout.addView(circleLineView);
        layout.addView(checkableCircleView);


        // 创建并显示Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(layout);
        builder.setCancelable(true); //可以点击外部区域关闭
        AlertDialog dialog = builder.create();

        // 设置窗口关闭时的监听器
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                circleLineView.stopRecording();
            }
        });

        accentTimes = new long[startingAccentCount];
        detectedAccentNum = 0;
        circleLineView.addAccentEventListener(new AccentEventListener() {
            @Override
            public void onAccentEvent(AccentEvent event) {
                accentTimes[detectedAccentNum] = event.getCurrentTime();
                checkableCircleView.setCheck(detectedAccentNum,true);
                detectedAccentNum++;
                if(detectedAccentNum == startingAccentCount)
                {
                    calculateAndStartBeats();
                    dialog.cancel();
                }

            }
        });

        // 显示弹窗
        dialog.show();

    }


    private void showVibratingDotSettingDialog() {
        // 创建一个Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // 创建Dialog布局
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.multi_vibrating_dot_setting_dialog_layout, null);
        LinearLayout container = dialogView.findViewById(R.id.itemsContainer);
        ImageButton addButton = dialogView.findViewById(R.id.addItemButton);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button confirmButton = dialogView.findViewById(R.id.confirmButton);

        // 填充Dialog中已有的数据
        for (int i = 0; i < soundsList.size(); i++) {
            addItem(container, soundsList.get(i), i);
        }

        // 监听添加按钮
        addButton.setOnClickListener(v -> {
            // 添加一项
            addItem(container, new ItemData("【None】", 50), soundsList.size());
        });

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();


        // 确认按钮，保存设置
        confirmButton.setOnClickListener(v -> {
            // 在此保存当前的设置
            soundsList.clear();
            for (int i = 0; i < container.getChildCount(); i++) {
                View itemView = container.getChildAt(i);
                Spinner spinner = itemView.findViewById(R.id.itemSpinner);
                SeekBar seekBar = itemView.findViewById(R.id.itemSeekBar);

                ItemData data = new ItemData(spinner.getSelectedItem().toString(), seekBar.getProgress());
                soundsList.add(data);
            }
            setVibratingDotSounds();
            save();
            dialog.dismiss();
        });

        // 取消按钮
        cancelButton.setOnClickListener(v -> dialog.dismiss());


        dialog.show();
    }

    // 用于动态添加物品项
    private void addItem(LinearLayout container, ItemData itemData, int index) {
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.multi_vibrating_dot_setting_item_layout, container, false);

        // 设置序号
        TextView indexText = itemView.findViewById(R.id.serialNumber);
        indexText.setText(String.valueOf(index + 1));

        // 设置下拉选框
        Spinner soundSpinner = itemView.findViewById(R.id.itemSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, audioManager.getAllSoundsName());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        soundSpinner.setAdapter(adapter);
        soundSpinner.setSelection(adapter.getPosition(itemData.getSoundName()));

        // 设置SeekBar
        SeekBar soundSeekBar = itemView.findViewById(R.id.itemSeekBar);
        soundSeekBar.setProgress(itemData.getSeekBarValue());

        // 设置删除按钮
        ImageButton deleteButton = itemView.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(v -> {
            if (container.getChildCount() > 1) {
                container.removeView(itemView);
                updateItemSerialNumbers(container);
            }
        });

        container.addView(itemView);
    }

    // 更新所有物品项的序号
    private void updateItemSerialNumbers(LinearLayout container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View itemView = container.getChildAt(i);
            TextView serialNumberView = itemView.findViewById(R.id.serialNumber);
            serialNumberView.setText(String.valueOf(i + 1));
        }
    }


    // 数据类，用于存储每个物品项的状态
    private static class ItemData implements Serializable{
        private String soundName;
        private int seekBarValue;

        public ItemData(String soundName, int seekBarValue) {
            this.soundName = soundName;
            this.seekBarValue = seekBarValue;
        }

        public String getSoundName() {
            return soundName;
        }

        public void setSoundName(String soundName) {
            this.soundName = soundName;
        }

        public int getSeekBarValue() {
            return seekBarValue;
        }

        public void setSeekBarValue(int seekBarValue) {
            this.seekBarValue = seekBarValue;
        }
    }


    public void showParameterSettingDialog() {
        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_setting_parameters, null);
        builder.setView(dialogView);

        // Initialize views
        final SeekBar seekBarThreshold = dialogView.findViewById(R.id.seekbar_threshold);
        final EditText editTextThreshold = dialogView.findViewById(R.id.edittext_threshold);
        final SeekBar seekBarRisingEdgeThreshold = dialogView.findViewById(R.id.seekbar_rising_edge_threshold);
        final EditText editTextRisingEdgeThreshold = dialogView.findViewById(R.id.edittext_rising_edge_threshold);
        final SeekBar seekBarRefractoryPeriod = dialogView.findViewById(R.id.seekbar_refractory_period);
        final EditText editTextRefractoryPeriod = dialogView.findViewById(R.id.edittext_refractory_period);
        final SeekBar seekBarFallingEdgeThreshold = dialogView.findViewById(R.id.seekbar_falling_edge_threshold);
        final EditText editTextFallingEdgeThreshold = dialogView.findViewById(R.id.edittext_falling_edge_threshold);
        Spinner averageModeSpinner = dialogView.findViewById(R.id.average_mode_spinner);
        SeekBar seekBarStartingBeat = dialogView.findViewById(R.id.seekbar_starting_beat);
        EditText editTextStartingBeat = dialogView.findViewById(R.id.edittext_starting_beat);


        AverageAlgorithm[] averageAlgorithms = AverageAlgorithm.getAllAverageModes();
        String[] averageModes = Arrays.stream(averageAlgorithms).map(AverageAlgorithm::getDescription).toArray(String[]::new);

        // 设置Spinner的Adapter
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, averageModes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        averageModeSpinner.setAdapter(spinnerAdapter);
        // 初始化Spinner的选中项为字段中的值
        int index = 0;
        for (int i = 0; i < averageAlgorithms.length; i++)
        {
            if(averageAlgorithms[i] == AverageMode)
            {
                index = i;
                break;
            }
        }
        averageModeSpinner.setSelection(index);

        // Set initial values from saved parameters
        seekBarThreshold.setProgress(threshold - 100); // SeekBar ranges from 0 to 31900
        editTextThreshold.setText(String.valueOf(threshold));
        seekBarRisingEdgeThreshold.setProgress(risingEdgeThreshold - 1000);
        editTextRisingEdgeThreshold.setText(String.valueOf(risingEdgeThreshold));
        seekBarRefractoryPeriod.setProgress(refractoryPeriod);
        editTextRefractoryPeriod.setText(String.valueOf(refractoryPeriod));
        seekBarFallingEdgeThreshold.setProgress(fallingEdgeThreshold - 1000);
        editTextFallingEdgeThreshold.setText(String.valueOf(fallingEdgeThreshold));
        seekBarStartingBeat.setProgress(startingAccentCount);
        editTextStartingBeat.setText(String.valueOf(startingAccentCount));

        // Update EditText when SeekBar changes
        seekBarThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //editTextThreshold.setText(String.valueOf(progress + 100));
                EditTextExtensions.setTextIfChanged(editTextThreshold,String.valueOf(progress + 100));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekBarRisingEdgeThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //editTextRisingEdgeThreshold.setText(String.valueOf(progress + 1000));
                EditTextExtensions.setTextIfChanged(editTextRisingEdgeThreshold,String.valueOf(progress + 1000));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekBarRefractoryPeriod.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //editTextRefractoryPeriod.setText(String.valueOf(progress));
                EditTextExtensions.setTextIfChanged(editTextRefractoryPeriod,String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekBarFallingEdgeThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //editTextFallingEdgeThreshold.setText(String.valueOf(progress + 1000));
                EditTextExtensions.setTextIfChanged(editTextFallingEdgeThreshold,String.valueOf(progress + 1000));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekBarStartingBeat.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //editTextStartingBeat.setText(String.valueOf(progress));
                EditTextExtensions.setTextIfChanged(editTextStartingBeat,String.valueOf(progress));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Update SeekBar when EditText changes
        editTextThreshold.addTextChangedListener(new SimpleTextWatcher(seekBarThreshold, 100));
        editTextRisingEdgeThreshold.addTextChangedListener(new SimpleTextWatcher(seekBarRisingEdgeThreshold, 1000));
        editTextRefractoryPeriod.addTextChangedListener(new SimpleTextWatcher(seekBarRefractoryPeriod, 0));
        editTextFallingEdgeThreshold.addTextChangedListener(new SimpleTextWatcher(seekBarFallingEdgeThreshold, 1000));
        editTextStartingBeat.addTextChangedListener(new SimpleTextWatcher(seekBarStartingBeat,0));

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);


        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(view-> dialog.dismiss());

        dialogView.findViewById(R.id.btn_confirm).setOnClickListener(view -> {
           threshold = Integer.parseInt(editTextThreshold.getText().toString());
           risingEdgeThreshold = Integer.parseInt(editTextRisingEdgeThreshold.getText().toString());
           refractoryPeriod = Integer.parseInt(editTextRefractoryPeriod.getText().toString());
           fallingEdgeThreshold = Integer.parseInt(editTextFallingEdgeThreshold.getText().toString());
           startingAccentCount = Integer.parseInt(editTextStartingBeat.getText().toString());
           AverageMode = averageAlgorithms[averageModeSpinner.getSelectedItemPosition()];
           waveformCircle.SetListenerArguments(this.threshold,this.risingEdgeThreshold,this.fallingEdgeThreshold,this.refractoryPeriod);

           save();
           dialog.dismiss();
        });

        dialog.show();
    }

    // Helper class to handle EditText changes
    public static class SimpleTextWatcher implements TextWatcher {
        private SeekBar seekBar;
        private int offset;

        public SimpleTextWatcher(SeekBar seekBar, int offset) {
            this.seekBar = seekBar;
            this.offset = offset;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int after) {
//            try {
//                int value = Integer.parseInt(charSequence.toString());
//                seekBar.setProgress(value - offset);
//            } catch (NumberFormatException e) {
//                // Handle invalid input
//            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            try {
                int value = Integer.parseInt(editable.toString());
                seekBar.setProgress(value - offset);
            } catch (NumberFormatException ignored) {

            }
        }
    }


    // 内部用于序列化的类
    public static class StartingBlockPanelSetting implements Serializable
    {
        private AverageAlgorithm AverageMode = AverageAlgorithm.ARITHMETIC_MEAN; // 平均模式，用于节拍间隔的计算
        private int interval = 200;
        private int startingAccentCount  = 4;
        private boolean isStarted = false;
        private List<ItemData> soundsList = new ArrayList<>();
        private int threshold = 12000;
        private int risingEdgeThreshold = 7800;
        private int refractoryPeriod = 200;
        private int fallingEdgeThreshold = 6800;

        public StartingBlockPanelSetting() {
            soundsList.add(new ItemData("【None】", 50));
        }

        public StartingBlockPanelSetting(AverageAlgorithm averageMode, int interval, int startingAccentCount, boolean isStarted, List<ItemData> soundsList, int threshold, int risingEdgeThreshold, int refractoryPeriod, int fallingEdgeThreshold) {
            this.AverageMode = averageMode;
            this.interval = interval;
            this.startingAccentCount = startingAccentCount;
            this.isStarted = isStarted;
            this.soundsList = soundsList;
            this.threshold = threshold;
            this.risingEdgeThreshold = risingEdgeThreshold;
            this.refractoryPeriod = refractoryPeriod;
            this.fallingEdgeThreshold = fallingEdgeThreshold;
        }
    }

    public void save()
    {
        StartingBlockPanelSetting setting = new StartingBlockPanelSetting(AverageMode,interval,startingAccentCount,isStarted,soundsList,threshold,risingEdgeThreshold,refractoryPeriod,fallingEdgeThreshold);
        FileManager.getInstance("").saveObject(fileName, setting);
    }

    public static void saveDefault(String fileName)
    {
        StartingBlockPanelSetting setting = new StartingBlockPanelSetting();
        FileManager.getInstance("").saveObject(fileName, setting);
    }

    public void load()
    {
        StartingBlockPanelSetting setting = FileManager.getInstance("").loadObject(fileName);
        if (setting == null){return;}
        this.AverageMode = setting.AverageMode;
        this.startingAccentCount = setting.startingAccentCount;
        this.isStarted = setting.isStarted;
        this.soundsList = setting.soundsList;
        this.threshold = setting.threshold;
        this.risingEdgeThreshold = setting.risingEdgeThreshold;
        this.refractoryPeriod = setting.refractoryPeriod;
        this.fallingEdgeThreshold = setting.fallingEdgeThreshold;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (isPlaying) {
            isPlaying = false;
            stopVibration();
        }
    }


}