package fr.wakemybus.playground.geofencing.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.util.ArrayList;

/**
 * Created by thibaultguegan on 08/02/15.
 */
public abstract class AbstractService extends Service {

    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_REGISTER_CLIENT_CONFIRMATION = 3;

    protected Intent mStartIntent;

    protected ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    @SuppressLint("HandlerLeak")
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    onReceiveMessage(msg);
                    break;

                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    onReceiveMessage(msg);
                    break;

                default:
                    onReceiveMessage(msg);
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        mStartIntent = intent;
        onStartService();
        return mMessenger.getBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        onStopService();
    }

    public void sendClientMessage(int what) {
        sendClientMessage(what, -1, -1, null);
    }

    public void sendClientMessage(int what, Bundle b) {
        sendClientMessage(what, -1, -1, b);
    }

    public void sendClientMessage(int what, int arg1, int arg2, Bundle b) {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                Message msg = Message.obtain(null, what);

                if (msg != null) {
                    if (b != null) {
                        msg.setData(b);
                    }

                    if(arg1 >=0){
                        msg.arg1 = arg1;
                    }

                    if(arg2 >= 0){
                        msg.arg2 = arg2;
                    }

                    mClients.get(i).send(msg);
                }
            } catch (RemoteException e) {
                // The client is dead so remove it from the list.
                if (mClients.size() > i) {
                    mClients.remove(i);
                }
            }
        }
    }

    /**
     * Send a reply to a message
     * @param replyToMsg The message to reply to
     * @param what Mesage type to reply with
     * @param b Bundle to embed into message (may be null)
     */
    public void replyToClientMessage(Message replyToMsg, int what, Bundle b) {
        replyToClientMessage(replyToMsg, what, b, -1, -1);
    }

    /**
     * Send a reply to a message
     * @param replyToMsg The message to reply to
     * @param what Mesage type to reply with
     * @param b Bundle to embed into message (may be null)
     * @param arg1 Optional parameter 1
     * @param arg2 Optional parameter 2
     */
    public void replyToClientMessage(Message replyToMsg, int what, Bundle b, int arg1, int arg2) {
        Message replyMsg = Message.obtain(null, what);

        if (replyMsg != null) {
            replyMsg.arg1 = arg1;
            replyMsg.arg2 = arg2;

            if (b != null) {
                replyMsg.setData(b);
            }

            if (replyToMsg.replyTo != null) {
                try {
                    replyToMsg.replyTo.send(replyMsg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public abstract void onStartService();
    public abstract void onStopService();
    public abstract void onReceiveMessage(Message msg);
}
