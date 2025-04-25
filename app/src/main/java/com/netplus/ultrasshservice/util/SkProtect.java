package com.socksfast.ultrasshservice.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import android.util.Log;
import android.widget.Toast;
import com.netplus.vpn.BuildConfig;
import com.netplus.vpn.R;
/**
 * @author Skank3r
 */
public class SkProtect {

	//private static final String TAG = SkProtect.class.getSimpleName();
    
    private static final String TAG = (new Object() {
   int MDevz;
   public String toString() {
      byte[] buf = new byte[16];
      MDevz = 1967362708;
      buf[0] = (byte) (MDevz >>> 6);
      MDevz = 1614867443;
      buf[1] = (byte) (MDevz >>> 9);
      MDevz = 1708163778;
      buf[2] = (byte) (MDevz >>> 1);
      MDevz = -1916662339;
      buf[3] = (byte) (MDevz >>> 21);
      MDevz = -32480336;
      buf[4] = (byte) (MDevz >>> 8);
      MDevz = 1220768584;
      buf[5] = (byte) (MDevz >>> 3);
      MDevz = -683312828;
      buf[6] = (byte) (MDevz >>> 20);
      MDevz = 1099799508;
      buf[7] = (byte) (MDevz >>> 6);
      MDevz = 1587003506;
      buf[8] = (byte) (MDevz >>> 14);
      MDevz = 111135900;
      buf[9] = (byte) (MDevz >>> 17);
      MDevz = -827868774;
      buf[10] = (byte) (MDevz >>> 2);
      MDevz = -1717265596;
      buf[11] = (byte) (MDevz >>> 18);
      MDevz = 929248368;
      buf[12] = (byte) (MDevz >>> 16);
      MDevz = -1154172215;
      buf[13] = (byte) (MDevz >>> 15);
      MDevz = 560044123;
      buf[14] = (byte) (MDevz >>> 16);
      MDevz = -1151024515;
      buf[15] = (byte) (MDevz >>> 19);
      return new String(buf);
   }
}.toString());
	
	private static final String APP_BASE = (new Object() {
   int MDevz;
   public String toString() {
      byte[] buf = new byte[15];
      MDevz = -216103624;
      buf[0] = (byte) (MDevz >>> 19);
      MDevz = 317026155;
      buf[1] = (byte) (MDevz >>> 8);
      MDevz = 504211099;
      buf[2] = (byte) (MDevz >>> 13);
      MDevz = 807142112;
      buf[3] = (byte) (MDevz >>> 4);
      MDevz = 1534360984;
      buf[4] = (byte) (MDevz >>> 19);
      MDevz = 726426882;
      buf[5] = (byte) (MDevz >>> 8);
      MDevz = -227653823;
      buf[6] = (byte) (MDevz >>> 4);
      MDevz = 1895118367;
      buf[7] = (byte) (MDevz >>> 24);
      MDevz = -1553620824;
      buf[8] = (byte) (MDevz >>> 19);
      MDevz = 1197181115;
      buf[9] = (byte) (MDevz >>> 20);
      MDevz = 359906749;
      buf[10] = (byte) (MDevz >>> 16);
      MDevz = 1057274452;
      buf[11] = (byte) (MDevz >>> 10);
      MDevz = -341846804;
      buf[12] = (byte) (MDevz >>> 1);
      MDevz = 935444646;
      buf[13] = (byte) (MDevz >>> 10);
      MDevz = 183821755;
      buf[14] = (byte) (MDevz >>> 2);
      return new String(buf);
   }
}.toString());


	private static final String x = (new Object() {
   int MDevz;
   public String toString() {
      byte[] buf = new byte[11];
      MDevz = 790334895;
      buf[0] = (byte) (MDevz >>> 14);
      MDevz = -1042625833;
      buf[1] = (byte) (MDevz >>> 9);
      MDevz = 330986330;
      buf[2] = (byte) (MDevz >>> 15);
      MDevz = 1100743901;
      buf[3] = (byte) (MDevz >>> 14);
      MDevz = 1038533457;
      buf[4] = (byte) (MDevz >>> 12);
      MDevz = -1035297418;
      buf[5] = (byte) (MDevz >>> 6);
      MDevz = 638934258;
      buf[6] = (byte) (MDevz >>> 6);
      MDevz = -2110189311;
      buf[7] = (byte) (MDevz >>> 3);
      MDevz = 710818628;
      buf[8] = (byte) (MDevz >>> 7);
      MDevz = -212059009;
      buf[9] = (byte) (MDevz >>> 14);
      MDevz = -1378607557;
      buf[10] = (byte) (MDevz >>> 21);
      return new String(buf);
   }
}.toString());
	// Assinatura da Google Play
	//private static final String APP_SIGNATURE = "XbhYZ4Bz/9F4cWLIDMg0wl/+jl8=\n";

	private static SkProtect mInstance;

	private Context mContext;
	
	public static void init(Context context) {
		if (mInstance == null) {
			mInstance = new SkProtect(context);

			// This method will print your certificate signature to the logcat.
			//AndroidTamperingProtectionUtils.getCertificateSignature(context);
		}
	}

	private SkProtect(Context context) {
		mContext = context;
	}
	
	/*public void tamperProtect() {
		AndroidTamperingProtection androidTamperingProtection = new AndroidTamperingProtection.Builder(mContext, APP_SIGNATURE)
			.installOnlyFromPlayStore(false) // By default is set to false.
			.build();

		if (!androidTamperingProtection.validate()) {
			throw new RuntimeException();
		}
	}*/
	
	public void simpleProtect() {
		if (!APP_BASE.equals(mContext.getPackageName().toLowerCase()) ||
				!mContext.getString(R.string.app_name).toLowerCase().equals(x)) {
			throw new RuntimeException();
		}
	}

	public static void CharlieProtect() {
		if (mInstance == null) return;
			
		mInstance.simpleProtect();
		
		// ative apenas ao enviar pra PlayStore
		//mInstance.tamperProtect();
	}
}