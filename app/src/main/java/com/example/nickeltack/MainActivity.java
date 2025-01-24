package com.example.nickeltack;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.nickeltack.funclist.FileManager;
import com.example.nickeltack.funclist.FuncListFragment;
import com.example.nickeltack.funclist.ListDialogFragment;
import com.example.nickeltack.funclist.PanelType;
import com.example.nickeltack.metronome.CommonMetronomeFragment;
import com.example.nickeltack.metronome.ComplexMetronomeFragment;
import com.example.nickeltack.metronome.Metronome1Fragment;
import com.example.nickeltack.monitor.RhythmDiagnotorFragment;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FuncListFragment listFragment;
    private View overlay;

    public static MainActivity instance;
    private List<String> usedNames;

    private int selectedIconId = -1;

    private FileManager fileManager;

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

        usedNames = new ArrayList<>();
        fileManager = FileManager.getInstance(String.valueOf(getFilesDir()));

        instance = this;

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

        ImageButton showListButton = findViewById(R.id.showListButton);
        showListButton.setOnClickListener(v -> {
            // 弹出 ListFragment
            showListFragment();
            //ChangeUserInterface(PanelType.COMMON_METRONOME_PANEL);
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

            case COMPLEX_METRONOME_PANEL:
                ChangeUserInterface(new ComplexMetronomeFragment());
                break;

            case RHYTHM_DIAGNOTOR_PANEL:
                ChangeUserInterface(new RhythmDiagnotorFragment());
                break;

            default:
                break;
        }
    }


    public void showCreateDialog() {
        // 创建Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.create_dialog_layout, null);

        // 获取布局中的控件
        HorizontalScrollView scrollView = view.findViewById(R.id.scrollView_for_icons);
        EditText etName = view.findViewById(R.id.et_name);
        Button btnConfirm = view.findViewById(R.id.btn_confirm);
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        LinearLayout iconLayout = view.findViewById(R.id.iconLayout);

        // 添加图标
        List<Integer> iconIds = new ArrayList<>(PanelType.getIcons());

        // 你可以根据需要添加更多图标

        for (Integer iconId : iconIds) {
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(iconId);
            ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(200, 200);
            imageView.setLayoutParams(layoutParams);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(20, 20, 20, 20);
            imageView.setTag(iconId); // 用tag来标识每个图标
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 取消上一个选择的状态
                    for (int i = 0; i < iconLayout.getChildCount(); i++) {
                        ImageView iv = (ImageView) iconLayout.getChildAt(i);
                        iv.setBackground(null); // 取消背景
                    }
                    // 设置当前选中
                    imageView.setBackgroundResource(R.drawable.selected_border); // 选中的背景
                    selectedIconId = iconId;
                }
            });
            iconLayout.addView(imageView);
        }

        // 监听文本输入框
        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String name = s.toString().trim();
                // 检查文件名是否已经存在
                if (usedNames.contains(name)) {
                    // 如果重名，禁用确认按钮，并提示用户
                    btnConfirm.setEnabled(false);
                    etName.setError("文件名已存在，请重新输入");
                } else if (isValidFileName(name)) {
                    // 如果文件名有效且不重复，启用确认按钮
                    btnConfirm.setEnabled(true);
                    etName.setError(null);
                } else {
                    // 文件名无效
                    btnConfirm.setEnabled(false);
                    etName.setError("文件名包含非法字符");
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}
        });

        builder.setView(view);
        AlertDialog dialog = builder.create();

        // 提交按钮点击事件
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString().trim();
                if (usedNames.contains(name)) {
                    Toast.makeText(MainActivity.this, "文件名已存在", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
                else if (!isValidFileName(name))
                {
                    Toast.makeText(MainActivity.this, "文件名包含非法字符", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
                else if(selectedIconId == -1)
                {
                    Toast.makeText(MainActivity.this, "未选择面板类型", Toast.LENGTH_SHORT).show();
                }
                else {
                    // 处理确认逻辑
                    usedNames.add(name); // 将文件名加入已使用名单
                    Toast.makeText(MainActivity.this, "文件名已保存，文件类型ID为："+ selectedIconId, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }

            }
        });

        // 取消按钮点击事件
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 关闭Dialog
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    // 检查文件名是否合法
    private boolean isValidFileName(String name) {
        // 文件名不能包含的非法字符（在Android中，文件名不能包含以下字符）
        String illegalChars = "/\\?%*:|\"<>.";
        for (char c : illegalChars.toCharArray()) {
            if (name.indexOf(c) != -1) {
                return false;
            }
        }
        return true;
    }



}