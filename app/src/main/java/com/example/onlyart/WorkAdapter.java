package com.example.onlyart;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;

public class WorkAdapter extends RecyclerView.Adapter<WorkAdapter.WorkViewHolder> {

    private List<Work> workList;

    public WorkAdapter(List<Work> workList) {
        this.workList = workList;
    }

    @NonNull
    @Override
    public WorkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_work, parent, false);
        return new WorkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkViewHolder holder, int position) {
        Work work = workList.get(position);

        Glide.with(holder.itemView.getContext())
                .load(work.getImageUrl())
                .fitCenter()
                .override(Target.SIZE_ORIGINAL)
                .into(holder.imageView);

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), DetailActivity.class);
            intent.putExtra("imageUrl", work.getImageUrl());
            intent.putExtra("uploaderUid", work.getUid());
            intent.putExtra("tags", work.getTags());
            intent.putExtra("title", work.getTitle());
            intent.putExtra("desc", work.getDesc());
            intent.putExtra("ai_generated", work.getAiGenerated());
            intent.putExtra("key", work.getKey());
            intent.putExtra("imagePath", work.getImagePath());
            view.getContext().startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return workList.size();
    }

    public static class WorkViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public WorkViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}

