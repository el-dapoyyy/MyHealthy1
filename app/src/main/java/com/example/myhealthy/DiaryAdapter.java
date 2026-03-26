package com.example.myhealthy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.ViewHolder> {

    public interface OnDeleteListener {
        void onDelete(int position);
    }

    private final List<DiaryEntry> entries;
    private final OnDeleteListener deleteListener;

    public DiaryAdapter(List<DiaryEntry> entries, OnDeleteListener deleteListener) {
        this.entries = entries;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_diary_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DiaryEntry entry = entries.get(position);
        holder.tvName.setText(entry.foodName);
        holder.tvMealType.setText(entry.mealType);
        holder.tvCalories.setText(entry.calories + " kkal");
        holder.btnDelete.setOnClickListener(v -> deleteListener.onDelete(position));
    }

    @Override
    public int getItemCount() { return entries.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvMealType, tvCalories;
        ImageView btnDelete;

        ViewHolder(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvEntryName);
            tvMealType = v.findViewById(R.id.tvEntryMealType);
            tvCalories = v.findViewById(R.id.tvEntryCalories);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
