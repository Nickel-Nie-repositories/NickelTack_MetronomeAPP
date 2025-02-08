package com.example.nickeltack.funclist;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.nickeltack.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class FuncListFragment extends Fragment {

    private RecyclerView recyclerView;
    private MyAdapter adapter;
    private List<ListItem> itemList = new ArrayList<>();

    private final String recordFileName = "ListForAll_259786859.rrr";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_func_list, container, false);

        recyclerView = rootView.findViewById(R.id.recyclerView);

//         准备数据
//        itemList.clear();
//        itemList.add(new ListItem(PanelType.COMMON_METRONOME_PANEL,"example 1"));
//        itemList.add(new ListItem(PanelType.COMPLEX_METRONOME_PANEL,"example 2"));
//        itemList.add(new ListItem(PanelType.RHYTHM_DIAGNOTOR_PANEL,"example 3"));
//        itemList.add(new ListItem(PanelType.STARTING_BLOCK_PANEL,"example 4"));
//        itemList.add(new ListItem(PanelType.NONE,"ADD"));

//        save();
//        Log.d("TAG_0","load start");
        load();
//        Log.d("TAG_0","load end");
        if (itemList.isEmpty())
        {
            itemList.add(new ListItem(PanelType.NONE,"ADD"));
        }

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

    public void removeListItem(ListItem item)
    {
        int index = itemList.indexOf(item);
        itemList.remove(item);
        adapter.notifyItemRemoved(index);
        save();
    }

    public void addListItem(ListItem item)
    {
        int insertIndex = itemList.size() - 1;
        itemList.add(insertIndex, item);
        adapter.notifyItemInserted(insertIndex);
        save();
    }

    public List<String> getUsedNames()
    {
        List<String> usedNames = new ArrayList<>();
        for ( int i = 0 ; i< itemList.size() ; i++)
        {
            usedNames.add(itemList.get(i).getPanelName());
        }
        return usedNames;
    }

    public void save()
    {
        File file = new File(requireContext().getFilesDir(), recordFileName);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(itemList);
        } catch (IOException e) {
            return;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @SuppressWarnings("unchecked")
    public void load()
    {

        File file = new File(requireContext().getFilesDir(), recordFileName);

        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            //Log.d("TAG_0","objectStream open");
            List<ListItem> temp = (List<ListItem>) in.readObject();
            if (temp == null){ Log.d("TAG_0","null!");return;}
            itemList.clear();
            for (ListItem listItem : temp) {
                int insertIndex = itemList.size();
                itemList.add(insertIndex, listItem);
                //Log.d("TAG_0","————list load:" + listItem.getPanelName());
            }

            //adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.d("TAG_0",e.toString());
            return;
        }

    }
}