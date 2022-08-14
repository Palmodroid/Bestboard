package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;

public class ButtonSticky extends ButtonMainTouch implements Cloneable
    {
    /*
     * There ara different directions with different angles:
     * - Four compass points: north - east - south - west
     *
     *  Easiest: just compare deltaX with +/- deltaY - and get one of the quaters
     *
     * - Six hexagonal directions (sides of the hexagon): hex1 - hex6
     *      Be careful! y starts from the TOP! to the bottom
     *      tan 60 = 1,732
     *
     * if y > 0 - LOWER HALF
     *
     *      if x/y < -1,732 -> LEFT (LOWER HALF) - x(-) y(+)
     *      if x/y < 0      -> DOWN LEFT
     *      if x/y <  1,732 -> DOWN RIGHT
     *                      -> RIGHT (LOWER HALF) - x(+) y(+)
     *
     * if y < 0 - UPPER HALF - opposite direction
     *
     *      if x/y < -1,732 -> RIGHT (UPPER HALF) - x(+) y(-)
     *      if x/y < 0      -> UP RIGHT
     *      if x/y <  1,732 -> UP LEFT
     *                      -> LEFT (UPPER HALF) - x(-) y(-)
     *
     *  if y == 0 ->
     *
     *      if x < 0 -> LEFT
     *      if x > 0 -> RIGHT
     *
     * - There is a new, "rotated" hexagon, which fires in the direction of button edges
     *
     * - Clock-like directions: clock1-clock12 - same idea
     *
     * if y > 0 - LOWER HALF
     *
     *      if x/y < -3,732 -> CLOCK9 (LOWER HALF) - x(-) y(+)
     *      if x/y < -1,000 -> CLOCK8
     *      if x/y < -0,267 -> CLOCK7
     *      if x/y <  0,267 -> CLOCK6
     *      if x/y <  1,000 -> CLOCK5
     *      if x/y <  3,732 -> CLOCK4
     *                      -> CLOCK3 (LOWER HALF) - x(-) y(+)
     *
     * if y < 0 - UPPER HALF
     *
     *      if x/y < -3,732 -> CLOCK3 (UPPER HALF) - x(+) y(-)
     *      if x/y < -1,000 -> CLOCK2
     *      if x/y < -0,267 -> CLOCK1
     *      if x/y <  0,267 -> CLOCK12
     *      if x/y <  1,000 -> CLOCK11
     *      if x/y <  3,732 -> CLOCK10
     *                      -> CLOCK9 (UPPER HALF) - x(-) y(-)
     * if  y == 0
     *
     *      if x < 0 -> CLOCK9 (LEFT)
     *      if x > 0 -> CLOCK3 (RIGHT)
     *
     * if none of these are given, then SECOND, if there is no SECOND then first is returned
     */


    /*
     * There are four different "direction" sets:
     *  COMPASS - 4 x 90°
     *  HEXAGONAL - 6 x 60°
     *  HEXEDGE - 6 x 60°
     *  CLOCKLIKE - 12 x 30°
     * The smallest "not null" will be returned
     */
    public static final int CENTER_PACKET = 0;         // 0 FIRST - 1 SECOND
    public static final int COMPASS_PACKET = 1;        // 0 NORTH - 1 EAST - 2 SOUTH - 3 WEST
    public static final int HEXAGONAL_PACKET = 2;      // 0 UR - 1 R  - 2 DR - 3 DL - 4 L  - 5 UL
    public static final int HEXEDGE_PACKET = 3;        // 0 U  - 1 RU - 2 RD - 3 D  - 4 lD - 5 LU
    public static final int CLOCKLIKE_PACKET = 4;      // 0 (CLOCK1) - 11 (CLOCK12)

    /*
    TOKEN_NORTH, TOKEN_EAST, TOKEN_SOUTH, TOKEN_WEST,

    TOKEN_HEXU TOKEN_HEXD TOKEN_HEXLU TOKEN_HEXLD TOKEN_HEXRU TOKEN_HEXRD

    TOKEN_HEXR, TOKEN_HEXDR, TOKEN_HEXDL, TOKEN_HEXL, TOKEN_HEXUL, TOKEN_HEXUR,

    TOKEN_CLOCK1, TOKEN_CLOCK2, TOKEN_CLOCK3, TOKEN_CLOCK4, TOKEN_CLOCK5, TOKEN_CLOCK6,
    TOKEN_CLOCK7, TOKEN_CLOCK8, TOKEN_CLOCK9, TOKEN_CLOCK10, TOKEN_CLOCK11, TOKEN_CLOCK12,
    */

    public static final int SIZE = CLOCKLIKE_PACKET + 1;

    public static final int CENTER_SIZE = 2;
    public static final int FIRST = 0;
    public static final int SECOND = 1;

    public static final int COMPASS_SIZE = 4;
    public static final int NORTH = 0;
    public static final int EAST = 1;
    public static final int SOUTH = 2;
    public static final int WEST = 3;

    public static final int HEXAGONAL_SIZE = 6;
    public static final int UR = 0;
    public static final int R = 1;
    public static final int DR = 2;
    public static final int DL = 3;
    public static final int L = 4;
    public static final int UL = 5;

    public static final int HEXEDGE_SIZE = 6;
    public static final int U = 0;
    public static final int RU = 1;
    public static final int RD = 2;
    public static final int D = 3;
    public static final int LD = 4;
    public static final int LU = 5;

    public static final int CLOCKLIKE_SIZE = 12;
    // CLOCKLIKE PAKETS -> index is clock -1

    /*
     * There are five packet arrays.
     * Packet arrays (packetArrays[A]) CANNOT BE NULL,
     * but each packet element can be null (packetArrays[A][E])
     */
    Packet[][] packetArrays;

    // last packet was sent from this packetArray
    // cannot be invalid! Always point to one packetarry! (Even if the element is null
    volatile int activePacketArray = 0;

    // last packet sent from activePacketArray
    // can be -1 - means element is null or not valid
    volatile int activePacketElement = -1;

    /* Just to know: int len = getLayout().halfHexagonWidthInPixels; */

    /** if repeat is allowed, than "far" areas - more than 2xlen far - start repeat function **/
    boolean allowRepeat;

    /** Area of the repeat: TRUE for "far" areas; FALSE for CENTER and near areas */
    boolean repeatAreaReached = false;



    @Override
    public ButtonSticky clone()
        {
        return (ButtonSticky)super.clone();
        }


    // packetArrays cannot be null, but elements can be null!

    /**
     * ButtonStickyJoy do not use HEXAGON and CLOCKLIKE pacets, nor repeat,
     * but uses this base class to store its values
     */

    public ButtonSticky(Packet[][] packetArrays, boolean allowRepeat )
        {
        /* Just to check packets
        for ( int a = 0; a < SIZE; a++ )
            {
            for ( int e = 0; e < packetArrays[a].length; e++ )
                {
                Scribe.debug( "A: " + a + " E: " + e + " - " +
                        ( packetArrays[a][e] == null ? "n/a" : packetArrays[a][e].getTitleString()) );
                }
            }
        */
        this.packetArrays = packetArrays;
        this.allowRepeat = allowRepeat;
        }

    // TODO - These methods could be united
    @Override
    public boolean isFirstStringChanging()
        {
        // packetArray cannot be null, but element can be null
        return packetArrays[CENTER_PACKET][FIRST] != null &&
                packetArrays[CENTER_PACKET][FIRST].isTitleStringChanging();
        }

    @Override
    public String getFirstString()
        {
        // packetArray cannot be null, but element can be null
        return packetArrays[CENTER_PACKET][FIRST] != null ?
                packetArrays[CENTER_PACKET][FIRST].getTitleString() : "";
        }

    @Override
    public boolean isSecondStringChanging()
        {
        // packetArray cannot be null, but element can be null
        return packetArrays[CENTER_PACKET][SECOND] != null &&
                packetArrays[CENTER_PACKET][SECOND].isTitleStringChanging();
        }

    @Override
    public String getSecondString()
        {
        // packetArray cannot be null, but element can be null
        return packetArrays[CENTER_PACKET][SECOND] != null ?
                packetArrays[CENTER_PACKET][SECOND].getTitleString() : "";
        }


    /**
     * Only TOUCH_DOWN can trigger this. packetFirst can be null!
     */
    @Override
    public void mainTouchStart( boolean isTouchDown )
        {
        activePacketArray = CENTER_PACKET;
        activePacketElement = -1; // CENTER_PACKET will be sent only at mainTouchEnd

        /* IF CENTER_PACKET SHOULD BE SENT AT START
        if ( packetArrays[activePacketArray][0] != null )
            {
            activePacketElement = 0;
            packetArrays[activePacketArray][0].send();
            }
        else
            activePacketElement = -1; // not necessary, it was set at start
        */
        }


    @Override
    public void mainTouchEnd( boolean isTouchUp )
        {
        // CENTER_PACKET is sent only here
        if ( activePacketArray == CENTER_PACKET && activePacketElement == -1 &&
                packetArrays[CENTER_PACKET][FIRST] != null )
            {
            layout.softBoardData.vibrate(SoftBoardData.VIBRATE_PRIMARY);

            activePacketElement = FIRST;
            packetArrays[CENTER_PACKET][FIRST].send();
            }

        if ( activePacketElement != -1 )
            {
            packetArrays[activePacketArray][activePacketElement].release();
            }
        }

    /**
     * mainTouchSticky() is called, when MOVE was detected on a STICKY button
     * DOWN and UP call mainTouchStart() and mainTouchEnd() on a regular way
     * @param deltaX X movement from the origin
     * @param deltaY Y movement from the origin
     * @return +1 START REPEATER - 0 LEAVE REPEATER AS IS - -1 STOP REPEATER
     */
    public int mainTouchSticky( int deltaX, int deltaY )
        {
        // if area is "empty" (no packet is defined) then CENTRAL_PACKET is used
        int new_activePacketArray = CENTER_PACKET;
        int new_activePacketElement = -1; // -1 there is no packet for this area

        boolean new_repeatAreaReached;

        if ( !isCenter(deltaX, deltaY) )
            {
            if ((new_activePacketElement = getClocklikeAngle(deltaX, deltaY)) != -1
                    && packetArrays[CLOCKLIKE_PACKET][new_activePacketElement] != null)
                {
                new_activePacketArray = CLOCKLIKE_PACKET;
                }
            else if ((new_activePacketElement = getHexEdgeAngle(deltaX, deltaY)) != -1
                    && packetArrays[HEXEDGE_PACKET][new_activePacketElement] != null)
                {
                new_activePacketArray = HEXEDGE_PACKET;
                }
            else if ((new_activePacketElement = getHexagonalAngle(deltaX, deltaY)) != -1
                    && packetArrays[HEXAGONAL_PACKET][new_activePacketElement] != null)
                {
                new_activePacketArray = HEXAGONAL_PACKET;
                }
            else if ((new_activePacketElement = getCompassAngle(deltaX, deltaY)) != -1
                    && packetArrays[COMPASS_PACKET][new_activePacketElement] != null)
                {
                new_activePacketArray = COMPASS_PACKET;
                }
            else // CENTER_PACKET - SECOND (or FIRST sent later if SECOND is missing)
                {
                new_activePacketElement = (packetArrays[CENTER_PACKET][SECOND] != null) ? SECOND : -1;
                }
            }

        // newActivePacketElement is -1, if there is no new packet to send

        boolean areaHasChanged =
                activePacketArray != new_activePacketArray || activePacketElement != new_activePacketElement;


        if ( allowRepeat )
            {
            new_repeatAreaReached = isRepeatAreaReached(deltaX, deltaY);

            if ( areaHasChanged )
                {
                // NO undo if repeat is enabled

                activePacketArray = new_activePacketArray;
                activePacketElement = new_activePacketElement;

                // NO fireSecondary is needed inside repeat area - repeat will fire, and send packet
                if ( !repeatAreaReached && !new_repeatAreaReached )
                    {
                    fireSecondary( 0 );
                    }
                }

            // Repeat was changed
            if ( repeatAreaReached != new_repeatAreaReached )
                {
                repeatAreaReached = new_repeatAreaReached;
                return repeatAreaReached ? 1 : -1; // +1 START -1 STOP repeater
                }
            }

        else if ( areaHasChanged ) // && Repeat is NOT allowed
            {
            // Clear previous string: if area was changed
            // CENTER - always -1, so no undo is needed
            // OTHER DIRS - undo only, when NO repeat
            if (activePacketElement != -1)
                layout.softBoardData.softBoardListener.undoLastString();

            // If repeat is NOT allowed then each new area change result in a new send
            activePacketArray = new_activePacketArray;
            activePacketElement = new_activePacketElement;

            fireSecondary( 0 );
            }

        return 0;
        }


    /**
     * Sticky button calls fireSecondary
     * @param type of the activation: ON_STAY (-1) ON_CIRCLE (1) or ON_HARD_PRESS (2)
     * @return
     */
    @Override
    public boolean fireSecondary(int type)
        {
        if ( activePacketElement >= 0 )
            {
            layout.softBoardData.vibrate(SoftBoardData.VIBRATE_SECONDARY);
            packetArrays[activePacketArray][activePacketElement].send();
            }

        return false;
        }


    // Repeat will call this function with ON_STAY
    // No other calls can happen in ButtonSticky
    @Override
    public boolean mainTouchSecondary(int type)
        {
        if ( activePacketElement >= 0 )
            {
            layout.softBoardData.vibrate(SoftBoardData.VIBRATE_REPEATED);
            packetArrays[activePacketArray][activePacketElement].send();
            }
        return true; // repeat at repeat time
        }


    protected int getLength()
        {
        return getLayout().halfHexagonWidthInPixels;
        }

    private boolean isCenter( int x, int y )
        {
        return x < getLength() && x > -getLength() && y < getLength() && y > -getLength();
        }

    private boolean isRepeatAreaReached( int x, int y )
        {
        return x < -2*getLength() || x > 2*getLength() || y < -2*getLength() || y > 2*getLength();
        }

    private int getCompassAngle( int x, int y )
        {
        if ( y > x )
            {
            return ( -y > x ) ? WEST : SOUTH;
            }
        else
            {
            return ( -y > x ) ? NORTH : EAST;
            }
        }

    private int getHexagonalAngle( int x, int y )
        {
        if ( y == 0 )
            {
            return ( x < 0 ) ? L : R;
            }
        else
            {
            int angle = x * 1000 / y;

            if ( y > 0 )
                {
                if (angle < -1732)
                    return L;
                if (angle < 0)
                    return DL;
                if (angle < 1732)
                    return DR;
                return R;
                }
            // if ( y < 0 )
            if (angle < -1732)
                return R;
            if (angle < 0)
                return UR;
            if (angle < 1732)
                return UL;
            return L;
            }
        }

    private int getHexEdgeAngle( int x, int y )
        {
        if ( y == 0 )
            {
            return ( x < 0 ) ? LU : RU;
            }
        else
            {
            int angle = x * 1000 / y;

            if ( y > 0 )
                {
                if (angle < -577)
                    return LD;
                if (angle < 577)
                    return D;
                return RD;
                }
            // if ( y < 0 )
            if (angle < -577)
                return RU;
            if (angle < 577)
                return U;
            return LU;
            }
        }

    /*
    private int getClocklikeAngle( int x, int y )
        {
        // INDEX = CLOCK - 1 !! index 0 is clock 1!

        if ( y == 0 )
            {
            return ( x < 0 ) ? 8 : 2; // CLOCK9 and CLOCK3
            }
        else
            {
            int angle = x * 1000 / y;

            if (y > 0)    // lower half
                {
                if (angle < -3732)
                    return 8; // CLOCK9, lower half
                if (angle < -1000)
                    return 7; // CLOCK8
                if (angle < -267)
                    return 6; // CLOCK7
                if (angle < 267)
                    return 5; // CLOCK6
                if (angle < 1000)
                    return 4; // CLOCK5
                if (angle < 3732)
                    return 3; // CLOCK4
                return 2; // CLOCK3, lower half
                }
            // if ( y < 0 ) // upper half
            if (angle < -3732)
                return 2; // CLOCK3 upper half
            if (angle < -1000)
                return 1; // CLOCK2
            if (angle < -267)
                return 0; // CLOCK1
            if (angle < 267)
                return 11; // CLOCK12
            if (angle < 1000)
                return 10; // CLOCK11
            if (angle < 3732)
                return 9; // CLOCK10
            return 8; // CLOCK9 upper half
            }
        }
        */

    private int getClocklikeAngle( int x, int y )
        {
        // INDEX = CLOCK - 1 !! index 0 is clock 1!

        if ( y == 0 )
            {
            return ( x < 0 ) ? 8 : 2; // CLOCK9 and CLOCK3
            }
        else
            {
            int angle = x * 1000 / y;

            if (y > 0)    // lower half
                {
                if (angle < -3732-373)          //    -∞ to -3733
                    return 8; // CLOCK9, lower half
                if (angle < -1000-100 && angle > -3732+373)          // -3732 to -1001
                    return 7; // CLOCK8
                if (angle < -267-26 && angle > -1000+100)           // -1000 to -268
                    return 6; // CLOCK7
                if (angle < 267-26 && angle > -267+26)            // -267  to +266
                    return 5; // CLOCK6
                if (angle < 1000-100 && angle > 267+26)           // +267  to +999
                    return 4; // CLOCK5
                if (angle < 3732-373 && angle > 1000+100)           // +1000 to +3731
                    return 3; // CLOCK4
                if (angle > 3732+373)
                    return 2; // CLOCK3, lower half   // +3732 to +∞

                return -1;
                }
            // if ( y < 0 ) // upper half
            if (angle < -3732)
                return 2; // CLOCK3 upper half
            if (angle < -1000)
                return 1; // CLOCK2
            if (angle < -267)
                return 0; // CLOCK1
            if (angle < 267)
                return 11; // CLOCK12
            if (angle < 1000)
                return 10; // CLOCK11
            if (angle < 3732)
                return 9; // CLOCK10
            return 8; // CLOCK9 upper half
            }
        }

    }
