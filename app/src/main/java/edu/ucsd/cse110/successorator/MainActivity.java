package edu.ucsd.cse110.successorator;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;
import java.util.Objects;

import edu.ucsd.cse110.successorator.databinding.ActivityMainBinding;
import edu.ucsd.cse110.successorator.ui.ActionBarUpdater;
import edu.ucsd.cse110.successorator.ui.taskList.dialog.CreateTaskDialogFragment;
import edu.ucsd.cse110.successorator.util.DateSubject;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        edu.ucsd.cse110.successorator.databinding.ActivityMainBinding view = ActivityMainBinding.inflate(getLayoutInflater(), null, false);

        setContentView(view.getRoot());

        view.fragmentContainer.setOnClickListener(v -> {
            ((SuccessoratorApplication) getApplicationContext()).getDateSubject().setItem(new Date());
        });

        // Hides Android's default ActionBar so we can use our own
        Objects.requireNonNull(getSupportActionBar()).hide();

        // Instantiate the actionBarUpdater
        ActionBarUpdater actionBarUpdater = new ActionBarUpdater(this);

        // Get DateSubject observable from Application, then add ActionBarUpdater as observer
        DateSubject dateSubject = ((SuccessoratorApplication) getApplicationContext()).getDateSubject();
        dateSubject.observe(actionBarUpdater);

        // Create FocusSwitcherListener
        findViewById(R.id.focus_switch).setOnClickListener(this::onFocusSwitchClick);

        // Date Picker Listener
        findViewById(R.id.date_title).setOnClickListener(this::onDateTitleClick);

        // Create AddTaskListener
        findViewById(R.id.add_task).setOnClickListener(this::onAddTaskClick);

    }

    private void onDateTitleClick(View view) {
        // Does nothing for now...
    }

    private void onAddTaskClick(View view) {
        CreateTaskDialogFragment dialogFragment = CreateTaskDialogFragment.newInstance();
        dialogFragment.show(getSupportFragmentManager(), "CreateTaskDialogFragment");
    }

    private void onFocusSwitchClick(View view) {
        // Does nothing for now....
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Set to current day, notifies ActionBarUpdater
        DateSubject dateSubject = ((SuccessoratorApplication) getApplicationContext()).getDateSubject();
        dateSubject.setItem(new Date());
        dateSubject.loadDate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        var itemId = item.getItemId();

        if (itemId == R.id.add_task) {
            var dialogFragment = CreateTaskDialogFragment.newInstance();
            dialogFragment.show(getSupportFragmentManager(), "CreateTaskDialogFragment");
        }

        return super.onOptionsItemSelected(item);
    }

}
