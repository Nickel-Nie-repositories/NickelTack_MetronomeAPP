package com.example.nickeltack.funclist;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nickeltack.MainActivity;
import com.example.nickeltack.R;

import java.util.List;

// RecyclerView Adapter
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    public static final int VIEW_TYPE_ITEM = 0;
    public static final int VIEW_TYPE_ADD_BUTTON = 1;
    private final List<ListItem> mItemList;

    // 构造函数
    public MyAdapter(List<ListItem> itemList) {
        this.mItemList = itemList;
    }

    @Override
    public int getItemViewType(int position) {
        // 假设数据列表的最后一个是添加按钮
        if (position == mItemList.size()  - 1) {
            return VIEW_TYPE_ADD_BUTTON;  // 添加按钮视图类型
        } else {
            return VIEW_TYPE_ITEM;  // 常规项视图类型
        }
    }

    // 创建新视图
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_ADD_BUTTON) {
            // 创建并返回添加按钮视图
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_add, parent, false);
        } else {
            // 创建并返回常规列表项视图
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        }
        return new ViewHolder(view);
    }

    // 绑定数据到视图
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ListItem item = mItemList.get(position);

        if (getItemViewType(position) == VIEW_TYPE_ITEM) {
            holder.textView.setText(item.getPanelName());
            holder.imageView.setImageResource(PanelType.getIconResource(item.getPanelType()));
            holder.itemView.setOnClickListener(v -> {
                // 处理点击事件
                item.OnClick();
            });
        }
        else {
            holder.itemView.setOnClickListener(v -> {
                // 处理点击事件
                MainActivity.instance.showCreateDialog();
            });
        }


    }

    // 返回数据项的总数
    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    // ViewHolder 内部类
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text);
            imageView = itemView.findViewById(R.id.icon);
        }
    }
}
