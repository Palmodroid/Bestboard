package org.lattilad.bestboard.buttons;


import org.lattilad.bestboard.SoftBoardData;

/**
 * From a sticky button you cannot swipe to other buttons, but it contains four different buttons -
 * (over its first button) depending in which direction do you try to leave the button.
 * First packet should be undo-able (Text),
 * others can be any type: Text(String) or Hard-key or function
 */
public class ButtonStickyJoy extends ButtonSticky implements Cloneable
    {
    private int sentX = 0;
    private int sentY = 0;


    @Override
    public ButtonStickyJoy clone()
        {
        return (ButtonStickyJoy)super.clone();
        }

    public ButtonStickyJoy( Packet[][] packetArrays )
        {
        // only CENTER and COMPASS packetArrays are used, repeat is NOT used
        super( packetArrays, false );
        }


    // IF CENTER should be sent at TOUCH DOWN

    /**
     * Only TOUCH_DOWN can trigger this. packetFirst can be null!
     *
    @Override
    public void mainTouchStart( boolean isTouchDown )
        {
        super.mainTouchStart( isTouchDown );

        // activePacketArray = CENTER_PACKET;
        // activePacketElement = 0; OR = -1; (if center packet is missing)

        sentX = 0;
        sentY = 0;
        }
    // mainTouchEnd will close active element (if any)
    */

    // IF CENTER IS SENT AT RELEASE

    /**
     * Only TOUCH_DOWN can trigger this. packetFirst can be null!
     */
    @Override
    public void mainTouchStart( boolean isTouchDown )
        {
        layout.softBoardData.vibrate(SoftBoardData.VIBRATE_PRIMARY);
        activePacketArray = CENTER_PACKET;

        sentX = 0;
        sentY = 0;
        }


    @Override
    public void mainTouchEnd( boolean isTouchUp )
        {
        if ( activePacketArray == CENTER_PACKET )
            {
            if (packetArrays[activePacketArray][0] != null)
                {
                activePacketElement = 0;
                packetArrays[activePacketArray][0].send();
                }
            else
                activePacketElement = -1; // not necessary, it was set at start
            }

        if ( activePacketElement != -1 )
            {
            packetArrays[activePacketArray][activePacketElement].release();
            }
        }

    // END CENTER AT RELEASE - Check _mainTouchSticky


    /**
     * Completely different algorithm from super
     * If MOVE is bigger than getLength() (in any direction) then _mainTouchSticky() helper method
     * is called with the new direction (and this point will bi the new origin)
     * From now on only COMPASS_PACKET directions are active - it cannot return to CENTER any more.
     * @param deltaX X movement from the origin
     * @param deltaY Y movement from the origin
     * @return +1 START REPEATER - 0 LEAVE REPEATER AS IS - -1 STOP REPEATER
     */
    public int mainTouchSticky( int deltaX, int deltaY )
        {
        // Scribe.debug(Debug.TOUCH, "STICKY DELTA X: " + deltaX + " Y: " + deltaY);
        if ( deltaX > sentX + getLength() )
            {
            _mainTouchSticky( EAST );
            sentX += getLength();
            }
        else if ( deltaX < sentX - getLength() )
            {
            _mainTouchSticky( WEST );
            sentX -= getLength();
            }

        if ( deltaY > sentY + getLength() )
            {
            _mainTouchSticky( SOUTH );
            sentY += getLength();
            }
        else if ( deltaY < sentY - getLength() )
            {
            _mainTouchSticky( NORTH );
            sentY -= getLength();
            }

        return 0;
        }

    private void _mainTouchSticky( int new_activePacketElement )
        {
        // IF CENTER IS SENT AT TOUCH!!
        // Center should be undo-ed!
        // if ( activePacketArray == CENTER_PACKET && activePacketElement != -1 )
        //    {
        //    layout.softBoardData.softBoardListener.undoLastString();
        //    }

        activePacketArray = COMPASS_PACKET;

        activePacketElement = (packetArrays[COMPASS_PACKET][new_activePacketElement] == null) ?
                -1 : new_activePacketElement;

        fireSecondary( 0 );
        }

    }
