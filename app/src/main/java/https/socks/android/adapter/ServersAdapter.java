package https.socks.android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import https.socks.android.activities.ServersViewHolder;
import https.socks.android.model.ServerModel;
import java.util.List;
import com.netplus.vpn.R;

public class ServersAdapter extends RecyclerView.Adapter<ServersViewHolder>{
    
    private List<ServerModel> servidores;
    private onItemClickListener listener;
    
    public ServersAdapter(List<ServerModel> model) {
        servidores = model;
    }
    
    public void setOnItemClick(onItemClickListener mlistener) {
        listener = mlistener;
    }
    
    @NonNull
    @Override
    public ServersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.servers_item, parent, false);
        
        return new ServersViewHolder(view, listener);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ServersViewHolder holder, int position) {
        ServerModel model = servidores.get(position);
        holder.bind(model);
    }
    
    @Override
    public int getItemCount() {
        return servidores.size();
    }
    
    public interface onItemClickListener {
        void onItemClick(int position);
    }
}
