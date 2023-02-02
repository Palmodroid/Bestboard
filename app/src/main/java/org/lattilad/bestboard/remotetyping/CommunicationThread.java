package org.lattilad.bestboard.remotetyping;

import android.bluetooth.BluetoothSocket;
import android.os.Message;
import android.util.Log;

import org.lattilad.bestboard.scribe.Scribe;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class CommunicationThread  extends Thread
{
    private final BluetoothSocket socket;
    private final InputStream input;
    private final OutputStream output;
    private byte[] buffer;

    ConnectActivity connectActivity;

    public CommunicationThread(BluetoothSocket socket, ConnectActivity connectActivity)
    {
        this.socket = socket;
        this.connectActivity = connectActivity;
        InputStream tempInput = null;
        OutputStream tempOutput = null;

        try
        {
            tempInput = socket.getInputStream();
            tempOutput = socket.getOutputStream();
        } catch (IOException e)
        {
            Scribe.error("Remote - Could not create streams");
            //maybe a toast for feedback?
            connectActivity.cancelActivity();
        }

        input = tempInput;
        output = tempOutput;
    }

    public void run()
    {
        buffer = new byte[1024];
        int size;

        Message connected = connectActivity.handler.obtainMessage(ConnectActivity.CONNECTION_ESTABLISHED, -1, -1);
        connected.sendToTarget();

        while(true)
        {
            try
            {
                size = input.read(buffer);
                Object obj = BluetoothMessage.deserialise(buffer);//TODO make this compatible with other message types
                //Message msg = connectActivity.handler.obtainMessage(ConnectActivity.MESSAGE_RECIEVED, size, -1, buffer);
                Message msg = connectActivity.handler.obtainMessage(ConnectActivity.MESSAGE_RECIEVED, -1, -1, obj);
                msg.sendToTarget();
            } catch (IOException | ClassNotFoundException e)
            {
                Scribe.error("Remote - error reading from stream");/*
                Message msg = handler.obtainMessage(MESSAGE_ERROR, -1, -1);
                msg.sendToTarget();*/
                break;
            }
        }
    }

    public void write(byte[] bytes)
    {
        try
        {
            output.write(bytes);
            Message msg = connectActivity.handler.obtainMessage(ConnectActivity.MESSAGE_SENT, -1, -1, buffer);
            msg.sendToTarget();
        } catch (IOException e)
        {
            Scribe.error("Remote - error writing to stream");/*
            Message msg = handler.obtainMessage(MESSAGE_ERROR, -1, -1);
            msg.sendToTarget();*/
        }
    }

    public void write(String text, int autospace)
    {
        try
        {
            Message1 ms1 = new Message1(text, autospace);
            write(ms1.serialise());
        } catch (IOException e)
        {
            Scribe.error("Remote - error writing to stream");
        }
    }

    public void write(int length)
    {
        try
        {
            Message2 ms2 = new Message2(length);
            write(ms2.serialise());
            Message msg = connectActivity.handler.obtainMessage(ConnectActivity.TOAST_TEXT, -1, -1, "senddelete sent");
            msg.sendToTarget();
        } catch (IOException e)
        {
            Scribe.error("Remote - error writing to stream");
        }
    }

    public void cancel()
    {
        try
        {
            socket.close();
        } catch (IOException e)
        {
            Scribe.error("Remote - could not close the communication socket");
        }
    }
}

class BluetoothMessage implements Serializable
{
    public byte[] serialise() throws IOException
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(this);
        return byteArrayOutputStream.toByteArray();
    }

    public static Object deserialise(byte[] msg) throws IOException, ClassNotFoundException
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(msg);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        return objectInputStream.readObject();
    }
}

//class for messages that contain a String and an int (used for sendString as parameters "text" and "autospace")
class Message1 extends BluetoothMessage
{
    public String text;
    public int autospace;

    public Message1(String text, int autospace){
        this.text = text;
        this.autospace = autospace;
    }
}

//class for messages that contain an int (used for sendDelete as parameter "length")
//an int could be sent just converted into a byte[], but this is used for consistency reasons
class Message2 extends BluetoothMessage
{
    public int length;

    public Message2(int length)
    {
        this.length = length;
    }
}
