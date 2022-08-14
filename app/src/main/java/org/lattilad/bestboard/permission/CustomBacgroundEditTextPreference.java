package org.lattilad.bestboard.permission;

import android.content.Context;
import android.graphics.Color;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class CustomBacgroundEditTextPreference extends EditTextPreference
    {
    public CustomBacgroundEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr)
        {
        super(context, attrs, defStyleAttr);
        }

    public CustomBacgroundEditTextPreference(Context context, AttributeSet attrs)
        {
        super(context, attrs);
        }

    public CustomBacgroundEditTextPreference(Context context)
        {
        super(context);
        }

    @Override
    public View getView(View convertView, ViewGroup parent)
        {
        View v = super.getView(convertView, parent);
        v.setBackgroundColor( Color.YELLOW );
        return v;
        }

    }
