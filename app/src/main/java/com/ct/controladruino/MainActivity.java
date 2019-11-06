package com.ct.controladruino;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    public final String ON_DOOR = "1";
    public final String OFF_DOOR = "2";
    public final String ON_FAN = "60";
    public final String OFF_FAN = "61";
    public final String ON_LED_LIV = "10";
    public final String OFF_LED_LIV = "11";
    public final String ON_LED_BED = "20";
    public final String OFF_LED_BED = "21";
    public final String ON_LED_BATH = "30";
    public final String OFF_LED_BATH = "31";
    public final String ON_LED_GAR = "40";
    public final String OFF_LED_GAR = "41";
    public final String ON_LED_OUT = "50";
    public final String OFF_LED_OUT = "51";


    @BindView(R.id.iv_fan)
    ImageView ivFan;
    @BindView(R.id.iv_auto_door)
    ImageView ivAutoDoor;
    @BindView(R.id.iv_led)
    ImageView ivLed;

    @BindView(R.id.layout_fan)
    View layoutFan;
    @BindView(R.id.layout_gara)
    View layoutGara;
    @BindView(R.id.layout_led)
    View layoutLed;

    @BindView(R.id.swt_gara)
    SwitchCompat swtGara;
    @BindView(R.id.swt_fan)
    SwitchCompat swtFan;
    @BindView(R.id.swt_led_bath)
    SwitchCompat swtLedBath;
    @BindView(R.id.swt_led_bed)
    SwitchCompat swtLedBed;
    @BindView(R.id.swt_led_liv)
    SwitchCompat swtLedLiv;
    @BindView(R.id.swt_led_out)
    SwitchCompat swtLedOut;
    @BindView(R.id.bt_connect)
    Button btConnect;


    BluetoothSPP bluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        bluetooth = new BluetoothSPP(this);

        if (!bluetooth.isBluetoothAvailable()) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not available", Toast.LENGTH_SHORT).show();
            finish();
        }

        bluetooth.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                btConnect.setText("Connected to " + name);
            }

            public void onDeviceDisconnected() {
                btConnect.setText("Connection lost");
            }

            public void onDeviceConnectionFailed() {
                btConnect.setText("Unable to connect");
            }
        });

        btConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetooth.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bluetooth.disconnect();
                } else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
            }
        });
        onClickSwitch();

//		on.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				bluetooth.send(ON, true);
//			}
//		});
//
//		off.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				bluetooth.send(OFF, true);
//			}
//		});
//
    }

    public void onStart() {
        super.onStart();
        if (!bluetooth.isBluetoothEnabled()) {
            bluetooth.enable();
        } else {
            if (!bluetooth.isServiceAvailable()) {
                bluetooth.setupService();
                bluetooth.startService(BluetoothState.DEVICE_OTHER);
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        bluetooth.stopService();
    }

    @OnClick({R.id.iv_led, R.id.iv_fan, R.id.iv_auto_door})
    public void onClickMenu(View view) {
        switch (view.getId()) {
            case R.id.iv_auto_door:
                layoutGara.setVisibility(View.VISIBLE);
                layoutFan.setVisibility(View.GONE);
                layoutLed.setVisibility(View.GONE);
                break;
            case R.id.iv_fan:
                layoutGara.setVisibility(View.GONE);
                layoutFan.setVisibility(View.VISIBLE);
                layoutLed.setVisibility(View.GONE);
                break;
            case R.id.iv_led:
                layoutGara.setVisibility(View.GONE);
                layoutFan.setVisibility(View.GONE);
                layoutLed.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void onClickSwitch() {
        this.swtFan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    bluetooth.send(ON_FAN, true);
                }else {
                    bluetooth.send(OFF_FAN,false);
                }
            }
        });

        this.swtGara.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    bluetooth.send(ON_DOOR, true);
                }else {
                    bluetooth.send(OFF_DOOR,false);
                }
            }
        });
        this.swtLedLiv.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    bluetooth.send(ON_LED_LIV, true);
                }else {
                    bluetooth.send(OFF_LED_LIV,false);
                }
            }
        });
        this.swtLedBath.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    bluetooth.send(ON_LED_BATH, true);
                }else {
                    bluetooth.send(OFF_LED_BATH,false);
                }
            }
        });
        this.swtLedBed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    bluetooth.send(ON_LED_BED, true);
                }else {
                    bluetooth.send(OFF_LED_BED,false);
                }
            }
        });
        this.swtLedOut.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    bluetooth.send(ON_LED_OUT, true);
                }else {
                    bluetooth.send(OFF_LED_OUT,false);
                }
            }
        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bluetooth.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bluetooth.setupService();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

}
