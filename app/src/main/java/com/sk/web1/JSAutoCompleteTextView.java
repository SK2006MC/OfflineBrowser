package com.sk.web1;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import org.json.JSONArray;
import org.json.JSONStringer;

public class JSAutoCompleteTextView extends AutoCompleteTextView {

  private static final String TAG = "JSAutoCompleteTextView";
  private static final long DEBOUNCE_DELAY = 300;
  private final Handler handler = new Handler(Looper.getMainLooper());
  private Runnable pendingAutocompleteTask;
  private WebView webView;
  ArrayAdapter<String> adapter;
  private final Set<String> jsKeywords =
      new HashSet<>(
          Arrays.asList(
              "var",
              "let",
              "const",
              "function",
              "if",
              "else",
              "for",
              "while",
              "return",
              "try",
              "catch",
              "finally",
              "switch",
              "case",
              "break",
              "default",
              "new",
              "this",
              "typeof",
              "instanceof",
              "delete",
              "void",
              "debugger"));

  public JSAutoCompleteTextView(Context context) {
    super(context);
    init();
  }

  public JSAutoCompleteTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public JSAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    adapter =
        new ArrayAdapter<>(
            getContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
    setAdapter(adapter);
    addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (webView != null) {
              if (pendingAutocompleteTask != null) {
                handler.removeCallbacks(pendingAutocompleteTask);
              }

              pendingAutocompleteTask = () -> updateAutocompleteSuggestions(s.toString());
              handler.postDelayed(pendingAutocompleteTask, DEBOUNCE_DELAY);
            }
          }

          @Override
          public void afterTextChanged(Editable s) {}
        });
  }

  public void setWebView(WebView webView) {
    this.webView = webView;
    if (this.webView != null) {}
  }

  private void updateAutocompleteSuggestions(String input) {
    if (webView == null || adapter == null) {
      Log.w(TAG, "WebView or Adapter is null. Skipping autocomplete update.");
      return;
    }

    String jsCode;
    if (input.contains(".")) {
      int lastDotIndex = input.lastIndexOf(".");
      String objectName = input.substring(0, lastDotIndex);
      jsCode =
          "(function() { try { if ("
              + objectName
              + " !== null) { return Object.getOwnPropertyNames("
              + objectName
              + "); } else { return []; } } catch (e) { console.log(e);return []; } })();";
    } else {
      jsCode = "(function() { return Object.getOwnPropertyNames(window); })();";
    }

    webView.evaluateJavascript(
        jsCode,
        result -> {
          List<String> suggestions = new ArrayList<>();
          if (result != null) {
            String formattedResult = "";
            JSONArray jsA;
            ArrayList<String> variables=new ArrayList<String>();

            try {
              jsA = new JSONArray(result);
              for (int i = 0; i < jsA.length(); ++i) {
                variables.add(jsA.getString(i));
              }
            } catch (Exception err) {
              Log.d(TAG, err.toString());
            }

            String completionTarget = input;
            if (input.contains(".")) {
              completionTarget = input.substring(input.lastIndexOf(".") + 1);
            }

            for (String variable : variables) {
              String trimmedVar = variable.trim();
              if (trimmedVar.toLowerCase().startsWith(completionTarget.toLowerCase())) {
                if (input.contains(".")) {
                  suggestions.add(input.substring(0, input.lastIndexOf(".") + 1) + trimmedVar);
                } else {
                  suggestions.add(trimmedVar);
                }
              }
            }
          } else {
            Log.e(TAG, "Failed to get variables for autocomplete.");
          }
          for (String keyword : jsKeywords) {
            if (keyword.toLowerCase().startsWith(input.toLowerCase())) {
              suggestions.add(keyword);
            }
          }

          new Handler(Looper.getMainLooper())
              .post(
                  () -> {
                    if (adapter != null) {
                      adapter.clear();
                      adapter.addAll(suggestions);
                      adapter.notifyDataSetChanged();
                    } else {
                      Log.e(TAG, "Adapter is unexpectedly null in UI thread callback.");
                    }
                  });
        });
  }
}
