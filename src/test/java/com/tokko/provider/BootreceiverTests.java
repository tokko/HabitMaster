package com.tokko.provider;

import android.content.Context;

import com.tokko.BuildConfig;
import com.tokko.notificationmanager.Bootreceiver;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowNotificationManager;

import java.util.List;

@Config(emulateSdk = 19, constants = BuildConfig.class, manifest = "src/main/AndroidManifest.xml")
@RunWith(RobolectricTestRunner.class)
public class BootreceiverTests {

    private ShadowNotificationManager snm;
    private Context context;

    @Before
    public void setup(){
        context = RuntimeEnvironment.application.getApplicationContext();
        snm = Shadows.shadowOf(((android.app.NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE)));
    }

    @Test
    public void receiverRegistered(){
        List<ShadowApplication.Wrapper> registeredReceivers = ShadowApplication.getInstance().getRegisteredReceivers();

        Assert.assertFalse(registeredReceivers.isEmpty());

        boolean receiverFound = false;
        for (ShadowApplication.Wrapper wrapper : registeredReceivers) {
            if (!receiverFound)
                receiverFound = Bootreceiver.class.getSimpleName().equals(
                        wrapper.broadcastReceiver.getClass().getSimpleName());
        }

        Assert.assertTrue(receiverFound);
    }
}
