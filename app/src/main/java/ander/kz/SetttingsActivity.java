package ander.kz;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class SetttingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        TextView t1 = (TextView) findViewById(R.id.send_message_us);
        t1.setMovementMethod(LinkMovementMethod.getInstance());
        TextView t2 = (TextView) findViewById(R.id.our_apps_link);
        t2.setMovementMethod(LinkMovementMethod.getInstance());
        TextView t3 = (TextView) findViewById(R.id.someone);
        t3.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
