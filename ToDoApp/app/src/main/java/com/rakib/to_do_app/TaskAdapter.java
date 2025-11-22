package com.rakib.to_do_app;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList = new ArrayList<>();
    private OnTaskActionListener listener;

    public interface OnTaskActionListener {
        void onTaskEdit(int position);
        void onTaskDelete(int position);
        void onTaskStatusChange(int position, String newStatus);
    }

    public TaskAdapter(List<Task> taskList, OnTaskActionListener listener) {
        this.taskList = new ArrayList<>(taskList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.txtTitle.setText(task.getTitle());
        holder.txtDescription.setText(task.getDescription());
        holder.txtDate.setText(task.getDate());

        // Display Priority
        if (task.getPriority() != null && !task.getPriority().isEmpty()) {
            holder.txtPriority.setText(task.getPriority());
            holder.txtPriority.setVisibility(View.VISIBLE);

            // Set priority background color based on priority level
            int priorityColor = getPriorityColor(holder.itemView.getContext(), task.getPriority());
            holder.txtPriority.setBackgroundColor(priorityColor);

            // Set text color to white for better visibility
            holder.txtPriority.setTextColor(Color.WHITE);
        } else {
            holder.txtPriority.setVisibility(View.GONE);
        }

        if (task.getCategory() != null && !task.getCategory().isEmpty()) {
            holder.txtCategory.setText("Category: " + task.getCategory());
            holder.txtCategory.setVisibility(View.VISIBLE);
        } else {
            holder.txtCategory.setVisibility(View.GONE);
        }

        if (task.getStartTime() != null && task.getEndTime() != null) {
            String timeRange = task.getStartTime() + " - " + task.getEndTime();
            holder.txtTime.setText(timeRange);
            holder.txtTime.setVisibility(View.VISIBLE);
        } else {
            holder.txtTime.setVisibility(View.GONE);
        }

        if (task.getStatus() != null) {
            holder.txtStatus.setText("Status: " + getStatusDisplayText(task.getStatus()));
            holder.txtStatus.setVisibility(View.VISIBLE);
            int statusColor = getStatusColor(holder.itemView.getContext(), task.getStatus());
            holder.txtStatus.setTextColor(statusColor);
        } else {
            holder.txtStatus.setVisibility(View.GONE);
        }

        holder.btnMore.setOnClickListener(v -> showPopupMenu(v, position, task.getStatus()));
    }

    // Add this helper method to get priority colors
    private int getPriorityColor(Context context, String priority) {
        switch (priority.toLowerCase()) {
            case "high":
                return ContextCompat.getColor(context, R.color.priority_high);
            case "medium":
                return ContextCompat.getColor(context, R.color.priority_medium);
            case "low":
                return ContextCompat.getColor(context, R.color.priority_low);
            default:
                return ContextCompat.getColor(context, R.color.priority_default);
        }
    }

    private int getStatusColor(Context context, String status) {
        switch (status) {
            case "running":
                return ContextCompat.getColor(context, android.R.color.holo_orange_dark);
            case "completed":
                return ContextCompat.getColor(context, android.R.color.holo_green_dark);
            default:
                return ContextCompat.getColor(context, android.R.color.holo_orange_dark);
        }
    }

    private void showPopupMenu(View view, int position, String currentStatus) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.inflate(R.menu.menu_task_options);
        popup.setOnMenuItemClickListener(item -> {
            if (listener == null) return false;

            int id = item.getItemId();
            if (id == R.id.action_edit) {
                listener.onTaskEdit(position);
                return true;
            } else if (id == R.id.action_delete) {
                listener.onTaskDelete(position);
                return true;
            } else if (id == R.id.action_mark_in_progress) {
                listener.onTaskStatusChange(position, "running");
                return true;
            } else if (id == R.id.action_mark_completed) {
                listener.onTaskStatusChange(position, "completed");
                return true;
            }
            return false;
        });
        popup.show();
    }

    private String getStatusDisplayText(String status) {
        switch (status) {
            case "running": return "In Progress";
            case "completed": return "Completed";
            default: return "In Progress";
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void updateList(List<Task> newList) {
        this.taskList.clear();
        this.taskList.addAll(newList);
        notifyDataSetChanged();
    }

    public Task getTaskAt(int position) {
        if (position >= 0 && position < taskList.size()) {
            return taskList.get(position);
        }
        return null;
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtDescription, txtDate, txtCategory, txtTime, txtStatus, txtPriority;
        ImageView btnMore;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.taskTitleText);
            txtDescription = itemView.findViewById(R.id.taskDescriptionText);
            txtDate = itemView.findViewById(R.id.taskDateText);
            txtCategory = itemView.findViewById(R.id.taskCategoryText);
            txtTime = itemView.findViewById(R.id.taskTimeText);
            txtStatus = itemView.findViewById(R.id.taskStatusText);
            txtPriority = itemView.findViewById(R.id.txtPriority); // Make sure this ID exists in your layout
            btnMore = itemView.findViewById(R.id.btnMoreOptions);
        }
    }
}