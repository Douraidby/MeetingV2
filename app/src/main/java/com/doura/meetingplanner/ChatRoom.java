package com.doura.meetingplanner;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChatRoom extends AppCompatActivity {

    private DatabaseReference mref;
    private EditText message_txt;
    private String cuName;
    private String cuGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatroom_layout);
        setTitle("Chat room");

        Bundle extras = getIntent().getExtras();
        cuGroup = extras.getString("group");
        cuName = extras.getString("user");
        mref = FirebaseDatabase.getInstance().getReference().child(cuGroup).child("ChatRoom");

        ImageView send_btn = (ImageView) findViewById(R.id.message_btn);
        message_txt = (EditText) findViewById(R.id.message_txt);

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatMessage mChatRoom = new ChatMessage(cuName,message_txt.getText().toString());
                mref.push().setValue(mChatRoom);
                message_txt.setText(null);
            }
        });

        DisplayChatMessage();
    }

    private void DisplayChatMessage() {
        FirebaseListAdapter<ChatMessage> mFirebaselistAdapter;
        ListView mListview = (ListView) findViewById(R.id.list_of_message);

        mFirebaselistAdapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class, R.layout.chat_list_item, mref) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                TextView messageuser, messagetext, messagetime;
                messageuser = (TextView) v.findViewById(R.id.message_user);
                messagetext = (TextView) v.findViewById(R.id.message_text);
                messagetime = (TextView) v.findViewById(R.id.message_time);

                messageuser.setText(model.getcUser());
                messagetext.setText(model.getcMessage());
                messagetime.setText(android.text.format.DateFormat.format("dd-MM-yyyy (HH:mm:ss)",model.getcTime()));
            }
        };
        mListview.setAdapter(mFirebaselistAdapter);
    }
}
