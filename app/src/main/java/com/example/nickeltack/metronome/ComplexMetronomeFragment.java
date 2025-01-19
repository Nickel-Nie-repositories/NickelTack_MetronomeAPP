package com.example.nickeltack.metronome;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import com.example.nickeltack.MainActivity;
import com.example.nickeltack.R;


public class ComplexMetronomeFragment extends Fragment {


    private String rhythmsInput = ""; // 用于存储输入的节奏型字符串

    public ComplexMetronomeFragment() {
        // Required empty public constructor
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

        rootView.findViewById(R.id.rhythmic_pattern_input).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog();
            }
        });

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
            Toast.makeText(getContext(), "输入已保存: " + rhythmsInput, Toast.LENGTH_SHORT).show();
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
        String regex = "^(\\d+(\\.\\d+)?(\\/\\d+(\\.\\d+)?)?([+\\s,;]?\\d+(\\.\\d+)?(\\/\\d+(\\.\\d+)?)?)*)$";
        return input.matches(regex);
    }

}