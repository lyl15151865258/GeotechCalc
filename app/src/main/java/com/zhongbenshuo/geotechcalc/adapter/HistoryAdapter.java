package com.zhongbenshuo.geotechcalc.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.zhongbenshuo.geotechcalc.R;
import com.zhongbenshuo.geotechcalc.bean.Data;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private final List<Data> list;

    public HistoryAdapter(List<Data> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        Data data = list.get(position);
        holder.tvNo.setText(String.valueOf(position + 1));
        holder.tvFormula.setText("公式：" + data.getFormula());
        holder.tvThreshold.setText("阈值：" + data.getThreshold() + "%");
        holder.tvTimes.setText("变量值：" + data.getTimes());
        holder.tvResult.setText("压实度：" + data.getResult() + "%");
        holder.tvEffective.setText( data.getEffective());
        holder.tvDatetime.setText("时间：" + data.getDatetime());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {

        private final AppCompatTextView tvNo, tvFormula, tvThreshold, tvTimes, tvResult, tvEffective, tvDatetime;

        private HistoryViewHolder(View itemView) {
            super(itemView);
            tvNo = itemView.findViewById(R.id.tvNo);
            tvFormula = itemView.findViewById(R.id.tvFormula);
            tvThreshold = itemView.findViewById(R.id.tvThreshold);
            tvTimes = itemView.findViewById(R.id.tvTimes);
            tvResult = itemView.findViewById(R.id.tvResult);
            tvEffective = itemView.findViewById(R.id.tvEffective);
            tvDatetime = itemView.findViewById(R.id.tvDatetime);
        }
    }
}
