package com.slipkprojects.ultrasshservice.tunnel;

import android.app.Service;
import android.content.Context;

import java.io.IOException;

import com.netfreemexico.generador.constants.AppConstants;
import com.slipkprojects.ultrasshservice.logger.SkStatus;

import android.content.IntentFilter;
import com.slipkprojects.ultrasshservice.tunnel.vpn.V2Listener;

import com.slipkprojects.ultrasshservice.tunnel.vpn.TunnelVpnService;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.net.Inet4Address;
import java.util.List;

import com.slipkprojects.ultrasshservice.tunnel.vpn.VpnUtils;

import android.util.Log;

import com.slipkprojects.ultrasshservice.tunnel.vpn.TunnelState;

import android.content.Intent;

import com.slipkprojects.ultrasshservice.tunnel.vpn.TunnelVpnSettings;

import android.content.BroadcastReceiver;

import com.slipkprojects.ultrasshservice.SocksHttpService;
import com.slipkprojects.ultrasshservice.tunnel.vpn.TunnelVpnManager;
import com.slipkprojects.ultrasshservice.config.Settings;

import android.os.Handler;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.trilead.ssh2.transport.TransportManager;

import java.util.concurrent.CountDownLatch;

import com.trilead.ssh2.Connection;

import android.widget.Toast;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.File;

import com.trilead.ssh2.KnownHosts;
import com.slipkprojects.ultrasshservice.config.SettingsConstants;
import com.trilead.ssh2.ProxyData;
import com.trilead.ssh2.DynamicPortForwarder;
import com.trilead.ssh2.ConnectionMonitor;
import com.trilead.ssh2.DebugLogger;
import com.trilead.ssh2.InteractiveCallback;
import com.trilead.ssh2.ServerHostKeyVerifier;
import com.netplus.vpn.R;
import com.slipkprojects.ultrasshservice.config.PasswordCache;

import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.ProxyInfo;
import android.os.Build;
import android.net.NetworkRequest;
import android.net.NetworkCapabilities;
import android.net.Network;
import android.preference.PreferenceManager;

public class TunnelManagerThread
        implements Runnable, ConnectionMonitor, InteractiveCallback,
        ServerHostKeyVerifier, DebugLogger {
    private static final String TAG = TunnelManagerThread.class.getSimpleName();

    private OnStopCliente mListener;
    private Context mContext;
    private Handler mHandler;
    private Settings mConfig;
    private boolean mRunning = false, mStopping = false, mStarting = false;
    private static UDPListener udpListener;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback callback;
    private boolean runningatesccn = false;
    private UDPTunnel udpTunnel;
    private boolean v2rayrunning = false;
    private V2Tunnel v2Tunnel;
    private static V2Listener v2Listener;
    private SharedPreferences prefs;

    private CountDownLatch mTunnelThreadStopSignal;
    //private ConnectivityManager mCmgr;

    public interface OnStopCliente {
        void onStop();
    }

    public TunnelManagerThread(Handler handler, Context context) {
        mContext = context;
        mHandler = handler;

        mConfig = new Settings(context);
        prefs = mConfig.getPrefsPrivate();
        setCallback();
        udpListener = new UDPListener() {
            @Override
            public void onConnecting() {
                SkStatus.updateStateString(SkStatus.SSH_CONECTANDO, "Conectando");
            }
            @Override
            public void onConnected() {
                try {
                    SkStatus.updateStateString(SkStatus.SSH_CONECTADO, "SSH Connection Established");
                    SkStatus.logInfo("<font color='green'><strong>" + mContext.getString(R.string.state_connected) + "</strong></font>");

                    mConnected = true;
                    startTunnelVpnService();
                } catch (Exception e)  {

                }
            }
            @Override
            public void onNetworkLost() {
            }
            @Override
            public void onAuthFailed() {
                stopAll();
            }
            @Override
            public void onReconnecting() {
                stopAll();
            }
            @Override
            public void onConnectionLost() {
                stopAll();
            }
            @Override
            public void onError() {
                stopAll();
            }
            @Override
            public void onDisconnected() {

            }
        };
        v2Listener = new V2Listener() {
            @Override
            public boolean onProtect(int socket) {
                return false;
            }

            @Override
            public Service getService() {
                return null;
            }

            @Override
            public void startService() {
                SkStatus.updateStateString(SkStatus.SSH_CONECTANDO, "Conectando");
            }

            @Override
            public void stopService() {
            }
            @Override
            public void onConnected() {
                try {
                    SkStatus.updateStateString(SkStatus.SSH_CONECTADO, "SSH Connection Established");
                    SkStatus.logInfo("<font color='#00adbe'><strong>V2ray Conectado</strong></font>");
                    mConnected = false;
                    startTunnelVpnService();
                } catch (Exception ignored)  {

                }

            }

            @Override
            public void onError() {
                Intent stopTunnel = new Intent(SocksHttpService.TUNNEL_SSH_STOP_SERVICE);
                LocalBroadcastManager.getInstance(context).sendBroadcast(stopTunnel);

            }
        };
    }

    public void setOnStopClienteListener(OnStopCliente listener) {
        mListener = listener;
    }

    public static UDPListener getUDPListener() {
        return udpListener;
    }

    public static V2Listener getV2rayServicesListener() {
        return v2Listener;
    }

    @Override
    public void run() {
        mStarting = true;
        mTunnelThreadStopSignal = new CountDownLatch(1);
        SharedPreferences prefs = mConfig.getPrefsPrivate();
        int tunnelType = prefs.getInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_DIRECT);
        if (tunnelType == Settings.bTUNNEL_TYPE_SSH_PROXY) {
            SkStatus.logInfo("<strong>" + "Secure Shell (SSH) | HTTP Injector" + "</strong>");
        } else if (tunnelType == Settings.bTUNNEL_TYPE_SSH_SSL) {
            SkStatus.logInfo("<strong>" + "Secure Shell (SSL) | HTTP Injector" + "</strong>");
        } else if (tunnelType == Settings.bTUNNEL_TYPE_PAY_SSL) {
            SkStatus.logInfo("<strong>" + "Secure Shell (SSL + SSH) | HTTP Injector" + "</strong>");
        } else if (tunnelType == Settings.bTUNNEL_TYPE_UDP) {
            SkStatus.logInfo("<strong>" + "UDP Protocol (Hysteria) | HTTP Injector" + "</strong>");
        } else if (tunnelType == Settings.bTUNNEL_TYPE_SLOWDNS) {
            SkStatus.logInfo("<strong>" + "Secure Shell (SlowDNS) | HTTP Injector" + "</strong>");
        } else if (tunnelType == Settings.bTUNNEL_TYPE_V2RAY) {
            SkStatus.logInfo("<strong>" + "V2RAY (Vmess/Vless) | HTTP Injector" + "</strong>");
        } else {
            SkStatus.logInfo("<strong>" + "Direct Connection" + "</strong>");
        }
        int tries = 0;
        while (!mStopping) {
            try {
                if (!TunnelUtils.isNetworkOnline(mContext)) {
                    SkStatus.updateStateString(SkStatus.SSH_AGUARDANDO_REDE, mContext.getString(R.string.state_nonetwork));

                    SkStatus.logInfo(R.string.state_nonetwork);

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e2) {
                        stopAll();
                        break;
                    }
                } else {
                    if (tries > 0)
                        SkStatus.logInfo("<strong>" + mContext.getString(R.string.state_reconnecting) + "</strong>");

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e2) {
                        stopAll();
                        break;
                    }

                    if(isudpmode()){
                        if (udpTunnel == null) {
                            udpTunnel = new UDPTunnel(mContext);
                            udpTunnel.iniciarUdp();
                            runningatesccn = true;
                        }
                    } else if (isv2raymode()) {
                        if (v2Tunnel == null) {
                            v2Tunnel = new V2Tunnel(mContext);
                            V2Tunnel.StartV2ray(mContext.getApplicationContext(), "Default", mConfig.getPrivString(Settings.V2RAY_JSON), null);
                            v2rayrunning = true;
                        }
                    } else{
                        startClienteSSH();
                    }
                    break;
                }
            } catch (Exception e) {

                //  SkStatus.logError("<strong>" + mContext.getString(R.string.state_disconnected) + "</strong>");
                closeSSH();

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e2) {
                    stopAll();
                    break;
                }
            }

            tries++;
        }

        mStarting = false;

        if (!mStopping) {
            try {
                mTunnelThreadStopSignal.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (mListener != null) {
            mListener.onStop();
        }
    }

    public void stopAll() {
        if (mStopping) return;

        SkStatus.updateStateString(SkStatus.SSH_PARANDO, mContext.getString(R.string.stopping_service_ssh));
        //   SkStatus.logInfo(mContext.getString(R.string.stopping_service_ssh));
        SkStatus.logInfo("<strong><html><font color=\"#e51400\">" + "Disconnected" + "</font></html></strong>");
        new Thread(new Runnable() {
            @Override
            public void run() {
                mStopping = true;

                if (mTunnelThreadStopSignal != null)
                    mTunnelThreadStopSignal.countDown();

                if (isudpmode()) {
                    if (runningatesccn) {
                        udpTunnel.detenerUdp();
                        udpTunnel = null;
                        runningatesccn = false;
                        connectivityManager.unregisterNetworkCallback(callback);
                    }
                    if (mConnected) {
                        stopForwarder();
                    }
                    mRunning = false;
                    mStarting = false;
                    mReconnecting = false;
                } else if (isv2raymode()) {
                    if (v2rayrunning) {
                        V2Tunnel.StopV2ray(mContext.getApplicationContext());
                        v2Tunnel = null;
                        v2rayrunning= false;
                        connectivityManager.unregisterNetworkCallback(callback);
                    }
                    if (mConnected) {
                        stopForwarder();
                    }
                    mRunning = false;
                    mStarting = false;
                    mReconnecting = false;
                } else {
                    closeSSH();
                    mRunning = false;
                    mStarting = false;
                    mReconnecting = false;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }

                SkStatus.updateStateString(SkStatus.SSH_DESCONECTADO, mContext.getString(R.string.state_disconnected));
            }
        }).start();
    }


    /**
     * Forwarder
     */

    protected void startForwarder(int portaLocal) throws Exception {
        if (!mConnected) {
            throw new Exception();
        }

        startForwarderSocks(portaLocal);

        startTunnelVpnService();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (!mConnected) break;

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        break;
                    }

                    if (lastPingLatency > 0) {
                        SkStatus.logInfo(String.format("Ping Latency: %d ms", lastPingLatency));
                        break;
                    }
                }
            }
        }).start();
    }

    protected void stopForwarder() {
        stopTunnelVpnService();

        stopForwarderSocks();
    }


    /**
     * Cliente SSH
     */

    private final static int AUTH_TRIES = 1;
    private final static int RECONNECT_TRIES = 5;

    private Connection mConnection;

    private boolean mConnected = false;

    protected void startClienteSSH() throws Exception {
        mStopping = false;
        mRunning = true;

        String servidor = mConfig.getPrivString(Settings.SERVIDOR_KEY);
        int porta = Integer.parseInt(mConfig.getPrivString(Settings.SERVIDOR_PORTA_KEY));
        String usuario = mConfig.getPrivString(Settings.USUARIO_KEY);

        String _senha = mConfig.getPrivString(Settings.SENHA_KEY);
        String senha = _senha.isEmpty() ? PasswordCache.getAuthPassword(null, false) : _senha;

        String keyPath = mConfig.getSSHKeypath();
        int portaLocal = Integer.parseInt(mConfig.getPrivString(Settings.PORTA_LOCAL_KEY));

        try {

            conectar(servidor, porta);

            for (int i = 0; i < AUTH_TRIES; i++) {
                if (mStopping) {
                    return;
                }

                try {
                    autenticar(usuario, senha, keyPath);

                    break;
                } catch (IOException e) {
                    if (i + 1 >= AUTH_TRIES) {
                        throw new IOException("Authentication failed");
                    } else {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e2) {
                            return;
                        }
                    }
                }
            }

            SkStatus.updateStateString(SkStatus.SSH_CONECTADO, "SSH Connection Established");
            // SkStatus.logInfo("<strong>" + mContext.getString(R.string.state_connected) + "</strong>");
            SkStatus.logInfo("<strong><html><font color=\"#2AFF0D\">" + "Connected" + "</strong>");
            SkStatus.logInfo("Tunnelling started");
            if (mConfig.getSSHPinger() > 0) {
                startPinger(mConfig.getSSHPinger());
            }

            startForwarder(portaLocal);

        } catch (Exception e) {
            mConnected = false;

            throw e;
        }
    }

    public synchronized void closeSSH() {
        stopForwarder();
        stopPinger();

        if (mConnection != null) {
            //  SkStatus.logDebug("Stopping SSH Service");
            mConnection.close();
        }
    }

    protected void conectar(String servidor, int porta) throws Exception {
        if (!mStarting) {
            throw new Exception();
        }

        SharedPreferences prefs = mConfig.getPrefsPrivate();

        // aqui deve conectar
        try {

            mConnection = new Connection(servidor, porta);

            if (mConfig.getModoDebug() && !prefs.getBoolean(Settings.CONFIG_PROTEGER_KEY, false)) {
                // Desativado, pois estava enchendo o Logger
                //mConnection.enableDebugging(true, this);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "Debug mode enabled",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // delay sleep
            if (mConfig.getIsDisabledDelaySSH()) {
                mConnection.setTCPNoDelay(true);
            }

            // proxy
            addProxy(prefs.getBoolean(Settings.CONFIG_PROTEGER_KEY, false), prefs.getInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_DIRECT),
                    (!prefs.getBoolean(Settings.PROXY_USAR_DEFAULT_PAYLOAD, true) ? mConfig.getPrivString(Settings.CUSTOM_PAYLOAD_KEY) : null), mConfig.getPrivString(Settings.CUSTOM_SNI),
                    mConnection);

            // monitora a conexão
            mConnection.addConnectionMonitor(this);

            if (Build.VERSION.SDK_INT >= 23) {
                ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                ProxyInfo proxy = cm.getDefaultProxy();
                if (proxy != null) {
                    // SkStatus.logInfo("<strong>Network Proxy:</strong> " + String.format("%s:%d", proxy.getHost(), proxy.getPort()));
                }
            }

            SkStatus.updateStateString(SkStatus.SSH_CONECTANDO, mContext.getString(R.string.state_connecting));
            SkStatus.logInfo(R.string.state_connecting);

            mConnection.connect(this, 10 * 1000, 20 * 1000);

            mConnected = true;

        } catch (Exception e) {

            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));

            String cause = e.getCause().toString();
            if (useProxy && cause.contains("Key exchange was not finished")) {
                SkStatus.logError("Proxy: Connection Lost");
            } else {
                //SkStatus.logError("SSH: " + cause);
            }

            throw new Exception(e);
        }
    }


    /**
     * Autenticação
     */

    private static final String AUTH_PUBLICKEY = "publickey",
            AUTH_PASSWORD = "password", AUTH_KEYBOARDINTERACTIVE = "keyboard-interactive";

    protected void autenticar(String usuario, String senha, String keyPath) throws IOException {
        if (!mConnected) {
            throw new IOException();
        }

        SkStatus.updateStateString(SkStatus.SSH_AUTENTICANDO, mContext.getString(R.string.state_auth));

        try {
            if (mConnection.isAuthMethodAvailable(usuario,
                    AUTH_PASSWORD)) {

                SkStatus.logInfo("Authenticating with password");

                if (mConnection.authenticateWithPassword(usuario,
                        senha)) {
                    // SkStatus.logInfo( mContext.getString(R.string.state_auth_success));
                }
            }
        } catch (IllegalStateException e) {
            Log.e(TAG,
                    "Connection went away while we were trying to authenticate",
                    e);
        } catch (Exception e) {
            Log.e(TAG, "Problem during handleAuthentication()", e);
        }

        try {
            if (mConnection.isAuthMethodAvailable(usuario,
                    AUTH_PUBLICKEY) && keyPath != null && !keyPath.isEmpty()) {
                File f = new File(keyPath);
                if (f.exists()) {
                    if (senha.equals("")) senha = null;

                    // SkStatus.logInfo("Authenticating com public key");

                    if (mConnection.authenticateWithPublicKey(usuario, f,
                            senha)) {
                        SkStatus.logInfo("<strong>" + mContext.getString(R.string.state_auth_success) + "</strong>");
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "Host does not support 'Public key' authentication.");
        }

        /*try {
         if (mConnection.authenticateWithNone(mSettings.usuario)) {
         Log.d(TAG, "Authenticate with none");
         return true;
         }
         } catch (Exception e) {
         Log.d(TAG, "Host does not support 'none' authentication.");
         }

         // TODO: Need verification

         try {
         if (mConnection.isAuthMethodAvailable(mSettings.usuario,
         AUTH_KEYBOARDINTERACTIVE)) {
         if (mConnection.authenticateWithKeyboardInteractive(
         mSettings.usuario, this))
         return true;
         }
         } catch (Exception e) {
         Log.d(TAG,
         "Host does not support 'Keyboard-Interactive' authentication.");
         }*/

        if (!mConnection.isAuthenticationComplete()) {
            //  Authen = true;
            SkStatus.logInfo("<font color = #C01C47> User or Password Expired");
            stopAll();
            //  SkStatus.updateStateString(SkStatus.Failed_authenticate, mContext.getString(R.string.state_auth_failed));
            throw new IOException("It was not possible to authenticate with the data provided");
        }
    }

    // XXX: Is it right?
    @Override
    public String[] replyToChallenge(String name, String instruction,
                                     int numPrompts, String[] prompt, boolean[] echo) throws Exception {
        String[] responses = new String[numPrompts];
        for (int i = 0; i < numPrompts; i++) {
            // request response from user for each prompt
            if (prompt[i].toLowerCase().contains("password"))
                responses[i] = mConfig.getPrivString(Settings.SENHA_KEY);
        }
        return responses;
    }


    /**
     * ServerHostKeyVerifier
     * Fingerprint
     */

    @Override
    public boolean verifyServerHostKey(String hostname, int port,
                                       String serverHostKeyAlgorithm, byte[] serverHostKey)
            throws Exception {

        String fingerPrint = KnownHosts.createHexFingerprint(
                serverHostKeyAlgorithm, serverHostKey);
        //int fingerPrintStatus = SSHConstants.FINGER_PRINT_CHANGED;

        SkStatus.logInfo("Hostkey Fingerprint: " + fingerPrint);

        //Log.d(TAG, "Finger Print Type: " + "");

        return true;
    }


    /**
     * Proxy
     */

    private boolean useProxy = false;

    protected void addProxy(boolean isProteger, int mTunnelType, String mCustomPayload, String mCustomSNI, Connection conn) throws Exception {

        if (mTunnelType != 0) {
            useProxy = true;

            switch (mTunnelType) {
                case Settings.bTUNNEL_TYPE_SSH_DIRECT:
                    if (mCustomPayload != null) {
                        try {
                            ProxyData proxyData = new HttpProxyCustom(mConfig.getPrivString(Settings.SERVIDOR_KEY), Integer.parseInt(mConfig.getPrivString(Settings.SERVIDOR_PORTA_KEY)),
                                    null, null, mCustomPayload, true, mContext);

                            conn.setProxyData(proxyData);

                            if (!mCustomPayload.isEmpty() && !isProteger) ;
                            // SkStatus.logInfo("Payload: Direct");

                        } catch (Exception e) {
                            throw new Exception(mContext.getString(R.string.error_proxy_invalid));
                        }
                    } else {
                        useProxy = false;
                    }
                    break;

                case Settings.bTUNNEL_TYPE_SSH_PROXY:
                    String customPayload = mCustomPayload;

                    if (customPayload != null && customPayload.isEmpty()) {
                        customPayload = null;
                    }

                    String servidor = mConfig.getPrivString(Settings.PROXY_IP_KEY);
                    int porta = Integer.parseInt(mConfig.getPrivString(Settings.PROXY_PORTA_KEY));

                    try {
                        ProxyData proxyData = new HttpProxyCustom(servidor, porta,
                                null, null, customPayload, false, mContext);

                        if (!isProteger)
                            //  SkStatus.logInfo(String.format("Remote Proxy: Locked", servidor, porta));
                            conn.setProxyData(proxyData);

                        if (customPayload != null && !customPayload.isEmpty() && !isProteger) ;
                        //SkStatus.logInfo("Payload: Locked")

                    } catch (Exception e) {
                        SkStatus.logError(R.string.error_proxy_invalid);

                        throw new Exception(mContext.getString(R.string.error_proxy_invalid));
                    }
                    break;
                case Settings.bTUNNEL_TYPE_SSH_SSL:
                    String customsni = mCustomPayload;

                    if (customsni != null && customsni.isEmpty()) {
                        customPayload = null;
                    }

                    String server = mConfig.getPrivString(Settings.SERVIDOR_KEY);
                    int port = Integer.parseInt(mConfig.getPrivString(Settings.SERVIDOR_PORTA_KEY));

                    try {
                        ProxyData proxyData = new SSLTunnelProxy(server, port, customsni, mContext);
                        conn.setProxyData(proxyData);

                    } catch (Exception e) {
                        SkStatus.logInfo(e.getMessage());
                    }

                    break;

                case Settings.bTUNNEL_TYPE_PAY_SSL:
                    String customSNI2 = mCustomSNI;
                    if (customSNI2 != null && customSNI2.isEmpty()) {
                        customSNI2 = null;
                    }
                    String customPayload2 = mCustomPayload;

                    if (customPayload2 != null && customPayload2.isEmpty()) {
                        customPayload2 = null;
                    }

                    String sshServer2 = mConfig.getPrivString(Settings.SERVIDOR_KEY);
                    int sshPort2 = Integer.parseInt(mConfig.getPrivString(Settings.SERVIDOR_PORTA_KEY));

                    try {

                        SSLProxy sslTun = new SSLProxy(sshServer2, sshPort2, customSNI2, customPayload2, mContext);
                        conn.setProxyData(sslTun);

                    } catch (Exception e) {
                        SkStatus.logInfo(e.getMessage());
                    }

                    break;
                case Settings.bTUNNEL_TYPE_SLOWDNS:

                    useProxy = false;

                    break;
                default:
                    useProxy = false;
            }
        }
    }


    /**
     * Socks5 Forwarder
     */

    private DynamicPortForwarder dpf;

    private synchronized void startForwarderSocks(int portaLocal) throws Exception {
        if (!mConnected) {
            throw new Exception();
        }

        //  SkStatus.logInfo("starting socks local");
        //    SkStatus.logDebug(String.format("socks local listen: %d", portaLocal));

        try {

            int nThreads = mConfig.getMaximoThreadsSocks();

            if (nThreads > 0) {
                dpf = mConnection.createDynamicPortForwarder(portaLocal, nThreads);

                SkStatus.logDebug("socks local number threads: " + Integer.toString(nThreads));
            } else {
                dpf = mConnection.createDynamicPortForwarder(portaLocal);
            }

        } catch (Exception e) {
            SkStatus.logError("Socks Local: " + e.getCause().toString());

            throw new Exception();
        }
    }

    private synchronized void stopForwarderSocks() {
        if (dpf != null) {
            try {
                dpf.close();
            } catch (IOException e) {
            }
            dpf = null;
        }
    }


    /**
     * Pinger
     */

    private Thread thPing;
    private long lastPingLatency = -1;

    private void startPinger(final int timePing) throws Exception {
        if (!mConnected) {
            throw new Exception();
        }

        //SkStatus.logInfo("starting pinger");

        thPing = new Thread() {
            @Override
            public void run() {
                while (mConnected) {
                    try {
                        makePinger();
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                //     SkStatus.logDebug("pinger stopped");
            }

            private synchronized void makePinger() throws InterruptedException {
                try {
                    if (mConnection != null) {
                        long ping = mConnection.ping();
                        if (lastPingLatency < 0) {
                            lastPingLatency = ping;
                        }
                    } else throw new InterruptedException();
                } catch (Exception e) {
                    Log.e(TAG, "ping error", e);
                }

                if (timePing == 0)
                    return;

                if (timePing > 0)
                    sleep(timePing * 1000);
                else {
                    SkStatus.logError("ping invalid");
                    throw new InterruptedException();
                }
            }
        };

        // inicia
        thPing.start();
    }

    private synchronized void stopPinger() {
        if (thPing != null && thPing.isAlive()) {
            //  SkStatus.logInfo("stopping pinger");

            thPing.interrupt();
            thPing = null;
        }
    }

    /**
     * Connection Monitor
     */

    @Override
    public void connectionLost(Throwable reason) {
        if (mStarting || mStopping || mReconnecting) {
            return;
        }

        SkStatus.logError("<strong>" + mContext.getString(R.string.log_conection_lost) + "</strong>");

        if (reason != null) {
            if (reason.getMessage().contains(
                    "There was a problem during connect")) {
                return;
            } else if (reason.getMessage().contains(
                    "Closed due to user request")) {
                return;
            } else if (reason.getMessage().contains(
                    "The connect timeout expired")) {
                stopAll();
                return;
            }
        } else {
            stopAll();
            return;
        }

        reconnectSSH();
    }

    public boolean mReconnecting = false;

    public void reconnectSSH() {
        if (mStarting || mStopping || mReconnecting) {
            return;
        }

        mReconnecting = true;

        closeSSH();

        SkStatus.updateStateString(SkStatus.SSH_RECONECTANDO, "Reconnecting..");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            mReconnecting = false;
            return;
        }

        for (int i = 0; i < RECONNECT_TRIES; i++) {
            if (mStopping) {
                mReconnecting = false;
                return;
            }

            int sleepTime = 5;
            if (!TunnelUtils.isNetworkOnline(mContext)) {
                SkStatus.updateStateString(SkStatus.SSH_AGUARDANDO_REDE, "Waiting for network ..");

                SkStatus.logInfo(R.string.state_nonetwork);
            } else {
                sleepTime = 3;
                mStarting = true;
                SkStatus.updateStateString(SkStatus.SSH_RECONECTANDO, "Reconnecting..");

                SkStatus.logInfo("<strong>" + mContext.getString(R.string.state_reconnecting) + "</strong>");

                try {
                    startClienteSSH();

                    mStarting = false;
                    mReconnecting = false;
                    //mConnected = true;

                    return;
                } catch (Exception e) {
                    // SkStatus.logInfo("<strong>" + mContext.getString(R.string.state_disconnected) + "</strong>");
                    SkStatus.logInfo("<strong><html><font color=\"#FF3D00\">" + "Disconnected" + "</strong>");
                }

                mStarting = false;
            }

            try {
                Thread.sleep(sleepTime * 1000);
                i--;
            } catch (InterruptedException e2) {
                mReconnecting = false;
                return;
            }
        }

        mReconnecting = false;

        stopAll();
    }

    @Override
    public void onReceiveInfo(int id, String msg) {
        if (id == SERVER_BANNER) {
            SkStatus.logInfo("<strong>" + mContext.getString(R.string.log_server_banner) + "</strong> " + msg);
        }
    }


    /**
     * Debug Logger
     */

    @Override
    public void log(int level, String className, String message) {
        SkStatus.logDebug(String.format("%s: %s", className, message));
    }


    /**
     * Vpn Tunnel
     */

    String serverAddr;

    protected void startTunnelVpnService() throws IOException {
        if (!mConnected) {
            throw new IOException();
        }

        //SkStatus.logInfo("starting tunnel service");
        SharedPreferences prefs = mConfig.getPrefsPrivate();
        SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        // Broadcast
        IntentFilter broadcastFilter =
                new IntentFilter(TunnelVpnService.TUNNEL_VPN_DISCONNECT_BROADCAST);
        broadcastFilter.addAction(TunnelVpnService.TUNNEL_VPN_START_BROADCAST);
        // Inicia Broadcast
        LocalBroadcastManager.getInstance(mContext)
                .registerReceiver(m_vpnTunnelBroadcastReceiver, broadcastFilter);

        String m_socksServerAddress = String.format("127.0.0.1:%s", mConfig.getPrivString(Settings.PORTA_LOCAL_KEY));
        boolean m_dnsForward = mConfig.getVpnDnsForward();
        String m_udpResolver = mConfig.getVpnUdpForward() ? mConfig.getVpnUdpResolver() : null;

        String servidorIP = mConfig.getPrivString(Settings.SERVIDOR_KEY);

        if (prefs.getInt(Settings.TUNNELTYPE_KEY, Settings.bTUNNEL_TYPE_SSH_DIRECT) == Settings.bTUNNEL_TYPE_SSH_PROXY) {
            try {
                servidorIP = mConfig.getPrivString(Settings.PROXY_IP_KEY);
            } catch(Exception e) {
                SkStatus.logError(R.string.error_proxy_invalid);

                throw new IOException(mContext.getString(R.string.error_proxy_invalid));
            }
        }

        try {
            if (isudpmode()) {
                serverAddr = servidorIP = getIPv4Addresses(InetAddress.getAllByName(prefs.getString(Settings.UDP_SERVER, ""))).getHostAddress();
            } else {
                InetAddress servidorAddr = TransportManager.createInetAddress(servidorIP);
                serverAddr = servidorIP = servidorAddr.getHostAddress(); }
        } catch (UnknownHostException e) {
            throw new IOException(mContext.getString(R.string.error_server_ip_invalid));
        }

        String[] m_excludeIps = {servidorIP};

        String[] m_dnsResolvers = null;
        if (m_dnsForward) {
            m_dnsResolvers = new String[]{mConfig.getVpnDnsResolver1(), mConfig.getVpnDnsResolver2()};
        } else {
            List<String> lista = VpnUtils.getNetworkDnsServer(mContext);
            m_dnsResolvers = new String[]{lista.get(0)};
        }

        if (m_dnsForward) {
            if (mPref.getString(Settings.DNSTYPE_KEY, Settings.DNS_GOOGLE_KEY).equals(Settings.DNS_GOOGLE_KEY)) {
                // SkStatus.logInfo("Google DNS enabled");
                SkStatus.logInfo("DNS Forwarding: Google DNS");
            } else if (mPref.getString(Settings.DNSTYPE_KEY, Settings.DNS_GOOGLE_KEY).equals(Settings.DNS_CUSTOM_KEY)) {
                //SkStatus.logInfo("Custom DNS enabled");
                SkStatus.logInfo("DNS Forwarding: Custom DNS");
            } else {
                //  SkStatus.logInfo("Default DNS enabled");
                SkStatus.logInfo("DNS Forwarding: Default DNS");
            }

        } else {
            // SkStatus.logInfo("DNS Disabled");
        }

        if (isServiceVpnRunning()) {
            Log.d(TAG, "already running service");

            TunnelVpnManager tunnelManager = TunnelState.getTunnelState()
                    .getTunnelManager();

            if (tunnelManager != null) {
                tunnelManager.restartTunnel(m_socksServerAddress);
            }

            return;
        }

        Intent startTunnelVpn = new Intent(mContext, TunnelVpnService.class);
        startTunnelVpn.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        TunnelVpnSettings settings = new TunnelVpnSettings(m_socksServerAddress, m_dnsForward, m_dnsResolvers,
                (m_dnsForward && m_udpResolver == null || !m_dnsForward && m_udpResolver != null), m_udpResolver, m_excludeIps,
                mConfig.getIsFilterApps(), mConfig.getIsFilterBypassMode(), mConfig.getFilterApps(), mConfig.getIsTetheringSubnet(), mConfig.getBypass());
        startTunnelVpn.putExtra(TunnelVpnManager.VPN_SETTINGS, settings);

        if (mContext.startService(startTunnelVpn) == null) {
            SkStatus.logInfo("failed to start tunnel vpn service");

            throw new IOException("Vpn Service failed to start");
        }

        TunnelState.getTunnelState().setStartingTunnelManager();
    }


    public static boolean isServiceVpnRunning() {
        TunnelState tunnelState = TunnelState.getTunnelState();
        return tunnelState.getStartingTunnelManager() || tunnelState.getTunnelManager() != null;
    }

    public static Inet4Address getIPv4Addresses(InetAddress[] inetAddressArr) {
        for (InetAddress inetAddress : inetAddressArr) {
            if (inetAddress instanceof Inet4Address) {
                return (Inet4Address) inetAddress;
            }
        }
        return null;
    }

    protected synchronized void stopTunnelVpnService() {
        if (!isServiceVpnRunning()) {
            return;
        }

        // Use signalStopService to asynchronously stop the service.
        // 1. VpnService doesn't respond to stopService calls
        // 2. The UI will not block while waiting for stopService to return
        // This scheme assumes that the UI will monitor that the service is
        // running while the Activity is not bound to it. This is the state
        // while the tunnel is shutting down.
        //   SkStatus.logInfo("stopping tunnel service");

        TunnelVpnManager currentTunnelManager = TunnelState.getTunnelState()
                .getTunnelManager();

        if (currentTunnelManager != null) {
            currentTunnelManager.signalStopService();
        }

        /*if (mThreadLocation != null && mThreadLocation.isAlive()) {
         mThreadLocation.interrupt();
         }
         mThreadLocation = null;*/

        // Parando Broadcast
        LocalBroadcastManager.getInstance(mContext)
                .unregisterReceiver(m_vpnTunnelBroadcastReceiver);
    }

    //private Thread mThreadLocation;

    // Local BroadcastReceiver
    private BroadcastReceiver m_vpnTunnelBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public synchronized void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (TunnelVpnService.TUNNEL_VPN_START_BROADCAST.equals(action)) {
                boolean startSuccess = intent.getBooleanExtra(TunnelVpnService.TUNNEL_VPN_START_SUCCESS_EXTRA, true);

                if (!startSuccess) {
                    stopAll();
                }

            } else if (TunnelVpnService.TUNNEL_VPN_DISCONNECT_BROADCAST.equals(action)) {
                stopAll();
            }
        }
    };

    private boolean isudpmode() {
        return prefs.getInt(Settings.TUNNELTYPE_KEY, 0) == Settings.bTUNNEL_TYPE_UDP; }
    private boolean isv2raymode() {
        return prefs.getInt(Settings.TUNNELTYPE_KEY, 0) == Settings.bTUNNEL_TYPE_V2RAY; }

    private void setCallback() {
        connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest request = new NetworkRequest.Builder().build();
        callback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
            }
            @Override
            public void onLost(Network network) {
                if (runningatesccn) {
                    if(isudpmode() || isv2raymode()){
                        stopAll();
                    }
                }
            }
        };
        connectivityManager.registerNetworkCallback(request, callback); }

}
