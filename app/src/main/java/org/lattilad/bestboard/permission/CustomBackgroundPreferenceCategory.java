package org.lattilad.bestboard.permission;

import android.content.Context;
import android.graphics.Color;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class CustomBackgroundPreferenceCategory extends PreferenceCategory
    {

    public CustomBackgroundPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr)
        {
        super(context, attrs, defStyleAttr);
        }

    public CustomBackgroundPreferenceCategory(Context context, AttributeSet attrs)
        {
        super(context, attrs);
        }

    public CustomBackgroundPreferenceCategory(Context context)
        {
        super(context);
        }

    @Override
    public View getView(View convertView, ViewGroup parent)
        {
        View v = super.getView(convertView, parent);
        v.setBackgroundColor( Color.RED );
        return v;
        }

    }
