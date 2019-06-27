package GUI;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.ceroxlol.remindme.R;

import java.util.ArrayList;

import Data.AppointmentHandler;

public class AppointmentsAdapter extends ArrayAdapter<AppointmentHandler> {

    public AppointmentsAdapter(@NonNull Context context, ArrayList<AppointmentHandler> data) {
        super(context, 0, data);
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        // Get the data item for this position
        final AppointmentHandler appointment = getItem(i);
        // Check if an existing view is being reused, otherwise inflate the view
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.appointments_layout, parent, false);
        }
        // Lookup view for data population
        TextView appointmentName = (TextView) view.findViewById(R.id.textViewAppointmentName);
        TextView appointmentText = (TextView) view.findViewById(R.id.textViewAppointmentText);
        TextView appointmentType = (TextView) view.findViewById(R.id.textViewAppointmentType);
        TextView appointmentPrio = (TextView) view.findViewById(R.id.textViewAppointmentPrio);

        // Populate the data into the template view using the data object
        appointmentName.setText(appointment.getName());
        appointmentText.setText(appointment.getAppointmentText());
        //Because integer is a special type of snowflake, "" needs to be inserted in front to be a string
        appointmentType.setText(""+appointment.getType());
        //Or use String.valeOf
        appointmentPrio.setText(String.valueOf(appointment.getPriority()));

        // Return the completed view to render on screen
        return view;
    }
}
