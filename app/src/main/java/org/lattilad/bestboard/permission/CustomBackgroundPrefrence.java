package org.lattilad.bestboard.permission;

import android.content.Context;
import android.graphics.Color;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Some hints about changing Preference background:
 * https://stackoverflow.com/questions/16108609/android-creating-custom-preference/33842737
 * https://stackoverflow.com/questions/8143838/setting-preference-layout-and-changing-the-attribute-in-it
 */
public class CustomBackgroundPrefrence extends Preference
    {

    public CustomBackgroundPrefrence(Context context)
        {
        super(context);
        }

    public CustomBackgroundPrefrence(Context context, AttributeSet attrs)
        {
        super(context, attrs);
        }

    public CustomBackgroundPrefrence(Context context, AttributeSet attrs, int defStyle)
        {
        super(context, attrs, defStyle);
        }

    @Override
    public View getView(View convertView, ViewGroup parent)
        {
        View v = super.getView(convertView, parent);
        v.setBackgroundColor( Color.GREEN );
        return v;
        }

    }