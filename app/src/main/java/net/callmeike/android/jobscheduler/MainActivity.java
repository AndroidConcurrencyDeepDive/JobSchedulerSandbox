package net.callmeike.android.jobscheduler;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import net.callmeike.android.jobscheduler.scheduler.SimpleJobService;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
    }

    @OnClick(R.id.start)
    void startTask() { SimpleJobService.startSampleTask(MainActivity.this); }

    @OnClick(R.id.stop)
    void stopTask() { SimpleJobService.cancelAllSampleTasks(MainActivity.this); }
}
