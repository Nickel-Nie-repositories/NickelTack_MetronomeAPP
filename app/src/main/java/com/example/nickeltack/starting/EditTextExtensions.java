package com.example.nickeltack.starting;

import android.widget.EditText;

public class EditTextExtensions {
    public static void setTextIfChanged(EditText editText, String newText) {
        if (!editText.getText().toString().equals(newText)) {
            editText.setText(newText);
            editText.setSelection(newText.length()); // 让光标保持在文本末尾
        }
    }
}
