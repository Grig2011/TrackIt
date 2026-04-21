package grig.yeganyan.trackit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import grig.yeganyan.trackit.ProgressEntry;

public class ProgressEntryAdapter extends RecyclerView.Adapter<ProgressEntryAdapter.VH> {

    private final List<ProgressEntry> entries;
    private final String unit;
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public ProgressEntryAdapter(List<ProgressEntry> entries, String unit) {
        this.entries = entries;
        this.unit    = unit;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_progress_entry, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ProgressEntry e = entries.get(position);
        String sign    = e.delta >= 0 ? "+" : "";
        String deltaStr = e.delta == (long) e.delta
                ? String.valueOf((long) e.delta) : String.valueOf(e.delta);
        String totalStr = e.runningTotal == (long) e.runningTotal
                ? String.valueOf((long) e.runningTotal) : String.valueOf(e.runningTotal);

        h.tvDelta.setText(sign + deltaStr + " " + unit);
        h.tvDelta.setTextColor(e.delta >= 0 ? 0xFF2D6A4F : 0xFFE07070);
        h.tvTotal.setText("Total: " + totalStr);
        h.tvTime.setText(sdf.format(e.timestamp));
    }

    @Override
    public int getItemCount() { return entries.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDelta, tvTotal, tvTime;
        VH(@NonNull View v) {
            super(v);
            tvDelta = v.findViewById(R.id.tvDelta);
            tvTotal = v.findViewById(R.id.tvTotal);
            tvTime  = v.findViewById(R.id.tvTime);
        }
    }
}
