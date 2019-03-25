package com.example.brunovsiq.mapchat.screens.chat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.brunovsiq.mapchat.R;
import com.example.brunovsiq.mapchat.models.Message;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter {

    private Context context;
    private ArrayList<Message> messageList;
    private Message notification;
    private MessageViewHolder messageViewHolder;
    private String senderName;

    public MessageAdapter (Context context, ArrayList<Message> messageList, String senderName) {
        this.context = context;
        this.messageList = messageList;
        this.senderName = senderName;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.message_item, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        messageViewHolder = (MessageViewHolder) holder;

        messageViewHolder.nameAndDate.setText(messageList.get(position).getAuthor());
        messageViewHolder.messageText.setText(messageList.get(position).getText());

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public TextView nameAndDate;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message_text);
            nameAndDate = itemView.findViewById(R.id.sender_name_date);
        }
    }

}
