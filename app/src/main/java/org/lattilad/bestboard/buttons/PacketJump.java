package org.lattilad.bestboard.buttons;

import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.parser.Tokenizer;

/**
 * PacketJump implements the function of ButtonSwitch
 *
 * TODO It cannot change color of the underlaing button
 * It only sends a touch/release pair to buttontable, to change board immediately
 *
 * TODO if there is a BUTTON SWITCH how can multitouch work together with maintouch?
 */
public class PacketJump extends Packet
    {
    private long layoutId;
    private boolean lockKey;

    // BOARD table is filled up only after the definition of the boards
    // At the time of the definition of the SWITCH keys, no data is
    // available about the table, so index verification is not possible
    // Special 'BACK' layoutId means: GO BACK
    public PacketJump(SoftBoardData softBoardData, long layoutId, boolean lockKey )
        {
        super(softBoardData,
                ( layoutId == -1L ) ? "BACK" : ((lockKey ? "L" : "") + Tokenizer.regenerateKeyword(layoutId)));

        this.layoutId = layoutId;
        this.lockKey = lockKey;
        }

    /* getColor is copied from SwitchButton, but currently Packet cannot send color informations
    @Override
    public int getColor()
        {
        int state = layout.softBoardData.boardTable.getState(layoutId);

        if ( state == BoardTable.ACTIVE )
            return layout.softBoardData.metaColor;
        else if ( state == BoardTable.LOCKED )
            return layout.softBoardData.lockColor;
        else if ( state == BoardTable.TOUCHED && layout.softBoardData.displayTouch )
            return layout.softBoardData.touchColor;

        // It is only needed by CAPS, but all meta-buttons will know it.
        else if ( showAutoCaps &&
                // layout.softBoardData.autoFuncEnabled && !! autocaps cannot be set without autofuncEnabled
                layout.softBoardData.layoutStates.metaStates[LayoutStates.META_CAPS].getState() == CapsState.AUTOCAPS_ON )
            return layout.softBoardData.autoColor;

        // If state == HIDDEN, then no redraw is needed
        return super.getColor();
        }
    */


    /**
     * Send generates a touch-release event, which brings us to the next board.
     * Because this is a MAIN touch, no "stay" is possible. So release doesn't need to be in
     * "release" function, it can happen immediately
     */
    @Override
    public void send()
        {
        softBoardData.boardTable.touch(layoutId);
        softBoardData.boardTable.release(layoutId, lockKey );

        // normally (when multitouch changes board) maintouch continues on the next board
        // when maintouch
        softBoardData.softBoardListener.getLayoutView().breakMainTouch();
        }

    /*
    sendSecondary cannot work is primary is not undoable
     */

    }
