package com.rakib.to_do_app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        // --- Priority Badge ---
        if (task.getPriority() != null && !task.getPriority().isEmpty()) {
            holder.txtPriority.setText(task.getPriority());
            holder.txtPriority.setVisibility(View.VISIBLE);

            int priorityColor = getPriorityColor(holder.itemView.getContext(), task.getPriority());
            GradientDrawable priorityBg = new GradientDrawable();
            priorityBg.setColor(priorityColor);
            priorityBg.setCornerRadius(16f);
            holder.txtPriority.setBackground(priorityBg);
            holder.txtPriority.setTextColor(Color.WHITE);
        } else {
            holder.txtPriority.setVisibility(View.GONE);
        }

        // --- Category ---
        if (task.getCategory() != null && !task.getCategory().isEmpty()) {
            holder.txtCategory.setText("Category: " + task.getCategory());
            holder.txtCategory.setVisibility(View.VISIBLE);
        } else {
            holder.txtCategory.setVisibility(View.GONE);
        }

        // --- Time Range ---
        if (task.getStartTime() != null && task.getEndTime() != null) {
            String timeRange = task.getStartTime() + " - " + task.getEndTime();
            holder.txtTime.setText(timeRange);
            holder.txtTime.setVisibility(View.VISIBLE);
        } else {
            holder.txtTime.setVisibility(View.GONE);
        }

        // --- FIX: Status Logic ---
        if (task.getStatus() != null) {
            // Get correct text ("In Progress", "Completed", or "Missed")
            holder.txtStatus.setText("Status: " + getStatusDisplayText(task.getStatus()));
            holder.txtStatus.setVisibility(View.VISIBLE);

            // Get correct color (Orange, Green, or Red)
            int statusColor = getStatusColor(holder.itemView.getContext(), task.getStatus());
            holder.txtStatus.setTextColor(statusColor);
        } else {
            holder.txtStatus.setVisibility(View.GONE);
        }

        // --- Color Dot ---
        if (task.getColorTag() != null && !task.getColorTag().isEmpty() && holder.colorDot != null) {
            try {
                int color = Color.parseColor(task.getColorTag());
                GradientDrawable dotBg = new GradientDrawable();
                dotBg.setShape(GradientDrawable.OVAL);
                dotBg.setColor(color);
                holder.colorDot.setBackground(dotBg);
                holder.colorDot.setVisibility(View.VISIBLE);
            } catch (IllegalArgumentException e) {
                holder.colorDot.setVisibility(View.GONE);
            }
        } else if (holder.colorDot != null) {
            holder.colorDot.setVisibility(View.GONE);
        }

        holder.btnMore.setOnClickListener(v -> showPopupMenu(v, position, task.getStatus()));
    }

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

    // --- FIX: ADDED RED COLOR FOR MISSED TASKS ---
    private int getStatusColor(Context context, String status) {
        switch (status) {
            case "running":
                return ContextCompat.getColor(context, android.R.color.holo_orange_dark);
            case "completed":
                return ContextCompat.getColor(context, android.R.color.holo_green_dark);
            case "unfinished":
                return ContextCompat.getColor(context, android.R.color.holo_red_dark); // Red for Missed
            default:
                return ContextCompat.getColor(context, android.R.color.holo_orange_dark);
        }
    }

    // --- FIX: ADDED TEXT FOR MISSED TASKS ---
    private String getStatusDisplayText(String status) {
        switch (status) {
            case "running": return "In Progress";
            case "completed": return "Completed";
            case "unfinished": return "Missed"; // Text for Missed
            default: return "In Progress";
        }
    }

    private void showPopupMenu(View view, int position, String currentStatus) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.inflate(R.menu.menu_task_options);

        // Dynamically update menu items based on status
        if ("unfinished".equals(currentStatus)) {
            // If missed, allow retry (mark as running)
            popup.getMenu().findItem(R.id.action_mark_in_progress).setTitle("Restart Task");
        }

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

    public void sortList(String sortBy) {
        if (taskList.isEmpty()) return;

        Comparator<Task> comparator;

        switch (sortBy.toLowerCase()) {
            case "date":
                comparator = (t1, t2) -> {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                        Date d1 = sdf.parse(t1.getDate());
                        Date d2 = sdf.parse(t2.getDate());
                        return d1.compareTo(d2);
                    } catch (Exception e) {
                        return 0;
                    }
                };
                break;

            case "priority":
                comparator = (t1, t2) -> {
                    int p1 = getPriorityValue(t1.getPriority());
                    int p2 = getPriorityValue(t2.getPriority());
                    return Integer.compare(p2, p1);
                };
                break;

            case "category":
                comparator = Comparator.comparing(Task::getCategory, Comparator.nullsLast(String::compareToIgnoreCase));
                break;

            default:
                comparator = Comparator.comparing(Task::getId);
                break;
        }

        Collections.sort(taskList, comparator);
        notifyDataSetChanged();
    }

    private int getPriorityValue(String priority) {
        if (priority == null) return 0;
        switch (priority.toLowerCase()) {
            case "high": return 3;
            case "medium": return 2;
            case "low": return 1;
            default: return 0;
        }
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtDescription, txtDate, txtCategory, txtTime, txtStatus, txtPriority;
        ImageView btnMore;
        View colorDot;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.taskTitleText);
            txtDescription = itemView.findViewById(R.id.taskDescriptionText);
            txtDate = itemView.findViewById(R.id.taskDateText);
            txtCategory = itemView.findViewById(R.id.taskCategoryText);
            txtTime = itemView.findViewById(R.id.taskTimeText);
            txtStatus = itemView.findViewById(R.id.taskStatusText);
            txtPriority = itemView.findViewById(R.id.txtPriority);
            btnMore = itemView.findViewById(R.id.btnMoreOptions);
            colorDot = itemView.findViewById(R.id.colorDot);
        }
    }
}