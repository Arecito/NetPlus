package https.socks.android.preference;

import androidx.preference.PreferenceFragmentCompat;
import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.preference.Preference;
import androidx.fragment.app.DialogFragment;
import androidx.preference.EditTextPreference;
import android.widget.Toast;
import android.view.View.OnClickListener;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import androidx.preference.CheckBoxPreference;
import android.content.Intent;
import https.socks.android.SocksHttpApp;
import com.netplus.vpn.R;
import androidx.preference.ListPreference;
import android.content.Context;
import androidx.appcompat.app.AppCompatDelegate;
import com.slipkprojects.ultrasshservice.logger.SkStatus;
import com.slipkprojects.ultrasshservice.config.SettingsConstants;
import com.slipkprojects.ultrasshservice.config.Settings;
import androidx.preference.PreferenceScreen;
import com.slipkprojects.ultrasshservice.logger.ConnectionStatus;
import android.os.Handler;
import android.app.Activity;
import https.socks.android.LauncherActivity;
import android.app.PendingIntent;
import android.app.AlarmManager;
import android.os.Build;
import androidx.appcompat.app.AlertDialog;
import https.socks.android.SocksHttpMainActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class SettingsPreference extends PreferenceFragmentCompat
	implements Preference.OnPreferenceChangeListener, SettingsConstants,
		SkStatus.StateListener
{
	private Handler mHandler;
	private SharedPreferences mPref;
	
	public static final String
		SSHSERVER_PREFERENCE_KEY = "screenSSHSettings";
		//ADVANCED_SCREEN_PREFERENCE_KEY = "screenAdvancedSettings";
		
	private String[] settings_disabled_keys = {
		DNSFORWARD_KEY,
		DNSRESOLVER_KEY1,
		DNSRESOLVER_KEY2,
		UDPFORWARD_KEY,
        USER_LOGIN,
        PASSW_LOGIN,
        USER_HWID,
		UDPRESOLVER_KEY,
		data_compression_key,
		PINGER_KEY,
		AUTO_CLEAR_LOGS_KEY,
		HIDE_LOG_KEY,
		//MODO_NOTURNO_KEY,
		
		IDIOMA_KEY
	};

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		mHandler = new Handler();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		
		SkStatus.addStateListener(this);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		
		SkStatus.removeStateListener(this);
	}
	
	
	@Override
    public void onCreatePreferences(Bundle bundle, String root_key)
	{
        // Load the Preferences from the XML file
        setPreferencesFromResource(R.xml.app_preferences, root_key);
		
		mPref = getPreferenceManager().getDefaultSharedPreferences(getContext());
		
		Preference udpForwardPreference = (CheckBoxPreference)
			findPreference(UDPFORWARD_KEY);
		udpForwardPreference.setOnPreferenceChangeListener(this);
		
		Preference dnsForwardPreference = (CheckBoxPreference)
			findPreference(DNSFORWARD_KEY);
		dnsForwardPreference.setOnPreferenceChangeListener(this);
		
		/*ListPreference modoNoturno = (ListPreference)
			findPreference(MODO_NOTURNO_KEY);
		modoNoturno.setOnPreferenceChangeListener(this);
		SettingsAdvancedPreference.setListPreferenceSummary(modoNoturno, modoNoturno.getValue());
		*/
		ListPreference idioma = (ListPreference)
			findPreference(IDIOMA_KEY);
		idioma.setOnPreferenceChangeListener(this);
		SettingsAdvancedPreference.setListPreferenceSummary(idioma, idioma.getValue());
		
		// update view
		setRunningTunnel(SkStatus.isTunnelActive());
	}
	
	private void onChangeUseVpn(boolean use_vpn){
		Preference udpResolverPreference = (EditTextPreference)
			findPreference(UDPRESOLVER_KEY);
		Preference dnsResolverPreference = (EditTextPreference)
			findPreference(DNSRESOLVER_KEY1);
		Preference dnsResolverPreference2 = (EditTextPreference)
				findPreference(DNSRESOLVER_KEY2);
		
		for (String key : settings_disabled_keys){
			getPreferenceManager().findPreference(key)
				.setEnabled(use_vpn);
		}

		use_vpn = true;
		if (use_vpn) {
			boolean isUdpForward = mPref.getBoolean(UDPFORWARD_KEY, false);
			boolean isDnsForward = mPref.getBoolean(DNSFORWARD_KEY, false);
			
			udpResolverPreference.setEnabled(isUdpForward);
			dnsResolverPreference.setEnabled(isDnsForward);
			dnsResolverPreference2.setEnabled(isDnsForward);
		}
		else {
			String[] list = {
				UDPFORWARD_KEY,
				UDPRESOLVER_KEY,
				DNSFORWARD_KEY,
				DNSRESOLVER_KEY1,
				DNSRESOLVER_KEY2
			};
			for (String key : list) {
				getPreferenceManager().findPreference(key)
					.setEnabled(false);
			}
		}
	}
	
	private void setRunningTunnel(boolean isRunning) {
		if (isRunning) {
			for (String key : settings_disabled_keys){
				getPreferenceManager().findPreference(key)
					.setEnabled(false);
			}
		}
		else {
			onChangeUseVpn(true);
		}
	}

	
	/**
	* Preference.OnPreferenceChangeListener
	* Implementação
	*/
	
	@Override
	public boolean onPreferenceChange(Preference pref, Object newValue)
	{
				switch (pref.getKey()) {
			case UDPFORWARD_KEY:
				boolean isUdpForward = (boolean) newValue;

				Preference udpResolverPreference = (EditTextPreference)
					findPreference(UDPRESOLVER_KEY);
				if (udpResolverPreference != null) {
					udpResolverPreference.setEnabled(isUdpForward);
				} else {
					android.util.Log.e("SettingsPreference", "Error: No se encontró la preferencia con clave " + UDPRESOLVER_KEY);
				}
			break;

			case DNSFORWARD_KEY:
				boolean isDnsForward = (boolean) newValue;

				Preference dnsResolverPreference1 = (EditTextPreference)
					findPreference("dnsResolver1"); // <--- Clave correcta para el DNS Primario
				Preference dnsResolverPreference2 = (EditTextPreference)
					findPreference("dnsResolver2"); // <--- Clave correcta para el DNS Secundario

				if (dnsResolverPreference1 != null) {
					dnsResolverPreference1.setEnabled(isDnsForward);
				} else {
					android.util.Log.e("SettingsPreference", "Error: No se encontró la preferencia con clave dnsResolver1");
				}

				if (dnsResolverPreference2 != null) {
					dnsResolverPreference2.setEnabled(isDnsForward);
				} else {
					android.util.Log.e("SettingsPreference", "Error: No se encontró la preferencia con clave dnsResolver2");
				}
			break;
			
			/*case MODO_NOTURNO_KEY:
				final String enableModoNoturno = (String)newValue;
				
				if (enableModoNoturno.equals(mPref.getString(MODO_NOTURNO_KEY, "off"))) {
					return false;
				}

				/*SettingsAdvancedPreference.setListPreferenceSummary(pref_list, (String) newValue);
				
				new AlertDialog.Builder(getContext())
					. setTitle(R.string.attention)
					. setMessage(R.string.restarting_app_theme)
					. setCancelable(false)
					. show();
				
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						Context context = SocksHttpApp.getApp();
						
						new Settings(context)
							.setModoNoturno(enableModoNoturno);
						
						// reinicia app
						Intent startActivity = new Intent(context, LauncherActivity.class);
						int pendingIntentId = 123456;
						PendingIntent pendingIntent = PendingIntent.getActivity(context, pendingIntentId, startActivity, PendingIntent.FLAG_CANCEL_CURRENT);

						AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
						mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 500, pendingIntent);

						// encerra tudo
						/*if (Build.VERSION.SDK_INT >= 16) {
							Activity act = getActivity();
							if (act != null)
								act.finishAffinity();
						}
						
						System.exit(0);
					}
				}, 300);
				
				getActivity().finish();
			return false;*/
			
			case IDIOMA_KEY:
				final String lang = (String) newValue;
				
				if (((String)newValue).equals(new Settings(getContext())
						.getIdioma())) {
					return false;
				}
				
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						Context context = SocksHttpApp.getApp();
						
						LocaleHelper.setNewLocale(context, lang);
						
						// reinicia app
						Intent startActivity = new Intent(context, LauncherActivity.class);
						int pendingIntentId = 123456;
						PendingIntent pendingIntent = PendingIntent.getActivity(context, pendingIntentId, startActivity, PendingIntent.FLAG_CANCEL_CURRENT);

						AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
						mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 500, pendingIntent);

						// encerra tudo
						/*if (Build.VERSION.SDK_INT >= 16) {
							getActivity().finishAffinity();
						}*/

						System.exit(0);
					}
				}, 300);
				
                getActivity().finish();
            return false;
		}
		return true;
	}

	@Override
	public void updateState(String state, String logMessage, int localizedResId, ConnectionStatus level, Intent intent)
	{
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				setRunningTunnel(SkStatus.isTunnelActive());
			}
		});
	}
	
	
}