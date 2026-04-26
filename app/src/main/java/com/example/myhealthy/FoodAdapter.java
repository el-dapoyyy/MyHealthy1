package com.example.myhealthy;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder> {

    private final Context context;
    private final List<FoodItem> foodList;

    public FoodAdapter(Context context, List<FoodItem> foodList) {
        this.context = context;
        this.foodList = (foodList == null) ? new ArrayList<>() : new ArrayList<>(foodList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.food_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodItem foodItem = foodList.get(position);

        holder.foodName.setText(foodItem.getName());

        float rating = foodItem.getRating();
        holder.tvRating.setText(String.format(Locale.US, "⭐ %.1f", rating));

        holder.foodImage.setImageResource(foodItem.getImageRes());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FoodDetailActivity.class);

            // SAMAKAN KEY dengan FoodDetailActivity yang kamu pakai
            intent.putExtra("name", foodItem.getName());
            intent.putExtra("desc", foodItem.getDescription());
            intent.putExtra("img", foodItem.getImageRes());
            intent.putExtra("prot", foodItem.getProtein());
            intent.putExtra("cal", foodItem.getCalories());
            intent.putExtra("fat", foodItem.getFat());
            intent.putExtra("carb", foodItem.getCarbs());
            intent.putExtra("info", foodItem.getAdditionalInfo());
            intent.putExtra("rating", foodItem.getRating());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    /** Update data tanpa re-assign list (biar tidak error final) */
    public void setData(List<FoodItem> newList) {
        foodList.clear();
        if (newList != null) foodList.addAll(newList);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView foodName, tvRating;
        ImageView foodImage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            foodName = itemView.findViewById(R.id.foodName);
            tvRating = itemView.findViewById(R.id.tvRating);
            foodImage = itemView.findViewById(R.id.foodImage);
        }
    }
}
