package co.rahala.beaconsdevfest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "BEACON";
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private TextView distanceTV;

    private TextToSpeech textToSpeech;

    // The Eddystone Service UUID, 0xFEAA.
    private static final ParcelUuid EDDYSTONE_SERVICE_UUID = ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB");

    // Eddystone frame types
    private static final byte TYPE_UID = 0x00;
    private static final byte TYPE_URL = 0x10;
    private static final byte TYPE_TLM = 0x20;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });

        distanceTV = (TextView) findViewById(R.id.tv_distance);


        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        //various nice parameters but requires API23
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build();


        //UUID F9:82:48:1C:E5:D7
        //URL D7:C0:EF:57:4E:07
        List<ScanFilter> filters = new ArrayList<ScanFilter>();
        filters.add(
                new ScanFilter.Builder()
                        .setServiceUuid(EDDYSTONE_SERVICE_UUID)
                        .build());

        final ScanCallback scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);

                byte[] data = result.getScanRecord().getServiceData(EDDYSTONE_SERVICE_UUID);
                if (data == null)
                    return;

                 /*
                byte frameType = data[0];
                if (frameType != TYPE_UID)
                    return;

                String namespace = new BigInteger(1, Arrays.copyOfRange(data, 2, 12)).toString(16);
                String instance = new BigInteger(1, Arrays.copyOfRange(data, 12, 18)).toString(16);

                if (!(namespace + instance).equals("edd1ebeac04e5defa017" + "c5612a8cc253"))
                    return;
*/

                //Received signal strength indication
                int rssi;
                int txPower;
                double distance;
                String deviceAddress = result.getDevice().getAddress();
                if (deviceAddress.equals("F9:82:48:1C:E5:D7")) {
                    Log.i(TAG, String.valueOf(result.getRssi()));
                    rssi = result.getRssi();

                    txPower = data[1];
                    // pathLoss = (txPower at 0m - rssi);
                    distance = Math.pow(10, ((txPower - rssi) - 41) / 20.0);
                    // because rssi is unstable, usually  only proximity zones are used:
                    // - immediate (very close to the beacon)
                    // - near (about 1-3 m from the beacon)
                    // - far (further away or the signal is fluctuating too much to make a better estimate)
                    // - unknown
                    Log.i("distance", String.format("%.2fm", distance));
                    distanceTV.setText(String.format("%.2fm", distance));

                    if (distance > 4) {
                        if (!textToSpeech.isSpeaking()) {
                            textToSpeech.speak("Don't forget your belongings!", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    }


                }

            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        };

        bluetoothLeScanner.startScan(filters, settings, scanCallback);

        findViewById(R.id.btn_stop_scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothLeScanner.stopScan(scanCallback);
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
