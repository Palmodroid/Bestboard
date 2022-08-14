package org.lattilad.bestboard.permission;

import android.content.Context;
import android.graphics.Color;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class CustomBackgroundCheckboxPreference extends CheckBoxPreference
    {
    public CustomBackgroundCheckboxPreference(Context context, AttributeSet attrs, int defStyleAttr)
        {
        super(context, attrs, defStyleAttr);
        }

    public CustomBackgroundCheckboxPreference(Context context, AttributeSet attrs)
        {
        super(context, attrs);
        }

    public CustomBackgroundCheckboxPreference(Context context)
        {
        super(context);
        }

    @Override
    public View getView(View convertView, ViewGroup parent)
        {
        View v = super.getView(convertView, parent);
        v.setBackgroundColor( Color.CYAN );
        return v;
        }
    }
