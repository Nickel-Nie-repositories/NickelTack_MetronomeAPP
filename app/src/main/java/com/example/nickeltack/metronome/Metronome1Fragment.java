package com.example.nickeltack.metronome;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.nickeltack.R;
import com.example.nickeltack.funclist.FileManager;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;


public class Metronome1Fragment extends Fragment {


    private Fraction timeSignature = new Fraction("4/4");
    private int quarterTimeValue = 120;
    private VibratingDotCircleView vibratingDotCircleView;

    private String fileName = "";

    boolean isPlaying = false;

    boolean isUserInteraction = false;
    private EditText editText;
    private Spinner spinner;
    private ArrayAdapter<CharSequence> adapter;

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


        // 设置拍号选择的默认值
        spinner = rootView.findViewById(R.id.beat_selector);

        // 创建适配器
        adapter = ArrayAdapter.createFromResource(getContext(), R.array.beat_numbers, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // 拍号选择器的响应
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // 根据选择的项执行相应操作
                String selectedItem = parentView.getItemAtPosition(position).toString();
                //Toast.makeText(getContext(), "选择了: " + selectedItem, Toast.LENGTH_SHORT).show();
                timeSignature = new Fraction(selectedItem);
                updateVibratingDotSetting();
                if(isUserInteraction){save();}
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // 处理没有选择任何项的情况
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


        // 载入。
        if(!Objects.equals(fileName, "")) {load();}

        spinner.post(() -> isUserInteraction = true);

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
        if (vibratingDotCircleView == null){return;}
        stopVibrating();
        int denominator = timeSignature.getDenominator();
        int nominator = timeSignature.getNumerator();
        vibratingDotCircleView.setNumDots(nominator);
        int interval = Math.round(60000f / quarterTimeValue) * 4 / denominator;
        int[] intervals = new int[1];
        intervals[0] = interval;
        vibratingDotCircleView.setIntervals(intervals);
    }

    private void updateSoundsSetting(String[] sounds)
    {
        if(vibratingDotCircleView == null || sounds == null || sounds.length == 0){return;}
        //Log.d("TAG_0","set sounds:"+ Arrays.toString(sounds));
        vibratingDotCircleView.setSounds(sounds);
    }

    private void updateUIs()
    {
        editText.setText(String.valueOf(quarterTimeValue));
        // 设置默认选中项
        int index = adapter.getPosition(timeSignature.toString());
        //Log.d("TAG_0","getPosition: item:" + timeSignature.toString() + " Position: "+ index);
        if (index > 0)
        {
            spinner.setSelection(index);
        }
    }


    // 内部用于序列化的类
    public static class MetronomePanelSetting implements Serializable
    {
        public Fraction timeSignature = new Fraction("4/4");
        public int quarterTimeValue = 120;
        public String[] Sounds = {"【None】", "【None】", "【None】", "【None】"};

        MetronomePanelSetting()
        {

        }

        MetronomePanelSetting(Fraction timeSignature, int quarterTimeValue, String[] sounds)
        {
            this.quarterTimeValue = quarterTimeValue;
            this.timeSignature = timeSignature;
            this.Sounds = sounds;
        }
    }

    public void save()
    {
        MetronomePanelSetting setting = new MetronomePanelSetting(timeSignature, quarterTimeValue, vibratingDotCircleView.getSounds());
        //Log.d("TAG_0", "save Panel: " +fileName+", value:" + timeSignature);
        FileManager.getInstance("").saveObject(fileName, setting);
    }

    public static void saveDefault(String fileName)
    {
        MetronomePanelSetting setting = new MetronomePanelSetting();
        FileManager.getInstance("").saveObject(fileName, setting);
    }

    public void load()
    {
        MetronomePanelSetting setting = FileManager.getInstance("").loadObject(fileName);
        if (setting != null)
        {
            //Log.d("TAG_0", "load Panel: " +fileName+", value:" + setting.timeSignature);
            this.timeSignature = setting.timeSignature;
            this.quarterTimeValue = setting.quarterTimeValue;
            updateVibratingDotSetting();
            vibratingDotCircleView.post(() -> updateSoundsSetting(setting.Sounds));
            updateUIs();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        stopVibrating();
    }

}