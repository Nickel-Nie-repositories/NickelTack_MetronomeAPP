package com.example.nickeltack.monitor;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.nickeltack.starting.AccentEvent;
import com.example.nickeltack.starting.AccentEventListener;
import com.example.nickeltack.starting.AverageAlgorithm;
import com.example.nickeltack.starting.StartingBlockFragment;

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
    private int settingReferenceBPM; // 设置中填入的参考BPM值
    private int currentReferenceBPM;
    private int currentReferenceMS;
    private boolean isRelativeToleranceEnabled; // 是否启用相对容差
    private boolean isAbsoluteToleranceEnabled; // 是否启用绝对容差
    private float relativeTolerance; // 相对容差，为百分比转换而来的小数，bpm的差距高于该值则说明节奏过快或过慢
    private int absoluteTolerance; //绝对容差，单位毫秒，时值差值超出该值则说明节奏过快或过慢


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
        simulateLogUpdates();

        waveformView.addAccentEventListener(new AccentEventListener() {
            @Override
            public void onAccentEvent(AccentEvent event) {
                onAccentDetected(event.getAmplitude());
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
        waveformView.startRecording();

    }

    private void stopRecording() {
        waveformView.stopRecording();
    }

    private void exportFile() {
        // 导出文件逻辑
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

    // 设置大数字内容和颜色
    public void setLargeNumber(String number, int color) {
        largeNumber.setText(number);
        largeNumber.setTextColor(color);
    }

    // 设置小数字内容和颜色
    public void setSmallNumber(String number, int color) {
        smallNumber.setText(number);
        smallNumber.setTextColor(color);
    }


    private void simulateLogUpdates() {
        // 模拟定时增加日志
        new Handler().postDelayed(() -> {
            addLogMessage("Clock Log Message " + System.currentTimeMillis());
            simulateLogUpdates(); // 循环调用
        }, 2000);
    }

    public void addLogMessage(String message) {
        logTextView.post(()->logTextView.append(message + "\n"));
        logScrollView.post(() -> logScrollView.fullScroll(View.FOCUS_DOWN)); // 滚动到底部
    }

    private void onAccentDetected(int amplitude)
    {
        addLogMessage("检测到重音,振幅数据：" + amplitude);
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
                editTextThreshold.setText(String.valueOf(progress + 100));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekBarRisingEdgeThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editTextRisingEdgeThreshold.setText(String.valueOf(progress + 1000));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekBarRefractoryPeriod.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editTextRefractoryPeriod.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekBarFallingEdgeThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editTextFallingEdgeThreshold.setText(String.valueOf(progress + 1000));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekBarSettingReferenceValue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editTextSettingReferenceValue.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekBarRelativeTolerance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editTextRelativeTolerance.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekBarAbsoluteTolerance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editTextAbsoluteTolerance.setText(String.valueOf(progress));
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


}