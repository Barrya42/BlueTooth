package com.example.derbenevsv.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.util.TimeUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.security.Timestamp;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.observers.DisposableCompletableObserver;

public class BTExchangeRxJava implements Door
{
    public static String COMMAND_OPEN_DOOR = "OpenDoor";
    public static String COMMAND_HELLO = "Hello";
    private static int RESPONSE_TIMEOUT = 3000;
    //    private static String HELLO_RESPONSE = "HelloResp";
    //    private Queue<String> commandQueue;в
    private BluetoothSocket bluetoothSocket;
    private boolean isHelloed = false;
    private DisposableCompletableObserver commandObserver;


    public BTExchangeRxJava(BluetoothSocket bluetoothSocket)
    {

        this.bluetoothSocket = bluetoothSocket;

    }


    private Single<Response> SendCommand(final String newCommand)
    {

        return Single.create(emitter ->
        {
            if (bluetoothSocket != null && bluetoothSocket.isConnected())
            {
                OutputStream outputStream = bluetoothSocket.getOutputStream();
                outputStream.write(newCommand.getBytes());
                outputStream.write('\r');//CR
                outputStream.write('\n');//NR
                //outputStream.close();
                InputStream inputStream = bluetoothSocket.getInputStream();
                Date time = new Date();
                long startWait = time.getTime();
                Response response = null;
                //int i =inputStream.read();
                do
                {
                    if (inputStream.available() > 0)
                    {
                        byte[] input = new byte[inputStream.available()];
                        inputStream.read(input);
                        String string = new String(input);
                        response = new Response(200, string);
                        break;
                        // TODO: 12.07.2018 Если отправляли команду "Привет", и пришел ответ, то нужно пометить флаг что поздоровались.
                    }
                }
                while ((new Date()).getTime() - startWait < RESPONSE_TIMEOUT);
                if (response == null)
                {
                    response = new Response(0, "");
                }
                emitter.onSuccess(response);
//                emitter.onComplete();
            }
            else
            {
                emitter.onError(new ConnectException("Connection with: " + bluetoothSocket.getRemoteDevice()
                        .getName() + " lost"));
            }
        });


    }

    public Single<Response> OpenDoor()
    {
        if (!isHelloed)
        {
            return SendCommand(COMMAND_HELLO);
        }
        else
        {
            return SendCommand(COMMAND_OPEN_DOOR);
        }

    }


}
