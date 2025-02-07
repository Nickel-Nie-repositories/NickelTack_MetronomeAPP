package com.example.nickeltack.monitor;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.nickeltack.R;
import com.example.nickeltack.metronome.ColorManager;
import com.example.nickeltack.metronome.Fraction;
import com.example.nickeltack.starting.AccentEvent;
import com.example.nickeltack.starting.AccentEventListener;
import com.example.nickeltack.starting.AverageAlgorithm;
import com.example.nickeltack.starting.EditTextExtensions;
import com.example.nickeltack.starting.StartingBlockFragment;

import java.io.Serializable;
import java.util.Arrays;


public class RhythmDiagnotorFragment extends Fragment {

    private WaveformView waveformView;
    private TextView largeNumber, smallNumber;
    private Button startStopButton, exportButton, settingsButton;

    private boolean isRecording = false;

    private FrameLayout waveformContainer;

    private TextView logTextView;
    private ScrollView logScrollView;

    private GridBackgroundView gridBackgroundView;

    private int threshold = 12000;
    private int risingEdgeThreshold = 7800;
    private int refractoryPeriod = 200;
    private int fallingEdgeThreshold = 6800;

    private ReferenceSourceModel referenceSourceModel = ReferenceSourceModel.REFERENCE_VALUE; // 参考源模式，选择以哪一个节奏数值作为参考
    private int settingReferenceBPM = 181; // 设置中填入的参考BPM值
    private int currentReferenceBPM;
    private int currentReferenceMS;
    private boolean isRelativeToleranceEnabled = false; // 是否启用相对容差
    private boolean isAbsoluteToleranceEnabled = false; // 是否启用绝对容差
    private float relativeTolerance = 0.1f; // 相对容差，为百分比转换而来的小数，bpm的差距高于该值则说明节奏过快或过慢
    private int absoluteTolerance = 100; //绝对容差，单位毫秒，时值差值超出该值则说明节奏过快或过慢

    private long lastAccentTime = 0;

    private MovingAverageQueue<Integer> globalMeanQueue = new MovingAverageQueue<>(50);
    private MovingAverageQueue<Integer> recentMeanQueue = new MovingAverageQueue<>(5);

    private int errorLevel = 0; //误差等级，共三级，表示本次节奏与参考节奏的误差量

    private String fileName;


    public RhythmDiagnotorFragment() {

    }
    public RhythmDiagnotorFragment(String fileName)
    {
        this.fileName = fileName;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rhythm_diagnotor, container, false);

        // 初始化控件
        waveformView = view.findViewById(R.id.waveformView);
        largeNumber = view.findViewById(R.id.largeNumber);
        smallNumber = view.findViewById(R.id.smallNumber);
        startStopButton = view.findViewById(R.id.startStopButton);
        exportButton = view.findViewById(R.id.exportButton);
        settingsButton = view.findViewById(R.id.settingsButton);

        waveformContainer = view.findViewById(R.id.waveformContainer);
        gridBackgroundView = view.findViewById(R.id.over_grid_background);

        logTextView = view.findViewById(R.id.logTextView);
        logScrollView = view.findViewById(R.id.logScrollView);
        //simulateLogUpdates();

        waveformView.addAccentEventListener(new AccentEventListener() {
            @Override
            public void onAccentEvent(AccentEvent event) {
                onAccentDetected(event.getAmplitude(), event.getCurrentTime());
            }
        });

        // 设置按钮点击事件
        setupButtons();

        return view;
    }

    private void setupButtons() {
        // 开始/停止按钮逻辑
        startStopButton.setOnClickListener(v -> {
            isRecording = !isRecording;
            startStopButton.setText(isRecording ? "停止" : "开始");

            if (isRecording) {
                gridBackgroundView.startAnimation();
                startRecording();
            } else {
                gridBackgroundView.stopAnimation();
                stopRecording();
            }
        });

        // 导出文件按钮逻辑
        exportButton.setOnClickListener(v -> exportFile());

        // 设置按钮逻辑
        settingsButton.setOnClickListener(v -> openSettings());
    }

    private void startRecording() {
        logTextView.setText("");
        resetTempParameters();
        waveformView.startRecording();
    }

    private void stopRecording() {
        waveformView.stopRecording();
    }

    private void exportFile() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain"); // 设置分享内容的类型（这里是文本）
        shareIntent.putExtra(Intent.EXTRA_TEXT, logTextView.getText()); // 设置要分享的文本内容
        startActivity(Intent.createChooser(shareIntent, "选择分享应用")); // 创建选择对话框，显示所有支持分享文本的应用
    }

    private void openSettings() {
        if (isRecording)
        {
            isRecording = false;
            startStopButton.setText("开始");
            gridBackgroundView.stopAnimation();
            stopRecording();
        }
        showParameterSettingDialog();
    }


    public void setLargeNumber(String number) {
        largeNumber.post(()-> largeNumber.setText(number));
    }
    public void setLargeNumberColor(int color) {
        largeNumber.post(()-> largeNumber.setTextColor(color));

    }

    public void setSmallNumber(String number) {
        smallNumber.post(new Runnable() {
            @Override
            public void run() {
                smallNumber.setText(number);
            }
        });
    }


    private void simulateLogUpdates() {
        // 模拟定时增加日志
        new Handler().postDelayed(() -> {
            addLogMessage("Clock Log Message " + System.currentTimeMillis());
            simulateLogUpdates();
        }, 2000);
    }

    public void addLogMessage(String message) {
        logTextView.post(()->logTextView.append(message + "\n"));
        logScrollView.post(() -> logScrollView.fullScroll(View.FOCUS_DOWN)); // 滚动到底部
    }

    private void onAccentDetected(int amplitude, long currentTime)
    {
        addLogMessage("检测到重音,振幅数据：" + amplitude);
        if (lastAccentTime == 0)
        {
            addLogMessage("————本次录制第一拍：" + currentTime);
        }
        else
        {
            // 不是第一拍则计算间隔 和 BPM
            int interval = (int) (currentTime - lastAccentTime);
            addLogMessage("————本拍毫秒数："+ interval);
            int currBPM = Math.round(60000f / interval);
            //Log.d("TAG_0","计算BPM" + currBPM);
            // 与参考值比对，获得BPM的真值
            Fraction fraction = new Fraction((double) currentReferenceBPM / currBPM);
            int trueCurrBPM = currBPM * fraction.getNumerator() / fraction.getDenominator();
            setLargeNumber(trueCurrBPM + " BPM");
            setSmallNumber("（*" + fraction.toString() + "）");
            //Log.d("TAG_0","显示BPM" + trueCurrBPM);
            int errorSymbol = Integer.compare(trueCurrBPM - currentReferenceBPM,0); //误差符号，可取0 ，-1 ，+1，表示误差的正负。
//            int[] errorLevel = {0}; //误差等级，共三级，表示本次节奏与参考节奏的误差量 // java不能引用传值...
            // 与参考值比对，检查节奏
            if (isRelativeToleranceEnabled)
            {
                double relativeError = (double) Math.abs(trueCurrBPM - currentReferenceBPM) / currentReferenceBPM;
                onRelativeErrorDetected(relativeError);
            }
            if (isAbsoluteToleranceEnabled)
            {
                int absoluteError = Math.abs(interval - currentReferenceMS);
                onAbsoluteErrorDetected(absoluteError);
            }
            setLargeNumberColor(ColorManager.errorLevelColors[ColorManager.errorLevelPivot + errorSymbol * errorLevel]);
            errorLevel = 0;
            // 更新参考值
            updateCurrentReferenceBPM(trueCurrBPM);
        }
        lastAccentTime = currentTime;
    }

    private void onRelativeErrorDetected(double relativeError)
    {
        for (int i =3; i > 0; i--)
        {
            if(relativeError > i*relativeTolerance)
            {
                errorLevel = Math.max(errorLevel,i);
                addLogMessage("————超出相对误差容限："+ Math.round(relativeTolerance*100) + "%，相对误差为："+ Math.round(relativeError*100) + "%");
                return;
            }
        }
    }
    private void onAbsoluteErrorDetected(int absoluteError)
    {
        for (int i =3; i > 0; i--)
        {
            if(absoluteError > i*absoluteTolerance)
            {
                errorLevel = Math.max(errorLevel,i);
                addLogMessage("————超出绝对误差容限："+ absoluteTolerance + "ms，绝对误差为："+ absoluteError + "ms");
                return;
            }
        }
    }

    public void showParameterSettingDialog() {
        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_setting_diagnotor_parameters, null);
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
        Spinner referenceSourceSpinner = dialogView.findViewById(R.id.spinner_reference_source_mode);
        SeekBar seekBarSettingReferenceValue = dialogView.findViewById(R.id.seekbar_setting_reference_value);
        EditText editTextSettingReferenceValue = dialogView.findViewById(R.id.edittext_setting_reference_value);
        final CheckBox checkBoxIsRelativeToleranceEnabled = dialogView.findViewById(R.id.checkbox_is_relative_tolerance_enabled);
        final CheckBox checkBoxIsAbsoluteToleranceEnabled = dialogView.findViewById(R.id.checkbox_is_absolute_tolerance_enabled);
        final SeekBar seekBarRelativeTolerance = dialogView.findViewById(R.id.seekbar_relative_tolerance);
        final EditText editTextRelativeTolerance = dialogView.findViewById(R.id.edittext_relative_tolerance);
        final SeekBar seekBarAbsoluteTolerance = dialogView.findViewById(R.id.seekbar_absolute_tolerance);
        final EditText editTextAbsoluteTolerance = dialogView.findViewById(R.id.edittext_absolute_tolerance);


        ReferenceSourceModel[] referenceSourceModels =ReferenceSourceModel.values();
        String[] referenceSourceModelStringsArrays = Arrays.stream(referenceSourceModels).map(ReferenceSourceModel::getDescription).toArray(String[]::new);

        // 设置Spinner的Adapter
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, referenceSourceModelStringsArrays);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        referenceSourceSpinner.setAdapter(spinnerAdapter);
        int index = 0;
        for (int i = 0; i < referenceSourceModels.length; i++)
        {
            if(referenceSourceModels[i] == referenceSourceModel)
            {
                index = i;
                break;
            }
        }
        referenceSourceSpinner.setSelection(index);

        // 初始化checkbox的显示
        checkBoxIsRelativeToleranceEnabled.setChecked(isRelativeToleranceEnabled);
        checkBoxIsAbsoluteToleranceEnabled.setChecked(isAbsoluteToleranceEnabled);

        // Set initial values from saved parameters
        seekBarThreshold.setProgress(threshold - 100); // SeekBar ranges from 0 to 31900
        editTextThreshold.setText(String.valueOf(threshold));
        seekBarRisingEdgeThreshold.setProgress(risingEdgeThreshold - 1000);
        editTextRisingEdgeThreshold.setText(String.valueOf(risingEdgeThreshold));
        seekBarRefractoryPeriod.setProgress(refractoryPeriod);
        editTextRefractoryPeriod.setText(String.valueOf(refractoryPeriod));
        seekBarFallingEdgeThreshold.setProgress(fallingEdgeThreshold - 1000);
        editTextFallingEdgeThreshold.setText(String.valueOf(fallingEdgeThreshold));
        seekBarSettingReferenceValue.setProgress(settingReferenceBPM);
        editTextSettingReferenceValue.setText(String.valueOf(settingReferenceBPM));
        seekBarRelativeTolerance.setProgress(Math.round(relativeTolerance*100));
        editTextRelativeTolerance.setText(String.valueOf(Math.round(relativeTolerance*100)));
        seekBarAbsoluteTolerance.setProgress(absoluteTolerance);
        editTextAbsoluteTolerance.setText(String.valueOf(absoluteTolerance));

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
        seekBarSettingReferenceValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //editTextSettingReferenceValue.setText(String.valueOf(progress));
                EditTextExtensions.setTextIfChanged(editTextSettingReferenceValue,String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekBarRelativeTolerance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //editTextRelativeTolerance.setText(String.valueOf(progress));
                EditTextExtensions.setTextIfChanged(editTextRelativeTolerance,String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekBarAbsoluteTolerance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //editTextAbsoluteTolerance.setText(String.valueOf(progress));
                EditTextExtensions.setTextIfChanged(editTextAbsoluteTolerance,String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Update SeekBar when EditText changes
        editTextThreshold.addTextChangedListener(new StartingBlockFragment.SimpleTextWatcher(seekBarThreshold, 100));
        editTextRisingEdgeThreshold.addTextChangedListener(new StartingBlockFragment.SimpleTextWatcher(seekBarRisingEdgeThreshold, 1000));
        editTextRefractoryPeriod.addTextChangedListener(new StartingBlockFragment.SimpleTextWatcher(seekBarRefractoryPeriod, 0));
        editTextFallingEdgeThreshold.addTextChangedListener(new StartingBlockFragment.SimpleTextWatcher(seekBarFallingEdgeThreshold, 1000));
        editTextSettingReferenceValue.addTextChangedListener(new StartingBlockFragment.SimpleTextWatcher(seekBarSettingReferenceValue,0));
        editTextRelativeTolerance.addTextChangedListener(new StartingBlockFragment.SimpleTextWatcher(seekBarRelativeTolerance,0));
        editTextAbsoluteTolerance.addTextChangedListener(new StartingBlockFragment.SimpleTextWatcher(seekBarAbsoluteTolerance,0));

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);


        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(view-> dialog.dismiss());

        dialogView.findViewById(R.id.btn_confirm).setOnClickListener(view -> {
            threshold = Integer.parseInt(editTextThreshold.getText().toString());
            risingEdgeThreshold = Integer.parseInt(editTextRisingEdgeThreshold.getText().toString());
            refractoryPeriod = Integer.parseInt(editTextRefractoryPeriod.getText().toString());
            fallingEdgeThreshold = Integer.parseInt(editTextFallingEdgeThreshold.getText().toString());
            settingReferenceBPM = Integer.parseInt(editTextSettingReferenceValue.getText().toString());
            referenceSourceModel = referenceSourceModels[referenceSourceSpinner.getSelectedItemPosition()];
            waveformView.SetListenerArguments(this.threshold,this.risingEdgeThreshold,this.fallingEdgeThreshold,this.refractoryPeriod);
            isRelativeToleranceEnabled = checkBoxIsRelativeToleranceEnabled.isChecked();
            isAbsoluteToleranceEnabled = checkBoxIsAbsoluteToleranceEnabled.isChecked();
            relativeTolerance = Integer.parseInt(editTextRelativeTolerance.getText().toString()) / 100f;
            absoluteTolerance = Integer.parseInt(editTextAbsoluteTolerance.getText().toString());

            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateCurrentReferenceMS()
    {
        this.currentReferenceMS = Math.round(60000f / currentReferenceBPM);
    }

    private void updateCurrentReferenceBPM(int currBeatBPM)
    {
        switch (referenceSourceModel)
        {
            case REFERENCE_VALUE:
                break;
            case REAL_TIME:
                currentReferenceBPM = currBeatBPM;
                updateCurrentReferenceMS();
                break;
            case GLOBAL_MEAN:
                currentReferenceBPM = (int) Math.round(globalMeanQueue.addData(currBeatBPM));
                updateCurrentReferenceMS();
                break;
            case RECENT_RHYTHM:
                currentReferenceBPM = (int) Math.round(recentMeanQueue.addData(currBeatBPM));
                updateCurrentReferenceMS();
                break;
        }
    }

    private void resetTempParameters()
    {
        lastAccentTime = 0;
        currentReferenceBPM = settingReferenceBPM;
        updateCurrentReferenceMS();
        globalMeanQueue.clear();
        recentMeanQueue.clear();
    }

    // 内部用于序列化的类
    public static class RhythmDiagnotorPanelSetting implements Serializable
    {
        private int threshold = 12000;
        private int risingEdgeThreshold = 7800;
        private int refractoryPeriod = 200;
        private int fallingEdgeThreshold = 6800;
        private ReferenceSourceModel referenceSourceModel = ReferenceSourceModel.REFERENCE_VALUE; // 参考源模式，选择以哪一个节奏数值作为参考
        private int settingReferenceBPM = 181; // 设置中填入的参考BPM值
        private boolean isRelativeToleranceEnabled = false; // 是否启用相对容差
        private boolean isAbsoluteToleranceEnabled = false; // 是否启用绝对容差
        private float relativeTolerance = 0.1f; // 相对容差，为百分比转换而来的小数，bpm的差距高于该值则说明节奏过快或过慢
        private int absoluteTolerance = 100; //绝对容差，单位毫秒，时值差值超出该值则说明节奏过快或过慢


    }

    public void save()
    {

    }

    public void load()
    {

    }


}