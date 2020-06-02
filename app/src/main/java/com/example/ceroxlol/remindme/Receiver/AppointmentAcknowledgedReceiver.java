package com.example.ceroxlol.remindme.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.ceroxlol.remindme.MainActivity;

import java.util.Calendar;

import Data.Appointment;

public class AppointmentAcknowledgedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Toast.makeText(context,"Appointment ack",Toast.LENGTH_SHORT).show();

        String action=intent.getStringExtra("action");
        int appointmentid = intent.getIntExtra("appointmentId", -1);
        if(action.equals("setAcknowledge")){
            updateAcknowledgeForAppointment(appointmentid);
        }
        else if(action.equals("setSnooze")){
            setRemindTimer(appointmentid);

        }
        //This is used to close the notification tray
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);
    }

    public void updateAcknowledgeForAppointment(int appointmentId){
        Appointment appointmentToUpdate = MainActivity.mDatabaseHelper.getAppointmentDaoRuntimeException().queryForId(appointmentId);
        appointmentToUpdate.setAcknowledged(true);
        MainActivity.mDatabaseHelper.getAppointmentDaoRuntimeException().update(appointmentToUpdate);
    }

    public void setRemindTimer(int appointmentId){
        Appointment appointmentToUpdate = MainActivity.mDatabaseHelper.getAppointmentDaoRuntimeException().queryForId(appointmentId);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(appointmentToUpdate.getAppointmentTime());
        calendar.add(Calendar.MINUTE, 10);
        appointmentToUpdate.setAppointmentRemindTime(calendar.getTime());
    }
}