package com.example.choks.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.choks.Model.Message_model;
import com.example.choks.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class Message_Adapter extends RecyclerView.Adapter<Message_Adapter.MessageViewHolder> {

    private List<Message_model> messageModels;
    private Context context;
    public static  final int MSG_TYPE_LEFT = 0;
    public static  final int MSG_TYPE_RIGHT = 1;
    String currentUserId,imageURL;
    FirebaseUser fuser;

    public Message_Adapter(List<Message_model> messageModels, Context context, String imageURL) {
        this.messageModels = messageModels;
        this.context = context;
        this.imageURL = imageURL;
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {

        }
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
            return new Message_Adapter.MessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
            return new Message_Adapter.MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message_model messageModel = messageModels.get(position);
        holder.textMessage.setText(messageModel.getText());

        if(position == messageModels.size()-1){
            if(messageModel.isSeen()){
                holder.textSeen.setText("Seen");
            }
            else{
                holder.textSeen.setText("Delivered");
            }
        }
        else{
            holder.textSeen.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return messageModels.size();
    }

    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        Message_model message = messageModels.get(position);
        if (message != null && message.getSenderId() != null && fuser != null && fuser.getUid() != null) {
            if (message.getSenderId().equals(fuser.getUid())) {
                return MSG_TYPE_RIGHT;
            } else {
                return MSG_TYPE_LEFT;
            }
        }
        return super.getItemViewType(position);
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage;
        TextView textSeen;
        CircleImageView profile;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.show_message);
            textSeen = itemView.findViewById(R.id.txt_seen);
            profile = itemView.findViewById(R.id.profile_image);
        }
    }
}
