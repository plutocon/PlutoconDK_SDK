package com.kongtech.dk.sdk.connection.writeable;

import android.content.Context;

import com.kongtech.dk.sdk.connection.PlutoconDKConnection;
import com.kongtech.dk.sdk.device.PlutoconDK;
import com.kongtech.dk.sdk.utils.DKUUID;

public abstract class WritableDKConnection extends PlutoconDKConnection {

    public WritableDKConnection(Context context, PlutoconDK plutoconDK) {
        super(context, plutoconDK);
    }

    public WritableDKConnection(Context context, String address, int type) {
        super(context, address, type);
    }

    protected void write(byte[] data){
        getPlutoconDKEditor().setProperty(DKUUID.WRITE_CHARACTERISTIC, data).commit();
    }
}
