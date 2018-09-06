package com.bluemaestro.utility.sdk.adapter;

import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;

public class GenericViewHolder extends RecyclerView.ViewHolder
{
    public final ViewDataBinding binding;

    public GenericViewHolder(ViewDataBinding binding)
    {
        super(binding.getRoot());
        this.binding = binding;
    }
}
