package com.example.nickeltack.metronome;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.example.nickeltack.R;
import com.example.nickeltack.funclist.FileManager;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;


public class ComplexMetronomeFragment extends Fragment {

    int quarterTimeValue = 120;
    VibratingDotCircleView vibratingDotCircleView;
    boolean isPlaying = false;

    private String rhythmsInput = "1/4 + 1/4 + 1/8 + 1/8 + 1/8 + 1/8"; // 用于存储输入的节奏型字符串

    private String fileName;
    private EditText editText;

    boolean isUserInteraction = false;

    public ComplexMetronomeFragment() {
        // Required empty public constructor
    }

    public ComplexMetronomeFragment(String fileName)
    {
        this.fileName = fileName;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_complex_metronome, container, false);

        // 设置 bpm输入框的 范围：
        editText = rootView.findViewById(R.id.bpm_input);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String input = editable.toString();
                int value = 0;
                try {
                    value = Integer.parseInt(input);

                    // 如果超出范围，给出提示并清除输入
                    if (value < 0 || value > 500) {
                        editText.setError("输入的值必须在 0 到 500 之间");
                    }
                } catch (NumberFormatException e) {
                    // 如果输入的不是数字，给出提示
                    editText.setError("请输入有效的数字");
                }
                if (value!= 0)
                {
                    quarterTimeValue = value;
                }
                updateVibratingDotSetting();
                if(isUserInteraction){save();}
            }
        });

        rootView.findViewById(R.id.rhythmic_pattern_input).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog();
            }
        });

        // 振动点配置
        vibratingDotCircleView = rootView.findViewById(R.id.vibrating_dot_circle);
        vibratingDotCircleView.setSoundChangeListener(new SoundChangeEventListener() {
            @Override
            public void onSoundChangeEvent(SoundChangeEvent soundChangeEvent) {
                save();
            }
        });

        // 绑定开始/停止按钮
        Button palyButton = rootView.findViewById(R.id.play_pause_button);
        Button stopButton = rootView.findViewById(R.id.stop_button);
        palyButton.setOnClickListener((view) -> startVibrating());
        stopButton.setOnClickListener((view) -> stopVibrating());

        updateVibratingDotSetting();

        if(!Objects.equals(fileName, "")) {load();}

        editText.post(() -> isUserInteraction = true);

        return rootView;
    }

    private void showInputDialog() {
        // 获取布局文件
        View dialogView = getLayoutInflater().inflate(R.layout.rhythm_input_dialog, null);

        // 获取控件
        EditText etInput = dialogView.findViewById(R.id.et_input);
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);

        // 填充上次输入的值
        etInput.setText(rhythmsInput);


        // 创建对话框
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("输入框")
                .setView(dialogView)
                .create();

        // 为输入框设置监听，验证输入是否合法
        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // 验证输入是否合法
                boolean isValid = isValidInput(charSequence.toString());
                btnConfirm.setEnabled(isValid);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // 点击确认按钮的逻辑
        btnConfirm.setOnClickListener(v -> {
            rhythmsInput = etInput.getText().toString();
            //Toast.makeText(getContext(), "输入已保存: " + rhythmsInput, Toast.LENGTH_SHORT).show();
            updateVibratingDotSetting();
            save();
            // 关闭对话框
            alertDialog.dismiss();
        });

        // 点击取消按钮的逻辑
        btnCancel.setOnClickListener(v -> alertDialog.dismiss());



        alertDialog.show();
    }

    // 验证输入是否合法的方法
    private boolean isValidInput(String input) {
        // 正则表达式验证规则：数字、分数和符号（加号、逗号、分号、空格）
        //String regex = "^(\\d+(\\.\\d+)?(\\/\\d+(\\.\\d+)?)?([+\\s,;]?\\d+(\\.\\d+)?(\\/\\d+(\\.\\d+)?)?)*)$";
        String regex = "^(\\s*\\d+(\\.\\d+)?(\\/\\d+(\\.\\d+)?)?\\s*([+\\s,;]\\s*\\d+(\\.\\d+)?(\\/\\d+(\\.\\d+)?)?\\s*)*)$";
        return input.matches(regex);
    }

    private void startVibrating()
    {
        if(!isPlaying){
            vibratingDotCircleView.startNonUniformVibrating();
            isPlaying = true;
        }
    }
    private void stopVibrating()
    {
        if(isPlaying){
            vibratingDotCircleView.stopNonUniformVibrating();
            isPlaying = false;
        }
    }

    private void updateVibratingDotSetting()
    {
        stopVibrating();
        int semibreveInterval = Math.round(60000f / quarterTimeValue) * 4;
        String[] tokens = rhythmsInput.split("[\\s,;+]+");
        //Log.d("TAG_0","tokens:" + Arrays.toString(tokens));
        int[] intervals = new int[tokens.length];
        int sumTimeValue = 0;
        for (int i=0; i<tokens.length; i++)
        {
            intervals[i] = (int) Math.round(StringToNumber(tokens[i])*semibreveInterval);
            //Log.d("TAG_0","interval[" + i+"]: "+ intervals[i]);
            sumTimeValue += intervals[i];
        }
        //Log.d("TAG_0","TimeValue Sum: "+ sumTimeValue);
        double[] newDotsAngles = new double[tokens.length-1];
        for (int i=0; i<newDotsAngles.length; i++)
        {
            newDotsAngles[i] = Math.PI * 2 * intervals[i] / sumTimeValue;
            //Log.d("TAG_0","Dot Angles[" + i+"]: "+ newDotsAngles[i]);
        }
        vibratingDotCircleView.setNumDots(intervals.length);
        vibratingDotCircleView.setIntervals(intervals);
        vibratingDotCircleView.setDotsAngles(newDotsAngles);
    }

    private double StringToNumber(String s){
        String[] parts = s.split("/");
        double ret;
        if (parts.length == 1) {
            ret = Double.parseDouble(parts[0]);
        }
        else if (parts.length == 2) {
            ret = Double.parseDouble(parts[0]) / Double.parseDouble(parts[1]);
        }
        else {
            ret = 0.0;
        }
        return ret;
    }


    private void updateSoundsSetting(String[] sounds)
    {
        if(vibratingDotCircleView == null){return;}
        //Log.d("TAG_0","set sounds:"+ Arrays.toString(sounds));
        vibratingDotCircleView.setSounds(sounds);
    }

    private void updateUIs()
    {
        editText.setText(String.valueOf(quarterTimeValue));
    }


    // 内部用于序列化的类
    public static class ComplexMetronomePanelSetting implements Serializable
    {
        private String rhythmsInput = "1/4 + 1/4 + 1/8 + 1/8 + 1/8 + 1/8";
        private int quarterTimeValue = 120;
        private String[] Sounds;

        ComplexMetronomePanelSetting()
        {

        }

        ComplexMetronomePanelSetting(String rhythmsInput, int quarterTimeValue, String[] sounds)
        {
            this.rhythmsInput = rhythmsInput;
            this.quarterTimeValue = quarterTimeValue;
            this.Sounds = sounds;
        }

    }

    public void save()
    {
        ComplexMetronomePanelSetting setting = new ComplexMetronomePanelSetting(rhythmsInput,quarterTimeValue,vibratingDotCircleView.getSounds());
        FileManager.getInstance("").saveObject(fileName, setting);
    }

    public static void saveDefault(String fileName)
    {
        ComplexMetronomePanelSetting setting = new ComplexMetronomePanelSetting();
        FileManager.getInstance("").saveObject(fileName, setting);
    }

    public void load()
    {
        ComplexMetronomePanelSetting setting = FileManager.getInstance("").loadObject(fileName);
        if (setting == null){return;}
        this.quarterTimeValue = setting.quarterTimeValue;
        this.rhythmsInput = setting.rhythmsInput;
        updateVibratingDotSetting();
        vibratingDotCircleView.post(() -> updateSoundsSetting(setting.Sounds));
        updateUIs();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        stopVibrating();
    }

}