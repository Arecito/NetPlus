package https.socks.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.Settings;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class IdValidator {

    private static final String PREFS_NAME = "AppPrefs";
    private static final String PREFS_KEY_IDS = "valid_ids";
    private static final String PREFS_KEY_VERSION = "ids_version";
    private Context context;
    private ValidationCallback callback;

    public IdValidator(Context context, ValidationCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    public void validateAndroidId() {
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        new CheckVersionAndUpdateIdsTask(androidId).execute();
    }

    private class CheckVersionAndUpdateIdsTask extends AsyncTask<Void, Void, Boolean> {

        private final String currentAndroidId;
        private final SharedPreferences sharedPreferences;

        public CheckVersionAndUpdateIdsTask(String androidId) {
            this.currentAndroidId = androidId;
            this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Set<String> validIds = new HashSet<>();
            String newVersion = null;

            try {
                // URL del archivo que contiene la versión y los IDs
                URL url = new URL("https://www.dropbox.com/scl/fi/d0jvtu9utlfa70kqs6eyg/version_and_ids.txt?rlkey=58zsla2rtswl4l3h93i9u3zly&dl=1");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                // Comprobar la respuesta del servidor
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return false;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;

                // Leer el archivo y extraer datos
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("VERSION=")) {
                        newVersion = line.split("=")[1].trim();
                    } else {
                        validIds.add(line.trim());
                    }
                }
                reader.close();

                // Obtener la versión almacenada
                String storedVersion = sharedPreferences.getString(PREFS_KEY_VERSION, "");

                // Comparar y actualizar si la versión ha cambiado
                if (!newVersion.equals(storedVersion)) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putStringSet(PREFS_KEY_IDS, validIds);
                    editor.putString(PREFS_KEY_VERSION, newVersion);
                    editor.apply();
                } else {
                    // Si la versión no ha cambiado, cargar los IDs almacenados
                    validIds = sharedPreferences.getStringSet(PREFS_KEY_IDS, new HashSet<>());
                }

                return validIds.contains(currentAndroidId);

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean isValid) {
            callback.onValidationComplete(isValid);
        }
    }

    public interface ValidationCallback {
        void onValidationComplete(boolean isValid);
    }
}