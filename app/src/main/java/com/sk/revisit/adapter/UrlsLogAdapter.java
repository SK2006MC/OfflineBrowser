package com.sk.revisit.adapter;


import com.androidx.recyclerView;

public class UrlsLogAdapter extends RecyclerView.Adapter<UrlViewHolder> {
    
    public UrlsLogAdapter(){
    }

    @Override
    public void  

    public static class UrlViewHolder extends RecyclerView.ViewHolder {
        ItemUrlLogBinding binding;

        UrlViewHolder(ItemUrlLogBinding binding){
            this.binding = binding;
        }

        public void bind(UrlLog urlLog){
            binding.urlText.setText(urlLog.url);
            binding.size.setText(urlLog.size);
            urlLog.setOnProgressChangeListener((p)->{
                binding.progress.setProgress((int)p);
            });
        }
    }
}

