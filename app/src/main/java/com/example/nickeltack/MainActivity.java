package com.example.nickeltack;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.ListFragment;

import com.example.nickeltack.fragments.TestFragment2;
import com.example.nickeltack.funclist.FuncListFragment;
import com.example.nickeltack.funclist.ListDialogFragment;
import com.example.nickeltack.funclist.PanelType;
import com.example.nickeltack.metronome.CommonMetronomeFragment;
import com.example.nickeltack.metronome.Metronome1Fragment;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private FuncListFragment listFragment;
    private View overlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listFragment = new FuncListFragment();
        overlay = findViewById(R.id.overlay);
        overlay.setVisibility(View.GONE);
        //hideListFragment();


        Button myButton = findViewById(R.id.button);
        myButton.setOnClickListener(view ->
            {
                Toast.makeText(MainActivity.this, "Button clicked!", Toast.LENGTH_SHORT).show();
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setCustomAnimations(R.anim.enter_anim, R.anim.exit_anim, R.anim.enter_anim, R.anim.exit_anim);
                transaction.replace(R.id.fragmentContainerView, new TestFragment2());
                transaction.addToBackStack(null);  // 添加到回退栈，null 表示不指定名称
                transaction.commit();
            }
        );

        Button showListButton = findViewById(R.id.showListButton);
        showListButton.setOnClickListener(v -> {
            // 弹出 ListFragment
            //showListFragment();
            ChangeUserInterface(PanelType.COMMON_METRONOME_PANEL);
            // showTemporarySnackbar(v, "Dialog Opened");
            SnackbarUtils.showTemporarySnackbar(findViewById(android.R.id.content), "zzz");

            //showListDialog();
        });

        overlay.setOnClickListener(v -> {
            hideListFragment();
            SnackbarUtils.showTemporarySnackbar(findViewById(android.R.id.content), "hide list");

        });


    }


    // 打开列表
    private void showListFragment() {
        // 使用 FragmentTransaction 来动态添加 ListFragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, listFragment); // 将 Fragment 放入容器
        transaction.addToBackStack(null); // 可选，允许回退到上一屏
        transaction.commit();
        overlay.setVisibility(View.VISIBLE);
    }

    // 关闭列表
    private void hideListFragment() {
        getSupportFragmentManager().beginTransaction().remove(listFragment).commit();
        Toast.makeText(MainActivity.this, "outside clicked!", Toast.LENGTH_SHORT).show();
        overlay.setVisibility(View.GONE);

    }



    //打开列表（Dialog版）
    public void showListDialog() {
        ListDialogFragment dialog = new ListDialogFragment();
        dialog.show(getSupportFragmentManager(), "ListDialog");
    }

    public void q(View view, String message) {
        // 创建 Snack bar 实例
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        // 获取 Snack bar 的根视图并设置半透明背景
        snackbar.getView().setBackgroundColor(Color.argb(128, 0, 0, 0));  // 半透明的黑色背景
        // 显示 Snack-bar
        snackbar.show();
    }

    private void ChangeUserInterface(@NonNull Fragment fragment)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        //transaction.setCustomAnimations(R.anim.enter_anim, R.anim.exit_anim, R.anim.enter_anim, R.anim.exit_anim);
        transaction.replace(R.id.fragmentContainerView, fragment);
        transaction.addToBackStack(null);  // 添加到回退栈，null 表示不指定名称
        transaction.commit();
    }


    public void ChangeUserInterface(PanelType panelType)
    {
        switch (panelType)
        {
            case COMMON_METRONOME_PANEL:
                ChangeUserInterface(new Metronome1Fragment());
                break;

            default:
                break;
        }
    }



}