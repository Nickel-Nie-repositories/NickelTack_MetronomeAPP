package com.example.nickeltack.funclist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.example.nickeltack.MainActivity;
import com.example.nickeltack.R;
import com.example.nickeltack.SnackbarUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ListDialogFragment extends DialogFragment {

    private List<ListItem> itemList;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // 创建一个 Dialog，包含 RecyclerView

        Log.d("TAG_0", "onCreate called");

        //AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        Context context = getActivity();
        assert context != null;
        Dialog dialog = new Dialog(context);

        //builder.setTitle("Select Item");

        itemList = new ArrayList<>();
        itemList.add(new ListItem(PanelType.COMMON_METRONOME_PANEL,"Item 1"));
        itemList.add(new ListItem(PanelType.COMPLEX_METRONOME_PANEL,"Item 2"));
        itemList.add(new ListItem(PanelType.RHYTHM_DIAGNOTOR_PANEL,"Item 3"));
        itemList.add(new ListItem(PanelType.STARTING_BLOCK_PANEL,"Item 4"));
        itemList.add(new ListItem(PanelType.NONE,"ADD"));

        dialog.setContentView(R.layout.fragment_list_dialog);

        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerViewForList);
        if (getContext() == null) {Log.e("TAG_0", "getContext() is null, cannot set LayoutManager");}
        if (recyclerView == null) {Log.e("TAG_0", "RecyclerView is null");}
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        MyAdapter adapter = new MyAdapter(itemList);  // 适配器需要初始化
        recyclerView.setAdapter(adapter);

        Window window = dialog.getWindow();
        if (window != null) {
            Log.d("TAG_0", "Set window size");
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.x = -20; // 设置水平偏移量
            layoutParams.y = 75; // 设置垂直偏移量
            layoutParams.width = 180;  // 设置宽度为屏幕宽度
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT; // 设置高度自适应内容
            window.setAttributes(layoutParams);
            window.setGravity(Gravity.TOP);
        }


        return dialog;
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("TAG_0", "onViewCreated called");
    }

    @Override
    public void onStart() {
        super.onStart();

        //Log.d("TAG_0", "调整弹窗大小");
        // 获取 Dialog 的 Window
//        Window window = Objects.requireNonNull(getDialog()).getWindow();
//        if (window != null) {
            // 设置对话框的位置和大小
            //Log.d("TAG_0", "调整弹窗大小2");
//            window.setGravity(Gravity.START); // 靠左显示
//            WindowManager.LayoutParams layoutParams = window.getAttributes();
//            layoutParams.x = 0; // 设置水平偏移量
//            layoutParams.y = 50; // 设置垂直偏移量
//            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;  // 设置宽度为屏幕宽度
//            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT; // 设置高度自适应内容
//            window.setAttributes(layoutParams);
            //window.setWindowAnimations(R.style.DialogAnimation);

//        }
    }

}