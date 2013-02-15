package com.wardware.givingtracker.ui;

import java.awt.GridBagConstraints;
import java.awt.Insets;

public class Gbc extends GridBagConstraints
{
    private static Gbc c = new Gbc();
    
    public static Gbc xyi(int x, int y, int i)
    {
        c.insets = new Insets(i, i, i, i);
        c.gridx = x; 
        c.gridy = y;
        c.gridwidth = 1;
        c.weightx = 0;
        return c;
    }
    
    public Gbc west()
    {
        c.anchor = GridBagConstraints.WEST;
        return c;
    }
    
    public Gbc east()
    {
        c.anchor = GridBagConstraints.EAST;
        return c;
    }
    
    public Gbc north()
    {
        c.anchor = GridBagConstraints.NORTH;
        return c;
    }
    
    public Gbc south()
    {
        c.anchor = GridBagConstraints.SOUTH;
        return c;
    }
    
    public Gbc horizontal()
    {
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        return c;
    }
    
    public Gbc both()
    {
        c.fill = GridBagConstraints.BOTH;
        return c;
    }
    
    public Gbc vertical()
    {
        c.fill = GridBagConstraints.VERTICAL;
        return c;
    }
    
    public Gbc gridWidth(int w) 
    {
        c.gridwidth = w;
        return c;
    }
    
    public Gbc top(int t)
    {
        c.insets.top = t;
        return c;
    }

    public Gbc bottom(int b)
    {
        c.insets.bottom = b;
        return c;
    }
    
    public Gbc left(int l)
    {
        c.insets.left = l;
        return c;
    }

    public Gbc right(int r)
    {
        c.insets.right = r;
        return c;
    }
}
