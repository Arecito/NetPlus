package https.socks.android.model;

import androidx.annotation.NonNull;

public class ServerModel {
    
    private String servername;
    private String serverinfo;
    private String serverflag;
    private int posicion;
    
    @NonNull
    public int getServerPosicion() {
        return posicion;
    }
    
    @NonNull
    public String getServerFlag() {
        return serverflag;
    }
    
    @NonNull
    public String getServerName() {
        return this.servername;
    }
     
    
    @NonNull
    public String getServerInfo() {
        return this.serverinfo;
    }
    
    public void setServerName(String servername) {
        this.servername = servername;
    }
    
    public void setServerInfo(String serverinfo) {
        this.serverinfo = serverinfo;
    }
    
    public void setServerFlag(String flag) {
        this.serverflag = flag;
    }
    
    public void setServerPosicion(int posicion) {
        this.posicion = posicion;
    }
    
}
