package com.example.brunovsiq.mapchat.screens.chat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.example.brunovsiq.mapchat.R;
import com.example.brunovsiq.mapchat.models.Author;
import com.example.brunovsiq.mapchat.models.Message;
import com.example.brunovsiq.mapchat.models.User;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.json.JSONObject;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

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

    Cipher cipher;

    {
        try {
            cipher = Cipher.getInstance("RSA/ECB/NoPadding");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);

        recyclerView = findViewById(R.id.messages_recyclerview);
        sendButton = findViewById(R.id.send_button);
        inputText =  findViewById(R.id.input_message);
        final String partnerName = getIntent().getStringExtra("partnerName");
        final String partnerKey = getIntent().getStringExtra("partnerKey");

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
//                    messageList.add(message);
//                    messageAdapter.notifyDataSetChanged();
                    String encryptedText = null;
                    try {
                        encryptedText = encrypt(inputText.getText().toString(), getKey(partnerKey));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    AndroidNetworking.post("https://kamorris.com/lab/send_message.php")
                            .addBodyParameter("user", User.getInstance().getUsername())
                            .addBodyParameter("partneruser", partnerName)
                            .addBodyParameter("message", encryptedText)
                            .setPriority(Priority.HIGH)
                            .build()
                            .getAsJSONObject(new JSONObjectRequestListener() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    // do anything with response
                                    Log.d("RESPONSE", "");
                                }

                                @Override
                                public void onError(ANError error) {
                                    // handle error
                                    Log.d("ERROR", "");
                                    if (error.getMessage().contains("OK")) {


                                    } else {

                                    }

                                    //OK is saved
                                }
                            });
                    inputText.setText("");
                }
            }
        });


    }

    public static PublicKey getKey(String key){
        try{
            byte[] byteKey = Base64.decode(key.getBytes(), Base64.DEFAULT);
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");

            return kf.generatePublic(X509publicKey);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public String encrypt(String text, PublicKey publicKey) throws Exception {
        if (text == null || text.length() == 0) {
            throw new Exception("Empty string");
        } else {

            byte[] encrypted = null;
            try {
                cipher.init(Cipher.ENCRYPT_MODE, publicKey);
                encrypted = cipher.doFinal(text.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        }

    }
}
