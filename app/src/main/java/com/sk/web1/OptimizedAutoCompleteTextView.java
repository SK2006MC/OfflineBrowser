package com.sk.web1;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OptimizedAutoCompleteTextView extends AutoCompleteTextView {

    private static final int DEFAULT_DEBOUNCE_DELAY = 300;
    private int debounceDelay = DEFAULT_DEBOUNCE_DELAY;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pendingRunnable;
    private List<String> allSuggestions = new ArrayList<>(); // Store all suggestions
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Context context;

    public OptimizedAutoCompleteTextView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public OptimizedAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public OptimizedAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        // Set up text watcher for debouncing
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (pendingRunnable != null) {
                    handler.removeCallbacks(pendingRunnable);
                }

                pendingRunnable = () -> {
                    performFiltering(s);
                };

                handler.postDelayed(pendingRunnable, debounceDelay);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void setSuggestions(List<String> suggestions) {
        this.allSuggestions = suggestions;
    }

    private void performFiltering(CharSequence constraint) {
        executorService.execute(() -> {
            List<String> filteredList = new ArrayList<>();
            if (constraint != null) {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (String suggestion : allSuggestions) {
                    if (suggestion.toLowerCase().contains(filterPattern)) {
                        filteredList.add(suggestion);
                    }
                }
            }
            handler.post(() -> {
                updateSuggestions(filteredList);
            });
        });
    }


    private void updateSuggestions(List<String> filteredSuggestions) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_dropdown_item_1line, filteredSuggestions);
        setAdapter(adapter);
        adapter.getFilter().filter(getText(), this);
    }

    public void setDebounceDelay(int delay) {
        this.debounceDelay = delay;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // Important: Prevent memory leaks
        handler.removeCallbacksAndMessages(null);
        executorService.shutdown();
        executorService = Executors.newSingleThreadExecutor();
    }
}
