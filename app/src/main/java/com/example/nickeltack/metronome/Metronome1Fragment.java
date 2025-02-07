package com.example.nickeltack.metronome;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.nickeltack.R;

import java.io.Serializable;


public class Metronome1Fragment extends Fragment {


    private Fraction timeSignature = new Fraction("4/4");
    private int quarterTimeValue = 120;
    private VibratingDotCircleView vibratingDotCircleView;

    private String fileName;

    boolean isPlaying = false;

    public Metronome1Fragment() {
        // Required empty public constructor
    }

    public Metronome1Fragment(String fileName)
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

        View rootView = inflater.inflate(R.layout.fragment_metronome1, container, false);

        // 设置 bpm输入框的 范围：

        EditText editText = rootView.findViewById(R.id.bpm_input);

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
            }
        });


        // 设置拍号选择的默认值
        Spinner spinner = rootView.findViewById(R.id.beat_selector);

        // 创建适配器
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.beat_numbers, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // 设置默认选中项，假设我们想选中第二个项（索引从 0 开始）
        spinner.setSelection(0);

        // 拍号选择器的响应
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // 根据选择的项执行相应操作
                String selectedItem = parentView.getItemAtPosition(position).toString();
                //Toast.makeText(getContext(), "选择了: " + selectedItem, Toast.LENGTH_SHORT).show();
                timeSignature = new Fraction(selectedItem);
                updateVibratingDotSetting();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // 处理没有选择任何项的情况
            }
        });

        // 振动点配置
        vibratingDotCircleView = rootView.findViewById(R.id.vibrating_dot_circle);

        // 绑定开始/停止按钮
        Button palyButton = rootView.findViewById(R.id.play_pause_button);
        Button stopButton = rootView.findViewById(R.id.stop_button);
        palyButton.setOnClickListener((view) -> startVibrating());
        stopButton.setOnClickListener((view) -> stopVibrating());
        updateVibratingDotSetting();

        return rootView;

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
        int denominator = timeSignature.getDenominator();
        int nominator = timeSignature.getNumerator();
        vibratingDotCircleView.setNumDots(nominator);
        int interval = Math.round(60000f / quarterTimeValue) * 4 / denominator;
        int[] intervals = new int[1];
        intervals[0] = interval;
        vibratingDotCircleView.setIntervals(intervals);
    }

    private void updateSoundsSetting()
    {

    }

    private void updateSoundsRecord()
    {

    }


    // 内部用于序列化的类
    public static class MetronomePanelSetting implements Serializable
    {
        private Fraction timeSignature = new Fraction("4/4");
        private int quarterTimeValue = 120;
        private String[] Sounds;
    }

    public void save()
    {

    }

    public void load()
    {

    }


}