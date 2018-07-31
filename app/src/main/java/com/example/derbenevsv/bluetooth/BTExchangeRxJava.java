package com.example.derbenevsv.bluetooth;

import android.bluetooth.BluetoothSocket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ConnectException;
import java.net.Socket;
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
        Single<Response> data =
                Single.create(emitter ->
                {
                    if (bluetoothSocket != null && bluetoothSocket.isConnected())
                    {
                        Socket socket = new Socket("mail.ru", 80);
                        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
//                        DataOutputStream outputStream = new DataOutputStream(bluetoothSocket.getOutputStream());
                        // TODO: 31.07.2018 в яве byte -127..128 в C 0..255;
                        outputStream.write(newCommand.getBytes());

                        //outputStream.write('\r');//CR
                        //outputStream.write('\n');//NR
                        //outputStream.close();
                        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
//                        DataInputStream inputStream = new DataInputStream(bluetoothSocket.getInputStream());

                        //TimeUnit.MILLISECONDS.sleep(100);
                        Response response = null;
                        String string = "";

                        while (inputStream.available() > 0)
                        {
                            byte[] input = new byte[inputStream.available()];
                            inputStream.read(input);
                            string += new String(input);

//                        break;
                            // TODO: 12.07.2018 Если отправляли команду "Привет", и пришел ответ, то нужно пометить флаг что поздоровались.
                        }

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
        return data.timeout(RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS)
                .onErrorResumeNext(Single.just(new Response(500, "timeout")));


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
