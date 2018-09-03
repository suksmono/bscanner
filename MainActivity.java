package abscanner.bscanner5;

import android.app.TabActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TabHost;
import android.widget.TextView;

public class MainActivity extends TabActivity {
    // ---
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // lock to portrait
        int orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        // or = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        setRequestedOrientation(orientation);

        // create the TabHost that will contain the Tabs
        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);

        TabHost.TabSpec tab1 = tabHost.newTabSpec("First Tab");
        TabHost.TabSpec tab2 = tabHost.newTabSpec("Second Tab");
        TabHost.TabSpec tab3 = tabHost.newTabSpec("Third tab");

        TextView textTab1 = new TextView(this);
        textTab1.setText("OPENING");
        textTab1.setTextSize(15);
        textTab1.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        tab1.setIndicator(textTab1).setContent(new Intent(this, Tab1Activity.class));
        tabHost.addTab(tab1);

        TextView textTab2 = new TextView(this);
        textTab2.setText("MEASUREMENT");
        textTab2.setTextSize(15);
        textTab2.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        tab2.setIndicator(textTab2).setContent(new Intent(this, Tab2Activity.class));
        tabHost.addTab(tab2);

        TextView textTab3 = new TextView(this);
        textTab3.setText("DISPLAY MAP");
        textTab3.setTextSize(15);
        textTab3.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        tab3.setIndicator(textTab3).setContent(new Intent(this, Tab3Activity.class));
        tabHost.addTab(tab3);

    }


}
