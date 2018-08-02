package com.example.derbenevsv.bluetooth;

import android.bluetooth.BluetoothSocket;

import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.observers.DisposableCompletableObserver;

public class BTExchangeRxJava implements Door
{
    public static String COMMAND_OPEN_DOOR = "54141f5a-fb34-494e-a9b4-516689876310";
    public static String COMMAND_HELLO = "3dfe191f-15f2-43cf-b2ac-4ac6d1c928fc";
    public static String COMMAND_CLOSE_DOOR = "c6361166-4c22-4bd9-b682-49812110f605";

    private static int RESPONSE_TIMEOUT = 3000;
    //    private static String HELLO_RESPONSE = "HelloResp";
    //    private Queue<String> commandQueue;в
    private BluetoothSocket bluetoothSocket;
    private boolean isHelloed = false;


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
//                        Socket socket = new Socket("mail.ru", 80);
//                        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                        DataOutputStream outputStream = new DataOutputStream(bluetoothSocket.getOutputStream());
                        // TODO: 31.07.2018 в яве byte -127..128 в C 0..255;
                        outputStream.write(newCommand.getBytes());
                        Response response = null;
                        StringBuilder string = new StringBuilder();

                        //outputStream.write('\r');//CR
                        //outputStream.write('\n');//NR
                        //outputStream.close();
//                        InputStreamReader inputStream = new InputStreamReader(socket.getInputStream());
                        InputStreamReader inputStream = new InputStreamReader(bluetoothSocket.getInputStream());

                        //TimeUnit.MILLISECONDS.sleep(100);
                        char[] buffer = new char[64];
                        int received;
                        while ((received = inputStream.read(buffer)) != -1)
                        {

                            inputStream.read();
                            string.append(buffer, 0, received);

//                        break;
                            // TODO: 12.07.2018 Если отправляли команду "Привет", и пришел ответ, то нужно пометить флаг что поздоровались.
                        }

                        if (string.length() < 36)
                        {
                            response = new Response(0, "");
                        }
                        emitter.onSuccess(response);
//                emitter.onComplete();
                    }
                    else
                    {
                        emitter.onError(new ConnectException("Connection with: " + bluetoothSocket.getRemoteDevice()
                                .getName() + " not established"));
                    }
                });
        return data.timeout(RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS)
                .onErrorResumeNext(Single.just(new Response(500, "timeout")));


    }

    @Override
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

    @Override
    public Single<Response> CloseDoor()
    {
        if (!isHelloed)
        {
            return SendCommand(COMMAND_HELLO);
        }
        else
        {
            return SendCommand(COMMAND_CLOSE_DOOR);
        }
    }


}
