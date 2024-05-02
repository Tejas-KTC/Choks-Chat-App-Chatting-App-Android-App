package com.example.choks.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.choks.MainActivity;
import com.example.choks.Message;
import com.example.choks.Model.User_Data;
import com.example.choks.R;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class User_Adapter extends RecyclerView.Adapter<User_Adapter.UserViewHolder> {

    private Context context;
    private List<User_Data> userList;

    public User_Adapter(Context context, List<User_Data> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User_Data user = userList.get(position);
        holder.bind(user);

        if (user.getUnseenMsgCount() > 0) {
            holder.textViewUnseen.setVisibility(View.VISIBLE);
            holder.textViewUnseen.setText(String.valueOf(user.getUnseenMsgCount()));
        } else {
            holder.textViewUnseen.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, Message.class);
                intent.putExtra("userID", user.getUsername());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageViewProfile;
        private TextView textViewUsername;
        private TextView textViewLastMessage;
        private TextView textViewUnseen;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProfile = itemView.findViewById(R.id.user_profile_image);
            textViewUsername = itemView.findViewById(R.id.user_name);
            textViewLastMessage = itemView.findViewById(R.id.user_last_msg);
            textViewUnseen = itemView.findViewById(R.id.user_unseen_msg);
        }

        public void bind(User_Data user) {
            textViewUsername.setText(user.getUsername());
            textViewLastMessage.setText(user.getLastMsgDate());

            Glide.with(itemView.getContext())
                    .load(user.getImageURL())
                    .placeholder(R.drawable.person_button_svgrepo_com)
                    .error(R.drawable.person_button_svgrepo_com)
                    .into(imageViewProfile);
        }
    }

}
