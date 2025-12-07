package com.rakib.to_do_app;

import android.content.res.ColorStateList;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat; // <-- NEW IMPORT
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private Button btnMyTasks, btnInProgress, btnCompleted;
    private RecyclerView recyclerTasks;
    private TextView txtEmptyState;
    private TaskAdapter taskAdapter;
    private List<Task> allTasks = new ArrayList<>();
    private String currentTab = "all";
    private String currentSearchQuery = "";
    private String currentSortBy = "date";
    private TaskManager taskManager;
    private SearchView searchTaskCategory;
    private ImageButton btnSort;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        taskManager = TaskManager.getInstance(requireContext());

        initializeViews(view);
        setupTaskAdapter();
        setupClickListeners();
        setupSearchAndSort();
        loadTasks();

        return view;
    }

    private void initializeViews(View view) {
        btnMyTasks = view.findViewById(R.id.btnMyTasks);
        btnInProgress = view.findViewById(R.id.btnInProgress);
        btnCompleted = view.findViewById(R.id.btnCompleted);
        recyclerTasks = view.findViewById(R.id.recyclerTasks);
        txtEmptyState = view.findViewById(R.id.txtEmptyState);
        searchTaskCategory = view.findViewById(R.id.searchTaskCategory);
        btnSort = view.findViewById(R.id.btnSort);
    }

    private void setupTaskAdapter() {
        taskAdapter = new TaskAdapter(new ArrayList<>(), new TaskAdapter.OnTaskActionListener() {
            @Override
            public void onTaskEdit(int position) {
                Task task = taskAdapter.getTaskAt(position);
                if (task != null) {
                    openEditTaskDialog(task);
                }
            }

            @Override
            public void onTaskDelete(int position) {
                Task task = taskAdapter.getTaskAt(position);
                if (task != null) {
                    ReminderManager reminderManager = new ReminderManager(requireContext());
                    reminderManager.cancelReminder(task);
                    taskManager.removeTask(task);
                    allTasks.remove(task);
                    filterAndSortTasks();
                    Toast.makeText(getContext(), "Task and reminder deleted", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onTaskStatusChange(int position, String newStatus) {
                Task task = taskAdapter.getTaskAt(position);
                if (task != null) {
                    task.setStatus(newStatus);
                    taskManager.updateTask(task);
                    filterAndSortTasks();
                    String statusText = newStatus.equals("running") ? "In Progress" : "Completed";
                    Toast.makeText(getContext(), "Task marked as " + statusText, Toast.LENGTH_SHORT).show();
                }
            }
        });

        recyclerTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerTasks.setAdapter(taskAdapter);
    }

    private void setupClickListeners() {
        btnMyTasks.setOnClickListener(v -> {
            selectTab(btnMyTasks);
            currentTab = "all";
            filterAndSortTasks();
        });

        btnInProgress.setOnClickListener(v -> {
            selectTab(btnInProgress);
            currentTab = "running";
            filterAndSortTasks();
        });

        btnCompleted.setOnClickListener(v -> {
            selectTab(btnCompleted);
            currentTab = "completed";
            filterAndSortTasks();
        });
    }

    private void setupSearchAndSort() {
        // 1. SEARCH VIEW SETUP
        searchTaskCategory.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearchQuery = query;
                filterAndSortTasks();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText;
                filterAndSortTasks();
                return true;
            }
        });

        // 2. SORT BUTTON SETUP
        btnSort.setOnClickListener(this::showSortMenu);
    }

    private void showSortMenu(View v) {
        PopupMenu popup = new PopupMenu(requireContext(), v);
        popup.getMenu().add(0, 1, 0, "Sort by Date");
        popup.getMenu().add(0, 2, 0, "Sort by Priority");
        popup.getMenu().add(0, 3, 0, "Sort by Category");

        popup.setOnMenuItemClickListener(item -> {
            String newSort = "";
            int itemId = item.getItemId();
            if (itemId == 1) {
                newSort = "date";
            } else if (itemId == 2) {
                newSort = "priority";
            } else if (itemId == 3) {
                newSort = "category";
            } else {
                return false;
            }

            if (!currentSortBy.equals(newSort)) {
                currentSortBy = newSort;
                filterAndSortTasks();
                Toast.makeText(requireContext(), "Sorted by " + item.getTitle(), Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        popup.show();
    }

    /**
     * FIX: Updated method to use theme-aware drawables and color state lists
     * instead of hardcoded colors, ensuring Day/Night mode compatibility.
     */
    private void selectTab(Button selectedButton) {

        // --- 1. Get Color State List for Text ---
        // This selector defines text color contrast (e.g., primary color when selected,
        // or the default colorOnSurface otherwise).
        ColorStateList textColors = ContextCompat.getColorStateList(requireContext(), R.color.btn_text_color_selector);

        // --- 2. Reset All Buttons to Unselected State ---
        Button[] allButtons = {btnMyTasks, btnInProgress, btnCompleted};
        for (Button button : allButtons) {
            button.setBackgroundResource(R.drawable.btn_unselected);
            button.setTextColor(textColors); // Set text color using selector
        }

        // --- 3. Set Selected Button State ---
        selectedButton.setBackgroundResource(R.drawable.btn_selected);
        // Note: The text color is already handled by the selector, which defaults to
        // the selected state color when btn_selected (solid color) is applied.
    }

    private void loadTasks() {
        allTasks = taskManager.getTasks();
        filterAndSortTasks();
    }

    private void filterAndSortTasks() {
        List<Task> filtered = new ArrayList<>();
        String status = currentTab;
        String query = currentSearchQuery.toLowerCase(Locale.getDefault());

        // 1. Applying Status Filter
        for (Task task : allTasks) {
            boolean statusMatches = status.equals("all") || (task.getStatus() != null && task.getStatus().equals(status));
            if (statusMatches) {
                filtered.add(task);
            }
        }

        // 2. Apply Search Filter by Category
        if (!query.isEmpty()) {
            List<Task> searchFiltered = new ArrayList<>();
            for (Task task : filtered) {
                if (task.getCategory() != null && task.getCategory().toLowerCase(Locale.getDefault()).contains(query)) {
                    searchFiltered.add(task);
                }
            }
            filtered = searchFiltered;
        }

        // 3. Apply Sorting
        if (taskAdapter != null) {
            taskAdapter.updateList(filtered);
            taskAdapter.sortList(currentSortBy);
        }

        // 4. Updating Empty State
        if (filtered.isEmpty()) {
            recyclerTasks.setVisibility(View.GONE);
            txtEmptyState.setVisibility(View.VISIBLE);
            if (!query.isEmpty()) {
                txtEmptyState.setText("No tasks match the category: \"" + currentSearchQuery + "\"");
            } else {
                switch (status) {
                    case "all":
                        txtEmptyState.setText("No tasks yet. Create your first task!");
                        break;
                    case "running":
                        txtEmptyState.setText("No tasks in progress");
                        break;
                    case "completed":
                        txtEmptyState.setText("No completed tasks yet");
                        break;
                }
            }
        } else {
            recyclerTasks.setVisibility(View.VISIBLE);
            txtEmptyState.setVisibility(View.GONE);
        }
    }

    private void openEditTaskDialog(Task task) {
        AddTaskDialog dialog = new AddTaskDialog();

        Bundle args = new Bundle();
        args.putString("title", task.getTitle());
        args.putString("description", task.getDescription());
        args.putString("date", task.getDate());
        args.putString("startTime", task.getStartTime());
        args.putString("endTime", task.getEndTime());
        args.putString("category", task.getCategory());
        args.putString("status", task.getStatus());
        args.putString("priority", task.getPriority());
        dialog.setArguments(args);

        dialog.setOnTaskCreatedListener(new AddTaskDialog.OnTaskCreatedListener() {
            @Override
            public void onTaskCreated(String title, String description, String date, String startTime, String endTime, String category, String status, String priority) {
                ReminderManager reminderManager = new ReminderManager(requireContext());
                reminderManager.cancelReminder(task);

                task.setTitle(title);
                task.setDescription(description);
                task.setDate(date);
                task.setStartTime(startTime);
                task.setEndTime(endTime);
                task.setCategory(category);
                task.setStatus(status);
                task.setPriority(priority);

                taskManager.updateTask(task);
                reminderManager.setReminder(task);
                loadTasks();

                Toast.makeText(requireContext(), "Task updated with new reminder!", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show(getParentFragmentManager(), "EditTaskDialog");
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasks();
    }
}