package org.lattilad.bestboard.remotetyping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.lattilad.bestboard.R;
import org.lattilad.bestboard.SoftBoardProcessor;
import org.lattilad.bestboard.scribe.Scribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class ConnectActivity extends Activity
{
    public static String deviceName;

    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_ENABLE_DISCOVERABLE = 1;

    public static final int CONNECTION_ESTABLISHED = 0;
    public static final int MESSAGE_SENT = 1;
    public static final int MESSAGE_RECIEVED = 2;
    public static final int TOAST_TEXT = 3;

    static final UUID BT_UUID = UUID.fromString("afcfb687-303c-4a1a-8d88-078517a75a21");

    ListView lw;

    public static BluetoothAdapter ba;
    ArrayList<String> deviceNames = new ArrayList<>();
    ArrayList<BluetoothDevice> btDevices = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;

    private ServerThread st = null;
    private ClientThread ct = null;
    public static CommunicationThread communicationThread = null;

    public static SoftBoardProcessor softBoardProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        lw = (ListView)findViewById(R.id.lw);

        ba = BluetoothAdapter.getDefaultAdapter();
        if(ba == null)
        {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            Intent returnIntent = new Intent();
            setResult(RESULT_CANCELED, returnIntent);
            finish();
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceNames);
        lw.setAdapter(arrayAdapter);
        lw.setOnItemClickListener(listItemClick);

        searchForDevices();
    }

    private void searchForDevices()
    {
        //turn on Bluetooth
        if(!ba.isEnabled())
        {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        }
        //start discovering nearby devices
        if(!ba.startDiscovery())
        {
            Toast.makeText(this, "Turn on location", Toast.LENGTH_SHORT).show();
            Intent returnIntent = new Intent();
            setResult(RESULT_CANCELED, returnIntent);
            finish();
        }
        //make the davice discoverable for other devices
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(intent, REQUEST_ENABLE_DISCOVERABLE);
        //TODO start serverthread
        st = new ServerThread();
        st.start();
    }

    private AdapterView.OnItemClickListener listItemClick = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            if(st != null)
            {
                st.cancel();
                st = null;
            }
            ct = new ClientThread(btDevices.get(position));
            ct.start();
        }
    };

    public static void setSoftBoardProcesssor(SoftBoardProcessor sbp) {softBoardProcessor = sbp;}

    //TODO convert from Toast to Scribe
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                Toast.makeText(getApplicationContext(),"Bluetooth Turned ON",Toast.LENGTH_SHORT).show();
            }
            if (resultCode == Activity.RESULT_CANCELED)
            {
                Toast.makeText(getApplicationContext(),"denied",Toast.LENGTH_SHORT).show();
                cancelActivity();
            }
        }
        else if(requestCode == REQUEST_ENABLE_DISCOVERABLE)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                Toast.makeText(getApplicationContext(),"device is now discoverable",Toast.LENGTH_SHORT).show();
            }
            if (resultCode == Activity.RESULT_CANCELED)
            {
                Toast.makeText(getApplicationContext(),"denied",Toast.LENGTH_SHORT).show();
                cancelActivity();
            }
        }
    }

    public Handler handler = new Handler(new Handler.Callback()
    {
        @Override
        public boolean handleMessage(@NonNull Message message)
        {
            switch(message.what)
            {
                case MESSAGE_RECIEVED:/*
                    byte[] buffer = (byte[])message.obj;
                    String content = new String(buffer, 0, message.arg1);*/
                    //process incoming message
                    //Toast.makeText(MainActivity.this, content, Toast.LENGTH_SHORT).show();
                    if(message.obj instanceof Message1)
                    {
                        Message1 m1 = (Message1) message.obj;
                        softBoardProcessor.sendString(m1.text, m1.autospace);
                        //Toast.makeText(getApplicationContext(), "sendstring recieved", Toast.LENGTH_SHORT).show();
                    }
                    else if(message.obj instanceof Message2)
                    {
                        Message2 ms2 = (Message2) message.obj;
                        Toast.makeText(getApplicationContext(), "senddelete recieved", Toast.LENGTH_SHORT).show();
                        softBoardProcessor.sendDelete(ms2.length);
                    }
                    break;
                case MESSAGE_SENT:
                    //maybe unnecessary
                    //Toast.makeText(MainActivity.this, "message sent", Toast.LENGTH_SHORT).show();
                    break;
                /*case MESSAGE_ERROR:
                    Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                    break;*/
                case CONNECTION_ESTABLISHED:
                    deviceNames.clear();
                    btDevices.clear();
                    arrayAdapter.notifyDataSetChanged();

                    //inputText.setText("");
                    //inputText.requestFocus();

                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    break;
                case TOAST_TEXT:
                    String txt = (String)message.obj;
                    Toast.makeText(getApplicationContext(), txt, Toast.LENGTH_SHORT).show();
                    break;
            }
            return true;
        }
    });

    private final BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                deviceNames.add(device.getName());
                btDevices.add(device);
                arrayAdapter.notifyDataSetChanged();
            }
        }
    };

    private class ServerThread extends Thread
    {
        private final BluetoothServerSocket serverSocket;

        public ServerThread()
        {
            BluetoothServerSocket tempServerSocket = null;
            try
            {
                tempServerSocket = ba.listenUsingRfcommWithServiceRecord(getString(R.string.app_name), ConnectActivity.BT_UUID);
            }
            catch (IOException e)
            {
                Scribe.error("Remote - BluetoothAdapter listen failed");
                //maybe a toast for feedback?
                //Message msg = handler.obtainMessage(ConnectActivity.TOAST_TEXT, -1, -1, "listen failed");
                //msg.sendToTarget();
                cancelActivity();
            }
            serverSocket = tempServerSocket;
        }

        public void run()
        {
            BluetoothSocket socket = null;

            while(true)
            {
                try
                {
                    socket = serverSocket.accept();
                }
                catch (IOException e)
                {
                    Scribe.error("Remote - serverSocket accept failed");
                    //maybe a toast for feedback?
                    //Message msg = handler.obtainMessage(ConnectActivity.TOAST_TEXT, -1, -1, "accept failed");
                    //msg.sendToTarget();

                    //cancelActivity();
                    //cancel forces accept to throw this exception, so there is no nedd to cancel the activity every time
                    break;
                }

                if(socket != null)
                {
                    communicationThread = new CommunicationThread(socket, ConnectActivity.this);
                    communicationThread.start();
                    st = null;
                    try
                    {
                        serverSocket.close();
                    } catch (IOException e)
                    {
                        Scribe.error("Remote - serverSocket close failed");
                        //maybe a toast for feedback?
                        //Message msg = handler.obtainMessage(ConnectActivity.TOAST_TEXT, -1, -1, "ssocket close failed");
                        //msg.sendToTarget();
                        cancelActivity();
                    }
                    break;
                }
            }
        }

        public void cancel()
        {
            try
            {
                serverSocket.close();
            }
            catch (IOException e)
            {
                Scribe.error("Remote - serverSocket close failed");
                //maybe a toast for feedback?
                //Message msg = handler.obtainMessage(ConnectActivity.TOAST_TEXT, -1, -1, "ssocket close failed");
                //msg.sendToTarget();
                cancelActivity();
            }
        }
    }

    private class ClientThread extends Thread
    {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;

        public ClientThread(BluetoothDevice device)
        {
            this.device = device;
            BluetoothSocket tempSocket = null;

            try
            {
                tempSocket = device.createInsecureRfcommSocketToServiceRecord(BT_UUID);
            } catch (IOException e)
            {
                Scribe.error("Remote - creating socket failed");
                //maybe a toast for feedback?
                //Message msg = handler.obtainMessage(ConnectActivity.TOAST_TEXT, -1, -1, "csocket creation failed");
                //msg.sendToTarget();
                cancelActivity();
            }
            socket = tempSocket;
        }

        public void run()
        {
            ba.cancelDiscovery();

            try
            {
                socket.connect();
            }
            catch (IOException connectException)
            {
                try
                {
                    socket.close();
                } catch (IOException closeException)
                {
                    Scribe.error("Remote - client socket close failed");
                    //maybe a toast for feedback?
                    //Message msg = handler.obtainMessage(ConnectActivity.TOAST_TEXT, -1, -1, "csocket close failed");
                    //msg.sendToTarget();
                    cancelActivity();
                }
                return;
            }
            communicationThread = new CommunicationThread(socket, ConnectActivity.this);
            communicationThread.start();

            ct = null;
            //cancel();
        }

        public void cancel()
        {
            try
            {
                socket.close();
            } catch (IOException e)
            {
                Scribe.error("Remote - client socket close failed");
                //maybe a toast for feedback?
                //Message msg = handler.obtainMessage(ConnectActivity.TOAST_TEXT, -1, -1, "csocket close failed");
                //msg.sendToTarget();
                cancelActivity();
            }
        }
    }

    void cancelActivity()
    {
        if(st != null)
        {
            st.cancel();
            st = null;
        }
        if(ct != null)
        {
            ct.cancel();
            ct = null;
        }
        if(communicationThread != null)
        {
            communicationThread.cancel();
            communicationThread = null;
        }
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        finish();
    }

    @Override
    public void onBackPressed()
    {
        Intent returnIntent = new Intent();
        if(communicationThread == null)
        {
            setResult(RESULT_CANCELED, returnIntent);
            finish();
        }
        else
        {
            setResult(RESULT_OK, returnIntent);
            finish();
        }
    }
}