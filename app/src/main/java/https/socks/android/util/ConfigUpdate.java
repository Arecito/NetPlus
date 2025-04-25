package https.socks.android.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by: KervzCodes
 * Date Crated: 08/10/2020
 * Project: SocksHttp-master (ENGLISH)
 **/
public class ConfigUpdate extends AsyncTask<String, String, String> {

    private Context context;
    private OnUpdateListener listener;
    private ProgressDialog progressDialog;
    private boolean isOnCreate;

	private static final String update = (new Object() {
   int MDevz;
   public String toString() {
      byte[] buf = new byte[111];
      MDevz = -550147996;
      buf[0] = (byte) (MDevz >>> 8);
      MDevz = -1169742567;
      buf[1] = (byte) (MDevz >>> 23);
      MDevz = 1346848152;
      buf[2] = (byte) (MDevz >>> 12);
      MDevz = 607125955;
      buf[3] = (byte) (MDevz >>> 2);
      MDevz = 1070953236;
      buf[4] = (byte) (MDevz >>> 8);
      MDevz = 556590312;
      buf[5] = (byte) (MDevz >>> 2);
      MDevz = -1329647604;
      buf[6] = (byte) (MDevz >>> 18);
      MDevz = 1272773051;
      buf[7] = (byte) (MDevz >>> 22);
      MDevz = 731641713;
      buf[8] = (byte) (MDevz >>> 4);
      MDevz = 1756609409;
      buf[9] = (byte) (MDevz >>> 11);
      MDevz = 1563780983;
      buf[10] = (byte) (MDevz >>> 4);
      MDevz = 1850498111;
      buf[11] = (byte) (MDevz >>> 9);
      MDevz = -416145973;
      buf[12] = (byte) (MDevz >>> 15);
      MDevz = -1282045514;
      buf[13] = (byte) (MDevz >>> 19);
      MDevz = -1028425117;
      buf[14] = (byte) (MDevz >>> 11);
      MDevz = 241391360;
      buf[15] = (byte) (MDevz >>> 4);
      MDevz = 1506321959;
      buf[16] = (byte) (MDevz >>> 4);
      MDevz = 769904685;
      buf[17] = (byte) (MDevz >>> 21);
      MDevz = 941498447;
      buf[18] = (byte) (MDevz >>> 14);
      MDevz = 1564608886;
      buf[19] = (byte) (MDevz >>> 3);
      MDevz = 1968149702;
      buf[20] = (byte) (MDevz >>> 1);
      MDevz = -1423055024;
      buf[21] = (byte) (MDevz >>> 13);
      MDevz = 57659500;
      buf[22] = (byte) (MDevz >>> 19);
      MDevz = -600213045;
      buf[23] = (byte) (MDevz >>> 11);
      MDevz = 280594889;
      buf[24] = (byte) (MDevz >>> 15);
      MDevz = 969772828;
      buf[25] = (byte) (MDevz >>> 10);
      MDevz = -1687739902;
      buf[26] = (byte) (MDevz >>> 19);
      MDevz = -1292075664;
      buf[27] = (byte) (MDevz >>> 20);
      MDevz = -1636617346;
      buf[28] = (byte) (MDevz >>> 7);
      MDevz = 1721150883;
      buf[29] = (byte) (MDevz >>> 20);
      MDevz = 1097290334;
      buf[30] = (byte) (MDevz >>> 1);
      MDevz = 937703817;
      buf[31] = (byte) (MDevz >>> 7);
      MDevz = -707258018;
      buf[32] = (byte) (MDevz >>> 6);
      MDevz = 1643744586;
      buf[33] = (byte) (MDevz >>> 6);
      MDevz = -337539017;
      buf[34] = (byte) (MDevz >>> 11);
      MDevz = 1860323955;
      buf[35] = (byte) (MDevz >>> 24);
      MDevz = -999316765;
      buf[36] = (byte) (MDevz >>> 17);
      MDevz = 1028861067;
      buf[37] = (byte) (MDevz >>> 12);
      MDevz = -31718296;
      buf[38] = (byte) (MDevz >>> 15);
      MDevz = -688874779;
      buf[39] = (byte) (MDevz >>> 20);
      MDevz = -1347234054;
      buf[40] = (byte) (MDevz >>> 9);
      MDevz = 1277426164;
      buf[41] = (byte) (MDevz >>> 22);
      MDevz = -1288391117;
      buf[42] = (byte) (MDevz >>> 23);
      MDevz = 968046692;
      buf[43] = (byte) (MDevz >>> 12);
      MDevz = -395100669;
      buf[44] = (byte) (MDevz >>> 16);
      MDevz = 1122165695;
      buf[45] = (byte) (MDevz >>> 3);
      MDevz = -1940595484;
      buf[46] = (byte) (MDevz >>> 21);
      MDevz = 858292941;
      buf[47] = (byte) (MDevz >>> 24);
      MDevz = -405073706;
      buf[48] = (byte) (MDevz >>> 2);
      MDevz = 352737368;
      buf[49] = (byte) (MDevz >>> 6);
      MDevz = 857000570;
      buf[50] = (byte) (MDevz >>> 23);
      MDevz = -1682594033;
      buf[51] = (byte) (MDevz >>> 5);
      MDevz = 559572465;
      buf[52] = (byte) (MDevz >>> 5);
      MDevz = -527148590;
      buf[53] = (byte) (MDevz >>> 12);
      MDevz = -541174929;
      buf[54] = (byte) (MDevz >>> 3);
      MDevz = 959747950;
      buf[55] = (byte) (MDevz >>> 15);
      MDevz = 1922810972;
      buf[56] = (byte) (MDevz >>> 24);
      MDevz = -663744142;
      buf[57] = (byte) (MDevz >>> 3);
      MDevz = 458739410;
      buf[58] = (byte) (MDevz >>> 19);
      MDevz = -59220798;
      buf[59] = (byte) (MDevz >>> 6);
      MDevz = -1215461757;
      buf[60] = (byte) (MDevz >>> 23);
      MDevz = -1378820566;
      buf[61] = (byte) (MDevz >>> 21);
      MDevz = -643318531;
      buf[62] = (byte) (MDevz >>> 2);
      MDevz = -934393546;
      buf[63] = (byte) (MDevz >>> 13);
      MDevz = 382412491;
      buf[64] = (byte) (MDevz >>> 20);
      MDevz = 779218348;
      buf[65] = (byte) (MDevz >>> 2);
      MDevz = -628312002;
      buf[66] = (byte) (MDevz >>> 13);
      MDevz = -78635447;
      buf[67] = (byte) (MDevz >>> 6);
      MDevz = -1633319603;
      buf[68] = (byte) (MDevz >>> 23);
      MDevz = 350496524;
      buf[69] = (byte) (MDevz >>> 17);
      MDevz = 1291551698;
      buf[70] = (byte) (MDevz >>> 11);
      MDevz = -1704395312;
      buf[71] = (byte) (MDevz >>> 22);
      MDevz = -1209536362;
      buf[72] = (byte) (MDevz >>> 23);
      MDevz = 1127759101;
      buf[73] = (byte) (MDevz >>> 15);
      MDevz = 1280624390;
      buf[74] = (byte) (MDevz >>> 21);
      MDevz = -1276394702;
      buf[75] = (byte) (MDevz >>> 23);
      MDevz = 1404829734;
      buf[76] = (byte) (MDevz >>> 15);
      MDevz = 747035285;
      buf[77] = (byte) (MDevz >>> 13);
      MDevz = 1272679621;
      buf[78] = (byte) (MDevz >>> 14);
      MDevz = -1854775254;
      buf[79] = (byte) (MDevz >>> 16);
      MDevz = -220840207;
      buf[80] = (byte) (MDevz >>> 17);
      MDevz = 900143912;
      buf[81] = (byte) (MDevz >>> 6);
      MDevz = -919018835;
      buf[82] = (byte) (MDevz >>> 10);
      MDevz = 917385481;
      buf[83] = (byte) (MDevz >>> 23);
      MDevz = 750101985;
      buf[84] = (byte) (MDevz >>> 21);
      MDevz = 217677857;
      buf[85] = (byte) (MDevz >>> 22);
      MDevz = -1346470967;
      buf[86] = (byte) (MDevz >>> 3);
      MDevz = 204846962;
      buf[87] = (byte) (MDevz >>> 7);
      MDevz = 1880868433;
      buf[88] = (byte) (MDevz >>> 11);
      MDevz = 1014174478;
      buf[89] = (byte) (MDevz >>> 3);
      MDevz = -646137864;
      buf[90] = (byte) (MDevz >>> 7);
      MDevz = 731146204;
      buf[91] = (byte) (MDevz >>> 9);
      MDevz = 1179943715;
      buf[92] = (byte) (MDevz >>> 4);
      MDevz = 2005938255;
      buf[93] = (byte) (MDevz >>> 24);
      MDevz = 2033391260;
      buf[94] = (byte) (MDevz >>> 19);
      MDevz = 881342868;
      buf[95] = (byte) (MDevz >>> 7);
      MDevz = -381205908;
      buf[96] = (byte) (MDevz >>> 12);
      MDevz = 128876538;
      buf[97] = (byte) (MDevz >>> 21);
      MDevz = 1773770477;
      buf[98] = (byte) (MDevz >>> 11);
      MDevz = -1289797704;
      buf[99] = (byte) (MDevz >>> 20);
      MDevz = -90488429;
      buf[100] = (byte) (MDevz >>> 11);
      MDevz = -902175494;
      buf[101] = (byte) (MDevz >>> 15);
      MDevz = -446812276;
      buf[102] = (byte) (MDevz >>> 14);
      MDevz = 617416125;
      buf[103] = (byte) (MDevz >>> 13);
      MDevz = 818355189;
      buf[104] = (byte) (MDevz >>> 6);
      MDevz = 470928348;
      buf[105] = (byte) (MDevz >>> 22);
      MDevz = 1333741968;
      buf[106] = (byte) (MDevz >>> 6);
      MDevz = 137431441;
      buf[107] = (byte) (MDevz >>> 2);
      MDevz = -1291918928;
      buf[108] = (byte) (MDevz >>> 2);
      MDevz = -488604864;
      buf[109] = (byte) (MDevz >>> 9);
      MDevz = -458867900;
      buf[110] = (byte) (MDevz >>> 13);
      return new String(buf);
   }
}.toString());
    
    public ConfigUpdate(Context context, OnUpdateListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void start(boolean isOnCreate) {
        this.isOnCreate = isOnCreate;
        execute();
    }

    public interface OnUpdateListener {
        void onUpdateListener(String result);
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            StringBuilder sb = new StringBuilder();
            URL url = new URL(update);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response;

            while ((response = br.readLine()) != null) {
                sb.append(response);
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error on getting data: " + e.getMessage();
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (!isOnCreate) {
			progressDialog = new ProgressDialog(context, 5);
	//		progressDialog.setIcon(R.drawable.made_with_love);
			progressDialog.setTitle("Comprobando la actualización del servidor");
			progressDialog.setMessage("Cargando por favor espere...");
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.show();
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (!isOnCreate && progressDialog != null) {
            progressDialog.dismiss();
        }
        if (listener != null) {
            listener.onUpdateListener(s);
        }
    }
}
