package com.usc.lugarlangfinal.adapters;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.usc.lugarlangfinal.R;
import com.usc.lugarlangfinal.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_message_item, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        // Handle basic markdown bolding (**text**) and newlines
        String processedText = message.getText()
                .replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>")
                .replace("\n", "<br>");

        holder.messageText.setText(HtmlCompat.fromHtml(processedText, HtmlCompat.FROM_HTML_MODE_LEGACY));

        if (message.isFromUser()) {
            holder.messageContainer.setBackgroundResource(R.drawable.chat_bubble_user);
            holder.messageText.setTextColor(holder.itemView.getResources().getColor(R.color.white));
            holder.messageText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            holder.messageContainer.setPadding(24, 18, 24, 18);
        } else {
            holder.messageContainer.setBackgroundResource(R.drawable.chat_bubble_assistant);
            holder.messageText.setTextColor(holder.itemView.getResources().getColor(R.color.black));
            holder.messageText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            holder.messageContainer.setPadding(24, 18, 24, 18);
        }

        ViewGroup.LayoutParams params = holder.messageContainer.getLayoutParams();
        if (message.isFromUser()) {
            holder.messageContainer.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        } else {
            holder.messageContainer.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
        holder.messageContainer.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        View messageContainer;
        TextView messageText;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageContainer = itemView.findViewById(R.id.chatMessageContainer);
            messageText = itemView.findViewById(R.id.tvChatText);
        }
    }
}
