package com.example.derbenevsv.bluetooth;

import io.reactivex.Single;

public interface Door
{
    Single<Response> OpenDoor();
    Single<Response> CloseDoor();
}
