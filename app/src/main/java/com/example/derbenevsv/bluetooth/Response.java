package com.example.derbenevsv.bluetooth;

public class Response
{
    private int resultCode;
    private String command;

    public Response(int resultCode, String command)
    {
        this.resultCode = resultCode;
        this.command = command;
    }

    public int getResultCode()
    {
        return resultCode;
    }

    public String getCommand()
    {
        return command;
    }
}
//