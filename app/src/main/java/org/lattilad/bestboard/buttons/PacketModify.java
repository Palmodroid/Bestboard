package org.lattilad.bestboard.buttons;
import org.lattilad.bestboard.R;
import org.lattilad.bestboard.SoftBoardData;
import org.lattilad.bestboard.modify.Modify;
import org.lattilad.bestboard.parser.Tokenizer;
import org.lattilad.bestboard.scribe.Scribe;

public class PacketModify extends Packet
    {
    private long modifyId;
    private boolean reverse;

    public PacketModify(SoftBoardData softBoardData, long modifyId, boolean reverse)
        {
        super(softBoardData);
        this.modifyId = modifyId;
        this.reverse = reverse;

        setTitleString("MOD");
        }

    @Override
    public void send()
        {
        Modify modify = softBoardData.modify.get( modifyId );
        if ( modify != null )
            {
            modify.change( reverse );
            }
        else
            {
            // Error message should mimic tokenizer error
            Scribe.error_secondary(
                    "[RUNTIME ERROR] " +
                            softBoardData.softBoardListener.getApplicationContext().getString( R.string.modify_missing ) +
                            Tokenizer.regenerateKeyword( modifyId ) );
            }

        }
    }
