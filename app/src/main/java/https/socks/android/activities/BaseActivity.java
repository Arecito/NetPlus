package https.socks.android.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.slipkprojects.ultrasshservice.config.Settings;
import com.netplus.vpn.R;
import https.socks.android.preference.LocaleHelper;
import https.socks.android.util.AESCrypt;
import https.socks.android.util.Utils;

import static android.content.pm.PackageManager.GET_META_DATA;
import org.json.*;
import java.io.*;
import android.widget.*;
import java.net.*;
import android.content.Intent;
import android.view.View;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.net.Uri;
import https.socks.android.SocksHttpApp;
import https.socks.android.ExceptionHandler;

/**
 * Created by Pankaj on 03-11-2017.
 */
public abstract class BaseActivity extends AppCompatActivity
{
	public static int mTheme = 0;
    public static final String PASSWORD = "PudayNaMalake@Pinasok2022";
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
	}
	
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(LocaleHelper.setLocale(base));
	}
	
	public static void antiremod(final Context c){
        if (!(((String) c.getPackageManager().getApplicationLabel(c.getApplicationInfo())).replace(" ", "_").toUpperCase().equals("NETPLUS_VPN") && c.getPackageName().equals("com.netplus.vpn"))) {
			View inflate = LayoutInflater.from(c).inflate(R.layout.notif, null);
			AlertDialog.Builder builer = new AlertDialog.Builder(c); 
			builer.setView(inflate); 
			TextView title = inflate.findViewById(R.id.notiftext1);
			TextView ms = inflate.findViewById(R.id.confimsg);
			TextView ok = inflate.findViewById(R.id.appButton1);
			TextView cancel = inflate.findViewById(R.id.appButton2);
			title.setText("Problem while opening the app");
			ms.setText("You are trying to remodify this application!\nPlease uninstall this app and then install the original one.\n");
			ok.setText("Exit");
			cancel.setText(".");
			cancel.setVisibility(View.GONE);
			final AlertDialog alert = builer.create(); 
			alert.setCanceledOnTouchOutside(false);
			alert.setCancelable(false);
			alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			alert.getWindow().setGravity(Gravity.CENTER); 
			ok.setOnClickListener(new View.OnClickListener() { 
					@Override
					public void onClick(View p1){
						System.exit(0);
					}
				});

			
			alert.show();
		}
    }
    
    public String getSign() {
        StringBuilder str = new StringBuilder();
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), getPackageManager().GET_SIGNATURES);
            for (Signature signs: pi.signatures) {
                str.append(signs.toCharsString());
            }
        } catch (Exception e) {}
        return str.toString();
    }
	
	protected void writeMyFile(JSONObject obj) {
		try {
			String encoded_name = 
				String.format("%s", URLEncoder.encode("Config.json", "UTF-8")
							  );

			File dir = new File(getFilesDir(), encoded_name);
			OutputStream out = new FileOutputStream(dir);
			String value = AESCrypt.encrypt(PASSWORD, obj.toString(2));
			out.write(value.getBytes());
			out.flush();

			if (out != null)
				out.close();

		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
	
    protected JSONObject getJSONConfig2(Context context) throws Exception {
		String json = null;
        File file = new File(context.getFilesDir(), "Config.json");
		if (file.exists()) {
			String json_file = Utils.readStream(new FileInputStream(file));
			json = AESCrypt.decrypt(PASSWORD, json_file);
			// return new JSONObject(json);
		} else {
			InputStream inputStream = context.getAssets().open("config/config.json");
			json = AESCrypt.decrypt(PASSWORD, Utils.readStream(inputStream));
			// return new JSONObject(json);
		}
        return new JSONObject(json);
    }
	
	protected void resetTitles() {
		try {
			ActivityInfo info = getPackageManager().getActivityInfo(getComponentName(), GET_META_DATA);
			if (info.labelRes != 0) {
				setTitle(info.labelRes);
			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
	}
}