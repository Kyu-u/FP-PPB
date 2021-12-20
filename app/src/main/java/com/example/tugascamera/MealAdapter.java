//package com.example.tugascamera;
//
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.bumptech.glide.Glide;
//
//import java.util.List;
//
//public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealHolder> {
//
//
//    private Context context;
//    private List<Meal> mealList;
//
//    public MealAdapter(Context context, List<Meal> meals){
//        this.context = context;
//        mealList = meals;
//    }
//    @NonNull
//    @Override
//    public MealHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(context).inflate(R.layout.recipe_list, parent, false);
//        return new MealHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull MealHolder holder, int position) {
//        Meal meal = mealList.get(position);
//        holder.name.setText(meal.getName().toString());
//
//        Glide.with(context).load(meal.getThumb()).into(holder.imageView);
//
//        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(context , DetailActivity.class);
//
//                Bundle bundle = new Bundle();
//                bundle.putString("name" , meal.getName());
//                bundle.putString("thumb" , meal.getThumb());
//
//                intent.putExtras(bundle);
//
//                context.startActivity(intent);
//            }
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return mealList.size();
//    }
//
//    public class MealHolder extends RecyclerView.ViewHolder{
//
//        ImageView imageView;
//        TextView name;
//        public MealHolder(@NonNull View itemView) {
//            super(itemView);
//
//            imageView = itemView.findViewById(R.id.imageView4);
//            name = itemView.findViewById(R.id.textView3);
//        }
//    }
//
//}
