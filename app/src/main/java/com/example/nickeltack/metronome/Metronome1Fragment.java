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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.nickeltack.R;


public class Metronome1Fragment extends Fragment {


    public Metronome1Fragment() {
        // Required empty public constructor
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
                try {
                    int value = Integer.parseInt(input);

                    // 如果超出范围，给出提示并清除输入
                    if (value < 0 || value > 500) {
                        editText.setError("输入的值必须在 0 到 500 之间");
                    }
                } catch (NumberFormatException e) {
                    // 如果输入的不是数字，给出提示
                    editText.setError("请输入有效的数字");
                }
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
                Toast.makeText(getContext(), "选择了: " + selectedItem, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // 处理没有选择任何项的情况
            }
        });


        return rootView;

    }
}