package com.silho.ideo.meetus.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.silho.ideo.meetus.alarmManager.ReminderScheduler;

/**
 * Created by Samuel on 04/12/2017.
 */

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
            ReminderScheduler.scheduleReminder(context);
        }
    }
}
