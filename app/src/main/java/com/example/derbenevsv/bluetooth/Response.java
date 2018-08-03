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

    public Response(String s) throws IncorrectResponse
    {
        String[] splitted = s.split(":");
        if (splitted.length != 2)
        {
            throw new IncorrectResponse("Not correct response: " + s);
        }
        else
        {
            try
            {
                resultCode = Integer.parseInt(splitted[0]);
                command = splitted[1];
            }
            catch (NumberFormatException e)
            {
                IncorrectResponse throwable = new IncorrectResponse("Not correct response: " + s);
                throwable.addSuppressed(e);
                throw throwable;
            }
        }
    }

    public int getResultCode()
    {
        return resultCode;
    }

    public String getCommand()
    {
        return command;
    }

    @Override
    public String toString()
    {
        return "Response{" +
                "resultCode=" + resultCode +
                ", command='" + command + '\'' +
                '}';
    }

    public boolean isSuccessful()
    {
        return resultCode == 200;
    }

    class IncorrectResponse extends Throwable
    {
        public IncorrectResponse(String message)
        {
            super(message);
        }
    }
}
//