package com.example.nickeltack.funclist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nickeltack.R;

import java.util.List;

// RecyclerView Adapter
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private final List<ListItem> mItemList;

    // 构造函数
    public MyAdapter(List<ListItem> itemList) {
        this.mItemList = itemList;
    }

    // 创建新视图
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    // 绑定数据到视图
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ListItem item = mItemList.get(position);
        holder.textView.setText(item.getTitle());
    }

    // 返回数据项的总数
    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    // ViewHolder 内部类
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
        }
    }
}
