package com.example.nickeltack.funclist;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.nickeltack.MainActivity;
import com.example.nickeltack.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class FuncListFragment extends Fragment {

    private RecyclerView recyclerView;
    private MyAdapter adapter;
    private List<ListItem> itemList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_func_list, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerView);

        // 准备数据
        itemList = new ArrayList<>();
        itemList.add(new ListItem("Item 1"));
        itemList.add(new ListItem("Item 2"));
        itemList.add(new ListItem("Item 3"));

        // 设置 RecyclerView 和 Adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MyAdapter(itemList);
        recyclerView.setAdapter(adapter);

        // 监听外部点击事件
//        rootView.setOnTouchListener((View v, MotionEvent event) -> {
//            if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
//                requireActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
//                Toast.makeText(getActivity(), "outside clicked!", Toast.LENGTH_SHORT).show();
//                v.performClick();
//            }
//            return true;
//        });

        return rootView;
    }
}