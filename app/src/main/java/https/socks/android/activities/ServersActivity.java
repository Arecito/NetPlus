package https.socks.android.activities;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netplus.vpn.R;
import com.slipkprojects.ultrasshservice.config.Settings;
import https.socks.android.SocksHttpMainActivity;
import https.socks.android.adapter.ServersAdapter;
import https.socks.android.model.ServerModel;
import https.socks.android.util.ConfigUtil;
import com.slipkprojects.ultrasshservice.logger.SkStatus;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class ServersActivity extends AppCompatActivity {
    private List<ServerModel> servidores;
    private RecyclerView recycler;
    private ServersAdapter adaptador;
    private ConfigUtil config;
    private Settings mConfig;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servers);
        Toolbar mToolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true); 
        servidores = new ArrayList<>();
        cargarservers();
        config = new ConfigUtil(this);
        mConfig = new Settings(this);
        recycler = findViewById(R.id.recycler_servers);
        adaptador = new ServersAdapter(servidores);
        adaptador.setOnItemClick(new ServersAdapter.onItemClickListener() {
            @Override
            public void onItemClick(int posicion) {
                loadServerData(posicion);
            }
        });
        
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adaptador);
    }
    
    private void cargarservers() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < config.getServersArray().length(); i++) {
                        JSONObject servers = config.getServersArray().getJSONObject(i);
                        String name = servers.getString("Name");
                        String info = servers.getString("sPais");
                        String flag = servers.getString("FLAG");
                        ServerModel modelo = new ServerModel();
                        modelo.setServerName(name);
                        modelo.setServerFlag(flag);
                        modelo.setServerPosicion(i);
                        if (info.isEmpty() || info == null) {
                            modelo.setServerInfo(getString(R.string.app_name));
                        } else {
                            modelo.setServerInfo(info);
                        }
                        servidores.add(modelo);
                        ServersActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adaptador.notifyDataSetChanged();
                            }
                        });
                    }
                } catch (JSONException e) {
                    ServersActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ServersActivity.this, "JSON Error Severs Activity: " + e.getMessage(), 1).show();
                        }
                    });    
                    
                }
            }
        }).start();
    }
    
    private void loadServerData(int JuancitoOficial) {
		try {
			SharedPreferences prefs = mConfig.getPrefsPrivate();
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
            String chaveKey = config.getServersArray().getJSONObject(JuancitoOficial).getString("Slowchave");
            String serverNameKey = config.getServersArray().getJSONObject(JuancitoOficial).getString("Nameserver");
            String dnsKey = config.getServersArray().getJSONObject(JuancitoOficial).getString("Slowdns");
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
     saveSpinner(JuancitoOficial);
            SocksHttpMainActivity.updateMainViews(getApplicationContext());
      finish();      

    } catch (Exception e) {
		SkStatus.logInfo(e.getMessage());
    }
	}
    
    private void saveSpinner(int position){
		SharedPreferences prefs = mConfig.getPrefsPrivate();
		SharedPreferences.Editor edit = prefs.edit();
		edit.putInt("LastSelectedServer", position);
		edit.apply();
	}
}
