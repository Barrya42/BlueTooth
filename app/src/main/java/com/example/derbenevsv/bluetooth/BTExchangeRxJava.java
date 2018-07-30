package com.example.derbenevsv.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.util.TimeUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.security.Timestamp;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.internal.operators.single.SingleTimeout;
import io.reactivex.observers.DisposableCompletableObserver;

public class BTExchangeRxJava implements Door
{
    public static int[] COMMAND_OPEN_DOOR = {0x02, 0x05};
    public static int[] COMMAND_HELLO = {0x02, 0x01};
    private static int RESPONSE_TIMEOUT = 6000;
    //    private static String HELLO_RESPONSE = "HelloResp";
    //    private Queue<String> commandQueue;в
    private BluetoothSocket bluetoothSocket;
    private boolean isHelloed = false;
    private DisposableCompletableObserver commandObserver;


    public BTExchangeRxJava(BluetoothSocket bluetoothSocket)
    {

        this.bluetoothSocket = bluetoothSocket;

    }


    private Single<Response> SendCommand(final int[] newCommand)
    {
        Single<Response> data =
                Single.create(emitter ->
                {
                    if (bluetoothSocket != null && bluetoothSocket.isConnected())
                    {
                        OutputStream outputStream = bluetoothSocket.getOutputStream();
                        // TODO: 31.07.2018 в яве byte -127..128 в C 0..255;
                        outputStream.write();
                        //outputStream.write('\r');//CR
                        //outputStream.write('\n');//NR
                        //outputStream.close();

                        InputStream inputStream = bluetoothSocket.getInputStream();
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
                        if (inputStream.available() == 0)
                        {
                            response = new Response(200, string);

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
