package com.example.nickeltack.funclist;

import android.content.Context;

import com.example.nickeltack.MainActivity;

import java.io.Serializable;

public class ListItem implements Serializable {

    private final PanelType panelType;
    private final String PanelName;

    public ListItem(PanelType panelType, String PanelName) {
        this.panelType = panelType;
        this.PanelName = PanelName;
    }

    public String getPanelName() {
        return PanelName;
    }

    public PanelType getPanelType()
    {
        return panelType;
    }

    public void OnClick()
    {
        MainActivity.instance.ChangeUserInterface(this.panelType);
    }

    public void OnLongClick()
    {
        MainActivity.instance.showDeleteFileDialog(this);
    }


}

