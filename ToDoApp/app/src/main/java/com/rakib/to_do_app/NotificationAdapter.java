package com.rakib.to_do_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<NotificationItem> notificationList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());

    public NotificationAdapter(List<NotificationItem> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem notification = notificationList.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private TextView textTitle, textMessage, textTime;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.tv_notification_title);
            textMessage = itemView.findViewById(R.id.tv_notification_message);
            textTime = itemView.findViewById(R.id.tv_notification_time);
        }

        public void bind(NotificationItem notification) {
            textTitle.setText(notification.getTitle());
            textMessage.setText(notification.getMessage());

            // Format timestamp
            String timeString = dateFormat.format(new Date(notification.getTimestamp()));
            textTime.setText(timeString);
        }
    }
}