package com.example.derbenevsv.bluetooth;

import android.bluetooth.BluetoothSocket;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;

import io.reactivex.Single;
import io.reactivex.observers.DisposableCompletableObserver;

public class ExchangeRxJava
{
    public static String COMMAND_OPEN_DOOR = "openDoor";
    public static String COMMAND_HELLO = "Hello";
    //    private static String HELLO_RESPONSE = "HelloResp";
    //    private Queue<String> commandQueue;в
    private BluetoothSocket bluetoothSocket;
    private boolean isHelloed = false;
    private DisposableCompletableObserver commandObserver;


    public ExchangeRxJava(BluetoothSocket bluetoothSocket)
    {

        this.bluetoothSocket = bluetoothSocket;

    }


    private Single SendCommand(final String newCommand)
    {

        return Single.create(emitter ->
        {
            if (bluetoothSocket.isConnected())
            {
                String response = "";
                OutputStream outputStream = bluetoothSocket.getOutputStream();
                outputStream.write(newCommand.getBytes());
                outputStream.write('\r');//CR
                outputStream.write('\n');//NR

                InputStream inputStream = bluetoothSocket.getInputStream();
                if (inputStream.available() > 0)
                {
                    throw new UnsupportedOperationException();
                    // TODO: 12.07.2018 Если отправляли команду "Привет", и пришел ответ, то нужно пометить флаг что поздоровались.
                }

                emitter.onSuccess(new Response(200, newCommand));
//                emitter.onComplete();
            }
            else
            {
                emitter.onError(new ConnectException("Connection with: " + bluetoothSocket.getRemoteDevice()
                        .getName() + " lost"));
            }
        });


    }

    public Single OpenDoor()
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
