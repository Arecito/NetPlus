/*
 * Created by JuancitoOficial VPN on 27/01/24 22:59
 *  Copyright (c) Telegram: @𝕵𝖚𝖆𝖓𝖈𝖎𝖙𝖔 ᵒᶠᶜ🌹1 . All rights reserved.
 */
package https.socks.android.activities;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netplus.vpn.R;
import https.socks.android.adapter.ServersAdapter;
import https.socks.android.model.ServerModel;
import java.io.InputStream;

public class ServersViewHolder extends RecyclerView.ViewHolder{
    private TextView servername;
    private TextView serverinfo;
    private ImageView servericon;
    private ImageView serversignal;
    private Context context;
    private ServerModel modelo;
    
    public ServersViewHolder(@NonNull View view, ServersAdapter.onItemClickListener listener) {
        super(view);
        context = view.getContext();
        servername = view.findViewById(R.id.servers_name);
        serverinfo = view.findViewById(R.id.servers_info);
        servericon = view.findViewById(R.id.servers_image);
        
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(modelo.getServerPosicion());
            }
        });
    }
    
    public void bind(ServerModel server) {
        modelo = server;
        servername.setText(server.getServerName());
        serverinfo.setText(server.getServerInfo());
        try { 
            setImagen(servericon, server.getServerFlag());
        } catch (Exception e) {
            servericon.setImageResource(R.drawable.icon);
        }
    }
    
    
    public void setImagen(ImageView im, String nameo) throws Exception {
        InputStream inputStream = context.getAssets().open("flags/" + nameo + ".webp");
		im.setImageDrawable(Drawable.createFromStream(inputStream, nameo + ".webp"));
	}
    
}
