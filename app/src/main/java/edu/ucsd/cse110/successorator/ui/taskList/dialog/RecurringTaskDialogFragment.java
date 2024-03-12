package edu.ucsd.cse110.successorator.ui.taskList.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import edu.ucsd.cse110.successorator.SuccessoratorApplication;
import edu.ucsd.cse110.successorator.TaskViewModel;
import edu.ucsd.cse110.successorator.databinding.RecurringTaskDialogBinding;
import edu.ucsd.cse110.successorator.lib.domain.Task;
import edu.ucsd.cse110.successorator.lib.domain.recurring.DailyRecurring;
import edu.ucsd.cse110.successorator.lib.domain.recurring.MonthlyRecurring;
import edu.ucsd.cse110.successorator.lib.domain.recurring.RecurringType;
import edu.ucsd.cse110.successorator.lib.domain.recurring.WeeklyRecurring;
import edu.ucsd.cse110.successorator.lib.domain.recurring.YearlyRecurring;
import edu.ucsd.cse110.successorator.ui.CalendarPickerDialog;
import edu.ucsd.cse110.successorator.util.DateSubject;

public class RecurringTaskDialogFragment extends DialogFragment {

    private RecurringTaskDialogBinding view;
    private TaskViewModel activityModel;

    RecurringTaskDialogFragment() {}

    public static RecurringTaskDialogFragment newInstance() {
        var fragment = new RecurringTaskDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        this.view = RecurringTaskDialogBinding.inflate(getLayoutInflater());

        view.saveButton.setOnClickListener(v -> addTask(getDialog()));
        view.calendarDialogOpener.setOnClickListener(v -> openCalendarPicker());
        return new AlertDialog.Builder(getActivity())
                .setView(view.getRoot())
                .create();
    }

    private void openCalendarPicker() {
        CalendarPickerDialog cpd = CalendarPickerDialog.newInstance();
        cpd.show(getParentFragmentManager(), "CalendarPickerDialog");
        // opens the calendar picker
    }

    private void addTask(DialogInterface dialog) {
        String input = view.addTaskDialog.getText().toString();
        if (input.length() == 0) {
            dialog.dismiss();
            return;
        }
        String frequency = "";

        SuccessoratorApplication app = (SuccessoratorApplication) requireActivity().getApplication();
        DateSubject dateSubject = app.getDateSubject();

        RecurringType recurringType;

        if (view.radioDaily.isChecked()) {
            Log.i("Daily Add", dialog.toString());
            frequency = " 2";
            recurringType = new DailyRecurring();
        } else if (view.radioWeekly.isChecked()) {
            Log.i("Weekly Add", dialog.toString());
            frequency = " 3";
            recurringType = new WeeklyRecurring(dateSubject.getDayOfWeek());
        } else if (view.radioMonthly.isChecked()) {
            Log.i("Monthly Add", dialog.toString());
            frequency = " 4";
            recurringType = new MonthlyRecurring(dateSubject.getWeekOfMonth(), dateSubject.getDayOfWeek());
        } else if (view.radioYearly.isChecked()) {
            Log.i("Yearly Add", dialog.toString());
            recurringType = new YearlyRecurring(dateSubject.getMonth(), dateSubject.getDayOfMonth());
            frequency = " 5";
        } else {
            throw new IllegalStateException("No Selection Made");
        }

        int recurringID = activityModel.getTasksRepository().generateRecurringID();
        var task = new Task(null, input + frequency, 0, false, recurringType, recurringID, app.getTaskView());

        if (task.isRecurring() && task.getRecurringType().checkIfToday(dateSubject.getItem())) {
            activityModel.getTasksRepository().addOnetimeTask(task);
        }

        activityModel.append(task);
        dialog.dismiss();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        var modelOwner = requireActivity();
        var modelFactory = ViewModelProvider.Factory.from(TaskViewModel.initializer);
        var modelProvider = new ViewModelProvider(modelOwner, modelFactory);
        this.activityModel = modelProvider.get(TaskViewModel.class);
    }


}
