package com.example.derbenevsv.bluetooth;

import android.bluetooth.BluetoothSocket;

import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;

public class BTExchangeRxJava implements Door
{
    public static String COMMAND_OPEN_DOOR = "54141f5a-fb34-494e-a9b4-516689876310";
    public static String COMMAND_HELLO = "3dfe191f-15f2-43cf-b2ac-4ac6d1c928fc";
    public static String COMMAND_CLOSE_DOOR = "c6361166-4c22-4bd9-b682-49812110f605";

    private static int RESPONSE_TIMEOUT = 30000;
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
//        Single<Response> data =
              return  Single.create(emitter ->
                {
                    if (bluetoothSocket != null && bluetoothSocket.isConnected())
                    {
//                        Socket socket = new Socket("mail.ru", 80);
//                        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                        DataOutputStream outputStream = new DataOutputStream(bluetoothSocket.getOutputStream());
                        // TODO: 31.07.2018 в яве byte -127..128 в C 0..255;
                        outputStream.write(newCommand.getBytes());
                        Response response = null;
                        StringBuilder stringBuilder = new StringBuilder();

                        //outputStream.write('\r');//CR
                        //outputStream.write('\n');//NR
                        //outputStream.close();
//                        InputStreamReader inputStream = new InputStreamReader(socket.getInputStream());
                        InputStreamReader inputStream = new InputStreamReader(bluetoothSocket.getInputStream(), StandardCharsets.UTF_8);
                        TimeUnit.MILLISECONDS.sleep(1500);

                        //TimeUnit.MILLISECONDS.sleep(100);
                        char[] buffer = new char[64];
                        int received;
                        //while ((received = inputStream.read(buffer)) != -1)
                        while (inputStream.ready())
                        {
                            received = inputStream.read(buffer);
//                            inputStream.read();
                            stringBuilder.append(buffer, 0, received);

//                        break;
                            // TODO: 12.07.2018 Если отправляли команду "Привет", и пришел ответ, то нужно пометить флаг что поздоровались.
                        }

                        try
                        {
                            response = new Response(stringBuilder.toString());
                        }
                        catch (Response.IncorrectResponse incorrectResponse)
                        {
                            emitter.onError(incorrectResponse);
                        }
                        if (newCommand.equals(COMMAND_HELLO) && response != null && response.isSuccessful())
                        {
                            isHelloed = true;
                        }
//                        else if (newCommand.equals(COMMAND_OPEN_DOOR) && response != null && response.isSuccessful())
                        emitter.onSuccess(response);
//                emitter.onComplete();
                    }
                    else
                    {
                        emitter.onError(new ConnectException("Connection with: " + bluetoothSocket.getRemoteDevice()
                                .getName() + " not established"));
                    }
                });
//        return data.timeout(RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS)
//                .onErrorResumeNext(Single.just(new Response(500, "timeout")));


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
