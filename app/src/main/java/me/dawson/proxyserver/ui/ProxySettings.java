/*WifiShare Juancito_Oficial*/
package me.dawson.proxyserver.ui;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import com.netplus.vpn.R;
import com.slipkprojects.ultrasshservice.tunnel.TunnelUtils;
import android.app.Notification;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.widget.ToggleButton;
import android.widget.Button;
import android.widget.ImageView;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.app.AlertDialog;
import android.view.Gravity;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;
import android.view.ViewGroup;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdRequest;

public class ProxySettings extends AppCompatActivity implements ServiceConnection,
		OnCheckedChangeListener {
	public static final String TAG = "ProxySettings";

	protected static final String KEY_PREFS = "proxy_pref";
	protected static final String KEY_ENABALE = "proxy_enable";

	private static int NOTIFICATION_ID = 20140701;
	private static final String PortDefault = "8080";

	private IProxyControl proxyControl = null;

	private TextView tvInfo;
	private ToggleButton cbEnable;
	private Button ZonaWifi;
	private TextView txtip;
	private TextView text_port;
    private ImageView wifion;
    private ImageView wifioff;

    private TextView ipx;
	
    private TextView ipmex;
    
    
    
  
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		setContentView(R.layout.proxy_settings);
		Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar_main);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true); 

		tvInfo = (TextView) findViewById(R.id.tv_info);
		cbEnable = (ToggleButton) findViewById(R.id.cb_enable);
		cbEnable.setOnCheckedChangeListener(this);
		ipx = (TextView) findViewById(R.id.ip_layoutx);
      	ipx.setText(TunnelUtils.getLocalIpAddress());

		AdView adVimew = (AdView) findViewById(R.id.addsmew);
		AdRequest adRequest = new AdRequest.Builder().build();
		adVimew.loadAd(adRequest);
        
        
        wifion = (ImageView) findViewById(R.id.wifion);
        wifioff = (ImageView) findViewById(R.id.wifioff);
		text_port = (TextView) findViewById(R.id.textport);
		text_port.setText(PortDefault);
		ZonaWifi = (Button) findViewById(R.id.zonawifi);
		ZonaWifi.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent tetherSettings = new Intent();
				tetherSettings.setClassName("com.android.settings", "com.android.settings.TetherSettings");
				startActivity(tetherSettings);
			}
		});

		Intent intent = new Intent(this, ProxyService.class);
		bindService(intent, this, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onServiceConnected(ComponentName cn, IBinder binder) {
		proxyControl = (IProxyControl) binder;
		if (proxyControl != null) {
			updateProxy();
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName cn) {
		proxyControl = null;
	}

	@Override
	protected void onDestroy() {
		unbindService(this);
		super.onDestroy();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		SharedPreferences sp = getSharedPreferences(KEY_PREFS, MODE_PRIVATE);
		sp.edit().putBoolean(KEY_ENABALE, isChecked).commit();
		updateProxy();
	}

	private void updateProxy() {
		if (proxyControl == null) {
			return;
		}

		boolean isRunning = false;
		try {
			isRunning = proxyControl.isRunning();
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		boolean shouldRun = getSharedPreferences(KEY_PREFS, MODE_PRIVATE)
				.getBoolean(KEY_ENABALE, false);
		if (shouldRun && !isRunning) {
			startProxy();
		} else if (!shouldRun && isRunning) {
			stopProxy();
		}

		try {
			isRunning = proxyControl.isRunning();
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		if (isRunning) {
			tvInfo.setText(R.string.proxy_on);
			cbEnable.setChecked(true);
			// Toast.makeText(this, "Wifi Sharet Desactivado", Toast.LENGTH_SHORT).show();
			
            wifioff.setVisibility(View.GONE);
            wifion.setVisibility(View.VISIBLE);
		} else {
			tvInfo.setText(R.string.proxy_off);
			cbEnable.setChecked(false);
			/// Toast.makeText(this, "Wifi Sharet Activo", Toast.LENGTH_SHORT).show();
		/*	LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View inflate = inflater.inflate(R.layout.wifi_desconectado, (ViewGroup) null);
            AlertDialog.Builder builer = new AlertDialog.Builder(this); 
            builer.setView(inflate); 
            //  ImageView iv = inflate.findViewById(R.id.icon);
            TextView title = inflate.findViewById(R.id.titulo);
            // TextView ms = inflate.findViewById(R.id.message);
            ImageView bubu = inflate.findViewById(R.id.afuera);
            // iv.setImageResource(R.drawable.src_images_mine_shareicon);
            title.setText("Wi-Fi Shared Desactivado, Activalo para compartir wifi.");
            // ms.setText("Ocurrio un error al buscar la actualizacion.\nPor favor contacta al desarrollador.");
            // bubu.setText("Ok");
            
            final AlertDialog alert = builer.create(); 
            alert.setCanceledOnTouchOutside(false);
            alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alert.getWindow().setGravity(Gravity.CENTER); 
            alert.getWindow().getAttributes().windowAnimations = R.style.dialog;
            alert.show();
            bubu.setOnClickListener(new View.OnClickListener() { 
                    @Override 
                    public void onClick(View v) { 
                        try
                        {
                            alert.dismiss();

                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                    }});

            alert.show(); */
            
            wifioff.setVisibility(View.VISIBLE);
            wifion.setVisibility(View.GONE);
		}
	}
    
    
    
    

	private void startProxy() {
		boolean started = false;
		try {
			started = proxyControl.start();
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		if (!started) {
			return;
		}

		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		Context context = getApplicationContext();

		Notification notification = new Notification();
		notification.icon = R.drawable.ic_launcher;
		notification.tickerText = getResources().getString(R.string.proxy_on);
		notification.when = System.currentTimeMillis();

		CharSequence contentTitle = getResources().getString(R.string.app_name);
		;
		CharSequence contentText = getResources().getString(
				R.string.service_text);
		Intent intent = new Intent(this, ProxySettings.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				intent, PendingIntent.FLAG_IMMUTABLE);

		/*notification.setLatestEventInfo(context, contentTitle, contentText,
				pendingIntent);*/
		NotificationManager (context, contentTitle, contentText, pendingIntent);
		
		notification.flags |= Notification.FLAG_ONGOING_EVENT;

		manager.notify(NOTIFICATION_ID, notification);
		
		Vibrator vb_service = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		vb_service.vibrate(10);
		Toast.makeText(this, getResources().getString(R.string.proxy_started),
				Toast.LENGTH_SHORT).show();
                
       /*  LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View inflate = inflater.inflate(R.layout.wifi_conectado, (ViewGroup) null);
        AlertDialog.Builder builer = new AlertDialog.Builder(this); 
        builer.setView(inflate); 
        //  ImageView iv = inflate.findViewById(R.id.icon);
        TextView title = inflate.findViewById(R.id.activos);
        // TextView ms = inflate.findViewById(R.id.message);
        ImageView bubu = inflate.findViewById(R.id.afuerax2);
        // iv.setImageResource(R.drawable.src_images_mine_shareicon);
        TextView ippro= inflate.findViewById(R.id.ippro);
         ippro.setText(TunnelUtils.getLocalIpAddress());
		
        title.setText("Wi-Fi Shared Activado ahora puede compartir wifi.");
        // ms.setText("Ocurrio un error al buscar la actualizacion.\nPor favor contacta al desarrollador.");
        // bubu.setText("Ok");
        final AlertDialog alert = builer.create(); 
        alert.setCanceledOnTouchOutside(false);
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alert.getWindow().setGravity(Gravity.CENTER); 
        alert.getWindow().getAttributes().windowAnimations = R.style.dialog;
        alert.show();
        bubu.setOnClickListener(new View.OnClickListener() { 
                @Override 
                public void onClick(View v) { 
                    try
                    {
                        alert.dismiss();

                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                }});

        alert.show(); */
        
                
	}
	
	private void NotificationManager(Context context, CharSequence contentTitle, CharSequence contentText, PendingIntent pendingIntent) {
	}

	private void stopProxy() {
		boolean stopped = false;

		try {
			stopped = proxyControl.stop();
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		if (!stopped) {
			return;
		}

		tvInfo.setText(R.string.proxy_off);
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(NOTIFICATION_ID);
		Vibrator vb_service = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		vb_service.vibrate(10);
		Toast.makeText(this, getResources().getString(R.string.proxy_stopped),
				Toast.LENGTH_SHORT).show();
                
                
        
	
                
                
	}

    @Override
    public String toString() {
        return "ProxySettings[ipx=" + ipx + "]";
    }
}