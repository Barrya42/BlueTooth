package com.example.derbenevsv.bluetooth;

import android.bluetooth.BluetoothSocket;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
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
        Single<Response> data =
                Single.create(emitter ->
                {
                    if (bluetoothSocket != null && bluetoothSocket.isConnected())
                    {
//                        Socket socket = new Socket("mail.ru", 80);
//                        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                        DataOutputStream outputStream = new DataOutputStream(bluetoothSocket.getOutputStream());
                        // TODO: 31.07.2018 в яве byte -127..128 в C 0..255;
                        try
                        {
                            outputStream.write(newCommand.getBytes());
                        }
                        catch (IOException e)
                        {
                            bluetoothSocket.close();
                            emitter.onError(new ConnectException("Connection with: " + bluetoothSocket
                                    .getRemoteDevice()
                                    .getName() + " not established"));
                        }
                        Response response = null;
                        StringBuilder stringBuilder = new StringBuilder();

                        //outputStream.write('\r');//CR
                        //outputStream.write('\n');//NR
                        InputStreamReader inputStream = new InputStreamReader(bluetoothSocket.getInputStream(), StandardCharsets.UTF_8);
                        TimeUnit.MILLISECONDS.sleep(200);
                        String newLine = System.getProperty("line.separator");
                        BufferedReader reader = new BufferedReader(inputStream);
                        //StringBuilder result = new StringBuilder();
                        String line;
//                        boolean flag = false;
                        try
                        {
                            line = reader.readLine();
                        }
                        catch (IOException e)
                        {
                            line = "";
                            bluetoothSocket.close();
                            emitter.onError(new ConnectException("Connection with: " + bluetoothSocket
                                    .getRemoteDevice()
                                    .getName() + " not established"));
                        }
                        //while (!flag && (line = reader.readLine()) != null)
//                        {
                        stringBuilder
                                //.append(flag ? newLine : "")
                                .append(line);
//                        flag = true;
//                        }

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
                        else if (response.getResultCode() == 501)
                        {
                            isHelloed = false;
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
