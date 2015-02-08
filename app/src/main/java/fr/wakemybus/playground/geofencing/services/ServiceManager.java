package fr.wakemybus.playground.geofencing.services;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
public class ServiceManager {
    private Class<? extends AbstractService> mServiceClass;
    private Context mActivity;
    private boolean mIsBound;
    private Messenger mService = null;
    private Handler mIncomingHandler = null;
    private final Messenger mMessenger = new Messenger(new IncomingHandler());

    private class QueuedMessage {
        public int message;
        public Bundle bundle;
        public int arg1, arg2;
        public boolean replyTo;
        public QueuedMessage(int m, int arg1, int arg2, Bundle b, boolean replyTo) {
            this.message = m;
            this.bundle = b;
            this.arg1 = arg1;
            this.arg2 = arg2;
            this.replyTo = replyTo;
        }
    }
    private ArrayList<QueuedMessage> mMessageQueue = new ArrayList<QueuedMessage>();

    @SuppressLint("HandlerLeak")
    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (mIncomingHandler != null) {
                mIncomingHandler.handleMessage(msg);
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mIsBound = true;

            try {
                // Send out the client registered message
                Message msg = Message.obtain(null, AbstractService.MSG_REGISTER_CLIENT);
                if (msg != null) {
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                }

                // Process any queued messages
                for (QueuedMessage qm : mMessageQueue) {
                    sendServiceMessage(qm.message, qm.arg1, qm.arg2, qm.bundle, qm.replyTo);
                }
                mMessageQueue.clear();
            } catch (RemoteException e) {
                // In this case the service has crashed before we could
                // even do anything with it.
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has
            // been unexpectedly disconnected - process crashed.
            mService = null;
            mIsBound = false;
        }
    };

    public ServiceManager(Context context,
                          Class<? extends AbstractService> serviceClass,
                          Handler incomingHandler) {
        this.mActivity = context;
        this.mServiceClass = serviceClass;
        this.mIncomingHandler = incomingHandler;
    }

    public void start() {
        doStartService();
        doBindService();
    }

    public void stop() {
        doUnbindService();
        doStopService();
    }

    /**
     * Use with caution (only in Activity.onDestroy())!
     */
    public void unbind() {
        doUnbindService();
    }

    public void sendServiceMessage(int message) {
        sendServiceMessage(message, 0, 0, null, false);
    }

    public void sendServiceMessage(int message, boolean replyTo) {
        sendServiceMessage(message, 0, 0, null, replyTo);
    }

    public void sendServiceMessage(int message, Bundle b){
        sendServiceMessage(message, 0, 0, b, false);
    }

    public void sendServiceMessage(int message, int arg1, int arg2, Bundle b, boolean replyTo) {
        if (!mIsBound) {
            // Queue message and connect service
            mMessageQueue.add(new QueuedMessage(message, arg1, arg2, b, replyTo));
            start();
            return;
        }

        Message msg = Message.obtain(null, message);

        if (msg != null) {
            if (b != null) {
                msg.setData(b);
            }

            if (arg1 != 0) {
                msg.arg1 = arg1;
            }

            if (arg2 != 0) {
                msg.arg2 = arg2;
            }

            if (replyTo) {
                msg.replyTo = mMessenger;
            }

            try {
                send(msg);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void send(Message msg) throws RemoteException {
        if (mIsBound) {
            if (mService != null) {
                mService.send(msg);
            }
        }
    }

    private void doStartService() {
        Intent i = new Intent(mActivity, mServiceClass);
        mActivity.startService(i);
    }

    private void doStopService() {
        mActivity.stopService(new Intent(mActivity, mServiceClass));
    }

    private void doBindService() {
        if (!mIsBound) {
            Intent i = new Intent(mActivity, mServiceClass);
            mActivity.bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it,
            // then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(
                            null, AbstractService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }

            // Detach our existing connection.
            mActivity.unbindService(mConnection);
            mIsBound = false;
        }
    }
}
