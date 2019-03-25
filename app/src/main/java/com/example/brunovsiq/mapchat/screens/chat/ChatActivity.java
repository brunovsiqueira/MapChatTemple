package com.example.brunovsiq.mapchat.screens.chat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.brunovsiq.mapchat.R;
import com.example.brunovsiq.mapchat.models.Author;
import com.example.brunovsiq.mapchat.models.Message;
import com.example.brunovsiq.mapchat.models.User;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.ArrayList;
import java.util.Calendar;

public class ChatActivity extends AppCompatActivity {

//    MessageInput messageInput;
//    MessagesList messagesList;
//    MessagesListAdapter<Message> adapter;
//    Author author;

    RecyclerView recyclerView;
    ArrayList<Message> messageList = new ArrayList<>();
    MessageAdapter messageAdapter;

    Button sendButton;
    EditText inputText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);

        recyclerView = findViewById(R.id.messages_recyclerview);
        sendButton = findViewById(R.id.send_button);
        inputText =  findViewById(R.id.input_message);
        String partnerName = getIntent().getStringExtra("partnerName");

        messageAdapter = new MessageAdapter(this, messageList, partnerName);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);
        recyclerView.scrollToPosition(messageList.size() - 1);


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inputText.getText().length() > 0) {
                    Calendar calendar = Calendar.getInstance();
                    Message message = new Message(inputText.getText().toString(), User.getInstance().getUsername(), calendar);
                    messageList.add(message);
                    messageAdapter.notifyDataSetChanged();
                    inputText.setText("");
                }
            }
        });




//        messageInput = findViewById(R.id.input);
//        messagesList = findViewById(R.id.messagesList);
//
//        author = new Author("1a", User.getInstance().getUsername(), null);
//
////        ImageLoader imageLoader = new ImageLoader() {
////            @Override
////            public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
////                Picasso.get().load(url).into(imageView);
////            }
////        };
//
//        adapter = new MessagesListAdapter<>(author.getId(), null);
//
//        messagesList.setAdapter(adapter, true);
//
//        messageInput.setInputListener(new MessageInput.InputListener() {
//            @Override
//            public boolean onSubmit(CharSequence input) {
//                //validate and send message
//                Calendar calendar = Calendar.getInstance();
//                Message message = new Message("2a", input.toString(), author, calendar);
//                //adapter.addToStart(message, true);
//                return true;
//            }
//        });

    }
}
