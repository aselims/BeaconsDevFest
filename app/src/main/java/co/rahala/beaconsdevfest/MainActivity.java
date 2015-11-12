package co.rahala.beaconsdevfest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private TextView stopScanTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        final BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
                Log.i(TAG, "Address: " + bluetoothDevice.getAddress() +
                                "\n RSSI:" + i +
                                "\n name:" + bluetoothDevice.getName());
            }
        };

        bluetoothAdapter.startLeScan(scanCallback);

        findViewById(R.id.btn_stop_scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothAdapter.stopLeScan(scanCallback);
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
