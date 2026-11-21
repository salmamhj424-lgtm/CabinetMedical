package com.example.cabinetmedical;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class RdvAdapter extends ArrayAdapter<Patient> {

    public RdvAdapter(Context context, List<Patient> items) {
        super(context, 0, items);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = LayoutInflater.from(getContext()).inflate(R.layout.item_rdv, parent, false);
        }
        Patient p = getItem(position);
        TextView tvDate = v.findViewById(R.id.tvRdvDate);
        TextView tvStatus = v.findViewById(R.id.tvRdvStatus);
        TextView tvNom = v.findViewById(R.id.tvRdvNom);
        TextView tvTel = v.findViewById(R.id.tvRdvTel);
        if (p != null) {
            long d = DateUtils.daysFromToday(p.rdv);
            tvDate.setText(p.rdv);
            tvStatus.setText(DateUtils.describe(d));
            tvStatus.setTextColor(DateUtils.color(d));
            tvNom.setText(p.nom);
            tvTel.setText("📞 " + p.telephone);
        }
        return v;
    }
}
