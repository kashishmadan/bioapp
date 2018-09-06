package com.bluemaestro.utility.sdk.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.bluemaestro.utility.sdk.R;
import com.bluemaestro.utility.sdk.databinding.ElementMessageBinding;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter
{


    private static final String TAG = "MessageAdapter";

    private ArrayList<String> messages;
    private Context context;

    public MessageAdapter(Context context, ArrayList<String> messages)
    {
        this.context = context;
        this.messages = messages;
    }

    public void addMessage(String message)
    {
        this.messages.add(message);
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GenericViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        ViewDataBinding viewDataBinding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.element_message, parent, false);
        return new GenericViewHolder(viewDataBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position)
    {

        GenericViewHolder viewHolder = (GenericViewHolder) holder;
        ElementMessageBinding elementMessageBinding = ((ElementMessageBinding) viewHolder.binding);
        elementMessageBinding.text.setText(this.messages.get(position));
    }

    @Override
    public int getItemCount()
    {
        return this.messages.size();
    }

}
