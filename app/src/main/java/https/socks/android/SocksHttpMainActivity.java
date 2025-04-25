package https.socks.android;
import https.socks.android.activities.SpeedTestActivity;
import android.content.res.ColorStateList;
import androidx.core.content.ContextCompat;
import java.net.HttpURLConnection;
import java.net.URL;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;
import com.slipkprojects.ultrasshservice.V2Service;
import com.slipkprojects.ultrasshservice.V2Configs;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import com.socksfast.ultrasshservice.util.SkProtect;
import android.widget.Toast;
import me.dawson.proxyserver.ui.ProxySettings;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.app.ActivityManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.netfreemexico.generador.NetFreeMXGen;
import com.netplus.vpn.R;
import com.slipkprojects.ultrasshservice.LaunchVpn;
import com.slipkprojects.ultrasshservice.config.ConfigParser;
import com.slipkprojects.ultrasshservice.config.Settings;
import com.slipkprojects.ultrasshservice.logger.ConnectionStatus;
import com.slipkprojects.ultrasshservice.logger.SkStatus;
import com.slipkprojects.ultrasshservice.tunnel.TunnelManagerHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import https.socks.android.activities.BaseActivity;
import https.socks.android.activities.ConfigGeralActivity;
import https.socks.android.activities.ServersActivity;
import https.socks.android.adapter.LogsAdapter;
import https.socks.android.util.AESCrypt;
import https.socks.android.util.ConfigUpdate;
import https.socks.android.util.ConfigUtil;
import https.socks.android.util.RetrieveData;
import https.socks.android.util.StoredData;
import https.socks.android.util.Utils;

/**
 * Activity Principal
 *
 * @author SlipkHunter
 */

@SuppressLint({"SetTextI18n", "NonConstantResourceId"})
@SuppressWarnings("deprecation")
public class SocksHttpMainActivity extends BaseActivity
        implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, SkStatus.StateListener, IdValidator.ValidationCallback {
    private SharedPreferences prefs;
    private DrawerLayout drawer;
    private static final int PICK_FILE = 85;

    private ViewPager vp;
    
    private LinearLayout serverlayout;
   private ImageView serverimage;
   private TextView servername, serverinfo;

    public static final String TODAY_DATA = "todaydata";
    private SharedPreferences myData;
    private static final int S_ONSTART_CALLED = 2;
    private static final int S_BIND_CALLED = 1;
    public TextView bytes_in_view;
    public TextView bytes_out_view;

    private static final String UPDATE_VIEWS = "MainUpdate";
    public static final String OPEN_LOGS = "com.slipkprojects.sockshttp:openLogs";
    private Settings mConfig;
    private Handler mHandler;
    private Button starterButton;
    private ConfigUtil config;
    private TextView serverName, serverInfo, textStatus, configVersion;
    private ImageView serverImage;
    private LinearLayout serversL;
    private static final String[] tabTitle = {"INICIO", "REGISTRO"};

    //poner en false si no quieres que la aplicacion sea modo token/user-pass, al poner en false se leera el usuario y contraseña del generador
    private final boolean MODO_TOKEN_USER_PASS = false;

       @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseActivity.antiremod(this);

        // Configura la política de StrictMode
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Inicializa el manejador y configuración
        mHandler = new Handler();
        mConfig = new Settings(this);

        // Configura el manejador de excepciones
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

        // Protección con SkProtect
        SkProtect.init(this);
        SkProtect.CharlieProtect();

        doLayout();
       setMainView();

        prefs = mConfig.getPrefsPrivate();
        myData = getSharedPreferences(TODAY_DATA, Context.MODE_PRIVATE);
        boolean showFirstTime = prefs.getBoolean("connect_first_time", true);
        int lastVersion = prefs.getInt("last_version", 0);

        // Configuración inicial si es la primera vez que se conecta
        if (showFirstTime) {
            SharedPreferences.Editor pEdit = prefs.edit();
            pEdit.putBoolean("connect_first_time", false);
            pEdit.apply();

            Settings.setDefaultConfig(this);
        }

        try {
            int idAtual = ConfigParser.getBuildId(this);

            if (lastVersion < idAtual) {
                SharedPreferences.Editor pEdit = prefs.edit();
                pEdit.putInt("last_version", idAtual);
                pEdit.apply();

                // Si se está actualizando
                if (!showFirstTime) {
                    if (lastVersion <= 12) {
                        Settings.setDefaultConfig(this);
                        Settings.clearSettings(this);
                    }
                }
            }
        } catch (IOException ignored) {
        }

        // Registro del receptor de broadcasts
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_VIEWS);
        filter.addAction(OPEN_LOGS);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mActivityReceiver, filter);

        if (!StoredData.isSetData) {
            StoredData.setZero();
        }

        liveData();

        NetFreeMXGen.getInstance().init(this, prefs, MODO_TOKEN_USER_PASS);

        // Activa esta línea de abajo si tu contraseña del token es personalizada
        // es decir que la contraseña no sea el token, cámbialo por tu contraseña
        // NetFreeMXGen.getInstance().setTokenPassword("Cristian");

        Utils.notificationP(this);

                // Crear una instancia de IdValidator y ejecutar la validación
        IdValidator idValidator = new IdValidator(this, this);
        idValidator.validateAndroidId();
    }
    
    private void borrarDatosApp() {
    ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
    if (activityManager != null) {
        // Borrar datos de la aplicación
        boolean resultado = activityManager.clearApplicationUserData();
        if (resultado) {
            Toast.makeText(this, "Datos de la aplicación borrados. Reiniciando...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error al borrar los datos", Toast.LENGTH_SHORT).show();
        }
    } else {
        Toast.makeText(this, "No se pudo borrar los datos", Toast.LENGTH_SHORT).show();
    }
}

    @Override
public void onValidationComplete(boolean isValid) {
    if (!isInternetAvailableThroughVPN()) {
        // Si no hay conexión, muestra un mensaje y no hace nada más
        Toast.makeText(this, "Sin conexión a Internet a través de la VPN", Toast.LENGTH_SHORT).show();
        return;
    }

    if (!isValid) {
        stopV2RayService();
        showInvalidIdDialog();
    }
}

private boolean isInternetAvailableThroughVPN() {
    try {
        HttpURLConnection urlc = (HttpURLConnection) (new URL("https://fast.com")).openConnection();
        urlc.setRequestProperty("User-Agent", "Test");
        urlc.setRequestProperty("Connection", "close");
        urlc.setConnectTimeout(1500);
        urlc.connect();
        return (urlc.getResponseCode() == 200);
    } catch (IOException e) {
        return false;
    }
}

private void stopV2RayService() {
    Intent stopIntent = new Intent(this, V2Service.class);
    stopIntent.putExtra("COMMAND", V2Configs.V2RAY_SERVICE_COMMANDS.STOP_SERVICE);
    startService(stopIntent);
}

private void showInvalidIdDialog() {
    String androidId = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
    AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Acceso denegado")
            .setMessage("Este dispositivo no está autorizado para usar esta aplicación.\n\nID del dispositivo: " + androidId)
            .setCancelable(false)
            .setPositiveButton("Cerrar", (dialogInterface, which) -> {
                finish();
                System.exit(0);
            })
            .show();

    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData clip = ClipData.newPlainText("Android ID", androidId);
    clipboard.setPrimaryClip(clip);
    Toast.makeText(this, "ID copiado al portapapeles", Toast.LENGTH_SHORT).show();

    new android.os.Handler().postDelayed(() -> {
        if (dialog.isShowing()) {
            dialog.dismiss();
            finish();
            System.exit(0);
        }
    }, 3000);
}
    public void s(View v) {
        startActivity(new Intent(SocksHttpMainActivity.this, ServersActivity.class));
    }

    private void doLayout() {
        setContentView(R.layout.activity_main_drawer);
        Toolbar toolbar_main = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar_main);
        prefs = mConfig.getPrefsPrivate();
        bytes_in_view = findViewById(R.id.bytes_in);
        bytes_out_view = findViewById(R.id.bytes_out);
        drawer = findViewById(R.id.drawer);
        NavigationView navi = findViewById(R.id.navigation);
        starterButton = findViewById(R.id.activity_starterButtonMain);
        config = new ConfigUtil(this);
        navi.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar_main, R.string.app_name, R.string.app_name);
        toggle.syncState();
        drawer.addDrawerListener(toggle);
        updateConfig(true);
        starterButton.setOnClickListener(this);
        doTabs();
        TextView version = (TextView)findViewById ( R.id.zac); // version update
  version.setText("" + config.getVersion()); //version update

        serverlayout = findViewById(R.id.layout_mainservers);
            serverlayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(SocksHttpMainActivity.this, ServersActivity.class));
                }
            });
            serverimage = findViewById(R.id.imagemainlayout);
            servername = findViewById(R.id.nombremainlayout);
            serverinfo = findViewById(R.id.infomainlayout);
        
        textStatus = findViewById(R.id.textStatus);
        ((TextView) findViewById(R.id.appVersion)).setText(getAppInfoString(this));
        
        
        
    }

    private void liveData() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.post(() -> getData());
            }
        }, 0, 1000);
    }

    public void getData() {
        boolean isRunning = SkStatus.isTunnelActive();
        long mUpload, mDownload, saved_Send, saved_Down/*,up, down*/;
        String saved_date, tDate;
        List<Long> allData;
        allData = RetrieveData.findData();
        mDownload = allData.get(0);
        mUpload = allData.get(1);
        StoredData.storedData(mDownload, mUpload);
        Calendar ca = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        tDate = sdf.format(ca.getTime());
        saved_date = myData.getString("today_date", "empty");
        SharedPreferences.Editor editor = myData.edit();
        if (saved_date.equals(tDate)) {
            saved_Send = myData.getLong("UP_DATA", 0);
            saved_Down = myData.getLong("DOWN_DATA", 0);
            editor.putLong("UP_DATA", mUpload + saved_Send);
            editor.putLong("DOWN_DATA", mDownload + saved_Down);
            editor.apply();
        } else {
            editor.clear();
            editor.putString("today_date", tDate);
            editor.apply();
        }
        if (isRunning) {
            bytes_out_view.setText(render_bandwidth(myData.getLong("UP_DATA", 0)));
            bytes_in_view.setText(render_bandwidth(myData.getLong("DOWN_DATA", 0)));
        } else {
            myData.edit().putLong("UP_DATA", 0).apply();
            myData.edit().putLong("DOWN_DATA", 0).apply();
        }
    }

    public void doTabs() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        LogsAdapter mLogAdapter = new LogsAdapter(layoutManager, this);
        RecyclerView logList = findViewById(R.id.recyclerLog);
        logList.setAdapter(mLogAdapter);
        logList.setLayoutManager(layoutManager);
        mLogAdapter.scrollToLastPosition();
        vp = findViewById(R.id.viewpager);
        TabLayout tabs = findViewById(R.id.tablayout);
        vp.setAdapter(new MyAdapter(Arrays.asList(tabTitle)));
        vp.setOffscreenPageLimit(2);
        tabs.setTabMode(TabLayout.MODE_FIXED);
        tabs.setTabGravity(TabLayout.GRAVITY_FILL);
        tabs.setupWithViewPager(vp);
    }

    @SuppressLint("StringFormatMatches")
    private String getAppInfoString(Context c) {
        c.getPackageManager();
        String version = "";
        try {
            @SuppressLint("PackageManagerGetSignatures")
            PackageInfo packageinfo = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
            version = String.format(packageinfo.versionName);
        } catch (PackageManager.NameNotFoundException ignore) {
        }
        return version;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            
            
            case R.id.feedback:
                startActivity(new Intent("android.intent.action.VIEW",
                        Uri.parse("https://wa.me/+50684285967")));
                break;
            
           case R.id.action_clear_cache:
    borrarCacheYForzarDetencion();
    break;
            
            case R.id.fb:
                startActivity(new Intent("android.intent.action.VIEW",
                        Uri.parse("https://wa.me/+50684285967")));
                break;
            case R.id.settings:
                Intent hntent = new Intent(this, ConfigGeralActivity.class);
                startActivity(hntent);
                break;
            
           case R.id.hostshare:
					Intent hostshare2 = new Intent(SocksHttpMainActivity.this, ProxySettings.class);
					startActivity(hostshare2);
					break;
            
           case R.id.vpn_activa:
                                Intent intent3 = new Intent();
                                intent3.setAction("android.settings.VPN_SETTINGS");
                                SocksHttpMainActivity.this.startActivity(intent3);
                                Toast.makeText((Context) SocksHttpMainActivity.this, (CharSequence) "Abriendo Ajustes APN", (int) 0).show();
                                break;
            
           case R.id.speedtest:
						     Intent speedtest = new Intent(SocksHttpMainActivity.this, SpeedTestActivity.class);
						     startActivity(speedtest);
							 break;
            
           case R.id.action_clear_data:
            borrarDatosApp();
            break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public class MyAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            // TODO: Implement this method
            return 2;
        }

        @Override
        public boolean isViewFromObject(@NonNull View p1, @NonNull Object p2) {
            // TODO: Implement this method
            return p1 == p2;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            int[] ids = new int[]{R.id.tab1, R.id.tab2};
            int id;
            id = ids[position];
            // TODO: Implement this method
            return findViewById(id);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // TODO: Implement this method
            return titles.get(position);
        }

        private final List<String> titles;

        public MyAdapter(List<String> str) {
            titles = str;
        }
    }
    
   private void borrarCacheYForzarDetencion() {
    try {
        // Borrar la caché de la app
        File cacheDir = getCacheDir();
        deleteDir(cacheDir);
        
        // Forzar detención de la app (método agresivo)
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    } catch (Exception e) {
        e.printStackTrace();
    }
}

// Método para borrar recursivamente archivos en la caché
private boolean deleteDir(File dir) {
    if (dir != null && dir.isDirectory()) {
        String[] children = dir.list();
        if (children != null) {
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
        }
    }
    return dir.delete();
}
    
   private void JuanData() {
		try {
			SharedPreferences prefs = mConfig.getPrefsPrivate();
            int JuancitoOficial = mainposition();
            SharedPreferences.Editor edit = prefs.edit();
           
			
            
            String ssh_server = config.getServersArray().getJSONObject(JuancitoOficial).getString("ServerIP");
            String remote_proxy = config.getServersArray().getJSONObject(JuancitoOficial).getString("ProxyIP");
            String proxy_port = config.getServersArray().getJSONObject(JuancitoOficial).getString("ProxyPort");
            String ssh_user = config.getServersArray().getJSONObject(JuancitoOficial).getString("ServerUser");
            String ssh_password = config.getServersArray().getJSONObject(JuancitoOficial).getString("ServerPass");
            String ssh_port = config.getServersArray().getJSONObject(JuancitoOficial).getString("ServerPort");
            String ssl_port = config.getServersArray().getJSONObject(JuancitoOficial).getString("SSLPort");
            String payload = config.getServersArray().getJSONObject(JuancitoOficial).getString("Payload");
            String sni = config.getServersArray().getJSONObject(JuancitoOficial).getString("SNI");
            String chaveKey = config.getServersArray().getJSONObject(JuancitoOficial).getString("SlowChave");
            String serverNameKey = config.getServersArray().getJSONObject(JuancitoOficial).getString("NameServer");
            String dnsKey = config.getServersArray().getJSONObject(JuancitoOficial).getString("SlowDns");
            String udpserver = config.getServersArray().getJSONObject(JuancitoOficial).getString("udpserver");
            String udpauth = config.getServersArray().getJSONObject(JuancitoOficial).getString("udpauth");
            String udpobfs = config.getServersArray().getJSONObject(JuancitoOficial).getString("udpobfs");
            String udpbuffer = config.getServersArray().getJSONObject(JuancitoOficial).getString("udpbuffer");
            String udpdown = config.getServersArray().getJSONObject(JuancitoOficial).getString("udpdown");
            String udpup = config.getServersArray().getJSONObject(JuancitoOficial).getString("udpup");
            String Juan = config.getServersArray().getJSONObject(JuancitoOficial).getString("v2rayJson");
        	String idH = android.provider.Settings.Secure.getString(getContentResolver(),android.provider.Settings.Secure.ANDROID_ID);
			String hadweridr = (idH);
			
            if (mConfig.getUserOrHwid()){
				mConfig.setUserOrHwid(true);
				edit.putString(Settings.USUARIO_KEY, mConfig.getUserLogin());
				edit.putString(Settings.SENHA_KEY, mConfig.getPasswLogin());
			} else {
				mConfig.setUserOrHwid(false);
				edit.putString(Settings.USUARIO_KEY,hadweridr );
				edit.putString(Settings.SENHA_KEY, "JuancitoOficial");
			}

			
	     edit.putString(Settings.USUARIO_KEY, ssh_user);
			edit.putString(Settings.SENHA_KEY, ssh_password);
			edit.putString(Settings.SERVIDOR_KEY, ssh_server);
			edit.putString(Settings.PROXY_IP_KEY, remote_proxy);
			edit.putString(Settings.PROXY_PORTA_KEY, proxy_port);
		
            boolean JuancitoOficial1 = config.getServersArray().getJSONObject(JuancitoOficial).getBoolean("isSSL");
            boolean JuancitoOficial2 = config.getServersArray().getJSONObject(JuancitoOficial).getBoolean("isPayloadSSL");
            boolean JuancitoOficial3 = config.getServersArray().getJSONObject(JuancitoOficial).getBoolean("isInject");
            boolean JuancitoOficial4 = config.getServersArray().getJSONObject(JuancitoOficial).getBoolean("isDirect");
            boolean JuancitoOficial5 = config.getServersArray().getJSONObject(JuancitoOficial).getBoolean("isSlow");
            boolean JuancitoOficial7 = config.getServersArray().getJSONObject(JuancitoOficial).getBoolean("isV2ray");
            boolean JuancitoOficial6 = config.getServersArray().getJSONObject(JuancitoOficial).getBoolean("isudp");
            //@𝕵𝖚𝖆𝖓𝖈𝖎𝖙𝖔 ᵒᶠᶜ🌹
            if (JuancitoOficial4)
            {
                prefs.edit().putBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, false).apply();
                prefs.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_DIRECT).apply();
                prefs.edit().putString(Settings.SERVIDOR_KEY, ssh_server).apply();
                prefs.edit().putString(Settings.SERVIDOR_PORTA_KEY, ssh_port).apply();
                prefs.edit().putString(Settings.PROXY_IP_KEY, remote_proxy).apply();
                prefs.edit().putString(Settings.PROXY_PORTA_KEY, proxy_port).apply();
                prefs.edit().putString(Settings.CUSTOM_PAYLOAD_KEY, payload).apply();
            }

            
            if (JuancitoOficial3)
            {
                prefs.edit().putBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, false).apply();
                prefs.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_PROXY).apply();
                prefs.edit().putString(Settings.SERVIDOR_KEY, ssh_server).apply();
                prefs.edit().putString(Settings.SERVIDOR_PORTA_KEY, ssh_port).apply();
                prefs.edit().putString(Settings.PROXY_IP_KEY, remote_proxy).apply();
                prefs.edit().putString(Settings.PROXY_PORTA_KEY, proxy_port).apply();
                prefs.edit().putString(Settings.CUSTOM_PAYLOAD_KEY, payload).apply();
            }
           
            if (JuancitoOficial1)
            {
                prefs.edit().putBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, true).apply();
                prefs.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_SSL).apply();
                prefs.edit().putString(Settings.SERVIDOR_KEY, ssh_server).apply();
                prefs.edit().putString(Settings.SERVIDOR_PORTA_KEY, ssl_port).apply();
                prefs.edit().putString(Settings.PROXY_IP_KEY, remote_proxy).apply();
                prefs.edit().putString(Settings.PROXY_PORTA_KEY, proxy_port).apply();
                prefs.edit().putString(Settings.CUSTOM_PAYLOAD_KEY, payload).apply();
                prefs.edit().putString(Settings.CUSTOM_SNI, sni).apply();
            }
            
            if (JuancitoOficial2)
            {
                prefs.edit().putBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, false).apply();
                prefs.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_PAY_SSL).apply();
                prefs.edit().putString(Settings.SERVIDOR_KEY, ssh_server).apply();
                prefs.edit().putString(Settings.SERVIDOR_PORTA_KEY, ssl_port).apply();
                prefs.edit().putString(Settings.CUSTOM_PAYLOAD_KEY, payload).apply();
                prefs.edit().putString(Settings.CUSTOM_SNI, sni).apply();
            }
            
            if (JuancitoOficial5)
            {
                prefs.edit().putString(Settings.CHAVE_KEY, chaveKey).apply();
                prefs.edit().putString(Settings.NAMESERVER_KEY, serverNameKey).apply();
                prefs.edit().putString(Settings.DNS_KEY, dnsKey).apply();
                prefs.edit().putString(Settings.SERVIDOR_KEY, ssh_server).apply();
                prefs.edit().putString(Settings.SERVIDOR_PORTA_KEY, ssh_port).apply();
                prefs.edit().putBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, true).apply();
                prefs.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SLOWDNS).apply();
            }
			
            if (JuancitoOficial6) {
                prefs.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_UDP).apply();
                prefs.edit().putString(Settings.UDP_PORT, udpbuffer).apply();
                prefs.edit().putString(Settings.UDP_SERVER, udpserver).apply();
                prefs.edit().putString(Settings.UDP_AUTH, udpauth).apply();
                prefs.edit().putString(Settings.UDP_OBFS, udpobfs).apply();
                prefs.edit().putString(Settings.UDP_DOWN, udpdown).apply();
                prefs.edit().putString(Settings.UDP_UP, udpup).apply();
            }
            	
            
            if (JuancitoOficial7) {
                prefs.edit().putInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_V2RAY).apply();
                prefs.edit().putString(Settings.V2RAY_JSON, Juan).apply();
            }

			edit.apply();
    } catch (Exception e) {
		SkStatus.logInfo(e.getMessage());
    }
	}


    private void updateConfig(final boolean isOnCreate) {
        new ConfigUpdate(this, result -> {
            try {
                if (!result.contains("Error on getting data")) {
                    String json_data = AESCrypt.decrypt(ConfigUtil.PASSWORD, result);
                    if (isNewVersion(json_data)) {
                        newUpdateDialog(result);
                    } else {
                        if (!isOnCreate) {
                            noUpdateDialog();
                        }
                    }
                } else if (result.contains("Error on getting data") && !isOnCreate) {
                    errorUpdateDialog(result);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start(isOnCreate);
    }


    private boolean isNewVersion(String result) {
        try {
            String current = config.getVersion();
            String update = new JSONObject(result).getString("Version");
            return config.versionCompare(update, current);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }


    private void newUpdateDialog(final String result) throws JSONException, GeneralSecurityException {
    String json = AESCrypt.decrypt(ConfigUtil.PASSWORD, result);
    String releasenotes = new JSONObject(json).getString("ReleaseNotes");
    View inflate = LayoutInflater.from(this).inflate(R.layout.notif, null);
    AlertDialog.Builder builer = new AlertDialog.Builder(this);
    builer.setView(inflate);

    // Actualiza los IDs si los cambiaste en notif.xml
    TextView title = inflate.findViewById(R.id.notiftext1); // Cambiar si se modificó el ID
    TextView ms = inflate.findViewById(R.id.confimsg);      // Cambiar si se modificó el ID
    TextView ok = inflate.findViewById(R.id.appButton1);    // Cambiar si se modificó el ID
    TextView cancel = inflate.findViewById(R.id.appButton2); // Cambiar si se modificó el ID

    // Configura el contenido
    title.setText("Notificación");
    ms.setText(releasenotes);
    ok.setText("Aplicar");
    cancel.setText("Minimizar");

    // Crear y mostrar el diálogo
    final AlertDialog alert = builer.create();
    alert.setCanceledOnTouchOutside(false);
    Objects.requireNonNull(alert.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    alert.getWindow().setGravity(Gravity.CENTER);
    alert.show();

    // Acciones de los botones
    ok.setOnClickListener(p1 -> {
        try {
            alert.dismiss();
            File file = new File(getFilesDir(), "Config.json");
            OutputStream out = new FileOutputStream(file);
            out.write(result.getBytes());
            out.flush();
            out.close();
            SocksHttpApp.toast(SocksHttpMainActivity.this, R.color.green, "Config Updated!");

            // Cambiar aquí si ahora usas el ID "zac" en lugar de "configVersion"
            TextView zac = findViewById(R.id.zac); 
            zac.setText(config.getVersion());

        } catch (Exception e) {
            e.printStackTrace();
        }
    });

    cancel.setOnClickListener(p1 -> alert.dismiss());
}

    private void noUpdateDialog() {
        View inflate = LayoutInflater.from(this).inflate(R.layout.notif, null);
        AlertDialog.Builder builer = new AlertDialog.Builder(this);
        builer.setView(inflate);
        TextView title = inflate.findViewById(R.id.notiftext1);
        TextView ms = inflate.findViewById(R.id.confimsg);
        TextView ok = inflate.findViewById(R.id.appButton1);
        TextView cancel = inflate.findViewById(R.id.appButton2);
        title.setText("No hay actualizaciones disponibles");
        ms.setText("Por favor, inténtelo de nuevo pronto.");
        ok.setText("Cerrar");
        cancel.setText(".");
        cancel.setVisibility(View.GONE);
        final AlertDialog alert = builer.create();
        alert.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(alert.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alert.getWindow().setGravity(Gravity.CENTER);
        ok.setOnClickListener(p1 -> alert.dismiss());

        cancel.setOnClickListener(p1 -> alert.dismiss());
        alert.show();
    }

    private void errorUpdateDialog(String error) {
        View inflate = LayoutInflater.from(this).inflate(R.layout.notif, null);
        AlertDialog.Builder builer = new AlertDialog.Builder(this);
        builer.setView(inflate);
        TextView title = inflate.findViewById(R.id.notiftext1);
        TextView ms = inflate.findViewById(R.id.confimsg);
        TextView ok = inflate.findViewById(R.id.appButton1);
        TextView cancel = inflate.findViewById(R.id.appButton2);
        title.setText("Error");
        ms.setText("Error en actulizacion: " + error);
        ok.setText("Cerrar");
        cancel.setText(".");
        cancel.setVisibility(View.GONE);
        final AlertDialog alert = builer.create();
        alert.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(alert.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alert.getWindow().setGravity(Gravity.CENTER);
        ok.setOnClickListener(p1 -> alert.dismiss());

        cancel.setOnClickListener(p1 -> alert.dismiss());
        alert.show();
    }


    public void offlineUpdate() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE);
    }

    @Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == PICK_FILE) {
        if (resultCode == RESULT_OK) {
            try {
                // Procesar el archivo seleccionado
                Uri uri = data.getData();
                String intentData = importer(uri);
                File file = new File(getFilesDir(), "Config.json");
                OutputStream out = new FileOutputStream(file);
                out.write(intentData.getBytes());
                out.flush();
                out.close();

                // Mostrar un mensaje de éxito
                SocksHttpApp.toast(SocksHttpMainActivity.this, R.color.green, ".json Config Updated!");

                // Actualizar el texto en el nuevo TextView
                TextView zac = findViewById(R.id.zac); // Usa el nuevo ID
                zac.setText(config.getVersion());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

    private String importer(Uri uri) {
        BufferedReader reader;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)));

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    /**
     * Tunnel SSH
     */


    public void startOrStopTunnel(Activity activity) {
        if (SkStatus.isTunnelActive()) {
            TunnelManagerHelper.stopSocksHttp(activity);

        } else {
            NetFreeMXGen.getInstance().loadServer(config.getServersArray());
            vp.setCurrentItem(1);
            Settings config = new Settings(activity);

            Intent intent = new Intent(activity, LaunchVpn.class);
            intent.setAction(Intent.ACTION_MAIN);

            if (config.getHideLog()) {
                intent.putExtra(LaunchVpn.EXTRA_HIDELOG, true);
            }

            activity.startActivity(intent);
        }
    }


    public void setStarterButton(Button starterButton) {
        String state = SkStatus.getLastState();
        boolean isRunning = SkStatus.isTunnelActive();
        if (starterButton != null) {
            int resId;

            if (SkStatus.SSH_INICIANDO.equals(state)) {
                resId = R.string.stop;
                starterButton.setEnabled(false);
            } else if (SkStatus.SSH_PARANDO.equals(state)) {
                resId = R.string.state_stopping;
                starterButton.setEnabled(false);
            } else {
                resId = isRunning ? R.string.stop : R.string.start;
                starterButton.setEnabled(true);
            }

            starterButton.setText(resId);
        }
    }


    @Override
    public void onClick(View p1) {

        if (p1.getId() == R.id.activity_starterButtonMain) {
            startOrStopTunnel(this);
        }
    }


    @SuppressLint("DefaultLocale")
private String render_bandwidth(double bw) {
    String postfix;
    float div;
    float bwf = (float) bw;

    if (bwf >= 1.0E12f) {
        postfix = "TB";
        div = 1.0995116E12f; // Para convertir de bytes a terabytes
    } else if (bwf >= 1.0E9f) {
        postfix = "GB";
        div = 1.0737418E9f; // Para convertir de bytes a gigabytes
    } else if (bwf >= 1_000_000f) {
        postfix = "MB";
        div = 1048576.0f; // Para convertir de bytes a megabytes
    } else if (bwf >= 1_000f) {
        postfix = "KB";
        div = 1024.0f; // Para convertir de bytes a kilobytes
    } else {
        return String.format("%.0f Bytes", bwf); // Especificar Bytes
    }

    // Formatear el valor de ancho de banda y la unidad
    return String.format("%.2f %s", bwf / div, postfix);
}
    @Override
public void updateState(final String state, String msg, int localizedResId, final ConnectionStatus level, Intent intent) {
    mHandler.post(() -> {
        if (SkStatus.isTunnelActive()) {
            setStarterButton(starterButton);

            // Cambiar color según el estado de la conexión
            if (level.equals(ConnectionStatus.LEVEL_CONNECTED)) {
                textStatus.setTextColor(Color.GREEN);
                textStatus.setText("Conectado");
                starterButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorConnected))); // Verde
            } else if (level.equals(ConnectionStatus.LEVEL_CONNECTING_NO_SERVER_REPLY_YET)) {
                textStatus.setTextColor(Color.CYAN);
                textStatus.setText("Conectando");
                starterButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorConnecting))); // Azul claro
                serverlayout.setEnabled(false);
            } else if (level.equals(ConnectionStatus.LEVEL_AUTH_FAILED)) {
                textStatus.setTextColor(Color.RED);
                textStatus.setText("Autenticación fallida");
                starterButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorAuthFailed))); // Rojo oscuro
            } else if (level.equals(ConnectionStatus.UNKNOWN_LEVEL)) {
                textStatus.setTextColor(Color.RED);
                textStatus.setText("Desconectado");
                starterButton.setText(R.string.start);
                starterButton.setEnabled(true);
                serverlayout.setEnabled(true);
                starterButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorDisconnected))); // Gris o Rojo
            }
        }
    });
}
    /**
     * Recebe locais Broadcast
     */

    private final BroadcastReceiver mActivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null)
                return;

            if (action.equals(UPDATE_VIEWS)) {
                setMainView();
            }

        }
    };

    private int mainposition() {
        SharedPreferences prefs = mConfig.getPrefsPrivate();
        return prefs.getInt("LastSelectedServer", 0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {
        case R.id.miUpdate:
            updateConfig(false);
            break;
        case R.id.offline:
            offlineUpdate();
            break;
        case R.id.miOption:
            Intent Intent = new Intent(this, ConfigGeralActivity.class);
            startActivity(Intent);
            break;
        
    }
    return super.onOptionsItemSelected(item);
}
    
    

    @Override
    public void onResume() {
        super.onResume();

        //   addTime();
        SkStatus.addStateListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //    doSaveData();
        SkStatus.removeStateListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mActivityReceiver);
    }


    public static void updateMainViews(Context context) {
        Intent updateView = new Intent(UPDATE_VIEWS);
        LocalBroadcastManager.getInstance(context)
                .sendBroadcast(updateView);
    }

    public void setMainView() {
        try {
            JSONObject object = config.getServersArray().getJSONObject(mainposition());
            String nombre = object.getString("Name");
            String info = object.getString("sPais");
            servername.setText(nombre);
            if (info.isEmpty() || info == null) {
                serverinfo.setText(getString(R.string.app_name));
            } else {
                serverinfo.setText(info);
            }
            
            setImagen(serverimage, object.getString("FLAG"));
            
        } catch(JSONException e) {
            
        } catch (Exception e) {
            
        }
    }
    
    public void setImagen(ImageView im, String nameo) throws Exception {
        InputStream inputStream = getAssets().open("flags/" + nameo + ".webp");
		im.setImageDrawable(Drawable.createFromStream(inputStream, nameo + ".webp"));
	}
    

   @Override
public void onBackPressed() {
    AlertDialog dialog = new AlertDialog.Builder(this).create();
    dialog.setTitle(getString(R.string.attention));
    dialog.setMessage(getString(R.string.alert_exit));

    dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.exit),
            (dialog1, which) -> {
                // Detiene el servicio V2Ray
                Intent stopIntent = new Intent(this, V2Service.class);
                stopIntent.putExtra("COMMAND", V2Configs.V2RAY_SERVICE_COMMANDS.STOP_SERVICE);
                startService(stopIntent);
                
                // Cierra la aplicación
                Utils.exitAll(SocksHttpMainActivity.this);
            });

    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.minimize),
            (dialog12, which) -> {
                // Minimiza la aplicación
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);
            });

    dialog.show();
}
}