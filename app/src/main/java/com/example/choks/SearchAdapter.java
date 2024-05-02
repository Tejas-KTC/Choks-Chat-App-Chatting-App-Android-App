package com.example.choks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.service.autofill.UserData;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.choks.Model.User_Data;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private List<User_Data> searchResults;
    private Context context;

    public SearchAdapter(List<User_Data> searchResults, Context context) {
        this.searchResults = searchResults;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User_Data searchResult = searchResults.get(position);
        holder.bind(searchResult);
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView user_name, user_last_msg;
        private ImageView user_profile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            user_profile = itemView.findViewById(R.id.user_profile_image);
            user_name = itemView.findViewById(R.id.user_name);
            user_last_msg = itemView.findViewById(R.id.user_last_msg);

            itemView.setOnClickListener(this);
        }

        public void bind(User_Data searchResult) {
            user_name.setText(searchResult.getUsername());
            user_last_msg.setText(searchResult.getLastMsgDate());

            Glide.with(context).load(searchResult.getImageURL()).into(user_profile);
        }

        @Override
        public void onClick(View v) {
            User_Data clickedUser = searchResults.get(getAdapterPosition());
            Intent intent = new Intent(context, Message.class);
            intent.putExtra("userID", clickedUser.getUsername());
            context.startActivity(intent);

            ((Activity) context).finish();
        }
    }
}

