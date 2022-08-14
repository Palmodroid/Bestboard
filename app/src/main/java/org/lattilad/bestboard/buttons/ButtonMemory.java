package org.lattilad.bestboard.buttons;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.lattilad.bestboard.R;
import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.parser.Tokenizer;
import org.lattilad.bestboard.scribe.Scribe;
import org.lattilad.bestboard.states.LayoutStates;
import org.lattilad.bestboard.states.MetaState;
import org.lattilad.bestboard.utils.StringUtils;

/**
 * ButtonMemory is a complex button:
 * 1. Empty memory:     MEM
 * 2. SHIFT-LOCK:       SEL
 * 3. Text stored:      text
 */
public class ButtonMemory extends ButtonMainTouch implements SisterButton, Cloneable
    {
    private final static int MEMORY_EMPTY = 1;
    private final static int MEMORY_SELECT = 2;
    private final static int MEMORY_TEXT = 3;

    private PacketTextSimple packet;
    private boolean done = false;
    private String abbreviation;

    private String prefsId = null;

    private int state;


    @Override
    public ButtonMemory clone()
        {
        return (ButtonMemory)super.clone();
        }

    // packet is obligatory, but can be empty
    // id is optional - if not null, then text is stored in preferences under this key
    public ButtonMemory( Long id, PacketTextSimple packet )
        {
        this.packet = packet;
        if ( id != null )
            {
            prefsId = "MEM_" + Tokenizer.regenerateKeyword( id );
            }
        // prefsId can be null, meaning: do not store memory text inside prefs
        }

    /**
     * Connect is not needed, because after connect setLayout will force to restore data
    @Override
    public void connect() { }
     */

    /**
     * Stores string of packet in prefrences under prefsId.
     * If prefsId is null - then store doesn't work.
     * @return true, if prefsId is NOT null, and string was stored
     */
    private void store( String string )
        {
        packet.setString(string);
        // Set a new memory string - if button has special id
        if ( prefsId != null )
            {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(
                    layout.softBoardData.softBoardListener.getApplicationContext());
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(prefsId, string);
            editor.apply();
            // Scribe.error("MEMORY BUTTON: " + prefsId + " stored: " + string );

            // refresh will refresh button itself!!
            layout.refreshSisterButton( prefsId );
            }
        }

    /**
     * An empty string should be stored - meaning, that original string was changed to no string
     * Otherwise no string could be set, if predefined string returns
     */
    private void clear()
        {
        store( "" );
        }

    @Override
    public void refreshSisterButton(String id)
        {
        // Try to retrieve, if id is defined
        if ( prefsId != null )
            {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(
                    layout.softBoardData.softBoardListener.getApplicationContext());
            // Try to read previously stored string from preferences
            String string = sharedPrefs.getString(prefsId, packet.getString());
            packet.setString(string);
            // Scribe.error("MEMORY BUTTON: " + prefsId + " retrieved: " + string );
            }

        if ( packet.getString().length() == 0 )
            {
            state = MEMORY_EMPTY;
            }
        else
            {
            state = MEMORY_TEXT;
            abbreviation = StringUtils.abbreviateString( packet.getTitleString(), 5 );
            }
        }


    @Override
    public boolean isColorChanging()
        {
        return true;
        }

    @Override
    public int getColor()
        {
        return (state == MEMORY_SELECT &&
                layout.softBoardData.layoutStates.metaStates[LayoutStates.META_SHIFT].getState() == MetaState.META_LOCK ) ?
                layout.softBoardData.lockColor : super.getColor();
        }

    @Override
    public boolean isFirstStringChanging()
        {
        return true;
        }

    @Override
    public String getFirstString()
        {
        if ( state == MEMORY_TEXT )
            return abbreviation;
        else if (state == MEMORY_SELECT )
            return "SEL";
        return  "MEM"; // MEMORY_EMPTY
        }


    @Override
    public void mainTouchStart( boolean isTouchDown )
        {
        if ( state == MEMORY_TEXT )
            {
            packet.send();
            done = true;

            layout.softBoardData.vibrate(SoftBoardData.VIBRATE_PRIMARY);
            }
        else if ( state == MEMORY_SELECT )
            {
            layout.softBoardData.layoutStates.metaStates[LayoutStates.META_SHIFT].setState(MetaState.META_OFF);
            store( layout.softBoardData.softBoardListener.getWordOrSelected() );

            layout.softBoardData.vibrate(SoftBoardData.VIBRATE_PRIMARY);
            }
        else // MEMORY_EMPTY - this state won't affect other sister buttons!!
            {
            layout.softBoardData.layoutStates.metaStates[LayoutStates.META_SHIFT].setState(MetaState.META_LOCK);
            state = MEMORY_SELECT;

            layout.softBoardData.vibrate(SoftBoardData.VIBRATE_PRIMARY);
            }
        }

    @Override
    public void mainTouchEnd( boolean isTouchUp )
        {
        if ( done )
            {
            packet.release();
            done = false;
            }
        }

    @Override
    public boolean fireSecondary(int type)
        {
        if ( done ) // MEMORY_TEXT
            {
            layout.softBoardData.softBoardListener.undoLastString();
            done = false;
            }
        if ( state == MEMORY_SELECT )
            {
            layout.softBoardData.layoutStates.metaStates[LayoutStates.META_SHIFT].setState(MetaState.META_OFF);
            }

        clear();
        layout.softBoardData.vibrate(SoftBoardData.VIBRATE_SECONDARY);

        return false;
        }

    }
